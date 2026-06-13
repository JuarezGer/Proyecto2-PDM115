package ues.fia.proyecto2_pdm115.rol;

import android.app.AlertDialog;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import ues.fia.proyecto2_pdm115.R;
import ues.fia.proyecto2_pdm115.controlDBLabCare;

public class EliminarRolActivity extends AppCompatActivity {

    private Spinner spRoles;
    private TextView txtDetalleRol;
    private Button btnEliminarRol;
    private Button btnLimpiarRol;
    private Button btnVolverEliminarRol;

    private controlDBLabCare db;
    private ArrayList<RolItem> listaRoles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eliminar_rol);

        enlazarVistas();
        abrirBaseDatos();
        configurarEventos();
        cargarRoles();
    }

    private void enlazarVistas() {
        spRoles = findViewById(R.id.spRoles);
        txtDetalleRol = findViewById(R.id.txtDetalleRol);
        btnEliminarRol = findViewById(R.id.btnEliminarRol);
        btnLimpiarRol = findViewById(R.id.btnLimpiarRol);
        btnVolverEliminarRol = findViewById(R.id.btnVolverEliminarRol);
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
                    mostrarDetalleRol(item);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        btnEliminarRol.setOnClickListener(v -> confirmarEliminacion());
        btnLimpiarRol.setOnClickListener(v -> limpiarSeleccion());
        btnVolverEliminarRol.setOnClickListener(v -> finish());
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
                txtDetalleRol.setText("No hay roles registrados.");
                btnEliminarRol.setEnabled(false);
            } else {
                btnEliminarRol.setEnabled(true);
            }

        } catch (Exception e) {
            mostrarMensaje("Error al cargar roles: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void mostrarDetalleRol(RolItem item) {
        txtDetalleRol.setText(
                "ID: " + item.id +
                        "\nNombre: " + item.nombre +
                        "\nDescripción: " + (item.descripcion.isEmpty() ? "Sin descripción" : item.descripcion)
        );
    }

    private void confirmarEliminacion() {
        if (listaRoles == null || listaRoles.isEmpty()) {
            mostrarMensaje("No hay registros para eliminar.");
            return;
        }

        RolItem seleccionado = (RolItem) spRoles.getSelectedItem();
        if (seleccionado == null) {
            mostrarMensaje("Seleccione un rol.");
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Confirmar eliminación")
                .setMessage("¿Está seguro de eliminar el rol?\n\n" + seleccionado.nombre)
                .setPositiveButton("Sí, eliminar", (dialog, which) -> eliminarRol(seleccionado))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarRol(RolItem seleccionado) {
        if (db == null || db.getDb() == null || !db.getDb().isOpen()) {
            abrirBaseDatos();
        }

        String mensaje = db.eliminarRol(seleccionado.id);
        mostrarMensaje(mensaje);

        cargarRoles();
    }

    private void limpiarSeleccion() {
        if (listaRoles != null && !listaRoles.isEmpty()) {
            spRoles.setSelection(0);
            mostrarDetalleRol(listaRoles.get(0));
        } else {
            txtDetalleRol.setText("No hay roles registrados.");
        }
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
