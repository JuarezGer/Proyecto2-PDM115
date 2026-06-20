package ues.fia.proyecto2_pdm115.edificio;

import android.app.AlertDialog;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import ues.fia.proyecto2_pdm115.R;
import ues.fia.proyecto2_pdm115.controlDBLabCare;

public class EliminarEdificiosActivity extends AppCompatActivity {

    private controlDBLabCare helper;

    private EditText editBuscarCodigo;
    private TextView txtNombre;
    private TextView txtDireccion;
    private TextView txtCoordenadas;

    private Button btnBuscar;
    private Button btnEliminar;
    private Button btnVolver;

    private int idEdificioSeleccionado = -1;
    private String nombreEdificioSeleccionado = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eliminar_edificios);

        helper = new controlDBLabCare(this);

        vincularVistas();
        configurarEventos();
        limpiarDetalle();
    }

    private void vincularVistas() {
        editBuscarCodigo = findViewById(R.id.editEliminarCodEdificio);
        txtNombre = findViewById(R.id.txtEliminarNombreEdificio);
        txtDireccion = findViewById(R.id.txtEliminarDireccionEdificio);
        txtCoordenadas = findViewById(R.id.txtEliminarCoordenadasEdificio);

        btnBuscar = findViewById(R.id.btnBuscarEliminarEdificio);
        btnEliminar = findViewById(R.id.btnConfirmarEliminarEdificio);
        btnVolver = findViewById(R.id.btnVolverEliminarEdificio);
    }

    private void configurarEventos() {
        btnBuscar.setOnClickListener(v -> buscarEdificio());
        btnEliminar.setOnClickListener(v -> confirmarEliminacion());
        btnVolver.setOnClickListener(v -> finish());
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
                nombreEdificioSeleccionado = cursor.getString(cursor.getColumnIndexOrThrow("nombre"));

                int idxLat = cursor.getColumnIndexOrThrow("latitud");
                int idxLon = cursor.getColumnIndexOrThrow("longitud");

                String lat = cursor.isNull(idxLat) ? "No asignada" : String.valueOf(cursor.getDouble(idxLat));
                String lon = cursor.isNull(idxLon) ? "No asignada" : String.valueOf(cursor.getDouble(idxLon));

                txtNombre.setText("Nombre: " + nombreEdificioSeleccionado);

                // Tu tabla edificios actual no tiene campo direccion.
                txtDireccion.setText("Dirección: No disponible en la tabla actual");

                txtCoordenadas.setText("Ubicación:\nLatitud: " + lat + "\nLongitud: " + lon);

                btnEliminar.setEnabled(true);
                Toast.makeText(this, "Edificio cargado para eliminación.", Toast.LENGTH_SHORT).show();
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

    private void confirmarEliminacion() {
        if (idEdificioSeleccionado == -1) {
            Toast.makeText(this, "Debe buscar y seleccionar un edificio válido primero.", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Confirmar eliminación")
                .setMessage("¿Está seguro de eliminar el edificio?\n\n" + nombreEdificioSeleccionado)
                .setPositiveButton("Sí, eliminar", (dialog, which) -> eliminarEdificio())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarEdificio() {
        try {
            helper.abrir();

            String resultado = helper.eliminarEdificio(idEdificioSeleccionado);
            Toast.makeText(this, resultado, Toast.LENGTH_LONG).show();

            if (resultado.toLowerCase().contains("correctamente")) {
                editBuscarCodigo.setText("");
                limpiarDetalle();
                editBuscarCodigo.requestFocus();
            }
        } catch (SQLException e) {
            Toast.makeText(this, "Error al abrir la base de datos: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error al eliminar edificio: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            helper.cerrar();
        }
    }

    private void limpiarDetalle() {
        idEdificioSeleccionado = -1;
        nombreEdificioSeleccionado = "";
        txtNombre.setText("Nombre: ");
        txtDireccion.setText("Dirección: ");
        txtCoordenadas.setText("Ubicación: ");
        btnEliminar.setEnabled(false);
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
