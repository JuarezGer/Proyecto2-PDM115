package ues.fia.proyecto2_pdm115.mantenimiento;

import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.HashMap;
import ues.fia.proyecto2_pdm115.R;
import ues.fia.proyecto2_pdm115.controlDBLabCare;

public class EliminarMantenimientoActivity extends AppCompatActivity {

    private controlDBLabCare helper;
    private RecyclerView recyclerView;
    private MantenimientoAdapter adapter;
    private TextView txtNoData;
    private ArrayList<HashMap<String, String>> listaMantenimientos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eliminar_mantenimiento);

        helper = new controlDBLabCare(this);
        recyclerView = findViewById(R.id.recyclerMantenimientos);
        txtNoData = findViewById(R.id.txtNoData);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        cargarDatos();
    }

    private void cargarDatos() {
        helper.abrir();
        Cursor cursor = helper.consultarMantenimientosCursor();
        listaMantenimientos = helper.cursorAListaMap(cursor);
        helper.cerrar();

        if (listaMantenimientos.isEmpty()) {
            txtNoData.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            txtNoData.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            
            if (adapter == null) {
                adapter = new MantenimientoAdapter(listaMantenimientos, (mantenimiento, position) -> {
                    mostrarDialogoConfirmacion(mantenimiento);
                });
                recyclerView.setAdapter(adapter);
            } else {
                adapter.updateData(listaMantenimientos);
            }
        }
    }

    private void mostrarDialogoConfirmacion(HashMap<String, String> mantenimiento) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmar eliminación");
        builder.setMessage("¿Estás seguro de que deseas eliminar este mantenimiento?\nEsta acción no se puede deshacer.");
        
        builder.setPositiveButton("Eliminar", (dialog, which) -> {
            String idStr = mantenimiento.get("id_mantenimiento");
            if (idStr != null) {
                int id = Integer.parseInt(idStr);
                eliminarMantenimiento(id);
            }
        });
        
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        // Personalizar el color del botón eliminar (opcionalmente)
        Button btnEliminar = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (btnEliminar != null) {
            btnEliminar.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
    }

    private void eliminarMantenimiento(int id) {
        helper.abrir();
        String resultado = helper.eliminarMantenimiento(id);
        helper.cerrar();

        if (resultado.contains("correctamente")) {
            Toast.makeText(this, resultado, Toast.LENGTH_SHORT).show();
            cargarDatos(); // Refrescar la lista
        } else {
            Toast.makeText(this, "Error: " + resultado, Toast.LENGTH_LONG).show();
        }
    }
}
