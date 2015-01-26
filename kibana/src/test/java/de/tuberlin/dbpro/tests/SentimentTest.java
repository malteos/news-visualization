package de.tuberlin.dbpro.tests;

import de.tuberlin.dbpro.model.Document;
import de.tuberlin.dbpro.processing.ProcessingException;
import de.tuberlin.dbpro.processing.sentiment.SentimentComponent;
import de.tuberlin.dbpro.storage.StorageManager;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Debugging only
 */
public class SentimentTest {
    SentimentComponent component;
    List<Document> docs;

    @Before
    public void setUp() throws ProcessingException {

        // create an example document
        /*
         * this.document = new PipelineDocument();
         * this.document.setUrl(String.valueOf(1));
         * this.document.setPublicationDate(DateTime.now()); ;
         * this.document.setText("Das ist ein einfacher Text.");
         */

        String keyword = "tu berlin";

        docs = new ArrayList<Document>();

        Document doc;

        doc = new Document(StorageManager.docIndex, StorageManager.docType, "1");
        doc.setTitle("WAZ: Handelsketten befürchten höhere Müllgebühren");
        doc.setFullText("ssen (ots) – Handelsunternehmen wie die Drogeriemarktkette Rossmann befürchten höhere Müllgebühren, sollte der Bund wie geplant die Verpackungsverordnung ändern. Wie die Westdeutsche Allgemeine Zeitung in Essen (Montagsausgabe) berichtet, will Bundesumweltministerin Barbara Hendricks (SPD) “Missbrauch” beim Grünen Punkt bekämpfen. In dem Entwurf für die Novelle der Verpackungsverordnung, der der WAZ vorliegt, heißt es: “Schlupflöcher drohen das Erfassungssystem insgesamt zu destabilisieren”. Nach Berechnungen des Informationsdiensts Euwid fehlen dem Dualen System, das Sammlung, Sortierung und Verwertung von Verpackungen mit dem Grünen Punkt organisiert, jährlich 130 bis 150 Millionen Euro, weil Handelsketten ihren Verpackungsmüll selbst entsorgen. Bundes- und Landesregierung glauben aber, dass manche Händler diese Mengen künstlich hochrechnen, um Gebühren zu sparen. Sollte die geplante Novelle der Verpackungsverordnung in Kraft treten, befürchtet die Drogeriekette Rossmann erhebliche Zusatzkosten. Das Unternehmen müsse dann die Verpackungsmaterialien weiter selbst entsorgen, aber trotzdem für das Duale System in voller Höhe zahlen, sagte ein Sprecher der WAZ. Fastfood-Ketten dagegen seien von der Neuregelung nicht betroffen, so der Informationsdienst Euwid. Pressekontakt:");
        docs.add(doc);

        doc = new Document(StorageManager.docIndex, StorageManager.docType, "1");
        doc.setTitle("Freie Wähler reichen Antrag auf Gymnasiums-Volksbegehren ein");
        doc.setFullText("München. Die Freien Wähler haben ihr Volksbegehren für eine Teil-Rückkehr zum neunjährigen Gymnasium offiziell auf den Weg gebracht. Am Freitag reichten sie ihren Antrag beim Innenministerium sein, samt der nötigen Unterschriften: 25 000 sind vorgeschrieben, eingereicht wurden knapp 27 000. Nun muss das Ministerium den Antrag prüfen. Sollte es wie erwartet keine Bedenken haben, kommt es zunächst zum Volksbegehren: Dann müssen sich binnen zwei Wochen zehn Prozent der stimmberechtigten Bürger in Unterschriftenlisten eintragen. Wird dieses Quorum erreicht, kommt es zum Volksentscheid - falls der Landtag das Anliegen nicht direkt umsetzt. «Wir sehen uns als Trendsetter für ein neues G9», sagte der Hauptinitiator und FW-Generalsekretär Michael Piazolo vor der Unterschriftenübergabe.");
        docs.add(doc);

        doc = new Document(StorageManager.docIndex, StorageManager.docType, "1");
        doc.setTitle("Wowereit spricht Flughafenchef Mehdorn Vertrauen aus");
        doc.setFullText("Der Berliner Regierende Bürgermeister Klaus Wowereit (SPD) hat dem Flughafenchef Hartmut Mehdorn das Vertrauen ausgesprochen.Berlin . Der Berliner Regierende Bürgermeister Klaus Wowereit (SPD) hat dem Flughafenchef Hartmut Mehdorn das Vertrauen ausgesprochen. „Herr Mehdorn hat unser Vertrauen. Und er ackert mit allen Kräften, dass tatsächlich dieses Projekt zum Erfolg geführt wird“, sagte Wowereit am Freitag in Berlin mit Bezug auf den noch nicht vollendeten Hauptstadtflughafen in Schönefeld. Mehdorn war zuletzt für sein Krisenmanagement kritisiert worden. Um die aktuelle Lage zu besprechen, hatten sich zuvor Brandenburgs Ministerpräsident Dietmar Woidke (SPD), der Verkehrsstaatssekretär des Bundes, Rainer Bomba, und Mehdorn bei Wowereit im Roten Rathaus getroffen. Wowereit ist auch Vorsitzender des Flughafens-Aufsichtsrats. „Wir sind uns einig, dass wir ein gemeinsames Ziel haben: den Flughafen so schnell wie möglich fertigzustellen. Da gibt es keinen Dissens“, sagte der Berliner Regierungschef. Es werde aber auch bei der nächsten Sitzung des Aufsichtsrats im April kein Eröffnungstermin genannt werden. Die Eröffnung des Flughafens Berlin Brandenburg war vor gut einem Jahr auf unbestimmte Zeit verschoben worden. Grund sind Bau- und Planungsmängel, vor allem beim Brandschutz des Terminals.");
        docs.add(doc);



        // create the component to test
        this.component = new SentimentComponent();
        this.component.init();

    }

    @Test
    public void readWordList() throws IOException {

        String path = getClass().getClassLoader().getResources("sentiment/negative-words.txt").nextElement().getPath();

        System.out.println(path);

    }
    @Test
    public void testProcess() {

        for (Document doc : docs) {

            System.out.println(doc.getTitle());
            System.out.println(doc.getFullText());

            Map<String, Double> result =  this.component.getSentimentScore(doc.getFullText());
            System.out.println(result);

        }

        //Assert.assertEquals(null, this.document.getSearchKeyword());
        /*
         * try { // this.component.process(this.document);
         *
         * // Assert.assertEquals(String.valueOf(6), this.document.getStatus());
         *
         * } catch (ProcessingException e) {
         *
         * fail(e.getMessage()); }
         */

    }
}
