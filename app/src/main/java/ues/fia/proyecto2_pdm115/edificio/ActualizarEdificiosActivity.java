package ues.fia.proyecto2_pdm115.edificio;

import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import ues.fia.proyecto2_pdm115.R;
import ues.fia.proyecto2_pdm115.VerMapaActivity;
import ues.fia.proyecto2_pdm115.controlDBLabCare;

public class ActualizarEdificiosActivity extends AppCompatActivity {

    private controlDBLabCare helper;
    private ActivityResultLauncher<Intent> mapaLauncher;

    private EditText editBuscarCodigo;
    private EditText editNombre;
    private EditText editDireccion;
    private EditText editLatitud;
    private EditText editLongitud;

    private Button btnBuscar;
    private Button btnGpsSimular;
    private Button btnActualizar;
    private Button btnVolver;

    private int idEdificioSeleccionado = -1;
    private String codigoOriginal = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actualizar_edificios);

        helper = new controlDBLabCare(this);

        registrarResultadoMapa();
        vincularVistas();
        configurarBotones();
        bloquearCamposEdicion(false);
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

                        Toast.makeText(this, "Nuevas coordenadas cargadas desde el mapa.", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void vincularVistas() {
        editBuscarCodigo = findViewById(R.id.editBuscarCodEdificio);
        editNombre = findViewById(R.id.editActNombreEdificio);
        editDireccion = findViewById(R.id.editActDireccionEdificio);
        editLatitud = findViewById(R.id.editActLatEdificio);
        editLongitud = findViewById(R.id.editActLonEdificio);

        btnBuscar = findViewById(R.id.btnBuscarEdificio);
        btnGpsSimular = findViewById(R.id.btnGpsActEdificio);
        btnActualizar = findViewById(R.id.btnConfirmarActEdificio);
        btnVolver = findViewById(R.id.btnVolverActEdificio);
    }

    private void configurarBotones() {
        btnBuscar.setOnClickListener(v -> buscarEdificio());
        btnGpsSimular.setOnClickListener(v -> abrirMapa());
        btnActualizar.setOnClickListener(v -> actualizarEdificio());
        btnVolver.setOnClickListener(v -> finish());
    }

    private void abrirMapa() {
        try {
            Intent intent = new Intent(this, VerMapaActivity.class);
            mapaLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(this, "No se pudo abrir el mapa: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void buscarEdificio() {
        String codBuscar = editBuscarCodigo.getText().toString().trim();

        if (codBuscar.isEmpty()) {
            editBuscarCodigo.setError("Ingrese el código del edificio");
            editBuscarCodigo.requestFocus();
            return;
        }

        Cursor cursor = null;

        try {
            helper.abrir();

            cursor = helper.getDb().query(
                    "edificios",
                    null,
                    "codigo = ?",
                    new String[]{codBuscar},
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                idEdificioSeleccionado = cursor.getInt(cursor.getColumnIndexOrThrow("id_edificio"));
                codigoOriginal = cursor.getString(cursor.getColumnIndexOrThrow("codigo"));

                editNombre.setText(cursor.getString(cursor.getColumnIndexOrThrow("nombre")));

                // Tu tabla edificios actual no tiene campo direccion.
                // Se limpia para que el layout pueda mantenerse sin generar error SQL.
                if (editDireccion != null) editDireccion.setText("");

                int latIndex = cursor.getColumnIndexOrThrow("latitud");
                int lonIndex = cursor.getColumnIndexOrThrow("longitud");

                editLatitud.setText(cursor.isNull(latIndex) ? "" : String.valueOf(cursor.getDouble(latIndex)));
                editLongitud.setText(cursor.isNull(lonIndex) ? "" : String.valueOf(cursor.getDouble(lonIndex)));

                bloquearCamposEdicion(true);
                Toast.makeText(this, "Edificio localizado.", Toast.LENGTH_SHORT).show();
            } else {
                limpiarDetalle();
                Toast.makeText(this, "No se encontró ningún edificio con ese código.", Toast.LENGTH_SHORT).show();
            }
        } catch (SQLException e) {
            Toast.makeText(this, "Error al abrir la base de datos: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error al buscar: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            if (cursor != null) cursor.close();
            helper.cerrar();
        }
    }

    private void actualizarEdificio() {
        if (idEdificioSeleccionado == -1) {
            Toast.makeText(this, "Primero busque un edificio válido.", Toast.LENGTH_SHORT).show();
            return;
        }

        String nombre = editNombre.getText().toString().trim();
        String direccion = editDireccion.getText().toString().trim();
        String latStr = editLatitud.getText().toString().trim();
        String lonStr = editLongitud.getText().toString().trim();

        if (nombre.isEmpty()) {
            editNombre.setError("El nombre es obligatorio");
            editNombre.requestFocus();
            return;
        }

        Double latitud = convertirDoubleNullable(latStr, "latitud");
        Double longitud = convertirDoubleNullable(lonStr, "longitud");

        if (!latStr.isEmpty() && latitud == null) return;
        if (!lonStr.isEmpty() && longitud == null) return;

        try {
            helper.abrir();

            // Se conserva el código original para evitar cambiar la llave lógica buscada.
            // La tabla edificios actual usa: nombre, codigo, latitud y longitud.
            String resultado = helper.actualizarEdificio(
                    idEdificioSeleccionado,
                    nombre,
                    codigoOriginal,
                    direccion,
                    latitud,
                    longitud
            );

            Toast.makeText(this, resultado, Toast.LENGTH_LONG).show();

            if (resultado.toLowerCase().contains("correctamente")) {
                limpiarDetalle();
                editBuscarCodigo.setText("");
                editBuscarCodigo.requestFocus();
            }
        } catch (SQLException e) {
            Toast.makeText(this, "Error al abrir la base de datos: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error al actualizar edificio: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            helper.cerrar();
        }
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

    private void bloquearCamposEdicion(boolean habilitar) {
        editNombre.setEnabled(habilitar);
        if (editDireccion != null) editDireccion.setEnabled(false);
        editLatitud.setEnabled(habilitar);
        editLongitud.setEnabled(habilitar);
        btnGpsSimular.setEnabled(habilitar);
        btnActualizar.setEnabled(habilitar);
    }

    private void limpiarDetalle() {
        idEdificioSeleccionado = -1;
        codigoOriginal = "";
        editNombre.setText("");
        if (editDireccion != null) editDireccion.setText("");
        editLatitud.setText("");
        editLongitud.setText("");
        bloquearCamposEdicion(false);
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
