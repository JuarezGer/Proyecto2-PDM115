package ues.fia.proyecto2_pdm115.mapa;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import ues.fia.proyecto2_pdm115.R;

public class MapasMenuActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_mapas);

        findViewById(R.id.cardMapaIncidencias).setOnClickListener(v ->
                Toast.makeText(this, "Aquí puedes abrir el mapa de incidencias.", Toast.LENGTH_LONG).show()
        );

        findViewById(R.id.cardMapaEquipos).setOnClickListener(v ->
                Toast.makeText(this, "Aquí puedes abrir el mapa de equipos o laboratorios.", Toast.LENGTH_LONG).show()
        );

        findViewById(R.id.btnVolverMapas).setOnClickListener(v -> finish());
    }
}
