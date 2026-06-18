package ues.fia.proyecto2_pdm115.reporte;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import ues.fia.proyecto2_pdm115.R;

public class ReportesMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reportes_menu);

        findViewById(R.id.cardGraficosIncidencias).setOnClickListener(v ->
                startActivity(new Intent(this, GraficosIncidenciasActivity.class))
        );

        findViewById(R.id.cardGraficosMantenimientos).setOnClickListener(v ->
                startActivity(new Intent(this, GraficosMantenimientosActivity.class))
        );

        findViewById(R.id.cardReporteGeneral).setOnClickListener(v ->
                startActivity(new Intent(this, VisualizarReporteGeneralActivity.class))
        );

        findViewById(R.id.btnVolverReportesMenu).setOnClickListener(v -> finish());
    }
}
