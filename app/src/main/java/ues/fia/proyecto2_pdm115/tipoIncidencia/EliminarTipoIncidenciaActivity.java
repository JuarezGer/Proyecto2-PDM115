package ues.fia.proyecto2_pdm115.tipoIncidencia;

import android.app.AlertDialog;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import ues.fia.proyecto2_pdm115.*;

public class EliminarTipoIncidenciaActivity extends AppCompatActivity {

    private Spinner spTipoIncidencia;
    private TextView txtDetalle;
    private Button btnEliminar;

    private controlDBLabCare db;
    private ArrayList<TipoIncidenciaItem> listaTipos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eliminar_tipo_incidencia);

        spTipoIncidencia = findViewById(R.id.spTipoIncidenciaEliminar);
        txtDetalle = findViewById(R.id.txtDetalleTipoIncidenciaEliminar);
        btnEliminar = findViewById(R.id.btnEliminarTipoIncidencia);

        db = new controlDBLabCare(this);

        try {
            db.abrir();
            cargarTiposIncidencia();
        } catch (SQLException e) {
            Toast.makeText(this, "Error al abrir base de datos: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        spTipoIncidencia.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                if (listaTipos != null && !listaTipos.isEmpty()) {
                    TipoIncidenciaItem item = listaTipos.get(position);
                    txtDetalle.setText("ID: " + item.id + "\nNombre: " + item.nombre);
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });

        btnEliminar.setOnClickListener(v -> confirmarEliminacion());
    }

    private void cargarTiposIncidencia() {
        listaTipos = new ArrayList<>();
        Cursor cursor = null;

        try {
            cursor = db.consultarTiposIncidenciaCursor();

            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("id_tipo_incidencia"));
                    String nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"));

                    listaTipos.add(new TipoIncidenciaItem(id, nombre));

                } while (cursor.moveToNext());
            }

            ArrayAdapter<TipoIncidenciaItem> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_spinner_item,
                    listaTipos
            );

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spTipoIncidencia.setAdapter(adapter);

            if (listaTipos.isEmpty()) {
                txtDetalle.setText("No hay tipos de incidencia registrados.");
                btnEliminar.setEnabled(false);
            } else {
                btnEliminar.setEnabled(true);
            }

        } catch (Exception e) {
            Toast.makeText(this, "Error al cargar tipos: " + e.getMessage(), Toast.LENGTH_LONG).show();

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void confirmarEliminacion() {
        if (listaTipos == null || listaTipos.isEmpty()) {
            Toast.makeText(this, "No hay registros para eliminar.", Toast.LENGTH_SHORT).show();
            return;
        }

        TipoIncidenciaItem seleccionado = (TipoIncidenciaItem) spTipoIncidencia.getSelectedItem();

        new AlertDialog.Builder(this)
                .setTitle("Confirmar eliminación")
                .setMessage("¿Está seguro de eliminar el tipo de incidencia?\n\n" + seleccionado.nombre)
                .setPositiveButton("Sí, eliminar", (dialog, which) -> eliminarTipoIncidencia(seleccionado))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarTipoIncidencia(TipoIncidenciaItem seleccionado) {
        String mensaje = db.eliminarTipoIncidencia(seleccionado.id);
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();

        cargarTiposIncidencia();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (db != null) {
            db.cerrar();
        }
    }

    private static class TipoIncidenciaItem {
        int id;
        String nombre;

        TipoIncidenciaItem(int id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }

        @Override
        public String toString() {
            return id + " - " + nombre;
        }
    }
}