package com.secres.geoworld;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

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

public class MainActivity extends AppCompatActivity {

    Placemark ventura;
    WorldWindow wwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create a WorldWindow (a GLSurfaceView)...
        wwd = new WorldWindow(getApplicationContext());
        // ... and add some map layers
        wwd.getLayers().addLayer(new BackgroundLayer());
        wwd.getLayers().addLayer(new BlueMarbleLandsatLayer());

        RenderableLayer placemarksLayer = new RenderableLayer("Placemarks");
        wwd.getLayers().addLayer(placemarksLayer);
        ventura = Placemark.createWithColorAndSize(Position.fromDegrees(37.336316657274814, -122.06142995877659, 0), new Color(0, 1, 1, 1), 20);
        placemarksLayer.addRenderable(ventura);

        adjustView();

        FloatingActionButton mapFab = findViewById(R.id.map_fab);
        mapFab.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("Enter coordinates:");

            // set the custom layout
            final View customLayout = getLayoutInflater().inflate(R.layout.dialog_layout, null);
            builder.setView(customLayout);

            builder.setPositiveButton("OK", (dialog, id) -> {
                EditText latitudeText = customLayout.findViewById(R.id.latitudeText);
                double latitudeDegrees = Double.parseDouble(latitudeText.getText().toString());

                EditText longitudeText = customLayout.findViewById(R.id.longitudeText);
                double longitudeDegrees = Double.parseDouble(longitudeText.getText().toString());

                placemarksLayer.removeRenderable(ventura);
                ventura = Placemark.createWithColorAndSize(Position.fromDegrees(latitudeDegrees, longitudeDegrees, 0), new Color(0, 1, 1, 1), 20);
                placemarksLayer.addRenderable(ventura);

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
        FrameLayout globeLayout = (FrameLayout) findViewById(R.id.globe);
        globeLayout.addView(wwd);
    }

    private void adjustView() {
        Position pos = ventura.getPosition();
        LookAt lookAt = new LookAt().set(pos.latitude, pos.longitude, pos.altitude, WorldWind.ABSOLUTE,
                1e7, 0, 0, 0);
        wwd.getNavigator().setAsLookAt(wwd.getGlobe(), lookAt);
    }

}