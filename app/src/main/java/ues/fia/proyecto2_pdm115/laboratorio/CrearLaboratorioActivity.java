package ues.fia.proyecto2_pdm115.laboratorio;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;
import ues.fia.proyecto2_pdm115.R;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;
import ues.fia.proyecto2_pdm115.controlDBLabCare;
import android.content.Intent;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;


import ues.fia.proyecto2_pdm115.VerMapaActivity;

public class CrearLaboratorioActivity extends AppCompatActivity {

    private controlDBLabCare helper;
    private ActivityResultLauncher<Intent> mapaLauncher;
    private Spinner spEdificio;
    private TextInputEditText editCodigo, editNombre, editPiso, editLatitud, editLongitud;
    private ImageButton btnGps;
    private Button btnCancelar, btnGuardar;


    private List<Integer> idsEdificios = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_laboratorio);

        helper = new controlDBLabCare(this);

        mapaLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {

                        double lat = result.getData().getDoubleExtra("LATITUD", 0.0);
                        double lon = result.getData().getDoubleExtra("LONGITUD", 0.0);

                        editLatitud.setText(String.valueOf(lat));
                        editLongitud.setText(String.valueOf(lon));

                        Toast.makeText(this, "Coordenadas del laboratorio cargadas.", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        vincularVistas();
        configurarSpinnerEdificios();
        configurarBotones();
    }

    private void vincularVistas() {
        spEdificio = findViewById(R.id.spinnerEdificioLab);
        editCodigo = findViewById(R.id.editCodigoLab);
        editNombre = findViewById(R.id.editNombreLab);
        editPiso = findViewById(R.id.editPisoLab);
        editLatitud = findViewById(R.id.editLatitudLab);
        editLongitud = findViewById(R.id.editLongitudLab);
        btnGps = findViewById(R.id.btnGpsLab);
        btnCancelar = findViewById(R.id.btnCancelarLab);
        btnGuardar = findViewById(R.id.btnGuardarLab);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbarLab);
        setSupportActionBar(toolbar);
    }

    private void configurarSpinnerEdificios() {
        helper.abrir();

        Cursor cursor = helper.consultarEdificiosCursor();
        List<String> listaEdificios = new ArrayList<>();

        listaEdificios.add("Selecciona un edificio");
        idsEdificios.add(-1);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    idsEdificios.add(cursor.getInt(0));
                    listaEdificios.add(cursor.getString(1) + " (" + cursor.getString(2) + ")");
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        helper.cerrar();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, listaEdificios);
        spEdificio.setAdapter(adapter);
    }

    private void configurarBotones() {

        btnGps.setOnClickListener(v -> {
            Intent intent = new Intent(this, VerMapaActivity.class);
            mapaLauncher.launch(intent);
        });

        btnCancelar.setOnClickListener(v -> finish());
        btnGuardar.setOnClickListener(v -> guardarLaboratorio());
    }

    private void guardarLaboratorio() {
        if (!validarCampos()) return;

        int idEdificio = idsEdificios.get(spEdificio.getSelectedItemPosition());

        String codigo = editCodigo.getText() != null ? editCodigo.getText().toString().trim() : "";
        String nombre = editNombre.getText() != null ? editNombre.getText().toString().trim() : "";
        String piso = editPiso.getText() != null ? editPiso.getText().toString().trim() : "";
        String latitudStr = editLatitud.getText() != null ? editLatitud.getText().toString().trim() : "";
        String longitudStr = editLongitud.getText() != null ? editLongitud.getText().toString().trim() : "";

        Double latitud = latitudStr.isEmpty() ? null : Double.parseDouble(latitudStr);
        Double longitud = longitudStr.isEmpty() ? null : Double.parseDouble(longitudStr);
        if (piso.isEmpty()) piso = null;

        helper.abrir();
        String resultado = helper.insertarLaboratorio(idEdificio, nombre, codigo, piso, latitud, longitud);
        helper.cerrar();

        Toast.makeText(this, resultado, Toast.LENGTH_LONG).show();

        if (resultado.contains("correctamente")) {
            finish();
        }
    }

    private boolean validarCampos() {
        boolean valido = true;

        if (spEdificio.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Debe seleccionar un edificio", Toast.LENGTH_SHORT).show();
            valido = false;
        }
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