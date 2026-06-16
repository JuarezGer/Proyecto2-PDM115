package ues.fia.proyecto2_pdm115.evidencia;

import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;
import java.util.Map;

import ues.fia.proyecto2_pdm115.R;
import ues.fia.proyecto2_pdm115.controlDBLabCare;

public class ListaEvidenciasActivity extends AppCompatActivity {

    private ListView lvEvidencias;
    private controlDBLabCare db;
    private EvidenciaAdapter adapter;
    private List<Map<String, String>> listaEvidencias;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_evidencias);

        lvEvidencias = findViewById(R.id.lvEvidencias);
        db = new controlDBLabCare(this);

        cargarEvidencias();

        lvEvidencias.setOnItemClickListener((parent, view, position, id) -> {
            Map<String, String> evidencia = listaEvidencias.get(position);

            Intent intent = new Intent(this, DetalleEvidenciaActivity.class);
            intent.putExtra("id_evidencia",     evidencia.get("id_evidencia"));
            intent.putExtra("tipo_evidencia",   evidencia.get("tipo_evidencia"));
            intent.putExtra("descripcion",      evidencia.get("descripcion"));
            intent.putExtra("ruta_archivo",     evidencia.get("ruta_archivo"));
            intent.putExtra("fecha_registro",   evidencia.get("fecha_registro"));
            intent.putExtra("id_incidencia",    evidencia.get("id_incidencia"));
            intent.putExtra("id_mantenimiento", evidencia.get("id_mantenimiento"));
            startActivity(intent);
        });
    }

    private void cargarEvidencias() {
        try {
            db.abrir();
            listaEvidencias = db.obtenerEvidencias();
            db.cerrar();

            if (listaEvidencias.isEmpty()) {
                Toast.makeText(this, "No hay evidencias registradas",
                        Toast.LENGTH_SHORT).show();
            }

            adapter = new EvidenciaAdapter(this, listaEvidencias);
            lvEvidencias.setAdapter(adapter);

        } catch (SQLException e) {
            Toast.makeText(this, "Error al cargar evidencias: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }
}