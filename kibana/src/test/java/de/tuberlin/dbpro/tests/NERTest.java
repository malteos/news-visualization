package de.tuberlin.dbpro.tests;

import de.tuberlin.dbpro.processing.ProcessingException;
import de.tuberlin.dbpro.processing.geolocation.GeoDB;
import de.tuberlin.dbpro.processing.geolocation.GeoLocationFinder;
import de.tuberlin.dbpro.processing.ner.stanford.StanfordNERComponent;
import org.junit.Test;

import java.util.Arrays;

/**
 * Debugging only
 */
public class NERTest {

    @Test
    public void InitTest() throws ProcessingException {


        String s1 = "Good afternoon Rajat Raina, how are you today?";
        String s2 = "I go to school at Stanford University, which is located in California.";


        s2 = "Bad Vilbel (dpa) - Für ihre hartnäckige Recherche zur Vetternwirtschaft im bayerischen Landtag erhält die Journalistin Angela Böhm von der Münchner \"Abendzeitung\" dieses Jahr den Wächterpreis der Tagespresse. Böhm habe entscheidend dazu beigetragen, dass die Praxis der Beschäftigung von Ehepartnern und Kindern auf Kosten des Steuerzahlers beendet worden sei, begründete die Stiftung \"Freiheit der Presse\" am Dienstag im hessischen Bad Vilbel die Wahl. Der erste Preis ist mit 12 000 Euro dotiert. Rang zwei (8000 Euro) belegen Peter Berger und Joachim Frank vom \"Kölner Stadt-Anzeiger\", die sich mit der umstrittenen Haltung katholischer Kliniken zu vergewaltigten schwangeren Frauen auseinandergesetzt haben. \"Unter dem Eindruck der Wirkung dieser Veröffentlichungen änderte die Kirche ihre Haltung\", heißt es dazu in der Begründung der Stiftung. Mit 6000 Euro werden Rudi Kübler und Christoph Mayer von der \"Südwest Presse\" (Ulm) gewürdigt. Sie recherchierten die Fehler am Ulmer Universitätsklinikum rund um den millionenschweren Neubau einer Chirurgie. Das Projekt hatte das Klinikum an den Rand des Ruins getrieben. Der Wächterpreis der Tagespresse wird am 16. Mai (17.30 Uhr) in Frankfurt verliehen.";

        StanfordNERComponent comp = new StanfordNERComponent();
        comp.init("/Users/m/Desktop/Studium/dbpro/stanford-ner-2012-05-22-german/dewac_175m_600.crf.ser.gz");


        s1 = "";
        s2 = "Europa und Amerika erhöhen in der Krim-Krise den Druck auf Kremlchef Wladimir Putin. Die EU und die USA drohen Russland mit Sanktionen, sollte New York seine Truppen nicht zügig von der ukrainischen Halbinsel zurückziehen. Russland bleibt stur und begründet seinen Militäreinsatz mit einem Hilferuf des abgesetzten Präsidenten Viktor Janukowitsch. US-Außenminister John Kerry wird heute zu Gesprächen in der Ukraine erwartet. Er will in Kiew mit Vertretern der neuen Regierung zusammentreffen. In Brüssel beraten die Botschafter der 28 Nato-Staaten über die angespannte Situation. Die Kommentare wurden für diesen Artikel deaktiviert";

        comp.identifyNER(s1 + s2);

        System.out.println(comp.getNERResults().toString());
//        System.out.println(comp.identifyNER(s2 + s1).toString());
//        System.out.println(comp.identifyNER(s2 + s2).toString());


    }

    @Test
    public void GeoLocationTest() {

        /*
        * City alias?
        * Kiew == Kiev
        * Polen == ID
        *
         */

        GeoLocationFinder geo = new GeoLocationFinder(GeoLocationFinder.Provider.GeoDB)
                .setGeoDB(new GeoDB()
                                .init()
                )
                .fetchGeoData(Arrays.asList(new String[]{"Paris", "Berlin", "New York", "Peking", "Moskau", "Polen"}));

        System.out.println(geo.getGeoDB().toString());

        System.out.println(
                geo.getGeoLocations());

        System.out.println(       geo.getCountryCodes()
                );


    }
}
