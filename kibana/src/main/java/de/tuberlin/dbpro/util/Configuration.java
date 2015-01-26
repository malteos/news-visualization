package de.tuberlin.dbpro.util;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.NoSuchElementException;

public class Configuration {

    /**
     * Logger for this class.
     */
    private static Logger LOG = Logger.getLogger(Configuration.class);
    /**
     * Singleton instance
     */
    private static Configuration instance = null;

    private PropertiesConfiguration properties;

    /**
     * Hidden constructor.
     */
    private Configuration() {

        try {
            this.properties = new PropertiesConfiguration(ConfigurationConstants.FILENAME_PROPERTIES);
        } catch (ConfigurationException e) {
            LOG.warn("Failed to load properties from: " + ConfigurationConstants.FILENAME_PROPERTIES
                    + ". Will try to load it from src/main/", e);
            try {
                this.properties = new PropertiesConfiguration("src/main/resources/"
                        + ConfigurationConstants.FILENAME_PROPERTIES);
            } catch (ConfigurationException e1) {
                LOG.warn("Failed to load properties from: " + "src/main/resources/"
                        + ConfigurationConstants.FILENAME_PROPERTIES, e1);

                throw new NullPointerException(e1.getMessage());
            }
        }
    }

    private static Configuration getInstance() {
        if (instance == null) {
            instance = new Configuration();
        }

        return instance;
    }

    public static BufferedReader getResourceAsReader(String property) throws IOException {
        return new BufferedReader(new InputStreamReader(
                getInstance().getClass().getClassLoader().getResourceAsStream(Configuration.getString(property))
        ));
    }

    /**
     * Returns the value for the key or null if no property with the given key
     * is available.
     *
     * @param _key
     * @return value for the key
     */
    public static Object getProperty(String _key) {

        return getInstance().getProperties().getProperty(_key);

    }

    /**
     * Returns the value for the key as string or null if no property with the
     * given key is available.
     *
     * @param _key
     * @return value for the key
     */
    public static String getString(String _key) {

        String result = null;

        try {
            result = getInstance().getProperties().getString(_key);
        } catch (NoSuchElementException e) {
            LOG.warn(e);
        }

        return result;

    }


    /**
     * Returns the value for the key as array of strings or null if no property with the
     * given key is available.
     *
     * @param _key
     * @return value for the key
     */
    public static String[] getStringArray(String _key) {

        String[] result = null;

        try {
            result = getInstance().getProperties().getStringArray(_key);
        } catch (NoSuchElementException e) {
            LOG.warn(e);
        }

        return result;

    }

    /**
     * Returns the value for the key as Double or null if no property with the
     * given key is available.
     *
     * @param _key
     * @return value for the key
     */
    public static Double getDouble(String _key) {

        Double result = null;

        try {
            result = getInstance().getProperties().getDouble(_key);
        } catch (NoSuchElementException e) {
            LOG.warn(e);
        }

        return result;

    }

    /**
     * Returns the value for the key or null if no property with the given key
     * is available.
     *
     * @param _key
     * @return value for the key
     */
    public static Integer getInteger(String _key) {

        Integer result = null;

        try {
            result = getInstance().getProperties().getInt(_key);
        } catch (NoSuchElementException e) {
            LOG.warn(e);
        }

        return result;

    }

    /**
     * @return the properties
     */
    private PropertiesConfiguration getProperties() {
        return this.properties;
    }

    /*
     * ############## PROPERTY KEYS #############
     */

    public static final String PROPERTY_SENTIMENT_POSITIVE = "dbpro.sentiment.dict.positive";

    public static final String PROPERTY_SENTIMENT_NEGATIVE = "dbpro.sentiment.dict.negative";

    public static final String PROPERTY_SENTIMENT_INVERT = "dbpro.sentiment.dict.invert";

    public static final String PROPERTY_SCHEDULE_GROUP = "dbpro.schedule.group";

    public static final String PROPERTY_SCHEDULE_CRON_EXPR = "pressreview.schedule.cron_expr";

    public static final String PROPERTY_NER_CLFPATH = "dbpro.ner.clf_path";

    public static final String PROPERTY_GEO_CSV_COUNTRY = "dbpro.geo.csv.country";
    public static final String PROPERTY_GEO_CSV_COUNTRY_ALIAS = "dbpro.geo.csv.country.alias";
    public static final String PROPERTY_GEO_CSV_CITY = "dbpro.geo.csv.city";

}

