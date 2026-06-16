package ues.fia.proyecto2_pdm115.evidencia;

import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.Map;

import ues.fia.proyecto2_pdm115.R;
import ues.fia.proyecto2_pdm115.controlDBLabCare;

public class SeleccionarEvidenciaActivity extends AppCompatActivity {

    private ListView lvEvidencias;
    private controlDBLabCare db;
    private EvidenciaAdapter adapter;
    private List<Map<String, String>> listaEvidencias;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Puedes reutilizar el mismo diseño de XML que usaste para la lista normal
        setContentView(R.layout.activity_lista_evidencias);

        lvEvidencias = findViewById(R.id.lvEvidencias);
        db = new controlDBLabCare(this);

        cargarEvidencias();

        lvEvidencias.setOnItemClickListener((parent, view, position, id) -> {
            Map<String, String> evidencia = listaEvidencias.get(position);
            String idString = evidencia.get("id_evidencia");

            if (idString != null && !idString.isEmpty()) {
                try {
                    // Convertimos el String a int para que ModificarEvidenciaActivity lo lea correctamente
                    int idEvidenciaInt = Integer.parseInt(idString);

                    Intent intent = new Intent(this, ModificarEvidenciaActivity.class);
                    intent.putExtra("id_evidencia", idEvidenciaInt);
                    startActivity(intent);

                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Error: ID de evidencia no válido", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "No se pudo obtener el ID de la evidencia", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Es importante recargar la lista cuando volvemos de modificar una evidencia
    @Override
    protected void onResume() {
        super.onResume();
        cargarEvidencias();
    }

    private void cargarEvidencias() {
        try {
            db.abrir();
            listaEvidencias = db.obtenerEvidencias();
            db.cerrar();

            if (listaEvidencias.isEmpty()) {
                Toast.makeText(this, "No hay evidencias registradas para modificar", Toast.LENGTH_SHORT).show();
            }

            adapter = new EvidenciaAdapter(this, listaEvidencias);
            lvEvidencias.setAdapter(adapter);

        } catch (SQLException e) {
            Toast.makeText(this, "Error al cargar evidencias: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}