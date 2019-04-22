package fi.tuni.friendsmap;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.mapboxsdk.utils.ColorUtils;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import fi.tuni.friendsmap.entity.User;
import fi.tuni.friendsmap.entity.UserLocation;

public class MainActivity extends AppCompatActivity implements LocationListener {

    /**
     * Request code for android callback.
     */
    public static final int ACCESS_LOCATION_REQUEST_CODE = 0;

    /**
     * Mapview instance of Friendsmap.
     */
    private MapView mapView;

    /**
     * MapboxMap instance of Friedsmap.
     */
    private MapboxMap mapboxMap;

    /**
     * Style of the mapbox.
     */
    private Style mapStyle;

    /**
     * Instance of a LocationManager used by this application.
     */
    private LocationManager locationManager;

    /**
     * Mapbox plugins SymbolManager instance which handles
     * the symbols that are drawn on the map.
     */
    private SymbolManager symbolManager;

    /**
     * Contains the Symbol of the local user.
     */
    private Symbol localUserSymbol;

    /**
     * Contains all Symbols of users marked to the application.
     * This List is updated when we make a call for the backend for users.
     */
    private List<Symbol> allSymbols;

    /**
     * Instance of a HttpHandler used by this application.
     */
    private HttpHandler httpHandler;

    /**
     * Instance of LocationsHandler user by this application.
     */
    private LocationsHandler locationsHandler;

    /**
     * Instance of a User which represents
     * the local user.
     */
    private User localUser;

