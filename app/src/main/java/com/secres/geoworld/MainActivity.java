package com.secres.geoworld;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.datastore.MapDataStore;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.download.TileDownloadLayer;
import org.mapsforge.map.layer.download.tilesource.OpenStreetMapMapnik;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.rendertheme.InternalRenderTheme;

import java.io.FileInputStream;
import java.util.Objects;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.LookAt;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globe.BasicElevationCoverage;
import gov.nasa.worldwind.layer.BackgroundLayer;
import gov.nasa.worldwind.layer.BlueMarbleLandsatLayer;
import gov.nasa.worldwind.layer.RenderableLayer;
import gov.nasa.worldwind.render.Color;
import gov.nasa.worldwind.shape.Placemark;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Placemark placemark;
    private WorldWindow wwd;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private MapView mapView;
    private static final int SELECT_MAP_FILE = 0;
    private double latitude = 37.336316657274814;
    private double longitude = -122.06142995877659;
    private double altitude = 0;
    private LookAt lookAt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.my_drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        setNavigationViewListener();

        AndroidGraphicFactory.createInstance(getApplication());
    }

    private void setNavigationViewListener() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        actionBarDrawerToggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.nav_home) {
            ViewGroup layoutToChange = findViewById(R.id.home);
            layoutToChange.removeAllViews();

            LayoutInflater inflater = LayoutInflater.from(this);
            RelativeLayout newLayout = (RelativeLayout) inflater.inflate(R.layout.home_layout, null);

            layoutToChange.addView(newLayout);
        }
        else if(id == R.id.nav_satellite) {
            ViewGroup layoutToChange = findViewById(R.id.home);
            layoutToChange.removeAllViews();

            LayoutInflater inflater = LayoutInflater.from(this);
            RelativeLayout newLayout = (RelativeLayout) inflater.inflate(R.layout.satellite_layout, null);

            layoutToChange.addView(newLayout);

            createSatelliteView();
        }
        else if(id == R.id.nav_map) {
            ViewGroup layoutToChange = findViewById(R.id.home);
            layoutToChange.removeAllViews();

            LayoutInflater inflater = LayoutInflater.from(this);
            RelativeLayout newLayout = (RelativeLayout) inflater.inflate(R.layout.map_layout, null);

            layoutToChange.addView(newLayout);
            createMapView();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void createSatelliteView() {
        // Create a WorldWindow (a GLSurfaceView)...
        wwd = new WorldWindow(getApplicationContext());
        // ... and add some map layers
        wwd.getLayers().addLayer(new BackgroundLayer());
        wwd.getLayers().addLayer(new BlueMarbleLandsatLayer());

        RenderableLayer placemarksLayer = new RenderableLayer("Placemarks");
        wwd.getLayers().addLayer(placemarksLayer);
        placemark = Placemark.createWithColorAndSize(Position.fromDegrees(latitude, longitude, altitude), new Color(0, 1, 1, 1), 20);
        placemarksLayer.addRenderable(placemark);

        adjustView();

        FloatingActionButton mapFab = findViewById(R.id.globe_fab);
        System.out.println(mapFab);
        mapFab.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("Enter coordinates:");

            final View customLayout = getLayoutInflater().inflate(R.layout.dialog_layout, null);
            builder.setView(customLayout);

            builder.setPositiveButton("OK", (dialog, id) -> {
                EditText latitudeText = customLayout.findViewById(R.id.latitudeText);
                latitude = Double.parseDouble(latitudeText.getText().toString());

                EditText longitudeText = customLayout.findViewById(R.id.longitudeText);
                longitude = Double.parseDouble(longitudeText.getText().toString());

                placemarksLayer.removeRenderable(placemark);
                placemark = Placemark.createWithColorAndSize(Position.fromDegrees(latitude, longitude, altitude), new Color(0, 1, 1, 1), 20);
                placemarksLayer.addRenderable(placemark);

                adjustView();

                wwd.requestRedraw();
            });

            builder.setNegativeButton("Cancel", (dialog, id) -> {
                dialog.cancel();
            });

            AlertDialog alert = builder.create();
            alert.show();
        });

        // Add the WorldWindow view object to the layout that was reserved for the globe.
        FrameLayout globeLayout = findViewById(R.id.globe);
        globeLayout.addView(wwd);
    }

    private void adjustView() {
        Position pos = placemark.getPosition();
        lookAt = new LookAt().set(pos.latitude, pos.longitude, pos.altitude, WorldWind.ABSOLUTE,
                1e7, 0, 0, 0);
        wwd.getNavigator().setAsLookAt(wwd.getGlobe(), lookAt);

        altitude = pos.altitude;
    }

    private void createMapView() {
        mapView = new MapView(getApplicationContext());

        Intent intent = new Intent(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ? Intent.ACTION_OPEN_DOCUMENT : Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, SELECT_MAP_FILE);

        // Add the MapView view object to the layout that was reserved for the map.
        FrameLayout globeLayout = findViewById(R.id.map);
        globeLayout.addView(mapView);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_MAP_FILE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                openMap(uri);
            }
        }
    }

    private void openMap(Uri uri) {
        try {
            mapView.getMapScaleBar().setVisible(true);
            mapView.setBuiltInZoomControls(true);

            TileCache tileCache = AndroidUtil.createTileCache(this, "mapcache",
                    mapView.getModel().displayModel.getTileSize(), 1f,
                    mapView.getModel().frameBufferModel.getOverdrawFactor());

            FileInputStream fis = (FileInputStream) getContentResolver().openInputStream(uri);
            MapDataStore mapDataStore = new MapFile(fis);
            TileRendererLayer tileRendererLayer = new TileRendererLayer(tileCache, mapDataStore,
                    mapView.getModel().mapViewPosition, AndroidGraphicFactory.INSTANCE);
            tileRendererLayer.setXmlRenderTheme(InternalRenderTheme.DEFAULT);

            mapView.getLayerManager().getLayers().add(tileRendererLayer);

            mapView.setCenter(new LatLong(52.517037, 13.38886));
            mapView.setZoomLevel((byte) 12);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}