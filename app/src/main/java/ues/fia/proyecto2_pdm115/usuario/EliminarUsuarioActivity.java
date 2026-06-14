package ues.fia.proyecto2_pdm115.usuario;

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
import java.util.HashMap;

import ues.fia.proyecto2_pdm115.R;
import ues.fia.proyecto2_pdm115.controlDBLabCare;

public class EliminarUsuarioActivity extends AppCompatActivity {

    private Spinner spUsuarioEliminar;
    private TextView txtDetalleUsuarioEliminar;
    private TextView txtMensajeEliminarUsuario;
    private Button btnEliminarUsuario;
    private Button btnLimpiarEliminarUsuario;
    private Button btnVolverEliminarUsuario;

    private controlDBLabCare db;
    private ArrayList<UsuarioItem> listaUsuarios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eliminar_usuario);

        enlazarVistas();
        abrirBaseDatos();
        cargarUsuarios();
        configurarEventos();
    }

    private void enlazarVistas() {
        spUsuarioEliminar = findViewById(R.id.spUsuarioEliminar);
        txtDetalleUsuarioEliminar = findViewById(R.id.txtDetalleUsuarioEliminar);
        txtMensajeEliminarUsuario = findViewById(R.id.txtMensajeEliminarUsuario);
        btnEliminarUsuario = findViewById(R.id.btnEliminarUsuario);
        btnLimpiarEliminarUsuario = findViewById(R.id.btnLimpiarEliminarUsuario);
        btnVolverEliminarUsuario = findViewById(R.id.btnVolverEliminarUsuario);
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
        spUsuarioEliminar.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mostrarDetalleUsuario();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        btnEliminarUsuario.setOnClickListener(v -> confirmarEliminacion());
        btnLimpiarEliminarUsuario.setOnClickListener(v -> mostrarDetalleUsuario());
        btnVolverEliminarUsuario.setOnClickListener(v -> finish());
    }

    private void cargarUsuarios() {
        listaUsuarios = new ArrayList<>();
        Cursor cursor = null;

        try {
            if (db == null || db.getDb() == null || !db.getDb().isOpen()) abrirBaseDatos();

            cursor = db.consultarUsuariosCursor();
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("id_usuario"));
                    String nombres = cursor.getString(cursor.getColumnIndexOrThrow("nombres"));
                    String apellidos = cursor.getString(cursor.getColumnIndexOrThrow("apellidos"));
                    String correo = cursor.getString(cursor.getColumnIndexOrThrow("correo"));
                    String rol = cursor.getString(cursor.getColumnIndexOrThrow("rol"));
                    listaUsuarios.add(new UsuarioItem(id, nombres, apellidos, correo, rol));
                } while (cursor.moveToNext());
            }

            ArrayAdapter<UsuarioItem> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listaUsuarios);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spUsuarioEliminar.setAdapter(adapter);

            if (listaUsuarios.isEmpty()) {
                txtDetalleUsuarioEliminar.setText("No hay usuarios registrados.");
                btnEliminarUsuario.setEnabled(false);
            } else {
                btnEliminarUsuario.setEnabled(true);
            }

        } catch (Exception e) {
            mostrarMensaje("Error al cargar usuarios: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private void mostrarDetalleUsuario() {
        if (listaUsuarios == null || listaUsuarios.isEmpty()) {
            txtDetalleUsuarioEliminar.setText("No hay usuarios registrados.");
            return;
        }

        UsuarioItem seleccionado = (UsuarioItem) spUsuarioEliminar.getSelectedItem();
        if (seleccionado == null) return;

        try {
            HashMap<String, String> usuario = db.consultarUsuario(seleccionado.id);
            String texto = "ID: " + seleccionado.id +
                    "\nNombres: " + valor(usuario, "nombres") +
                    "\nApellidos: " + valor(usuario, "apellidos") +
                    "\nCorreo: " + valor(usuario, "correo") +
                    "\nRol: " + valor(usuario, "rol") +
                    "\nUsa biometría: " + textoSiNo(valor(usuario, "usa_biometria")) +
                    "\nEstado: " + textoActivo(valor(usuario, "activo"));

            txtDetalleUsuarioEliminar.setText(texto);
            txtMensajeEliminarUsuario.setVisibility(View.GONE);
        } catch (Exception e) {
            txtDetalleUsuarioEliminar.setText("ID: " + seleccionado.id +
                    "\nUsuario: " + seleccionado.nombres + " " + seleccionado.apellidos +
                    "\nCorreo: " + seleccionado.correo +
                    "\nRol: " + seleccionado.rol);
        }
    }

    private void confirmarEliminacion() {
        if (listaUsuarios == null || listaUsuarios.isEmpty()) {
            Toast.makeText(this, "No hay registros para eliminar.", Toast.LENGTH_SHORT).show();
            return;
        }

        UsuarioItem seleccionado = (UsuarioItem) spUsuarioEliminar.getSelectedItem();
        if (seleccionado == null) return;

        new AlertDialog.Builder(this)
                .setTitle("Confirmar eliminación")
                .setMessage("¿Está seguro de eliminar el usuario?\n\n" + seleccionado.nombres + " " + seleccionado.apellidos)
                .setPositiveButton("Sí, eliminar", (dialog, which) -> eliminarUsuario(seleccionado))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarUsuario(UsuarioItem seleccionado) {
        String mensaje = db.eliminarUsuario(seleccionado.id);
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
        mostrarMensaje(mensaje);
        cargarUsuarios();
    }

    private String valor(HashMap<String, String> mapa, String clave) {
        if (mapa == null) return "";
        String valor = mapa.get(clave);
        return valor == null ? "" : valor;
    }

    private String textoSiNo(String valor) {
        return "1".equals(valor) ? "Sí" : "No";
    }

    private String textoActivo(String valor) {
        return "1".equals(valor) ? "Activo" : "Inactivo";
    }

    private void mostrarMensaje(String mensaje) {
        txtMensajeEliminarUsuario.setText(mensaje);
        txtMensajeEliminarUsuario.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null) {
            db.cerrar();
            db = null;
        }
    }

    private static class UsuarioItem {
        int id;
        String nombres;
        String apellidos;
        String correo;
        String rol;

        UsuarioItem(int id, String nombres, String apellidos, String correo, String rol) {
            this.id = id;
            this.nombres = nombres;
            this.apellidos = apellidos;
            this.correo = correo;
            this.rol = rol;
        }

        @Override
        public String toString() {
            return id + " - " + nombres + " " + apellidos;
        }
    }
}
