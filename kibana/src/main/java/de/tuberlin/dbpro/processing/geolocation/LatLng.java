package de.tuberlin.dbpro.processing.geolocation;

/**
 * Tuple of latitude / longitude
 */
public class LatLng {
    public double lat;
    public double lng;

    public LatLng(double latitude, double longitude) {
        lat = latitude;
        lng = longitude;
    }
}
