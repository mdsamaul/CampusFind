package com.example.campusfind;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.campusfind.databinding.ActivityMapPickerBinding;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

public class MapPickerActivity extends AppCompatActivity {

    private ActivityMapPickerBinding binding;
    private GeoPoint selectedPoint;
    private Marker marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // OSM Configuration
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        
        binding = ActivityMapPickerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupMap();

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

    private void setupMap() {
        binding.map.setTileSource(TileSourceFactory.MAPNIK);
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
