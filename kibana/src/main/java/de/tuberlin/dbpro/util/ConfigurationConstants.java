package de.tuberlin.dbpro.util;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class ConfigurationConstants {

    public static final String FILENAME_PROPERTIES = "dbpro.properties";

    //    public static final String FILENAME_KEYWORDS = "src/main/resources/keywords.txt";

    /**
     * Hidden constructor.
     */
    public ConfigurationConstants() {

    }

    /**
     * The project's default language for text processing is GERMAN
     */
    public static final Locale DEFAULT_LOCALE = Locale.GERMAN;

    /**
     * The project's default character encoding is UTF-8
     */
    public static final String DEFAULT_ENCODING = "UTF-8";

    /**
     * Simple date format that can be used for saving files.
     */
    public static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

}
