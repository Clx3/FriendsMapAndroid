package fi.tuni.friendsmap.entity;

/**
 * Representes the user that is authenticated to friends map application.
 * Will be used when making http requests to the backend.
 *
 */
public class User {

    private long userId;

    private String username;

    private UserLocation location;

    public User(long userId, String username, UserLocation location) {
        this.userId = userId;
        this.username = username;
        this.location = location;
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

    public UserLocation getLocation() {
        return location;
    }

    public void setLocation(UserLocation location) {
        this.location = location;
    }

}
