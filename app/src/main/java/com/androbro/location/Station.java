package com.androbro.location;

/**
 * Created by user on 2/21/2016.
 */
public class Station implements Comparable<Station> {

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    double distance;
    String url;
    double longitude;
    double latitude;

    @Override
    public int compareTo(Station other) {
        // compareTo should return < 0 if this is supposed to be
        // less than other, > 0 if this is supposed to be greater than
        // other and 0 if they are supposed to be equal
        int result = this.distance <= other.distance ? -1 : 1;
        return result;
    }

    @Override
    public String toString() {
        return "" + longitude + ", " + latitude;
    }
}
