package ues.fia.proyecto2_pdm115.tipoIncidencia;

import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import ues.fia.proyecto2_pdm115.*;

public class ActualizarTipoIncidenciaActivity extends AppCompatActivity {

    private Spinner spTipoIncidencia;
    private EditText edtNombre;
    private Button btnActualizar;

    private controlDBLabCare db;
    private ArrayList<TipoIncidenciaItem> listaTipos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actualizar_tipo_incidencia);

        spTipoIncidencia = findViewById(R.id.spTipoIncidenciaActualizar);
        edtNombre = findViewById(R.id.edtNombreTipoIncidenciaActualizar);
        btnActualizar = findViewById(R.id.btnActualizarTipoIncidencia);

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
                    edtNombre.setText(item.nombre);
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });

        btnActualizar.setOnClickListener(v -> actualizarTipoIncidencia());
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
                Toast.makeText(this, "No hay tipos de incidencia registrados.", Toast.LENGTH_LONG).show();
                btnActualizar.setEnabled(false);
            }

        } catch (Exception e) {
            Toast.makeText(this, "Error al cargar tipos: " + e.getMessage(), Toast.LENGTH_LONG).show();

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void actualizarTipoIncidencia() {
        if (listaTipos == null || listaTipos.isEmpty()) {
            Toast.makeText(this, "No hay registros para actualizar.", Toast.LENGTH_SHORT).show();
            return;
        }

        TipoIncidenciaItem seleccionado = (TipoIncidenciaItem) spTipoIncidencia.getSelectedItem();
        String nuevoNombre = edtNombre.getText().toString().trim();

        if (nuevoNombre.isEmpty()) {
            edtNombre.setError("Ingrese el nombre");
            edtNombre.requestFocus();
            return;
        }

        String mensaje = db.actualizarTipoIncidencia(seleccionado.id, nuevoNombre);
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