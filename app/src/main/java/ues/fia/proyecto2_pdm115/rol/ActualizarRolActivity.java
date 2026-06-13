package ues.fia.proyecto2_pdm115.rol;

import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import ues.fia.proyecto2_pdm115.R;
import ues.fia.proyecto2_pdm115.controlDBLabCare;

public class ActualizarRolActivity extends AppCompatActivity {

    private Spinner spRoles;
    private EditText txtIdRol;
    private EditText txtNombreRol;
    private EditText txtDescripcionRol;
    private Button btnActualizarRol;
    private Button btnLimpiarRol;
    private Button btnVolverActualizarRol;

    private controlDBLabCare db;
    private ArrayList<RolItem> listaRoles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actualizar_rol);

        enlazarVistas();
        abrirBaseDatos();
        configurarEventos();
        cargarRoles();
    }

    private void enlazarVistas() {
        spRoles = findViewById(R.id.spRoles);
        txtIdRol = findViewById(R.id.txtIdRol);
        txtNombreRol = findViewById(R.id.txtNombreRol);
        txtDescripcionRol = findViewById(R.id.txtDescripcionRol);
        btnActualizarRol = findViewById(R.id.btnActualizarRol);
        btnLimpiarRol = findViewById(R.id.btnLimpiarRol);
        btnVolverActualizarRol = findViewById(R.id.btnVolverActualizarRol);
    }

    private void abrirBaseDatos() {
        db = new controlDBLabCare(this);
        try {
            db.abrir();
        } catch (SQLException e) {
            mostrarMensaje("Error al abrir la base de datos: " + e.getMessage());
        } catch (Exception e) {
            mostrarMensaje("Error: " + e.getMessage());
        }
    }

    private void configurarEventos() {
        spRoles.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (listaRoles != null && !listaRoles.isEmpty()) {
                    RolItem item = listaRoles.get(position);
                    mostrarDatosRol(item);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        btnActualizarRol.setOnClickListener(v -> actualizarRol());
        btnLimpiarRol.setOnClickListener(v -> limpiarCampos());
        btnVolverActualizarRol.setOnClickListener(v -> finish());
    }

    private void cargarRoles() {
        listaRoles = new ArrayList<>();
        Cursor cursor = null;

        try {
            if (db == null || db.getDb() == null || !db.getDb().isOpen()) {
                abrirBaseDatos();
            }

            cursor = db.consultarRolesCursor();

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("id_rol"));
                    String nombre = obtenerTextoCursor(cursor, "nombre");
                    String descripcion = obtenerTextoCursor(cursor, "descripcion");

                    listaRoles.add(new RolItem(id, nombre, descripcion));
                } while (cursor.moveToNext());
            }

            ArrayAdapter<RolItem> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_spinner_item,
                    listaRoles
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spRoles.setAdapter(adapter);

            if (listaRoles.isEmpty()) {
                limpiarCampos();
                btnActualizarRol.setEnabled(false);
                mostrarMensaje("No hay roles registrados.");
            } else {
                btnActualizarRol.setEnabled(true);
            }

        } catch (Exception e) {
            mostrarMensaje("Error al cargar roles: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void mostrarDatosRol(RolItem item) {
        txtIdRol.setText(String.valueOf(item.id));
        txtNombreRol.setText(item.nombre);
        txtDescripcionRol.setText(item.descripcion);
    }

    private void actualizarRol() {
        if (listaRoles == null || listaRoles.isEmpty()) {
            mostrarMensaje("No hay registros para actualizar.");
            return;
        }

        RolItem seleccionado = (RolItem) spRoles.getSelectedItem();
        if (seleccionado == null) {
            mostrarMensaje("Seleccione un rol.");
            return;
        }

        String nombre = txtNombreRol.getText().toString().trim();
        String descripcion = txtDescripcionRol.getText().toString().trim();

        if (nombre.isEmpty()) {
            txtNombreRol.setError("Ingrese el nombre del rol");
            txtNombreRol.requestFocus();
            return;
        }

        if (db == null || db.getDb() == null || !db.getDb().isOpen()) {
            abrirBaseDatos();
        }

        String mensaje = db.actualizarRol(seleccionado.id, nombre, descripcion);
        mostrarMensaje(mensaje);

        cargarRoles();
    }

    private void limpiarCampos() {
        txtIdRol.setText("");
        txtNombreRol.setText("");
        txtDescripcionRol.setText("");
        txtNombreRol.requestFocus();
    }

    private String obtenerTextoCursor(Cursor cursor, String columna) {
        int index = cursor.getColumnIndex(columna);
        if (index < 0 || cursor.isNull(index)) {
            return "";
        }
        return cursor.getString(index);
    }

    private void mostrarMensaje(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null) {
            db.cerrar();
            db = null;
        }
    }

    private static class RolItem {
        int id;
        String nombre;
        String descripcion;

        RolItem(int id, String nombre, String descripcion) {
            this.id = id;
            this.nombre = nombre;
            this.descripcion = descripcion;
        }

        @Override
        public String toString() {
            return id + " - " + nombre;
        }
    }
}
