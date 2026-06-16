package ues.fia.proyecto2_pdm115.mapa;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import ues.fia.proyecto2_pdm115.R;

public class MapasMenuActivity extends Activity {

    public static final String EXTRA_TIPO_MAPA = "tipo_mapa";
    public static final String EXTRA_TITULO_MAPA = "titulo_mapa";

    public static final String TIPO_INCIDENCIAS = "incidencias";
    public static final String TIPO_EDIFICIOS = "edificios";
    public static final String TIPO_LABORATORIOS = "laboratorios";
    public static final String TIPO_MANTENIMIENTOS = "mantenimientos";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_mapas);

        findViewById(R.id.cardMapaIncidencias).setOnClickListener(v ->
                abrirMapa(TIPO_INCIDENCIAS, "Mapa de incidencias")
        );

        findViewById(R.id.cardMapaEdificios).setOnClickListener(v ->
                abrirMapa(TIPO_EDIFICIOS, "Mapa de edificios")
        );

        findViewById(R.id.cardMapaLaboratorios).setOnClickListener(v ->
                abrirMapa(TIPO_LABORATORIOS, "Mapa de laboratorios")
        );

        findViewById(R.id.cardMapaMantenimientos).setOnClickListener(v ->
                abrirMapa(TIPO_MANTENIMIENTOS, "Mapa de mantenimientos")
        );

        findViewById(R.id.btnVolverMapas).setOnClickListener(v -> finish());
    }

    private void abrirMapa(String tipoMapa, String tituloMapa) {
        Intent intent = new Intent(this, MapaDetalleActivity.class);
        intent.putExtra(EXTRA_TIPO_MAPA, tipoMapa);
        intent.putExtra(EXTRA_TITULO_MAPA, tituloMapa);
        startActivity(intent);
    }
}
