package ues.fia.proyecto2_pdm115.mantenimiento;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import ues.fia.proyecto2_pdm115.*;
import ues.fia.proyecto2_pdm115.equipo.EquipoMenuActivity;
import ues.fia.proyecto2_pdm115.indicencia.IncidenciaMenuActivity;
import ues.fia.proyecto2_pdm115.usuario.UsuarioMenuActivity;

public class MantenimientoMenuActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_mantenimiento);

        findViewById(R.id.btnCrearMantenimiento).setOnClickListener(v -> abrir(CrearMantenimientoActivity.class));
        findViewById(R.id.btnActualizarMantenimiento).setOnClickListener(v -> abrir(ModificarMantenimientoActivity.class));
        findViewById(R.id.btnEliminarMantenimiento).setOnClickListener(v -> abrir(EliminarMantenimientoActivity.class));
        findViewById(R.id.btnVisualizarMantenimiento).setOnClickListener(v -> abrir(ConsultarMantenimientoActivity.class));
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
