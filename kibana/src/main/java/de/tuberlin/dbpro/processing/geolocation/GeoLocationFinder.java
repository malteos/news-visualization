package de.tuberlin.dbpro.processing.geolocation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Geo Coding Class
 * <p/>
 * Transform geo string to lat/lng and/or country code.
 * Using: GeoDB or GoogleAPI
 */
public class GeoLocationFinder {
    public enum Provider {
        GeoDB,
        GoogleAPI
    }

    private Provider provider;
    private GeoDB geoDB = null;

    /**
     * Logger for this class.
     */
    private static Logger LOG = Logger.getLogger(GeoLocationFinder.class);

    private HashSet<LatLng> geoLocations = new HashSet<LatLng>();
    private HashSet<String> countryCodes = new HashSet<String>();

    List<String> locations;
    HashSet<String> uniqueLocations;

    public GeoLocationFinder fetchGeoData(List<String> locations) {
        this.locations = locations;
        return fetchGeoData(new LinkedHashSet<String>(locations));
    }

    public GeoLocationFinder(Provider provider) {
        this.provider = provider;
    }

    public HashSet<LatLng> getGeoLocations() {
        return geoLocations;
    }

    public HashSet<String> getCountryCodes() {
        return countryCodes;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public GeoLocationFinder fetchGeoData(HashSet<String> locations) {
        uniqueLocations = locations;

        countryCodes.clear();
        geoLocations.clear();

        for (String location : uniqueLocations) {
            try {
                switch (provider) {
                    default:
                        getGeoDBResponse(location);
                        break;

                    case GoogleAPI:
                        getGoogleGeoCodeResponse(location);
                        break;
                }

            } catch (Exception e) {
                LOG.error("Cannot get geodata for: " + location);
            }
        }
        return this;
    }

    public GeoDB getGeoDB() {
        if (geoDB == null) {
            geoDB = new GeoDB();
        }
        return geoDB;
    }

    public GeoLocationFinder setGeoDB(GeoDB db) {
        this.geoDB = db;
        return this;
    }

    public void getGeoDBResponse(String address) {
        address = address.toLowerCase();

        if (getGeoDB().containsCountry(address)) {
            countryCodes.add(getGeoDB().getCountry(address));

        } else if (getGeoDB().containsCity(address)) {
            GeoCity res = getGeoDB().getCity(address);

            countryCodes.add(res.countryCode);
            geoLocations.add(new LatLng(res.latitude, res.longitude));

        }
    }

    public void getGoogleGeoCodeResponse(String address) throws Exception {
        // Query Google API
        String url = "http://maps.googleapis.com/maps/api/geocode/json?sensor=false&address=" + URLEncoder.encode(address, "UTF-8");

        URLConnection urlConnection = new URL(url).openConnection();
        BufferedReader br = new BufferedReader(
                new InputStreamReader(urlConnection.getInputStream()));

        String inputLine;
        String response = "";
        while ((inputLine = br.readLine()) != null) {
            response += inputLine;
        }
        br.close();

        // Process JSON response
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);

        ObjectNode json = (ObjectNode) mapper.readTree(response);
        JsonNode results = json.get("results");
        if (results.size() > 0) {
            JsonNode firstResult = results.get(0);

            String type = firstResult.get("types").get(0).textValue();

            if (type.equals("locality")) {

                double lat = firstResult.get("geometry").get("location").get("lat").doubleValue();
                double lng = firstResult.get("geometry").get("location").get("lng").doubleValue();

                geoLocations.add(new LatLng(lat, lng));

            } else if (type.equals("country")) {
                String countryCode = firstResult.get("address_components").get(0).get("short_name").textValue();

                countryCodes.add(countryCode);
            }
        }


    }

}
