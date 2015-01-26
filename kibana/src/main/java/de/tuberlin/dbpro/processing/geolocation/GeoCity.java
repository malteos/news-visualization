package de.tuberlin.dbpro.processing.geolocation;

/**
 * Model of city with geo information and population
 */
public class GeoCity {
    public double latitude;
    public double longitude;
    public String countryCode;
    public int population;

    public GeoCity(double lat, double lng) {
        this.latitude = lat;
        this.longitude = lng;
    }

    public GeoCity(double lat, double lng, String countryCode, int population) {
        this.latitude = lat;
        this.longitude = lng;
        this.countryCode = countryCode;
        this.population = population;
    }

    public String toString() {
        return countryCode + ": " + population;
    }
}
