package com.busapp._Main;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.betterspinner.BetterSpinner;
import com.busapp.R;
import com.busapp._Utilities.CustomMapFragment;
import com.busapp._Utilities.GPS;
import com.busapp._Utilities.MapWrapperLayout;
import com.busapp._Utilities.UCSD_Bus_Server_Request;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.pnikosis.materialishprogress.ProgressWheel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import at.markushi.ui.CircleButton;


/*
2092: (A)(N) City Shuttle (Arriba/Nobel)
xxxx: (C) Coaster East
314: (C)(W) Coaster East/West (mid-day)
xxxx: (CP) Chancellor's Park
1263: (H) Hillcrest/Campus A.M.
1264: (H) Hillcrest/Campus P.M.
1114: (L) Clockwise Campus Loop - Peterson Hall to Torrey Pines Center
1113: (L) Counter Campus Loop - Torrey Pines Center to Peterson Hall
3159: (M) Mesa
1098: (P) Regents
2399: (S) SIO Loop
1434: (SC) Sanford Consortium Shuttle
xxxx: (W) Coaster West

the app will have the route id and route name hardcoded, and will directly retrieve the buses

tiny bus icons for the map markers


maybe don't give the user the option to show their location
 */

public class Bus_Map extends AppCompatActivity {
    private GoogleMap googleMap;
    private BetterSpinner routesSpinner;

    //ID #
    private final int CITY_SHUTTLE = 0;
    private final int COASTER_EAST = 1;
    private final int COASTER_EAST_WEST = 2;
    private final int CHANCELLOR = 3;
    private final int HILLCREST_AM = 4;
    private final int HILLCREST_PM = 5;
    private final int CLOCKWISE_CAMPUS_LOOP = 6;
    private final int COUNTER_CLOCKWISE_CAMPUS_LOOP = 7;
    private final int MESA = 8;
    private final int REGENTS = 9;
    private final int SIO_LOOP = 10;
    private final int SANFORD_CONSORTIUM = 11;
    private final int COASTER_WEST = 12;

    // Bus Names
    private final int CITY_SHUTTLE_ROUTE = 2092;
    private final int COASTER_EAST_ROUTE = 312;
    private final int COASTER_EAST_WEST_ROUTE = 314;
    private final int CHANCELLOR_ROUTE = 3168;
    private final int HILLCREST_AM_ROUTE = 1263;
    private final int HILLCREST_PM_ROUTE = 1264;
    private final int CLOCKWISE_CAMPUS_LOOP_ROUTE = 1114;
    private final int COUNTER_CLOCKWISE_CAMPUS_LOOP_ROUTE = 1113;
    private final int MESA_ROUTE = 3159;
    private final int REGENTS_ROUTE = 1098;
    private final int SIO_LOOP_ROUTE = 2399;
    private final int SANFORD_CONSORTIUM_ROUTE = 1434;
    private final int COASTER_WEST_ROUTE = 313;

    private Timer busRefreshTimer = new Timer();

    ArrayList<Marker> busMarkers = new ArrayList<>();

