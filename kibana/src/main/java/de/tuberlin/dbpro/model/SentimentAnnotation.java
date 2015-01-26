package de.tuberlin.dbpro.model;


import org.apache.log4j.Logger;

@Deprecated
public class SentimentAnnotation extends Annotation {
    private static Logger LOG = Logger.getLogger(SentimentAnnotation.class);

    private double scoreNouns = 0;
    private double scoreAdjectives = 0;
    private double scoreVerbs = 0;
    private double scoreAdverbs = 0;
    private double scoreTotal = 0;
    private int posWords = 0;
    private int negWords = 0;
    private int totalWords = 0;

    public int markerWidth = 148;
    public int markerPosition = markerWidth / 2;

    public double getScoreNouns() {
        return scoreNouns;
    }

    public void setScoreNouns(double scoreNouns) {
        this.scoreNouns = scoreNouns;
    }

    public double getScoreAdjectives() {
        return scoreAdjectives;
    }

    public void setScoreAdjectives(double scoreAdjectives) {
        this.scoreAdjectives = scoreAdjectives;
    }

    public double getScoreVerbs() {
        return scoreVerbs;
    }

    public void setScoreVerbs(double scoreVerbs) {
        this.scoreVerbs = scoreVerbs;
    }

    public double getScoreAdverbs() {
        return scoreAdverbs;
    }

    public void setScoreAdverbs(double scoreAdverbs) {
        this.scoreAdverbs = scoreAdverbs;
    }

    public int getPosWords() {
        return posWords;
    }

    public void setPosWords(int posWords) {
        this.posWords = posWords;
    }

    public int getNegWords() {
        return negWords;
    }

    public void setNegWords(int negWords) {
        this.negWords = negWords;
    }

    public int getTotalWords() {
        return totalWords;
    }

    public void setTotalWords(int totalWords) {
        this.totalWords = totalWords;
    }

    public double getScoreTotal() {
        return scoreTotal;
    }

    public void setScoreTotal(double scoreTotal) {
        this.scoreTotal = scoreTotal;
    }

    public void setMarkerPosition() {
        int min = -5;
        int max = 5;
        double score = this.scoreTotal;

        if (score < min)
            score = min;
        else if (score > max)
            score = max;

        this.markerPosition = (int) Math.round((markerWidth / 2) + (markerWidth / 2) / max * score);
    }

}
