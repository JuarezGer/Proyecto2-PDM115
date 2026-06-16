package ues.fia.proyecto2_pdm115.edificio;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import com.google.android.material.textfield.TextInputEditText;

import ues.fia.proyecto2_pdm115.R;
import ues.fia.proyecto2_pdm115.VerMapaActivity;
import ues.fia.proyecto2_pdm115.controlDBLabCare;

public class CrearEdificioActivity extends AppCompatActivity {

    private controlDBLabCare helper;
    private ActivityResultLauncher<Intent> mapaLauncher;
    private TextInputEditText editCodigo, editNombre, editDireccion, editLatitud, editLongitud;
    private ImageButton btnGpsEdificio;
    private Button btnCancelar, btnGuardar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_edificio);

        // Instanciamos el helper correcto de la base de datos
        helper = new controlDBLabCare(this);

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

        vincularVistas();
        configurarBotones();
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

        // Configurar la Toolbar superior
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void configurarBotones() {
        // 🚀 AQUÍ CAMBIAMOS TODO EL CÓDIGO VIEJO POR ESTA LÍNEA LIMPIA PARA BRINCAR A OPENSTREETMAP:
        btnGpsEdificio.setOnClickListener(v -> {
            Intent intent = new Intent(this, VerMapaActivity.class);
            mapaLauncher.launch(intent); // Abre tu mapa interactivo esperando la respuesta
        });

        // Configuración de los botones de acción estándar
        btnGuardar.setOnClickListener(v -> guardarEdificio());
        btnCancelar.setOnClickListener(v -> finish());
    }

    private void guardarEdificio() {
        if (!validarCampos()) return;

        String codigo = editCodigo.getText() != null ? editCodigo.getText().toString().trim() : "";
        String nombre = editNombre.getText() != null ? editNombre.getText().toString().trim() : "";
        String direccion = editDireccion.getText() != null ? editDireccion.getText().toString().trim() : ""; // 🚀 Capturamos la caja
        String latitudStr = editLatitud.getText() != null ? editLatitud.getText().toString().trim() : "";
        String longitudStr = editLongitud.getText() != null ? editLongitud.getText().toString().trim() : "";

        Double latitud = latitudStr.isEmpty() ? null : Double.parseDouble(latitudStr);
        Double longitud = longitudStr.isEmpty() ? null : Double.parseDouble(longitudStr);

        helper.abrir();
        // 🚀 Mandamos 'direccion' en el orden que pide tu base de datos
        String resultado = helper.insertarEdificio(nombre, codigo, direccion, latitud, longitud);
        helper.cerrar();

        Toast.makeText(this, resultado, Toast.LENGTH_LONG).show();
        if (resultado.contains("correctamente")) {
            finish();
        }
    }

    private boolean validarCampos() {
        boolean valido = true;

        if (editCodigo.getText() != null && editCodigo.getText().toString().trim().isEmpty()) {
            editCodigo.setError("El código es obligatorio");
            valido = false;
        }
        if (editNombre.getText() != null && editNombre.getText().toString().trim().isEmpty()) {
            editNombre.setError("El nombre es obligatorio");
            valido = false;
        }
        if (editLatitud.getText() != null && editLatitud.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Debe seleccionar la ubicación usando el mapa", Toast.LENGTH_SHORT).show();
            valido = false;
        }

        return valido;
    }
}