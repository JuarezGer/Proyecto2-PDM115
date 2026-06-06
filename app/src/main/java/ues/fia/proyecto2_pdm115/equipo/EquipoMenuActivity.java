package ues.fia.proyecto2_pdm115.equipo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import ues.fia.proyecto2_pdm115.*;
import ues.fia.proyecto2_pdm115.indicencia.IncidenciaMenuActivity;
import ues.fia.proyecto2_pdm115.usuario.UsuarioMenuActivity;

public class EquipoMenuActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_equipo);

        /*findViewById(R.id.btnCrearEquipo).setOnClickListener(v -> abrir(CrearEquipoActivity.class));
        findViewById(R.id.btnVisualizarEquipo).setOnClickListener(v -> abrir(VisualizarEquipoActivity.class));
        findViewById(R.id.btnActualizarEquipo).setOnClickListener(v -> abrir(ActualizarEquipoActivity.class));
        findViewById(R.id.btnEliminarEquipo).setOnClickListener(v -> abrir(EliminarEquipoActivity.class));*/
        findViewById(R.id.btnVolver).setOnClickListener(v -> finish());
        configurarBarraInferior();
    }

    private void abrir(Class<?> activityDestino) {
        startActivity(new Intent(this, activityDestino));
    }

    private void configurarBarraInferior() {
        findViewById(R.id.navHome).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        findViewById(R.id.navEquipos).setOnClickListener(v -> abrirDesdeBarra(EquipoMenuActivity.class));
        findViewById(R.id.navReportes).setOnClickListener(v -> abrirDesdeBarra(IncidenciaMenuActivity.class));
        findViewById(R.id.navPerfil).setOnClickListener(v -> abrirDesdeBarra(UsuarioMenuActivity.class));
    }

    private void abrirDesdeBarra(Class<?> activityDestino) {
        if (!activityDestino.equals(getClass())) {
            startActivity(new Intent(this, activityDestino));
        }
    }
}
