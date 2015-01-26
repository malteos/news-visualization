package de.tuberlin.dbpro.storage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.tuberlin.dbpro.model.Annotation;
import de.tuberlin.dbpro.model.Document;
import org.apache.log4j.Logger;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

@Deprecated
public class StorageManager {
    public final static String docIndex = "news";
    public final static String docType = "article";

    private static Logger LOG = Logger.getLogger(StorageManager.class);
    private Client client;

    private static StorageManager instance = null;

    public static StorageManager getInstance() {
        if (instance == null) {
            instance = new StorageManager();
            return instance;
        } else {
            return instance;
        }
    }

    public StorageManager() {
        client = new TransportClient()
                .addTransportAddress(new InetSocketTransportAddress("localhost", 9300));
    }

    public Client getClient() {
        return client;
    }

    public Document getDocByID(String docID) throws StorageException {

        GetResponse response = client.prepareGet(docIndex, docType, docID)
                .setFields(Document.FIELDS)
                .execute()
                .actionGet();

        return new Document(response);
    }


    public QueryBuilder getStatusQuery(Document.Status status) {

        if (status == Document.Status.NEW)
            return QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(), FilterBuilders.notFilter(FilterBuilders.existsFilter("status")));
        else
            return QueryBuilders.queryString("status:" + status);
    }

    public SearchResponse readDocumentsFromDB(Document.Status statusFilter,
                                              int from, int size) throws StorageException {

        LOG.debug("Reading documents from db with status " + statusFilter);

        return getClient().prepareSearch(docIndex)
                .setTypes(docType)

                .setQuery(getStatusQuery(statusFilter))
                        //.setQuery(QueryBuilders.matchAllQuery())

                .setSize(size)
                .setFrom(from)
                .addFields(Document.FIELDS)

                .execute()
                .actionGet();

    }

    /**
     * Reads all annotation from database matching documentId
     *
     * @param documentId
     * @return all annotations for this documentId
     * @throws Exception
     */
    public ArrayList<Annotation> readAnnotationsFromDatabase(String documentId) throws StorageException {

        ArrayList<Annotation> annos = new ArrayList<Annotation>();

        // Loop all possible annotaions
        for (Class annotype : Annotation.ANNOTATIONS) {
            try {
                /*
                String sql = "SELECT * FROM `" + Annotation.getTableName(annotype) + "` WHERE `documentId` = ?";

                Connection db = MySQLConnection.getInstance();
                PreparedStatement preparedStatement = db.prepareStatement(sql);
                preparedStatement.setString(1, documentId);

                ResultSet dbRes = preparedStatement.executeQuery();
                while (dbRes.next()) {

                    try {
                        Annotation annotation = (Annotation) annotype.newInstance();
                        annotation.setFieldsFromResultSet(dbRes);

                        annos.add( annotation );

                    } catch(Exception e) {
                        LOG.error("Cannot set annotations", e);
                    }
                }

                */
            } catch (Exception e) {
                LOG.error("Cannot fetch annoation " + annotype, e);
            }
        }
        return annos;
    }

    /**
     * Store (create/update) document and its annotations
     *
     * @param doc
     * @throws StorageException
     */
    public void storeDocument(Document doc) throws StorageException {
        storeDocument(doc, true);
    }

    /**
     * Store (create/update) document and optionally its annotations
     *
     * @param doc
     * @param storeAnnotations
     * @throws de.tuberlin.dbpro.storage.StorageException
     */
    public void storeDocument(Document doc, boolean storeAnnotations)
            throws StorageException {

        if (doc.getStatus() == Document.Status.NEW)
            throw new StorageException("Cannot store documents with status = NEW");

        try {
            // Update object
            XContentBuilder updates = jsonBuilder()
                    .startObject();

            // Set updated document fields
            updates.field("status", doc.getStatus());

            if (storeAnnotations) {
                // Object to Json Mapper
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(SerializationFeature.INDENT_OUTPUT, true);

                try {
                    // Add annotations
                    for (Annotation annotation : doc.getAnnotations()) {

                        updates.rawField(
                                "x" + annotation.getClass().getSimpleName(),

                                mapper.writeValueAsString(annotation).getBytes())

                        ;
                    }

                } catch (JsonProcessingException e) {
                    LOG.error("Cannot add annotation to update object. ", e);
                    e.printStackTrace();
                }
            }

            LOG.debug("Updating document in db: " + doc);

            // close update object
            updates.endObject();


            UpdateRequest updateRequest = new UpdateRequest();
            updateRequest.index(doc.getIndex());
            updateRequest.type(doc.getType());
            updateRequest.id(doc.getId());
            updateRequest.doc(updates);

            UpdateResponse res = client.update(updateRequest).get();

//            if(!res.isCreated())
//                throw new StorageException("Cannot update docId: " + doc.getId().toString() + "\n");

        } catch (IOException e) {
            throw new StorageException("JSON Builder error:" + e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new StorageException(e.getMessage());
        } catch (ExecutionException e) {
            e.printStackTrace();
            throw new StorageException(e.getMessage());
        }

    }

    public static void main(String[] args) throws Exception {
        StorageManager store = StorageManager.getInstance();
        //store.getDocumentsWithoutAnnotations(CrawlingAnnotation.class);

        Document d = new Document("news", "article", "3371");

        store.storeDocument(d);

    }
}
