package de.tuberlin.dbpro.processing.sentiment;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Expected format
 * Gefecht|NN	-0.3373	Gefechten,Gefechtes,Gefechte,Gefechts
 *
 *
 */
public class SentimentWord {

    private boolean positive = true;
    private String type;
    private String word;
    private List<String> aliase = new ArrayList<String>();
    private double score;

    public static final String TYPE_NOUN = "NN";
    public static final String TYPE_ADJECTIVE = "ADJX";
    public static final String TYPE_ADVERB = "ADV";
    public static final String TYPE_VERB = "VVINF";

    private static final String SEPARATOR_WORD = "|";
    private static final String SEPARATOR_BEFORE_SCORE = "\t";
    private static final String SEPARATOR_AFTER_SCORE = "\t";
    private static final String SEPARATOR_ALIAS = ",";


    public SentimentWord(String line) {
        try {
            word = line.substring(0, line.indexOf(SEPARATOR_WORD) ).toLowerCase();
            setType( line.substring(line.indexOf(SEPARATOR_WORD) + 1, line.indexOf(SEPARATOR_BEFORE_SCORE)));

            int beforescore = line.indexOf(SEPARATOR_BEFORE_SCORE) + 1;
            int afterscore = line.indexOf(SEPARATOR_AFTER_SCORE, beforescore);

            if(afterscore > 0) {
                setScore( line.substring(beforescore, afterscore ));
                aliase = (List<String>) Arrays.asList( line.substring(  afterscore + 1 ).toLowerCase().split(SEPARATOR_ALIAS) );
            } else {
                setScore( line.substring(beforescore ));
            }
        } catch(Exception e) {
            System.out.println( "Cannot read line: " + line);
            System.out.println( e.getMessage() );

            //System.exit(0);
        }
    }

    public void setPositive() {
        positive = true;
    }

    public void setNegative() {
        positive = false;
    }

    public boolean isPositive() {
        return positive;
    }

    public boolean isType(String needle) {
        if(type.equals(needle))
            return true;
        else
            return false;
    }

    public void setType(String type) {
        if(type.equals( TYPE_NOUN) || type.equals( TYPE_ADJECTIVE ) || type.equals( TYPE_VERB ) || type.equals( TYPE_ADVERB ))
            this.type = type;
        else
            System.err.println("Type not supported:" + type);
    }

    public void setScore(String score) {
        this.score = Double.parseDouble(score);
    }

    public String getWord() {
        return word;
    }

    public List<String> getAliase() {
        return aliase;
    }

    public String toString() {
        return word + " (score: " + score + "; " + aliase + ")";
    }

    public double getScore() {
        return score;
    }

    public static void main(String[] args) {

        System.out.println( new SentimentWord("brisant|ADJX	-0.0048	brisantester,brisantestes,brisanterem,brisanteren,brisantesten,brisanterer,brisantestem,brisanteres,brisantere,brisanter,brisante,brisantes,brisantest,brisanteste,brisantem,brisanten"));

    }
}
