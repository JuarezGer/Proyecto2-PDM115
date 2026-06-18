package ues.fia.proyecto2_pdm115.edificio;

import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;

import ues.fia.proyecto2_pdm115.R;
import ues.fia.proyecto2_pdm115.VerMapaActivity;
import ues.fia.proyecto2_pdm115.controlDBLabCare;

public class CrearEdificioActivity extends AppCompatActivity {

    private controlDBLabCare helper;
    private ActivityResultLauncher<Intent> mapaLauncher;

    private TextInputEditText editCodigo;
    private TextInputEditText editNombre;
    private TextInputEditText editDireccion;
    private TextInputEditText editLatitud;
    private TextInputEditText editLongitud;

    private ImageButton btnGpsEdificio;
    private Button btnCancelar;
    private Button btnGuardar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_edificio);

        helper = new controlDBLabCare(this);

        registrarResultadoMapa();
        vincularVistas();
        configurarBotones();
    }

    private void registrarResultadoMapa() {
        mapaLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        double lat = result.getData().getDoubleExtra("LATITUD", 0.0);
                        double lon = result.getData().getDoubleExtra("LONGITUD", 0.0);

                        editLatitud.setText(String.valueOf(lat));
                        editLongitud.setText(String.valueOf(lon));

                        Toast.makeText(this, "Coordenadas cargadas desde el mapa.", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void vincularVistas() {
        editCodigo = findViewById(R.id.editCodigoEdificio);
        editNombre = findViewById(R.id.editNombreEdificio);
        editDireccion = findViewById(R.id.editDireccionEdificio);
        editLatitud = findViewById(R.id.editLatitudEdificio);
        editLongitud = findViewById(R.id.editLongitudEdificio);
        btnGpsEdificio = findViewById(R.id.btnGpsEdificio);
        btnCancelar = findViewById(R.id.btnCancelarEdificio);
        btnGuardar = findViewById(R.id.btnGuardarEdificio);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
    }

    private void configurarBotones() {
        btnGpsEdificio.setOnClickListener(v -> abrirMapa());
        btnGuardar.setOnClickListener(v -> guardarEdificio());
        btnCancelar.setOnClickListener(v -> finish());
    }

    private void abrirMapa() {
        try {
            Intent intent = new Intent(this, VerMapaActivity.class);
            mapaLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(this, "No se pudo abrir el mapa: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void guardarEdificio() {
        if (!validarCampos()) return;

        String codigo = obtenerTexto(editCodigo);
        String nombre = obtenerTexto(editNombre);
        String direccion = obtenerTexto(editDireccion);
        String latitudStr = obtenerTexto(editLatitud);
        String longitudStr = obtenerTexto(editLongitud);

        Double latitud = convertirDoubleNullable(latitudStr, "latitud");
        Double longitud = convertirDoubleNullable(longitudStr, "longitud");

        if (!latitudStr.isEmpty() && latitud == null) return;
        if (!longitudStr.isEmpty() && longitud == null) return;

        try {
            helper.abrir();

            String resultado = helper.insertarEdificio(nombre, codigo, direccion, latitud, longitud);

            Toast.makeText(this, resultado, Toast.LENGTH_LONG).show();
            if (resultado.toLowerCase().contains("correctamente")) {
                limpiarCampos();
            }
        } catch (SQLException e) {
            Toast.makeText(this, "Error al abrir la base de datos: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error al guardar edificio: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            helper.cerrar();
        }
    }

    private boolean validarCampos() {
        boolean valido = true;

        if (obtenerTexto(editCodigo).isEmpty()) {
            editCodigo.setError("El código es obligatorio");
            editCodigo.requestFocus();
            valido = false;
        }

        if (obtenerTexto(editNombre).isEmpty()) {
            editNombre.setError("El nombre es obligatorio");
            if (valido) editNombre.requestFocus();
            valido = false;
        }

        String latitud = obtenerTexto(editLatitud);
        String longitud = obtenerTexto(editLongitud);

        if (!latitud.isEmpty() && convertirDoubleNullable(latitud, "latitud") == null) {
            if (valido) editLatitud.requestFocus();
            valido = false;
        }

        if (!longitud.isEmpty() && convertirDoubleNullable(longitud, "longitud") == null) {
            if (valido) editLongitud.requestFocus();
            valido = false;
        }

        return valido;
    }

    private String obtenerTexto(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private Double convertirDoubleNullable(String valor, String campo) {
        if (valor == null || valor.trim().isEmpty()) return null;
        try {
            return Double.parseDouble(valor.trim());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "El valor de " + campo + " no es válido.", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void limpiarCampos() {
        editCodigo.setText("");
        editNombre.setText("");
        if (editDireccion != null) editDireccion.setText("");
        editLatitud.setText("");
        editLongitud.setText("");
        editCodigo.requestFocus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (helper != null) {
            helper.cerrar();
            helper = null;
        }
    }
}
