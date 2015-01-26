package de.tuberlin.dbpro.processing.ner.stanford;

import de.tuberlin.dbpro.model.Document;
import de.tuberlin.dbpro.model.NERAnnotation;
import de.tuberlin.dbpro.model.SentimentAnnotation;
import de.tuberlin.dbpro.processing.ProcessingComponent;
import de.tuberlin.dbpro.processing.ProcessingException;
import de.tuberlin.dbpro.util.Configuration;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class StanfordNERComponent implements ProcessingComponent {

    final private static String CATEGORY_LOCATIONS = "I-LOC";
    final private static String CATEGORY_ORGANISATIONS = "I-ORG";
    final private static String CATEGORY_MISC = "I-MISC";
    final private static String CATEGORY_PERSONS = "I-PER";

    private LinkedHashMap<String, HashSet<String>> nerResults = new LinkedHashMap<String, HashSet<String>>();

	final private static Logger LOG = Logger
			.getLogger(StanfordNERComponent.class);
	private CRFClassifier<CoreLabel> classifier;

	public void init(String classifierPath) throws ProcessingException {

		try {

            if(classifierPath == null) {
                classifierPath = getClassifierPath();
            }

			classifier = CRFClassifier.getClassifierNoExceptions(classifierPath);
		} catch (IOException e) {
            throw new ProcessingException("Cannot open NER classifier at " + classifierPath + ": " + e.getMessage(), e);
		}

	}

	public String getClassifierPath() throws IOException {
		String path = Configuration
				.getString(Configuration.PROPERTY_NER_CLFPATH);
		// String path = "dbpro.ner.clf_path";
		return getClass().getClassLoader().getResources(path).nextElement()
				.getPath();
	}

	public static void main(String[] args) throws ProcessingException {

		String s1 = "Amerika ist paralysiert Die Vereinigten Staaten haben mit mehr als dem Feuer von Ferguson und einer gescheiterten Einwanderungspolitik zu k�mpfen. Doch von Washington geht kein Signal aus, dass sich Kongress und Regierung ihrer gemeinsamen Verantwortung bewusst w�ren. Eine Analyse.";
		String s2 = "Wenn ich Jahrzehnte j�nger w�re, h�tte ich ein Zelt aufgebaut vor dem Kanzleramt, bis eine Antwort k�me.� Nun ist Grass G�nther aber nicht Jahrzehnte j�nger, sondern genau so alt, wie er heute ist und insofern � aber nur insofern! � ein ganz gew�hnlicher Mensch. Das Zelten mag bei ihm wirklich eine Altersfrage sein. Kann man sich aber Grass, den J�ngeren, campierend vorstellen? Auch nicht so richtig. Noch weniger aber kann man sich vorstellen, bei ihm zu wohnen, selbst unter Zwang nicht. Wenn aber die Fl�chtlinge Pech haben und die neueste Schnapsidee von Grass tats�chlich aufgegriffen werden sollte, dann k�me  dies zumindest auf einige von ihnen zu. Der Literat hat n�mlich allen Ernstes vorgeschlagen, dass die Deutschen angesichts der Str�me von Fl�chtlingen dazu zwangsverpflichtet werden m�ssten, welche bei sich aufzunehmen. Nach dem Krieg habe es auch keine andere M�glichkeit gegeben, sonst h�tte man die 14 Millionen deutschen und deutschst�mmigen Fl�chtlinge nie und nimmer integrieren k�nnen. So viele werden es diesmal gottlob nicht sein. Nach j�ngsten Berechnungen halten sich zurzeit rund 200.000 Fl�chtlinge in Deutschland auf � f�r ein einzelnes Dorf wie Behlendorf bei L�beck, wo Grass wohnt, aber wohl zu viel, f�r Grassens Haus erst recht. Man wird ihm aber sicher ein Kontingent zuteilen. Denn obwohl er nicht extra seine Bereitschaft dazu erkl�rt hat, kann man nur annehmen, dass er selbst dabei mit gutem Beispiel vorangehen will. So erfreulich und humanit�r begr��enswert es f�r die Betroffenen sein wird, ein Dach �ber dem Kopf zu haben � beneiden mag man sie darum nicht. Denn will man das: mit einem unentwegt Pfeife schmauchenden Grantler abends vor dem Fernseher sitzen und sich bei jeder politischen Nachricht einreden lassen, ein R�ckfall in die NS-Zeit st�nde unmittelbar bevor? Im beiderseitigen Interesse und im Interesse der V�lkerfreundschaften sollte man im Garten ein Zelt aufschlagen. Das passierte 2006 in Oldenburg. Mouhammad Name wird erkannt? Ren� geht zu Starbucks. Felix wohnt beim Brandenburgr Tor. Der wohnt in Charlottenburg. Oder in Wedding. Alles wurde von Andreas und der WTO beobachtet. Danach gab es essen bei McDonald's. Greenpeace und die UN sind jetzt Freunde.";

		StanfordNERComponent comp = new StanfordNERComponent();
		comp.init(null);
        comp.identifyNER(s2);

		LinkedHashMap<String, HashSet<String>> map = comp.getNERResults();
		System.out.println(map.toString());

	}

    public LinkedHashMap<String, HashSet<String>> getNERResults() {
        return nerResults;
    }
	/**
	 * identify Name,organization location etc entities and return Map<List>
	 * 
	 * @param text
	 *            -- data
	 * @return Map<category, list tags> NOT UNIQUE
	 */
	public void identifyNER(String text) {

        LinkedHashMap<String, LinkedList<String>> tmpResults = new LinkedHashMap<String, LinkedList<String>>();
		List<List<CoreLabel>> classifiedSentences = classifier.classify(text);

		for (List<CoreLabel> classifiedSentence : classifiedSentences) {

            for(int i = 0; i < classifiedSentence.size(); i++) {
                CoreLabel coreLabel = classifiedSentence.get(i);

				String word = coreLabel.word();
				String category = coreLabel.get(CoreAnnotations.AnswerAnnotation.class);

				if (!"O".equals(category)) {
                    if(i > 0) {
                        // if previous category is the same, concatenate words
                        CoreLabel prevLabel = classifiedSentence.get(i - 1);
                        String prevCategory = prevLabel.get(CoreAnnotations.AnswerAnnotation.class);

                        if(category.equals(prevCategory)) {

                            // update prev category
                            tmpResults.get(category).removeLast();
                            word = prevLabel.word() + " " + coreLabel.word();
                        }
                    }

					if (!tmpResults.containsKey(category)) {
                        tmpResults.put(category, new LinkedList<String>());
                    }

                    tmpResults.get(category).add(word);
				}

			}

		}
        // Make tmpResults unique
        nerResults.clear();

        for(String category : tmpResults.keySet()) {
            nerResults.put(category, new HashSet<String>(tmpResults.get(category)));
        }
    }

	public Document process(Document doc) throws ProcessingException {

		// checke whether create or to update annotation
		NERAnnotation anno = null;
		LinkedHashMap<String, HashSet<String>> map = null;

		if (doc.getFullText() != null && doc.getFullText().length() > 0) {

			try {
				anno = new NERAnnotation();
				identifyNER(doc.getFullText());

                map = nerResults;
				ArrayList<String> list;
				if (map.get("I-LOC") != null){
					list = new ArrayList<String>(map.get("I-LOC"));
					anno.setNerLoc(list);
					anno.setNerLocCount(list.size());
				}
				if (map.get("I-MISC") != null){
					list = new ArrayList<String>(map.get("I-MISC"));
					anno.setNerMisc(list);
					anno.setNerMiscCount(list.size());
				}
				if (map.get("I-ORG") != null){
					list = new ArrayList<String>(map.get("I-ORG"));
					anno.setNerOrg(list);
					anno.setNerOrgCount(list.size());
				}
				if (map.get("I-PER") != null){
					list = new ArrayList<String>(map.get("I-PER"));
					anno.setNerPer(list);
					anno.setNerPerCount(list.size());
				}

			} catch (Exception e) {
				LOG.warn("Failed to perform NER analysis. Will not annotate document "
						+ doc.getId() + " Error message: " + e);
				anno = null;
			}
			// Add annotation to document
			if (anno != null) {
				doc.addAnnotation(anno);
			}

		}

		// doc.addAnnotation();

		return doc;

	}
}
