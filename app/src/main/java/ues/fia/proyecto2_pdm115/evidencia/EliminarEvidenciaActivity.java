package ues.fia.proyecto2_pdm115.evidencia;

import android.database.SQLException;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;
import java.util.Map;

import ues.fia.proyecto2_pdm115.R;
import ues.fia.proyecto2_pdm115.controlDBLabCare;

public class EliminarEvidenciaActivity extends AppCompatActivity {

    private ListView lvEvidencias;
    private Button btnEliminar;
    private controlDBLabCare db;
    private EvidenciaAdapter adapter;
    private List<Map<String, String>> listaEvidencias;
    private int posicionSeleccionada = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eliminar_evidencia);

        lvEvidencias = findViewById(R.id.lvEvidencias);
        btnEliminar  = findViewById(R.id.btnEliminar);

        db = new controlDBLabCare(this);

        cargarEvidencias();

        // Selección de item
        lvEvidencias.setOnItemClickListener((parent, view, position, id) -> {
            posicionSeleccionada = position;
            btnEliminar.setEnabled(true);
            // Resaltar visualmente la selección
            for (int i = 0; i < lvEvidencias.getChildCount(); i++) {
                lvEvidencias.getChildAt(i)
                        .setBackgroundColor(Color.TRANSPARENT);
            }
            view.setBackgroundColor(Color.parseColor("#FFE0E0"));
        });

        // Botón eliminar
        btnEliminar.setOnClickListener(v -> {
            if (posicionSeleccionada == -1) return;
            mostrarConfirmacion();
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

    private void mostrarConfirmacion() {
        Map<String, String> evidencia = listaEvidencias.get(posicionSeleccionada);
        String tipo  = evidencia.get("tipo_evidencia");
        int    id    = Integer.parseInt(evidencia.get("id_evidencia"));

        new AlertDialog.Builder(this)
                .setTitle("Confirmar eliminación")
                .setMessage("¿Está seguro que desea eliminar la evidencia de tipo \""
                        + tipo + "\"?\nEsta acción no se puede deshacer.")
                .setPositiveButton("Eliminar", (dialog, which) -> eliminar(id))
                .setNegativeButton("Cancelar", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void eliminar(int idEvidencia) {
        try {
            db.abrir();
            boolean exito = db.eliminarEvidencia(idEvidencia);
            db.cerrar();

            if (exito) {
                Toast.makeText(this, "Evidencia eliminada correctamente",
                        Toast.LENGTH_SHORT).show();
                posicionSeleccionada = -1;
                btnEliminar.setEnabled(false);
                cargarEvidencias(); // Recargar la lista
            } else {
                Toast.makeText(this, "No se pudo eliminar la evidencia",
                        Toast.LENGTH_SHORT).show();
            }

        } catch (SQLException e) {
            Toast.makeText(this, "Error al eliminar: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }
}