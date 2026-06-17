package ues.fia.proyecto2_pdm115;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.Button;
import android.widget.Toast;
import androidx.fragment.app.FragmentActivity;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;


public class VerMapaActivity  extends AppCompatActivity {

    private MapView map;
    private Marker marker;
    private GeoPoint puntoSeleccionado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        setContentView(R.layout.activity_ver_mapa);

        map = findViewById(R.id.mapaOsm);
        map.setMultiTouchControls(true);

        Button btnConfirmar = findViewById(R.id.btnConfirmarPunto);

        GeoPoint startPoint = new GeoPoint(13.7161, -89.2031);
        map.getController().setZoom(17.5);
        map.getController().setCenter(startPoint);

        marker = new Marker(map);
        marker.setTitle("Ubicación Seleccionada");

        MapEventsReceiver mReceive = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                puntoSeleccionado = p;

                marker.setPosition(p);
                map.getOverlays().remove(marker);
                map.getOverlays().add(marker);
                map.invalidate();

                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        };

        MapEventsOverlay overlayEvents = new MapEventsOverlay(mReceive);
        map.getOverlays().add(overlayEvents);

        btnConfirmar.setOnClickListener(v -> {
            if (puntoSeleccionado != null) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("LATITUD", puntoSeleccionado.getLatitude());
                resultIntent.putExtra("LONGITUD", puntoSeleccionado.getLongitude());
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(this, "Por favor, toca el mapa para marcar un punto.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}