package fi.tuni.friendsmap;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.Circle;
import com.mapbox.mapboxsdk.plugins.annotation.CircleManager;
import com.mapbox.mapboxsdk.plugins.annotation.CircleOptions;
import com.mapbox.mapboxsdk.plugins.annotation.Options;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.mapboxsdk.utils.ColorUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import fi.tuni.friendsmap.entity.User;
import fi.tuni.friendsmap.entity.UserLocation;

public class MainActivity extends AppCompatActivity implements LocationListener {

    public static final int ACCESS_LOCATION_REQUEST_CODE = 0;

    private MapView mapView;
    private MapboxMap mapboxMap;
    private Style mapStyle;

    private LocationManager locationManager;

    private SymbolManager symbolManager;
    private Symbol localUserSymbol;

    private HttpHandler httpHandler;
    private LocationsHandler locationsHandler;

    private User localUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getResources().getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_main);

        httpHandler = new HttpHandler(this);
        locationsHandler = new LocationsHandler(this, httpHandler);

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
                markAllUserLocationsToMap();
            }
            @Override
            public void onError() {

            }
        });
    }

    private void markAllUserLocationsToMap() {
        for(User user : locationsHandler.getUsersAndLocationsList()) {
            if (user.getUserId() != localUser.getUserId() && (user.getLocation().getLatitude() != -1 || user.getLocation().getLongitude() != -1)) {
                SymbolOptions options = new SymbolOptions()
                        .withLatLng(new LatLng(user.getLocation().getLatitude(), user.getLocation().getLongitude()))
                        .withIconImage("information-11")
                        .withIconSize(2f)
                        .withTextField("\n" + user.getUsername())
                        .withTextColor(ColorUtils.colorToRgbaString(Color.RED))
                        .withTextMaxWidth(7f);


                symbolManager.create(options);
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

                try {
                    httpHandler.updateUserAndItsLocation(localUser);

                    if(localUserSymbol != null)
                        symbolManager.delete(localUserSymbol);

                    localUserSymbol = symbolManager.create(getLocalUserSymbolOptions());

                    Toast.makeText(this, "Location marked succesfully.", Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Location marking failed.", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.deleteLocation:
                locationsHandler.deleteUsersLocation(localUser);

                if(localUserSymbol != null)
                    symbolManager.delete(localUserSymbol);
                break;

            case R.id.refreshLocations:
                refreshAndMarkAllUsersAndLocations();
                break;
        }
        return true;
    }

    private SymbolOptions getLocalUserSymbolOptions() {
        return new SymbolOptions()
                .withLatLng(new LatLng(localUser.getLocation().getLatitude(), localUser.getLocation().getLongitude()))
                .withIconImage("information-11")
                .withIconSize(2f)
                .withTextField(String.format("%nYOU (%s)", localUser.getUsername()))
                .withTextColor(ColorUtils.colorToRgbaString(Color.GREEN))
                .withTextMaxWidth(7f);
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
