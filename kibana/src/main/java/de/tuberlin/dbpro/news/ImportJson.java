package de.tuberlin.dbpro.news;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.tuberlin.dbpro.processing.geolocation.GeoDB;
import de.tuberlin.dbpro.processing.geolocation.GeoLocationFinder;
import de.tuberlin.dbpro.processing.heatmap.HeatmapComponent;
import de.tuberlin.dbpro.processing.ner.stanford.StanfordNERComponent;
import de.tuberlin.dbpro.processing.sentiment.SentimentComponent;
import org.apache.log4j.Logger;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import java.io.*;
import java.util.HashSet;

/**
 * Imports JSON file to Elastic Search
 */
public class ImportJson {

    /**
     * Logger for this class.
     */
    private static Logger LOG = Logger.getLogger(ImportJson.class);
    private static final int REQUESTS_PER_BULK = 250;

    public enum StorageEngine {
        FILE, ELASTICSEARCH, STDOUT;
    }

    private StorageEngine storageEngine = StorageEngine.ELASTICSEARCH;

    private SentimentComponent sentimentComponent;
    private HeatmapComponent heatmapComponent;
    private StanfordNERComponent stanfordNERComponent;
    private String nerClassifierPath;

    private PrintWriter failedWriter;
    private PrintWriter processedWriter;

    private BulkRequestBuilder bulkRequest;

    public boolean enableProcessing = true;
    public boolean enableScoreProcessing = false;
    public boolean enableReprocessLocations = true;

    private BufferedReader inputReader;
    GeoLocationFinder geoFinder;

    private String jsonPath; //
    private String targetIndex; // = "news";
    private String targetType; // = "article";
    private int serverPort = 9300;

    private int firstLine = -1;
    private int lastLine = -1;

    private Client client;

    /**
     * Read from input JSON line by line and add annotations of processing
     * components.
     */
    public void importJson() {

        int counter = 0;
        String line;
        try {
            failedWriter = new PrintWriter(jsonPath + ".failed");
            processedWriter = new PrintWriter(jsonPath + ".processed");

            inputReader = new BufferedReader(new FileReader(jsonPath));

            while ((line = inputReader.readLine()) != null) {
                if (counter > firstLine) {
                    try {
                        if (enableProcessing) {
                            line = processLine(line);
                        } else if (enableScoreProcessing) {
                            line = processScore(line);
                        } else if (enableReprocessLocations) {
                            line = reprocessLocations(line);
                        }

                        switch (storageEngine) {
                            case ELASTICSEARCH:
                                storeProcessedLineInElasticSearch(line, counter);
                                break;
                            case FILE:
                                storeProcessedLineInFile(line);
                                break;
                            default:
                                System.out.println(counter + ": " + line);
                                break;
                        }

                    } catch (Exception e) {
                        LOG.error("Error at line: " + line, e);
                        writeFailedLine(line);
                    }
//                LOG.debug(line);

                    if ((counter % 100) == 0) {
                        LOG.debug("Imported " + counter + " lines");
                    }
                }


                counter++;


                if (lastLine > 0 && counter > lastLine) {
                    LOG.info("Stopping at line " + lastLine);
                    break;
                }
            }
            sendBulkRequest();

            inputReader.close();
            failedWriter.close();

        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Cannot read/write file from: " + e.getMessage());
        }
    }

    /**
     * Initialize connection to ElasticSearch using TransportClient
     */
    public void initClient() {
        if (storageEngine == StorageEngine.ELASTICSEARCH) {
            client = new TransportClient()
                    .addTransportAddress(new InetSocketTransportAddress("localhost", serverPort));

            bulkRequest = client.prepareBulk();
        }
    }

    /**
     * Closes connection to ElasticSearch
     */
    public void closeClient() {
        if (storageEngine == StorageEngine.ELASTICSEARCH) {
            client.close();
        }
    }

    /**
     * Returns ElasticSearch client
     *
     * @return ElasticSearch client
     */
    public Client getClient() {
        return client;
    }


    /**
     * Adds normalized sentiment scores to processed news document.
     *
     * @param line
     * @return
     * @throws Exception
     */
    public String processScore(String line) throws Exception {

        // Init mapper
        ObjectMapper mapper = new ObjectMapper();
//        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);

        ObjectNode node = (ObjectNode) mapper.readTree(line);
//        line = mapper.writeValueAsString(node);
        try {
            double scoreTotal = node.get("sentiment").get("scoreTotal").asDouble();
            double scoreNormalized = node.get("sentiment").get("scoreNormalized").asDouble();

            int totalWords = node.get("sentiment").get("totalWords").asInt();
            double q = 0;
            double qN = 0;
            if (totalWords > 0) {
                q = scoreTotal / totalWords;
                qN = scoreNormalized / totalWords;
            }

            node.put("scoreTotalWords", q);
            node.put("scoreNormalizedWords", qN);

        } catch (Exception e) {

        }

        line = mapper.writeValueAsString(node);

        return line;
    }

    /**
     * Bug fixing processed geo locations.
     *
     * @param line
     * @return reprocessed line
     * @throws Exception
     */
    public String reprocessLocations(String line) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
//        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);

        ObjectNode node = (ObjectNode) mapper.readTree(line);

