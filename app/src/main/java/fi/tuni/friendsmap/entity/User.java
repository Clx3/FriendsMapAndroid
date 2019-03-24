package fi.tuni.friendsmap.entity;

/**
 * Representes the user that is authenticated to friends map application.
 * Will be used when making http requests to the backend.
 *
 */
public class User {

    private long userId;

    private String username;

    private double longitude;

    private double latitude;

    private String currentLocationInfo;

    public User(long userId, String username, double longitude, double latitude, String currentLocationInfo) {
        this.userId = userId;
        this.username = username;
        this.longitude = longitude;
        this.latitude = latitude;
        this.currentLocationInfo = currentLocationInfo;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public String getCurrentLocationInfo() {
        return currentLocationInfo;
    }

    public void setCurrentLocationInfo(String currentLocationInfo) {
        this.currentLocationInfo = currentLocationInfo;
    }
}
