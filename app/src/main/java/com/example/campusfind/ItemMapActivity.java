package com.example.campusfind;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import com.example.campusfind.databinding.ActivityItemMapBinding;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.overlay.Marker;

public class ItemMapActivity extends AppCompatActivity {

    private ActivityItemMapBinding binding;
    private double lat, lng;
    private String title;

    // Public Open Tile Source - Requires ZERO API Key and NO Billing
    public static final OnlineTileSourceBase FREE_MAP = new XYTileSource(
            "OSM_France",
            0, 19, 256, ".png",
            new String[]{
                    "https://a.tile.openstreetmap.fr/osmfr/",
                    "https://b.tile.openstreetmap.fr/osmfr/",
                    "https://c.tile.openstreetmap.fr/osmfr/"
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // OSM Configuration MUST be loaded BEFORE super.onCreate
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        Configuration.getInstance().setUserAgentValue(getPackageName());
        Configuration.getInstance().setOsmdroidBasePath(ctx.getCacheDir());
        Configuration.getInstance().setOsmdroidTileCache(ctx.getCacheDir());

        super.onCreate(savedInstanceState);

        binding = ActivityItemMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        lat = getIntent().getDoubleExtra("lat", 0);
        lng = getIntent().getDoubleExtra("lng", 0);
        title = getIntent().getStringExtra("title");

        setupMap();

        binding.fabBack.setOnClickListener(v -> finish());
    }

    private void setupMap() {
        binding.map.setTileSource(FREE_MAP);
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
