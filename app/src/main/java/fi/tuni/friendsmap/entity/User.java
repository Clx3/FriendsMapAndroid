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

    /**
     * In friendsmap if user does not have a location, its latitude and longitude in the backend
     * are set as -1000, so if either of those are true, user has not location.
     *
     * @return Returns if the user has location or not.
     */
    public boolean userHasLocation() {
        return getLocation().getLatitude() != -1000 || getLocation().getLongitude() != -1000;
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

    @Override
    public String toString() {
        return String.format("id: %d username: %s longitude: %f latitude %f location info: %s",
                getUserId(), getUsername(), getLocation().getLongitude(), getLocation().getLatitude(), getLocation().getDescription());
    }

}
