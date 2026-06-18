package ues.fia.proyecto2_pdm115.evidencia;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import ues.fia.proyecto2_pdm115.R;

public class DetalleEvidenciaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_evidencia);

        Intent intent = getIntent();

        TextView tvId      = findViewById(R.id.tvIdEvidencia);
        TextView tvTipo    = findViewById(R.id.tvTipo);
        TextView tvDesc    = findViewById(R.id.tvDescripcion);
        TextView tvRuta    = findViewById(R.id.tvRutaArchivo);
        TextView tvInc     = findViewById(R.id.tvIdIncidencia);
        TextView tvMant    = findViewById(R.id.tvIdMantenimiento);
        TextView tvFecha   = findViewById(R.id.tvFecha);

        tvId.setText("ID: "             + intent.getStringExtra("id_evidencia"));
        tvTipo.setText("Tipo: "         + intent.getStringExtra("tipo_evidencia"));
        tvDesc.setText("Descripción: "  + intent.getStringExtra("descripcion"));
        tvRuta.setText("Archivo: "      + intent.getStringExtra("ruta_archivo"));
        tvInc.setText("Incidencia: "    + intent.getStringExtra("id_incidencia"));
        tvMant.setText("Mantenimiento: "+ intent.getStringExtra("id_mantenimiento"));
        tvFecha.setText("Fecha: "       + intent.getStringExtra("fecha_registro"));
    }
}