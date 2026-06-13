package ues.fia.proyecto2_pdm115.rol;

import android.database.SQLException;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import ues.fia.proyecto2_pdm115.R;
import ues.fia.proyecto2_pdm115.controlDBLabCare;

public class CrearRolActivity extends AppCompatActivity {

    private EditText txtNombreRol;
    private EditText txtDescripcionRol;
    private Button btnGuardarRol;
    private Button btnLimpiarRol;
    private Button btnVolverCrearRol;

    private controlDBLabCare db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_rol);

        enlazarVistas();
        abrirBaseDatos();
        configurarEventos();
    }

    private void enlazarVistas() {
        txtNombreRol = findViewById(R.id.txtNombreRol);
        txtDescripcionRol = findViewById(R.id.txtDescripcionRol);
        btnGuardarRol = findViewById(R.id.btnGuardarRol);
        btnLimpiarRol = findViewById(R.id.btnLimpiarRol);
        btnVolverCrearRol = findViewById(R.id.btnVolverCrearRol);
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
        btnGuardarRol.setOnClickListener(v -> guardarRol());
        btnLimpiarRol.setOnClickListener(v -> limpiarCampos());
        btnVolverCrearRol.setOnClickListener(v -> finish());
    }

    private void guardarRol() {
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

        String mensaje = db.insertarRol(nombre, descripcion);
        mostrarMensaje(mensaje);

        if (mensaje.toLowerCase().contains("correctamente")) {
            limpiarCampos();
        }
    }

    private void limpiarCampos() {
        txtNombreRol.setText("");
        txtDescripcionRol.setText("");
        txtNombreRol.requestFocus();
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
}
