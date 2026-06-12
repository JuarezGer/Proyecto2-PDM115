package ues.fia.proyecto2_pdm115.tipoIncidencia;

import android.database.SQLException;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import ues.fia.proyecto2_pdm115.R;
import ues.fia.proyecto2_pdm115.controlDBLabCare;

public class CrearTipoIncidenciaActivity extends AppCompatActivity {

    private EditText edtNombreTipoIncidencia;
    private TextView txtMensajeCrearTipoIncidencia;
    private Button btnGuardarTipoIncidencia;
    private Button btnLimpiarTipoIncidencia;
    private Button btnVolverCrearTipoIncidencia;

    private controlDBLabCare db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_tipo_incidencia);

        enlazarVistas();
        abrirBaseDatos();
        configurarEventos();
    }

    private void enlazarVistas() {
        edtNombreTipoIncidencia = findViewById(R.id.edtNombreTipoIncidencia);
        txtMensajeCrearTipoIncidencia = findViewById(R.id.txtMensajeCrearTipoIncidencia);
        btnGuardarTipoIncidencia = findViewById(R.id.btnGuardarTipoIncidencia);
        btnLimpiarTipoIncidencia = findViewById(R.id.btnLimpiarTipoIncidencia);
        btnVolverCrearTipoIncidencia = findViewById(R.id.btnVolverCrearTipoIncidencia);
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
        btnGuardarTipoIncidencia.setOnClickListener(v -> guardarTipoIncidencia());
        btnLimpiarTipoIncidencia.setOnClickListener(v -> limpiarCampos());
        btnVolverCrearTipoIncidencia.setOnClickListener(v -> finish());
    }

    private void guardarTipoIncidencia() {
        String nombre = edtNombreTipoIncidencia.getText().toString().trim();

        if (nombre.isEmpty()) {
            edtNombreTipoIncidencia.setError("Ingrese el nombre del tipo de incidencia");
            edtNombreTipoIncidencia.requestFocus();
            return;
        }

        if (db == null || db.getDb() == null || !db.getDb().isOpen()) {
            abrirBaseDatos();
        }

        String mensaje = db.insertarTipoIncidencia(nombre);
        mostrarMensaje(mensaje);

        if (mensaje.toLowerCase().contains("correctamente")) {
            limpiarCampos();
        }
    }

    private void limpiarCampos() {
        edtNombreTipoIncidencia.setText("");
        edtNombreTipoIncidencia.requestFocus();
    }

    private void mostrarMensaje(String mensaje) {
        txtMensajeCrearTipoIncidencia.setText(mensaje);
        txtMensajeCrearTipoIncidencia.setVisibility(View.VISIBLE);
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
