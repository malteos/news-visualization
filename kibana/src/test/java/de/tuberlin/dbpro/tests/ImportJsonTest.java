package de.tuberlin.dbpro.tests;

import de.tuberlin.dbpro.news.ImportJson;
import org.junit.Test;

/**
 * Debugging only
 */
public class ImportJsonTest {
    @Test
    public void LocalTest() {
        ImportJson.main(new String[]{
                "/Users/m/Desktop/Studium/dbpro/news.json",
                "ELASTICSEARCH",
                "a1",
                "yarticle",
                "/Users/m/Desktop/Studium/dbpro/stanford-ner-2012-05-22-german/dewac_175m_600.crf.ser.gz",
                "y"
        });

    }

    @Test
    public void FileTest() {
        ImportJson.main(new String[]{
                "/Users/m/Desktop/Studium/dbpro/news.json",
                "FILE",
                "a1",
                "yarticle",
                "/Users/m/Desktop/Studium/dbpro/stanford-ner-2012-05-22-german/dewac_175m_600.crf.ser.gz",
                "y"
        });

    }

    @Test
    public void LocalProcessedTest() {
        ImportJson.main(new String[]{
                "/Users/m/Desktop/Studium/dbpro/news.json.processed",
                "STDOUT",
                "a1",
                "processed",
                "/Users/m/Desktop/Studium/dbpro/stanford-ner-2012-05-22-german/dewac_175m_600.crf.ser.gz",
                "n"
//                ,
//                "40",
//                "30",
        });

    }
}
