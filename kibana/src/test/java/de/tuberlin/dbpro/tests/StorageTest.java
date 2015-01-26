package de.tuberlin.dbpro.tests;

import de.tuberlin.dbpro.model.Document;
import de.tuberlin.dbpro.model.SentimentAnnotation;
import de.tuberlin.dbpro.storage.StorageException;
import de.tuberlin.dbpro.storage.StorageManager;
import org.elasticsearch.action.mlt.MoreLikeThisRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Debugging only
 */
public class StorageTest {
    @Test
    public void ReadByStatusTest() throws StorageException {
        SearchResponse response = StorageManager.getInstance().readDocumentsFromDB(Document.Status.PROCESSED, 0, 10);

        for (SearchHit hit : response.getHits()) {
            Document doc = new Document(hit);

            System.out.println( doc + doc.fieldsToString());
        }

    }

    @Test
    public void AnnotationTest() throws StorageException {
        String id = "300101";

        Document doc = StorageManager.getInstance().getDocByID(id);

        SentimentAnnotation senti = new SentimentAnnotation();
        senti.setPosWords(10);
        senti.setNegWords(50);
        senti.setScoreTotal(3.54);

        doc.addAnnotation(senti);


        // ---
        StorageManager.getInstance().storeDocument(doc);
        doc = StorageManager.getInstance().getDocByID(id);

        System.out.println(doc + doc.fieldsToString());

    }

    @Test
    public void StatusTest() throws StorageException {
        String id = "300101";

        Document doc = StorageManager.getInstance().getDocByID(id);

        System.out.println(doc + doc.fieldsToString());
        doc.setStatus(Document.Status.PROCESSED);

        // ---
        StorageManager.getInstance().storeDocument(doc);
        doc = StorageManager.getInstance().getDocByID(id);

        System.out.println(doc + doc.fieldsToString());

    }
    @Test
    public void MoreLikeThisTest() {

        SearchResponse mlt = StorageManager.getInstance().getClient()
            .moreLikeThis(new MoreLikeThisRequest("news")
                            .type("article")
                            .id("300100")
                            .minTermFreq(1)
                            .minDocFreq(1)
            )
            .actionGet();

        for (SearchHit hit : mlt.getHits()) {
            System.out.println(hit.getId());
            System.out.println(hit.getSourceAsString());

        }

    }

}
