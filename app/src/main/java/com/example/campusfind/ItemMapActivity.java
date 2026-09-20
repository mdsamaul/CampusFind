package com.example.campusfind;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import com.example.campusfind.databinding.ActivityItemMapBinding;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.overlay.Marker;

public class ItemMapActivity extends AppCompatActivity {

    private ActivityItemMapBinding binding;
    private double lat, lng;
    private String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // OSM Configuration
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        binding = ActivityItemMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        lat = getIntent().getDoubleExtra("lat", 0);
        lng = getIntent().getDoubleExtra("lng", 0);
        title = getIntent().getStringExtra("title");

        setupMap();

        binding.fabBack.setOnClickListener(v -> finish());
    }

    private void setupMap() {
        binding.map.setTileSource(TileSourceFactory.MAPNIK);
        binding.map.setMultiTouchControls(true);
        binding.map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.ALWAYS);

        GeoPoint startPoint = new GeoPoint(lat, lng);
        binding.map.getController().setZoom(17.0);
        binding.map.getController().setCenter(startPoint);

        Marker startMarker = new Marker(binding.map);
        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        startMarker.setTitle(title != null ? title : "Item Location");
        binding.map.getOverlays().add(startMarker);
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.map.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        binding.map.onPause();
    }
}
