package ues.fia.proyecto2_pdm115.edificio;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import ues.fia.proyecto2_pdm115.R;
import ues.fia.proyecto2_pdm115.controlDBLabCare;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.database.Cursor;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import ues.fia.proyecto2_pdm115.VerMapaActivity;

public class ActualizarEdificiosActivity extends AppCompatActivity {

    private controlDBLabCare helper;
    private ActivityResultLauncher<Intent> mapaLauncher;
    private EditText editBuscarCodigo, editNombre, editDireccion, editLatitud, editLongitud;
    private Button btnBuscar, btnGpsSimular, btnActualizar, btnVolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actualizar_edificios);

        helper = new controlDBLabCare(this);

        editBuscarCodigo = findViewById(R.id.editBuscarCodEdificio);
        editNombre = findViewById(R.id.editActNombreEdificio);
        editDireccion = findViewById(R.id.editActDireccionEdificio);
        editLatitud = findViewById(R.id.editActLatEdificio);
        editLongitud = findViewById(R.id.editActLonEdificio);

        btnBuscar = findViewById(R.id.btnBuscarEdificio);
        btnGpsSimular = findViewById(R.id.btnGpsActEdificio); // Este es tu botón de ubicación en el XML
        btnActualizar = findViewById(R.id.btnConfirmarActEdificio);
        btnVolver = findViewById(R.id.btnVolverActEdificio);

        // Inicializamos el receptor del mapa interactivo
        mapaLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        // Extraemos las nuevas coordenadas que el usuario seleccionó en OpenStreetMap
                        double lat = result.getData().getDoubleExtra("LATITUD", 0.0);
                        double lon = result.getData().getDoubleExtra("LONGITUD", 0.0);

                        // Las sobreescribimos en las cajas de texto automáticamente
                        editLatitud.setText(String.valueOf(lat));
                        editLongitud.setText(String.valueOf(lon));

                        Toast.makeText(this, "Nuevas coordenadas cargadas desde el mapa.", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        btnBuscar.setOnClickListener(v -> buscarEdificio());

        // 🚀 REEMPLAZAMOS LA SIMULACIÓN VIEJA POR LA APERTURA REAL DE OPENSTREETMAP:
        btnGpsSimular.setOnClickListener(v -> {
            Intent intent = new Intent(this, VerMapaActivity.class);
            mapaLauncher.launch(intent);
        });

        btnActualizar.setOnClickListener(v -> actualizarEdificio());
        btnVolver.setOnClickListener(v -> finish());
    }

    private void buscarEdificio() {
        String codBuscar = editBuscarCodigo.getText().toString().trim();
        if (codBuscar.isEmpty()) {
            Toast.makeText(this, "Por favor ingrese un código para buscar.", Toast.LENGTH_SHORT).show();
            return;
        }

        helper.abrir();

        try {
            Cursor cursor = helper.getDb().query("edificios", null, "codigo = ?", new String[]{codBuscar}, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                editNombre.setText(cursor.getString(cursor.getColumnIndexOrThrow("nombre")));

                try {
                    editDireccion.setText(cursor.getString(cursor.getColumnIndexOrThrow("direccion")));
                } catch (Exception e) {
                    editDireccion.setText("");
                }

                int latIndex = cursor.getColumnIndexOrThrow("latitud");
                int lonIndex = cursor.getColumnIndexOrThrow("longitud");

                editLatitud.setText(cursor.isNull(latIndex) ? "" : String.valueOf(cursor.getDouble(latIndex)));
                editLongitud.setText(cursor.isNull(lonIndex) ? "" : String.valueOf(cursor.getDouble(lonIndex)));

                Toast.makeText(this, "Edificio localizado.", Toast.LENGTH_SHORT).show();
                cursor.close();
            } else {
                Toast.makeText(this, "No se encontró ningún edificio con ese código.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error al buscar: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } finally {
            helper.cerrar();
        }
    }

    private void actualizarEdificio() {
        String codigo = editBuscarCodigo.getText().toString().trim();
        String nombre = editNombre.getText().toString().trim();
        String direccion = editDireccion.getText().toString().trim(); // 🚀 Capturamos la dirección modificada
        String latStr = editLatitud.getText().toString().trim();
        String lonStr = editLongitud.getText().toString().trim();

        if (codigo.isEmpty() || nombre.isEmpty()) {
            Toast.makeText(this, "El código y el nombre son obligatorios.", Toast.LENGTH_SHORT).show();
            return;
        }

        Double latitud = latStr.isEmpty() ? null : Double.parseDouble(latStr);
        Double longitud = lonStr.isEmpty() ? null : Double.parseDouble(lonStr);

        int idEdificio = -1;
        helper.abrir();

        Cursor cursor = helper.getDb().query("edificios", new String[]{"id_edificio"}, "codigo = ?", new String[]{codigo}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            idEdificio = cursor.getInt(0);
            cursor.close();
        }

        if (idEdificio == -1) {
            Toast.makeText(this, "Error: No se pudo obtener el ID del edificio.", Toast.LENGTH_SHORT).show();
            helper.cerrar();
            return;
        }

        // 🚀 Acomodamos 'direccion' respetando el orden exacto de los parámetros requeridos
        String resultado = helper.actualizarEdificio(idEdificio, nombre, codigo, direccion, latitud, longitud);
        helper.cerrar();

        Toast.makeText(this, resultado, Toast.LENGTH_LONG).show();
        if (resultado.contains("correctamente")) {
            finish();
        }
    }
}