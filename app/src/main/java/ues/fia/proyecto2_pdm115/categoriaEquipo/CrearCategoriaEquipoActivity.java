package ues.fia.proyecto2_pdm115.categoriaEquipo;

import android.database.SQLException;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import ues.fia.proyecto2_pdm115.R;
import ues.fia.proyecto2_pdm115.controlDBLabCare;

public class CrearCategoriaEquipoActivity extends AppCompatActivity {

    private EditText edtNombreCategoria;
    private TextView txtMensaje;
    private Button btnGuardar;
    private Button btnLimpiar;
    private Button btnVolver;

    private controlDBLabCare db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_categoria_equipo);

        enlazarVistas();
        abrirBaseDatos();
        configurarEventos();
    }

    private void enlazarVistas() {
        edtNombreCategoria = findViewById(R.id.edtNombreCategoriaCrear);
        txtMensaje = findViewById(R.id.txtMensajeCrearCategoria);

        btnGuardar = findViewById(R.id.btnGuardarCategoria);
        btnLimpiar = findViewById(R.id.btnLimpiarCategoria);
        btnVolver = findViewById(R.id.btnVolverCrearCategoria);
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
        btnGuardar.setOnClickListener(v -> guardarCategoria());
        btnLimpiar.setOnClickListener(v -> limpiarCampos());
        btnVolver.setOnClickListener(v -> finish());
    }

    private void guardarCategoria() {

        String nombre = edtNombreCategoria.getText().toString().trim();

        if (nombre.isEmpty()) {
            edtNombreCategoria.setError("Ingrese el nombre");
            edtNombreCategoria.requestFocus();
            return;
        }

        String mensaje = db.insertarCategoriaEquipo(nombre);

        mostrarMensaje(mensaje);

        if (mensaje.toLowerCase().contains("correctamente")) {
            limpiarCampos();
        }
    }

    private void limpiarCampos() {
        edtNombreCategoria.setText("");
        edtNombreCategoria.requestFocus();
    }

    private void mostrarMensaje(String mensaje) {
        txtMensaje.setText(mensaje);
        txtMensaje.setVisibility(View.VISIBLE);
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