package de.tuberlin.dbpro.processing.geolocation;

import de.tuberlin.dbpro.util.Configuration;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * Self-made geo coding with CSV.
 *
 * - can map country to iso country code
 * - can map city to lat/lng and iso country code
 * - a country match overpowers city match
 * - a city can be overwritten if it has a higher population
 *
 */
public class GeoDB {
    /**
     * Logger for this class.
     */
    private static Logger LOG = Logger.getLogger(GeoDB.class);

    private HashMap<String, String> countries = new HashMap<String, String>();
    private HashMap<String, GeoCity> cities = new HashMap<String, GeoCity>();

    private GeoDB instance = null;
    private static String csvFieldDelimiter = "\t";
    private String pathCityCSV;
    private String pathCountryCSV;
    private BufferedReader countryCsvReader;
    private BufferedReader cityCsvReader;

    public boolean containsCity(String needle) {
        return cities.containsKey(needle);
    }

    public GeoCity getCity(String key) {
        return cities.get(key);
    }

    public boolean containsCountry(String needle) {
        return countries.containsKey(needle);
    }

    public String getCountry(String key) {
        return countries.get(key);
    }

    public GeoDB() {

    }

    /**
     * Reading data from CSV
     *
     * @return GeoGB
     */
    public GeoDB init() {

        try {
            countryCsvReader = Configuration.getResourceAsReader(Configuration.PROPERTY_GEO_CSV_COUNTRY);
            readCountryCSV();

            countryCsvReader = Configuration.getResourceAsReader(Configuration.PROPERTY_GEO_CSV_COUNTRY_ALIAS);
            readCountryCSV();

            cityCsvReader = Configuration.getResourceAsReader(Configuration.PROPERTY_GEO_CSV_CITY);
            readCityCSV();
        } catch(IOException e) {
            LOG.error("Cannot read csv from resource", e);
        }


        return this;
    }

    public GeoDB(String cityCSV, String countryCSV) {
        pathCityCSV = cityCSV;
        pathCountryCSV = countryCSV;
    }

    public GeoDB setCityCSV(String path) {
        pathCityCSV = path;

        return this;
    }

    public GeoDB setCountryCSV(String path) {
        pathCountryCSV = path;

        return this;
    }


    /**
     * Expects CSV columns:
     * - cityname
     * - alias
     * - latitude
     * - longitude
     * - country code
     * - population
     */
    public GeoDB readCityCSV() {
        LOG.info("Reading City CSV");
        try {
            String line;

            if(cityCsvReader == null && pathCityCSV != null) {
                cityCsvReader = new BufferedReader(new FileReader(pathCityCSV));
            }

            while ((line = cityCsvReader.readLine()) != null) {

                String[] cols = line.split(csvFieldDelimiter);
                if(cols.length == 6) {

                    GeoCity city = new GeoCity(Double.parseDouble(cols[2]), Double.parseDouble(cols[3]), cols[4], Integer.parseInt(cols[5]));

                    addCity(cols[0].toLowerCase(), city);

                    String[] names = cols[1].split(",");

                    for(String alias : names) {
                        String name = alias.toLowerCase();
                        addCity(name, city);
                    }

                }
            }
            cityCsvReader.close();
        } catch(Exception e) {
            LOG.error("Cannot read CSV: " + cityCsvReader, e);
        }
        return this;
    }

    /**
     * Add city to db, cities with higher population can overwrite existing
     * city names.
     *
     * @param name
     * @param city
     * @return true: if city was added to db
     */
    public boolean addCity(String name, GeoCity city) {
        if(cities.containsKey(name)) {
            if(cities.get(name).population < city.population) {
                cities.replace(name, city);
                return true;
            } else {
                return false;
            }
        } else {
            cities.put(name, city);
            return true;
        }
    }

    /**
     * Expects CSV columns:
     * - countryCode
     * - countryName
     */
    public GeoDB readCountryCSV() {
        LOG.info("Reading Country CSV");
        try {
            String line;

            if(countryCsvReader == null && pathCountryCSV != null) {
                countryCsvReader = new BufferedReader(new FileReader(pathCountryCSV));
            }

            while ((line = countryCsvReader.readLine()) != null) {
                String[] cols = line.split(csvFieldDelimiter);

                if(cols.length == 2) {
                    countries.put(cols[1].toLowerCase(), cols[0]);
                }
            }

            countryCsvReader.close();
        } catch(Exception e) {
            LOG.error("Cannot read CSV: " + countryCsvReader, e);
        }
        return this;
    }

    public String toString() {
        return "SetSize:  " + (countries.size() + cities.size());
    }
}
