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

// Heredamos de FragmentActivity para el manejo correcto de fragmentos de Google
public class VerMapaActivity  extends AppCompatActivity {

    private MapView map;
    private Marker marker;
    private GeoPoint puntoSeleccionado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Cargar configuración obligatoria de la librería para caché offline
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        setContentView(R.layout.activity_ver_mapa);

        map = findViewById(R.id.mapaOsm);
        map.setMultiTouchControls(true); // Permitir zoom con dos dedos

        Button btnConfirmar = findViewById(R.id.btnConfirmarPunto);

        // Centrar mapa por defecto en la UES Central
        GeoPoint startPoint = new GeoPoint(13.7161, -89.2031);
        map.getController().setZoom(17.5);
        map.getController().setCenter(startPoint);

        // Creamos un marcador inicial vacío
        marker = new Marker(map);
        marker.setTitle("Ubicación Seleccionada");

        // Capturar los clics o toques del usuario en el mapa
        MapEventsReceiver mReceive = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                puntoSeleccionado = p;

                // Mover el marcador al punto donde el usuario tocó
                marker.setPosition(p);
                map.getOverlays().remove(marker); // Limpiar marcador anterior
                map.getOverlays().add(marker);    // Añadir el actualizado
                map.invalidate();                 // Refrescar mapa

                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        };

        MapEventsOverlay overlayEvents = new MapEventsOverlay(mReceive);
        map.getOverlays().add(overlayEvents);

        // Al confirmar, devolvemos las coordenadas a la pantalla de Crear/Actualizar
        btnConfirmar.setOnClickListener(v -> {
            if (puntoSeleccionado != null) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("LATITUD", puntoSeleccionado.getLatitude());
                resultIntent.putExtra("LONGITUD", puntoSeleccionado.getLongitude());
                setResult(RESULT_OK, resultIntent);
                finish(); // Cerramos el mapa y regresamos
            } else {
                Toast.makeText(this, "Por favor, toca el mapa para marcar un punto.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}