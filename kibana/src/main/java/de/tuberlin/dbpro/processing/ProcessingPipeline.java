package de.tuberlin.dbpro.processing;

import de.tuberlin.dbpro.model.Document;
import de.tuberlin.dbpro.processing.heatmap.HeatmapComponent;
import de.tuberlin.dbpro.processing.ner.stanford.StanfordNERComponent;
import de.tuberlin.dbpro.processing.sentiment.SentimentComponent;
import de.tuberlin.dbpro.storage.StorageException;
import de.tuberlin.dbpro.storage.StorageManager;
import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.search.SearchHit;

import java.util.Arrays;

@Deprecated
public class ProcessingPipeline {

    /**
     * Logger for this class.
     */
    private static Logger LOG = Logger.getLogger(ProcessingPipeline.class);

    public static final String COMPONENT_FAILED_MESSAGE = " failed to load and won't be available in the pipeline: ";


    /**
     * The sentiment analysis component
     */
    private SentimentComponent sentimentComponent;
    /**
     * The NER analysis component
     */
    private StanfordNERComponent nerComponent;
    private String nerClassifierPath;
    

    /**
     * The processing components of this pipeline
     */
    ProcessingComponent[] components = null;

    public ProcessingPipeline() {

        // TODO: initialize reader / writer if necessary
    }

    /**
     * Getter for the processing pipeline components.
     *
     * @return the initialized components
     */
    public ProcessingComponent[] getComponents() {

        if (this.components == null) {

            this.initializeComponents();

        }

        return this.components;
    }

    /**
     * This method initializes the components of the processing pipeline.
     */
    private void initializeComponents() {
        try {
//            this.sentimentComponent = new SentimentComponent();
//            this.sentimentComponent.init();


            this.nerComponent = new StanfordNERComponent();
            this.nerComponent.init(nerClassifierPath);

            ProcessingComponent[] components = new ProcessingComponent[] {
//                    this.sentimentComponent,
//                    this.nerComponent,
                    new HeatmapComponent()
            };

            this.setComponents(components);

        } catch (Exception e) {
            LOG.error("Failed initializing pipeline components");
            LOG.error(e.getMessage());

            System.exit(1);
        }

    }

    public ProcessingPipeline setNERClassifierPath(String path) {
        this.nerClassifierPath = path;
        return this;
    }

    /**
     * Process a news document to produce annotations.
     *
     * @param document
     *            The document to process.
     * @return
     * @throws ProcessingException
     */
    public Document process(Document document) throws ProcessingException {
        if (document == null) {
            LOG.warn("Document supplied for processing was <null>. Returning <null> as well.");
            return null;
        }

        processComponents(document, this.getComponents());
        document.setStatus(Document.Status.PROCESSED);
        return document;
    }

    /**
     * Calls the process() method of a number of ProcessingComponent objects on
     * a document
     *
     * @param doc
     *            The document to process
     * @param components
     *            The components that should process the document
     * @return The processed document
     */
    private Document processComponents(Document doc, ProcessingComponent... components) throws ProcessingException {
        for (ProcessingComponent component : components) {
            if (component != null) {
                String componentName = component.getClass().getSimpleName();
                try {

                    doc = component.process(doc);
                    LOG.trace("Processing finished by component " + component.getClass().getSimpleName());
                } catch (ProcessingException e) {
                    LOG.error("Processing document with " + componentName + " failed: " + e.toString() + " --> "
                            + Arrays.toString(e.getStackTrace()));
                    throw e;
                }
            } else {
                LOG.trace("Component is null, can't process document with it.");
            }
        }
        return doc;
    }

    /**
     * This method reads all news articles from the input directory, processes
     * each news article and saves the analysis result to the output directory.
     * Each processed news article is moved to the 'processed' directory, even
     * if the processing failed.
     *
     *
     * @throws Exception
     */
    public void execute() throws Exception {

        LOG.info("Start processing new documents...");

        Document.Status status = Document.Status.PROCESSED; // NEW

        initializeComponents();

        // get all unprocessed documents from DB
        try {
            int scrollSize = 15;
            SearchResponse response = null;
            int i = 0;
            while (response == null || response.getHits().hits().length != 0) {

                response = StorageManager.getInstance().readDocumentsFromDB(status, i * scrollSize, scrollSize);

                for (SearchHit hit : response.getHits()) {
                    Document document = new Document(hit);

                    try {

                        // process all news articles with the processing components
                        this.process(document);

                        StorageManager.getInstance().storeDocument(document);
                    } catch (ProcessingException e) {
                        LOG.warn("Failed to process document " + document.getId()
                                + " No output will be written for this document.", e);

                        // mark document as FAILED and update
                        document.setStatus(Document.Status.FAILED);
                        try {
                            StorageManager.getInstance().storeDocument(document, false);
                        } catch (StorageException ex) {
                            LOG.error(ex.getStackTrace());
                        }

                        continue;
                    } catch (Exception e) {
                        LOG.warn("Exception occurred for document " + document.getId(), e);

                        // mark document as FAILED and update
                        document.setStatus(Document.Status.FAILED);
                        try {
                            StorageManager.getInstance().storeDocument(document, false);
                        } catch (StorageException ex) {
                            LOG.error(ex.getStackTrace());
                        }
                        continue;

                    }
                }
            }
            i++;


            LOG.info("Finished processing.");
        } catch(NoNodeAvailableException e) {
            LOG.error("Cannot connect to ElasticSearch: " + e.getMessage());
            System.exit(1);
        }
        //LOG.info("Next processing at " + context.getNextFireTime());
    }

    /**
     * @param components
     *            the components to set
     */
    public void setComponents(ProcessingComponent[] components) {
        this.components = components;
    }

    /**
     * This method starts the processing pipeline and repeats the processing
     * according to the schedule in the properties file. In each processing
     * cycle the pipeline checks whether there are new news articles and then
     * processes them.
     *
     * @param args
     */
    public static void main(String[] args) throws Exception {


        new ProcessingPipeline()
                .setNERClassifierPath((args.length > 0 ? args[0] : null))
                .execute();

    }
}
