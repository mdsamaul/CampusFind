package com.example.campusfind;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.campusfind.databinding.ActivityMapPickerBinding;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

import java.util.List;
import java.util.Locale;

public class MapPickerActivity extends AppCompatActivity {

    private ActivityMapPickerBinding binding;
    private GeoPoint selectedPoint;
    private Marker marker;

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
        binding = ActivityMapPickerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupMap();
        setupSearch();

        binding.btnConfirmLocation.setOnClickListener(v -> {
            if (selectedPoint != null) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("lat", selectedPoint.getLatitude());
                resultIntent.putExtra("lng", selectedPoint.getLongitude());
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(this, "Please select a location first", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSearch() {
        binding.btnMapSearch.setOnClickListener(v -> performSearch());
        binding.etMapSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        });
    }

    private void performSearch() {
        String query = binding.etMapSearch.getText().toString().trim();
        if (query.isEmpty()) return;

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(query, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                GeoPoint targetPoint = new GeoPoint(address.getLatitude(), address.getLongitude());
                
                binding.map.getController().setZoom(17.0);
                binding.map.getController().animateTo(targetPoint);
                updateMarker(targetPoint);

                String placeName = address.getFeatureName() != null ? address.getFeatureName() : query;
                Toast.makeText(this, "Found: " + placeName, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Place not found. Try another search.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Search failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupMap() {
        binding.map.setTileSource(FREE_MAP);
        binding.map.setMultiTouchControls(true);
        binding.map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.ALWAYS);
        
        // Default to Dhaka
        GeoPoint startPoint = new GeoPoint(23.8103, 90.4125);
        binding.map.getController().setZoom(15.0);
        binding.map.getController().setCenter(startPoint);

        // Map Click Listener
        MapEventsReceiver mReceive = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                updateMarker(p);
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                updateMarker(p);
                return true;
            }
        };

        MapEventsOverlay evOverlay = new MapEventsOverlay(mReceive);
        binding.map.getOverlays().add(evOverlay);
    }

    private void updateMarker(GeoPoint p) {
        selectedPoint = p;
        if (marker != null) {
            binding.map.getOverlays().remove(marker);
        }
        marker = new Marker(binding.map);
        marker.setPosition(p);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle("Selected Location");
        binding.map.getOverlays().add(marker);
        binding.map.invalidate(); // Refresh map
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