    /**
     * MainActivitys onCreate.
     *
     * This method initializes the MapBox and its styles. Also
     * it initializes the locationHandler and httpHandler, and makes the
     * needed http calls to initialize the local users and all marked users
     * locations.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getResources().getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_main);

        httpHandler = new HttpHandler(this);
        locationsHandler = new LocationsHandler(this, httpHandler);

        allSymbols = new ArrayList<>();

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        localUser = getUserAndSetLocation();

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                MainActivity.this.mapboxMap = mapboxMap;
                MainActivity.this.mapStyle = mapboxMap.getStyle();

                mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        symbolManager = new SymbolManager(MainActivity.this.mapView, MainActivity.this.mapboxMap, style);
                        symbolManager.setTextAllowOverlap(true);
                        symbolManager.setIconAllowOverlap(true);
                        symbolManager.setIconPadding(2f);
                        symbolManager.setTextLineHeight(2f);

                        if(localUser.userHasLocation()) {
                            localUserSymbol = symbolManager.create(getLocalUserSymbolOptions());
                        }

                        refreshAndMarkAllUsersAndLocations();
                    }
                });

                mapboxMap.addOnMapClickListener(new MapboxMap.OnMapClickListener() {
                    @Override
                    public boolean onMapClick(@NonNull LatLng point) {
                        if(localUser.userHasLocation()) {
                            CameraPosition position = new CameraPosition.Builder()
                                    .target(new LatLng(localUser.getLocation().getLatitude(), localUser.getLocation().getLongitude())) // Sets the new camera position
                                    .zoom(17) // Sets the zoom
                                    .build(); // Creates a CameraPosition from the builder

                            MainActivity.this.mapboxMap.animateCamera(CameraUpdateFactory
                                    .newCameraPosition(position), 7000);
                        }

                        return true;
                    }
                });
            }
        });

    }

    /**
     * Android callback when it is requesting permissions. in this case Location permission.
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode == ACCESS_LOCATION_REQUEST_CODE) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                try {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);

                    localUser = getUserAndSetLocation();
                } catch(SecurityException e) {

                }
            } else {
                Toast.makeText(this, "Location disabled!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public User getUserAndSetLocation() {
        User outputUser = new User(-1,"", null);

        Intent intent = getIntent();

        outputUser.setUserId(intent.getLongExtra("userId", 0));
        outputUser.setUsername(intent.getStringExtra("username"));

        int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);

        if(permissionCheck == PackageManager.PERMISSION_DENIED) {
            String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};

            ActivityCompat.requestPermissions(MainActivity.this, permissions, ACCESS_LOCATION_REQUEST_CODE);
        } else {
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            UserLocation userLocation = new UserLocation(location.getLatitude(), location.getLongitude(), "");

            outputUser.setLocation(userLocation);
        }

        return outputUser;
    }

    private void refreshAndMarkAllUsersAndLocations() {
        locationsHandler.updateAllUsersAndLocations(new HttpHandler.VolleyCallBack() {
            @Override
            public void onSuccess() {
                symbolManager.delete(allSymbols);
                allSymbols.clear();

               if(localUser != null && localUser.userHasLocation()) {
                   System.out.println(localUser.getLocation().getLatitude());
                   System.out.println(localUser.getLocation().getLongitude());
                   localUserSymbol = symbolManager.create(getLocalUserSymbolOptions());
                   allSymbols.add(localUserSymbol);
                }
                markAllUserLocationsToMap();
            }
            @Override
            public void onError() {

            }
        });
    }

    private void markAllUserLocationsToMap() {
        for(User user : locationsHandler.getUsersAndLocationsList()) {
            if (user.getUserId() != localUser.getUserId() && (user.userHasLocation())) {
                SymbolOptions options = new SymbolOptions()
                        .withLatLng(new LatLng(user.getLocation().getLatitude(), user.getLocation().getLongitude()))
                        .withIconImage("information-11")
                        .withIconSize(2f)
                        .withTextField("\n" + user.getUsername())
                        .withTextColor(ColorUtils.colorToRgbaString(Color.RED))
                        .withTextMaxWidth(5f);
                Symbol symbol = symbolManager.create(options);
                allSymbols.add(symbol);
            }
        }
    }

    public void actionsMenuClicked(View v) {
        System.out.println("ASDAS");
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.friendsmap_menu, menu);
        return true;
    }

    /**
     * Handles toolbar menu button clicks(selections).
     *
     * @param menuItem Which menu item is selected/clicked.
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch(menuItem.getItemId()) {
            case R.id.markLocation:
                /* Deleting the previous mark */
                int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);

                if(permissionCheck == PackageManager.PERMISSION_DENIED) {
                    String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};

                    ActivityCompat.requestPermissions(MainActivity.this, permissions, ACCESS_LOCATION_REQUEST_CODE);
                } else {
                    Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    UserLocation userLocation = new UserLocation(location.getLatitude(), location.getLongitude(), "");

                    localUser.setLocation(userLocation);
                }

                updateLocalUsersLocationAndSymbol();
                break;

            case R.id.deleteLocation:
                locationsHandler.deleteUsersLocation(localUser);

                if(localUserSymbol != null) {
                    symbolManager.delete(localUserSymbol);
                    allSymbols.remove(localUserSymbol);
                }

                break;

            case R.id.refreshLocations:
                refreshAndMarkAllUsersAndLocations();
                break;
        }
        return true;
    }

    private void updateLocalUsersLocationAndSymbol() {
        try {
            httpHandler.updateUserAndItsLocation(localUser);

            if(localUserSymbol != null) {
                symbolManager.delete(localUserSymbol);
                allSymbols.remove(localUserSymbol);
            }

            localUserSymbol = symbolManager.create(getLocalUserSymbolOptions());

            allSymbols.add(localUserSymbol);

            Toast.makeText(this, "Location marked succesfully.", Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Location marking failed.", Toast.LENGTH_SHORT).show();
        }
    }

    private SymbolOptions getLocalUserSymbolOptions() {
        return new SymbolOptions()
                .withLatLng(new LatLng(localUser.getLocation().getLatitude(), localUser.getLocation().getLongitude()))
                .withIconImage("information-11")
                .withIconSize(2f)
                .withTextField("\n You - " + localUser.getUsername())
                .withTextColor(ColorUtils.colorToRgbaString(Color.GREEN))
                .withTextMaxWidth(8f);
    }

    // Add the mapView's own lifecycle methods to the activity's lifecycle methods
    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (symbolManager != null) {
            symbolManager.onDestroy();
        }

        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
