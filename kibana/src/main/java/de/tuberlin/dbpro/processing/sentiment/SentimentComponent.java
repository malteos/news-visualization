package de.tuberlin.dbpro.processing.sentiment;

import de.tuberlin.dbpro.model.Document;
import de.tuberlin.dbpro.model.SentimentAnnotation;
import de.tuberlin.dbpro.processing.ProcessingComponent;
import de.tuberlin.dbpro.processing.ProcessingException;
import de.tuberlin.dbpro.util.Configuration;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SentimentComponent implements ProcessingComponent {

    public static final double MAX_SCORE = 5;
    public static final double MIN_SCORE = -5;

    /**
     * based on - https://gist.github.com/leomelzer/3075236
     *
     * senti words - http://asv.informatik.uni-leipzig.de/download/sentiws.html
     *
     *
     */
    // used to store positive and negative words for scoring
    final private static Logger logger = Logger.getLogger(SentimentComponent.class);
    static HashMap<String, SentimentWord> words = new HashMap<String, SentimentWord>();
    static ArrayList<String> invertList;

    //
    //    double scoreNouns = 0;
    //    double scoreAdjectives = 0;
    //    double scoreVerbs = 0;
    //    double scoreAdverbs = 0;
    //    double scoreTotal = 0;
    //
    //    int posWords = 0;
    //    int negWords = 0;
    //    int totalWords = 0;

    // debug
    public static void main(String[] args) throws ProcessingException {
        SentimentComponent senti = new SentimentComponent();

        senti.init();

        logger.debug("SentimentScore: " + senti.getSentimentScore("TU Berlin ist nicht scheisse."));
    }

    public void init() throws ProcessingException {
        logger.info("init SentimentComponent (reading word lists..)");

        try {
            this.readWordLists();
        } catch (IOException e) {
            throw new ProcessingException("Cannot read from word lists: " + e.getMessage(), e);
        }
    }

    /**
     * does some string mangling and then calculates occurrences in positive /
     * negative word list and finally the delta
     *
     *
     * @param input
     *            String: the text to classify
     * @return score int: if < 0 then -1, if > 0 then 1 otherwise 0 - we don't
     *         care about the actual delta
     */
    public Map<String, Double> getSentimentScore(String input) {

        double scoreTotal = 0;
        double scoreNouns = 0;
        double scoreAdjectives = 0;
        double scoreVerbs = 0;
        double scoreAdverbs = 0;
        double posWords = 0;
        double negWords = 0;
        double totalWords = 0;

        // normalize!
        input = input.toLowerCase();
        input = input.trim();
        // remove all non alpha-numeric non whitespace chars
        // TODO: Umlaute?
        input = input.replaceAll("[^a-zA-Z0-9\\s]", "");

        // so what we got?
        String[] inputWords = input.split(" ");
        SentimentWord sw;

        totalWords = inputWords.length;

        // check if the current word appears in our reference lists...
        for (int i = 0; i < inputWords.length; i++) {

            if (words.containsKey(inputWords[i])) {
                sw = words.get(inputWords[i]);

                boolean invert = false;
                if (i > 0) {
                    String prevW = inputWords[i - 1];
                    invert = isInvertWord(prevW);
                }

                double score = (invert ? sw.getScore() * (-1) : sw.getScore());

                // Score by word type
                if (sw.isType(SentimentWord.TYPE_NOUN))
                    scoreNouns += score;
                else if (sw.isType(SentimentWord.TYPE_ADJECTIVE))
                    scoreAdjectives += score;
                else if (sw.isType(SentimentWord.TYPE_VERB))
                    scoreVerbs += score;
                else if (sw.isType(SentimentWord.TYPE_ADVERB))
                    scoreAdverbs += score;

                // Score by pos/neg count
                if ((sw.isPositive() && !invert) || !sw.isPositive() && invert)
                    posWords++;
                else
                    negWords++;

            }
        }

        // positive matches MINUS negative matches
        // or other formula?
        // (posWords - negWords);
        scoreTotal = scoreNouns + scoreAdjectives + scoreVerbs + scoreAdverbs;

        HashMap<String, Double> score = new HashMap<String, Double>();

        score.put("posWords", 1.0 * posWords);
        score.put("negWords", 1.0 * negWords);
        score.put("totalWords", 1.0 * totalWords);

        score.put("scoreNouns", scoreNouns);
        score.put("scoreVerbs", scoreVerbs);
        score.put("scoreAdverbs", scoreAdverbs);
        score.put("scoreAdjectives", scoreAdjectives);
        score.put("scoreTotal", scoreTotal);

        // Normalize score
        if(scoreTotal > MAX_SCORE) {
            score.put("scoreNormalized", MAX_SCORE);
        } else if(scoreTotal < MIN_SCORE) {
            score.put("scoreNormalized", MIN_SCORE);
        } else {
            score.put("scoreNormalized", scoreTotal);
        }

        return score;
    }

    private boolean isInvertWord(String word) {
        return SentimentComponent.invertList.contains(word);
    }

    private void readWordLists() throws IOException {

        // source: www.cs.uic.edu/~liub/FBS/sentiment-analysis.html
        BufferedReader negReader = Configuration.getResourceAsReader(Configuration.PROPERTY_SENTIMENT_NEGATIVE);
        BufferedReader posReader = Configuration.getResourceAsReader(Configuration.PROPERTY_SENTIMENT_POSITIVE);
        BufferedReader invReader = Configuration.getResourceAsReader(Configuration.PROPERTY_SENTIMENT_INVERT);

        // currently read word
        String line;
        SentimentWord sw;

        // add words to comparison list
        while ((line = negReader.readLine()) != null) {
            sw = new SentimentWord(line);
            sw.setNegative();
            words.put(sw.getWord(), sw);
            for (String alias : sw.getAliase()) {
                words.put(alias, sw);
            }
        }

        while ((line = posReader.readLine()) != null) {
            sw = new SentimentWord(line);
            sw.setPositive();
            words.put(sw.getWord(), sw);
            for (String alias : sw.getAliase()) {
                words.put(alias, sw);
            }
        }

        SentimentComponent.invertList = new ArrayList<String>();

        while ((line = invReader.readLine()) != null) {
            invertList.add(line);
        }

        // cleanup
        negReader.close();
        posReader.close();
        invReader.close();
    }

    @Override
    public Document process(Document _document) throws ProcessingException {

        //checke whether create or to update annotation
        SentimentAnnotation anno = null;
        Map<String, Double> scores = null;

        if (_document.getFullText() != null && _document.getFullText().length() > 0) {

            try {
                anno = new SentimentAnnotation();
                scores = this.getSentimentScore(_document.getFullText());

                // Set score to annotation
                anno.setNegWords(scores.get("negWords").intValue());
                anno.setPosWords(scores.get("posWords").intValue());

                anno.setTotalWords(scores.get("totalWords").intValue());

                anno.setScoreAdjectives(scores.get("scoreAdjectives"));
                anno.setScoreAdverbs(scores.get("scoreAdverbs"));
                anno.setScoreNouns(scores.get("scoreNouns"));
                anno.setScoreTotal(scores.get("scoreTotal"));
                anno.setScoreVerbs(scores.get("scoreVerbs"));

            } catch (Exception e) {
                logger.warn("Failed to perform sentiment analysis. Will not annotate document " + _document.getId());
                anno = null;
            }
            // Add annotation to document
            if (anno != null) {
                _document.addAnnotation(anno);
            }

        }

        return _document;
    }
}