    private CircleButton findUserLocation;
    private CircleButton resetMapPosition;
    private ProgressWheel progressWheel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus__map);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setVisibility(View.GONE);

        registerResources();
        registerListeners();

        configureGoogleMap();
        configureRoutesSpinner();
    }

    @Override
    protected void onResume()
    {
        super.onResume();



    }

    @Override
    protected void onStop()
    {
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_bus__map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void registerResources() {
        routesSpinner = (BetterSpinner) findViewById(R.id.routesSpinner);

        findUserLocation = (CircleButton) findViewById(R.id.findUserLocation);
        resetMapPosition = (CircleButton) findViewById(R.id.resetMapPosition);

        progressWheel = (ProgressWheel) findViewById(R.id.progress_wheel);
        progressWheel.setBarColor(Color.parseColor("#90caf9"));
        progressWheel.spin();

    }

    // Click Listener
    private void registerListeners() {
        routesSpinner.setOnItemClickedListener(new BetterSpinner.OnItemClicked() {
            @Override
            public void onItemClicked(AdapterView<?> adapterView, View view, int i, long l) {

                int selectedRoute = i;

                // Moves view to the different routes and show the routes
                if (selectedRoute == CITY_SHUTTLE) {
                    moveToLocation(14.181455f, 32.86911611644047f, -117.22821582108736f);
                    showRouteOnMap(CITY_SHUTTLE_ROUTE);
                } else if (selectedRoute == COASTER_EAST) {
                    moveToLocation(13.630302f, 32.887070970675424f, -117.22676508128642f);
                    showRouteOnMap(COASTER_EAST_ROUTE);
                } else if (selectedRoute == COASTER_EAST_WEST) {
                    moveToLocation(13.6584215f, 32.88784634382045f, -117.22984191030262f);
                    showRouteOnMap(COASTER_EAST_WEST_ROUTE);
                } else if (selectedRoute == CHANCELLOR) {
                    moveToLocation(14.736957f, 32.87715606987217f, -117.21317902207375f);
                    showRouteOnMap(CHANCELLOR_ROUTE);
                } else if (selectedRoute == HILLCREST_AM) {
                    moveToLocation(11.544835f, 32.81261737770714f, -117.19200767576694f);
                    showRouteOnMap(HILLCREST_AM_ROUTE);
                } else if (selectedRoute == HILLCREST_PM) {
                    moveToLocation(11.544835f, 32.81261737770714f, -117.19200767576694f);
                    showRouteOnMap(HILLCREST_PM_ROUTE);
                } else if (selectedRoute == CLOCKWISE_CAMPUS_LOOP) {
                    moveToLocation(14.408011f, 32.88058110733446f, -117.23654072731733f);
                    showRouteOnMap(CLOCKWISE_CAMPUS_LOOP_ROUTE);
                } else if (selectedRoute == COUNTER_CLOCKWISE_CAMPUS_LOOP) {
                    moveToLocation(14.408011f, 32.88058110733446f, -117.23654072731733f);
                    showRouteOnMap(COUNTER_CLOCKWISE_CAMPUS_LOOP_ROUTE);
                } else if (selectedRoute == MESA) {
                    moveToLocation(13.84421f, 32.872835164233265f, -117.2305067628622f);
                    showRouteOnMap(MESA_ROUTE);
                } else if (selectedRoute == REGENTS) {
                    moveToLocation(14.426807f, 32.881491124629385f, -117.22721099853517f);
                    showRouteOnMap(REGENTS_ROUTE);
                } else if (selectedRoute == SIO_LOOP) {
                    moveToLocation(14.627642f, 32.87027969082793f, -117.24596466869117f);
                    showRouteOnMap(SIO_LOOP_ROUTE);
                } else if (selectedRoute == SANFORD_CONSORTIUM) {
                    moveToLocation(14.01568f, 32.88050142406329f, -117.23274070769548f);
                    showRouteOnMap(SANFORD_CONSORTIUM_ROUTE);
                } else if (selectedRoute == COASTER_WEST) {
                    moveToLocation(13.60606f, 32.885407583754755f, -117.2330991178751f);
                    showRouteOnMap(COASTER_WEST_ROUTE);
                }
            }
        });

        // Reset Map Location to UCSD
        resetMapPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                googleMap.clear();
                busRefreshTimer.cancel();
                moveToLocation(14.220199f, 32.8806346048968f, -117.23714388906954f);
                routesSpinner.setText("Select a Bus Route");
                configureRoutesSpinner();
            }
        });

        // Find User Location Button
        findUserLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GPS.getUsersLocation(getApplicationContext(), new GPS.OnLocationReceivedListener() {
                    @Override
                    public void onLocationReceived(Location location, boolean success) {
                        Log.d("testing", "settinglocation");
                        moveToLocation(14.220199f, (float) location.getLatitude(), (float) location.getLongitude());
                        googleMap.addMarker(new MarkerOptions()
                                .position(new LatLng(location.getLatitude(), location.getLongitude()))
                                .title("You!")
                                .snippet("You are here."));
                    }
                });
            }
        });
    }

    private void configureGoogleMap() {

        CustomMapFragment customMapFragment = ((CustomMapFragment) getSupportFragmentManager().findFragmentById(findViewById(R.id.map).getId()));
        //CustomMapFragment customMapFragment = ((CustomMapFragment) fragmentManager.findFragmentById(R.id.map));
        customMapFragment.setOnDragListener(new MapWrapperLayout.OnDragListener() {
            @Override
            public void onDrag(MotionEvent motionEvent) {

                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {


                } else if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {

                } else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {

                }
            }
        });

        googleMap = customMapFragment.getMap();

        //googleMap.setInfoWindowAdapter(new CustomInfoWindow(this));

        /*
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        googleMap = mapFragment.getMap();
        */

        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        googleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {


            }
        });


        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {


                Log.d("testing", "zoom: " + googleMap.getCameraPosition().zoom + " Latitude: " + googleMap.getCameraPosition().target.latitude + " Longitude: " + googleMap.getCameraPosition().target.longitude);

            }
        });


        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

            @Override
            public boolean onMarkerClick(final Marker marker) {

                marker.showInfoWindow();

                return true;
            }
        });

        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

            @Override
            public void onInfoWindowClick(Marker marker) {
                Log.d("testing", "info window touched");

            }
        });

        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        googleMap.setMyLocationEnabled(false);

        moveToLocation(14.220199f, 32.8806346048968f, -117.23714388906954f);

        /*
        googleMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                moveToUser(location);
            }
        });
        */

        /*
        new UCSD_Bus_Server_Request().getBusRoutes(new UCSD_Bus_Server_Request.OnServerRespondListener() {
            @Override
            public void onServerRespond(boolean success, String jsonString) {

                try {
                    JSONArray thing = new JSONArray(jsonString);
                    for (int i = 0; i < thing.length(); i++) {
                        Log.d("testing", thing.getJSONObject(i).toString());
                    }
                }
                catch(JSONException e)
                {

                }
            }
        });
        */
    }

    // Changes the user's view
    private void moveToLocation(float zoomLevel, float latitude, float longitude) {
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), zoomLevel), 1000, new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
            }

            @Override
            public void onCancel() {
            }
        });
    }

    private void showRouteOnMap(int routeNumber)
    {
        googleMap.clear();
        progressWheel.setVisibility(View.VISIBLE);
        showPathOnMap(routeNumber);
        showBusesOnMap(routeNumber);
    }

    private void showPathOnMap(int routeNumber)
    {
        new UCSD_Bus_Server_Request().getRoutePath(routeNumber, new UCSD_Bus_Server_Request.OnServerRespondListener() {
            @Override
            public void onServerRespond(boolean success, String jsonString) {
                if (success) {
                    Log.d("testing", jsonString);

                    PolylineOptions options = new PolylineOptions().width(10).color(Color.BLUE).geodesic(true);

                    try {
                        JSONArray mainArray = new JSONArray(jsonString);
                        for (int i = 0; i < mainArray.length(); i++) {
                            JSONArray temp = mainArray.getJSONArray(i);
                            for (int j = 0; j < temp.length(); j++) {
                                JSONObject coordinates = temp.getJSONObject(j);

                                float latitude = (float) coordinates.getDouble("Latitude");
                                float longitude = (float) coordinates.getDouble("Longitude");

                                options.add(new LatLng(latitude, longitude));
                            }
                        }

                        googleMap.addPolyline(options);
                    } catch (JSONException e) {

                    }
                }
            }
        });
    }

    private void showBusesOnMap(final int routeNumber) {

        busRefreshTimer.cancel();
        busRefreshTimer = new Timer();
        busRefreshTimer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                new Handler(getMainLooper()).post(new Runnable() {

                    @Override
                    public void run() {
                        progressWheel.setVisibility(View.VISIBLE);
                    }
                });

                new UCSD_Bus_Server_Request().getBusesWithRoute(routeNumber, new UCSD_Bus_Server_Request.OnServerRespondListener() {
                    @Override
                    public void onServerRespond(boolean success, final String jsonString) {
                        if (success) {
                            Log.d("testing", jsonString);

                            for(int i = 0; i < busMarkers.size(); i++)
                                busMarkers.get(i).remove();

                            try {
                                JSONArray busArray = new JSONArray(jsonString);

                                for (int i = 0; i < busArray.length(); i++) {
                                    String busType = busArray.getJSONObject(i).getString("IconPrefix");
                                    String busName = busArray.getJSONObject(i).getString("Name");
                                    float busLatitude = (float) busArray.getJSONObject(i).getDouble("Latitude");
                                    float busLongitude = (float) busArray.getJSONObject(i).getDouble("Longitude");
                                    int stopped = busArray.getJSONObject(i).getInt("DoorStatus");
                                    String heading = busArray.getJSONObject(i).getString("Heading");

                                    int rotation;
                                    if (heading.equals("N"))
                                    {
                                        rotation = -90;
                                    }
                                    else if (heading.equals("NE"))
                                    {
                                        rotation = -45;
                                    }
                                    else if (heading.equals("E"))
                                    {
                                        rotation = 0;
                                    }
                                    else if (heading.equals("SE"))
                                    {
                                        rotation = 45;
                                    }
                                    else if (heading.equals("S"))
                                    {
                                        rotation = 90;
                                    }
                                    else if (heading.equals("SW"))
                                    {
                                        rotation = 135;
                                    }
                                    else if (heading.equals("W"))
                                    {
                                        rotation = 180;
                                    }
                                    else
                                    {
                                        rotation = -135;
                                    }


                                    MarkerOptions markerOptions = new MarkerOptions()
                                            .position(new LatLng(busLatitude, busLongitude))
                                            .title(busName)
                                            .snippet(busType)
                                            .rotation(rotation);

                                    if(stopped == 0)
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_moving));
                                    else
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_stopped));


                                    Marker marker = googleMap.addMarker(markerOptions);

                                    busMarkers.add(marker);
                                }
                            } catch (JSONException e) {
                                Log.d("testing", "showBusesOnMap: " + e.getMessage());
                            }
                        } else {
                            Log.d("testing", "bad");
                        }
                        progressWheel.setVisibility(View.INVISIBLE);
                    }
                });
            }
        }, 0, 5000);
    }

    private void configureRoutesSpinner() {
        String[] routeItems = {
                "(A)(N) City Shuttle (Arriba/Nobel)", //2092
                "(C) Coaster East", //xxxx
                "(C)(W) Coaster East/West (mid-day)", //314
                "(CP) Chancellor's Park", //xxxx
                "(H) Hillcrest/Campus A.M.", //1263
                "(H) Hillcrest/Campus P.M.", //1264
                "(L) Clockwise Campus Loop - Peterson Hall to Torrey Pines Center", //1114
                "(L) Counter Campus Loop - Torrey Pines Center to Peterson Hall", //1113
                "(M) Mesa", //3159
                "(P) Regents", //1098
                "(S) SIO Loop", //2399
                "(SC) Sanford Consortium Shuttle", //1434
                "(W) Coaster West"}; //xxxx
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item_color, routeItems);
        routesSpinner.setAdapter(sortAdapter);
    }
}