        if (node.get("ner") != null && node.get("ner").get("I-LOC") != null) {
            HashSet<String> locs = new HashSet<String>();
            for (JsonNode loc : node.path("ner").path("I-LOC")) {
                locs.add(loc.textValue());
            }

            geoFinder.fetchGeoData(locs);

            if (geoFinder.getCountryCodes().size() > 0)
                node.putPOJO("countryCodes", geoFinder.getCountryCodes());

            if (geoFinder.getGeoLocations().size() > 0)
                node.putPOJO("geoLocations", geoFinder.getGeoLocations());
        }

        line = mapper.writeValueAsString(node);

        return line;
    }

    /**
     * Adds annotations of processing components to news document.
     *
     * @param line unprocessed JSON-String
     * @return processed JSON-String
     * @throws Exception if processing fails
     */
    public String processLine(String line) throws Exception {

        // Init mapper
        ObjectMapper mapper = new ObjectMapper();
//        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);

        ObjectNode node = (ObjectNode) mapper.readTree(line);

        node.put("timeSlot", heatmapComponent.parseDate(node.path("dateString").textValue()));

        // NER
        stanfordNERComponent.identifyNER(node.path("news").textValue());

        node.putPOJO("ner", stanfordNERComponent.getNERResults());

        // Sentiment
        node.putPOJO("sentiment", sentimentComponent.getSentimentScore(node.path("news").textValue()));

        // GeoLocations
        if (stanfordNERComponent.getNERResults().get("I-LOC") != null) {

            geoFinder.fetchGeoData(stanfordNERComponent.getNERResults().get("I-LOC"));

            if (geoFinder.getCountryCodes().size() > 0)
                node.putPOJO("countryCodes", geoFinder.getCountryCodes());

            if (geoFinder.getGeoLocations().size() > 0)
                node.putPOJO("geoLocations", geoFinder.getGeoLocations());
        }

        line = mapper.writeValueAsString(node);

        return line;
    }

    /**
     * Stores processed line in file.
     *
     * @param line JSON-String of processed news document
     */
    private void storeProcessedLineInFile(String line) {
        processedWriter.println(line);
    }

    /**
     * Adds processed line to bulk request queue.
     *
     * @param line    JSON-String of processed news document
     * @param counter
     */
    private void storeProcessedLineInElasticSearch(String line, int counter) {
        IndexRequestBuilder request = getClient().prepareIndex(targetIndex, targetType)
                .setSource(line);

        bulkRequest.add(request);

        if ((counter % REQUESTS_PER_BULK) == 0) {
            sendBulkRequest();
        }

    }

    /**
     * Sends bulk request to ES if bulk limit is reached
     */
    public void sendBulkRequest() {
        if (storageEngine == StorageEngine.ELASTICSEARCH && bulkRequest.numberOfActions() > 0) {
            BulkResponse bulkResponse = bulkRequest.execute().actionGet();

            if (bulkResponse.hasFailures()) {
                LOG.error("Error sending bulk request: " + bulkResponse.buildFailureMessage());
            }

            bulkRequest = getClient().prepareBulk();
        }
    }

    /**
     * Initialize processing components
     * - Sentiment Component
     * - GeoFinder
     * - HeatMap
     * - Stanford NER
     */
    private void initComponents() {
        if (enableProcessing || enableReprocessLocations) {
            geoFinder = new GeoLocationFinder(GeoLocationFinder.Provider.GeoDB);
            geoFinder.setGeoDB(new GeoDB()
                            .init()
            );
        }

        if (enableProcessing) {
            try {
                sentimentComponent = new SentimentComponent();
                sentimentComponent.init();

                heatmapComponent = new HeatmapComponent();

                stanfordNERComponent = new StanfordNERComponent();
                stanfordNERComponent.init(nerClassifierPath);

            } catch (Exception e) {
                LOG.error("Cannot init components");
                System.exit(1);
            }
        }
    }

    /**
     * Writes failed line to log
     *
     * @param line JSON-String of processed news document
     */
    private void writeFailedLine(String line) {
        failedWriter.println(line);
    }

    /**
     * Execute JSON import
     *
     * @param args <JSON_PATH> <STORAGE> <INDEX> <TYPE> <NER_CLASSIFIER_PATH>
     */
    public static void main(String[] args) {

        if (args.length < 4) {
            System.err.println("Error - Arguments missing!");
            System.err.println(getUsage());
            System.exit(1);
        }

        System.out.println("Importing json to " + args[1] + "...");

        ImportJson ni = new ImportJson();

        if (args[1].toLowerCase().equals("file"))
            ni.storageEngine = StorageEngine.FILE;
        else if (args[1].toLowerCase().equals("elasticsearch"))
            ni.storageEngine = StorageEngine.ELASTICSEARCH;
        else
            ni.storageEngine = StorageEngine.STDOUT;

        ni.jsonPath = args[0];

        if (args.length > 6) {
            ni.lastLine = Integer.parseInt(args[6]);
        }

        if (args.length > 7) {
            ni.firstLine = Integer.parseInt(args[7]);
        }

        ni.enableProcessing = (args[5].equals("n") ? false : true);

        ni.targetIndex = args[2];
        ni.targetType = args[3];

        ni.nerClassifierPath = args[4];


        ni.initClient();
        ni.initComponents();
        ni.importJson();

        ni.closeClient();
    }


    /**
     * Returns parameters of main()-method
     *
     * @return Parameters of main()-method
     */
    public static String getUsage() {
        return "Run with: <JSON_PATH> <STORAGE> <INDEX> <TYPE> <NER_CLASSIFIER_PATH>";
    }

}
