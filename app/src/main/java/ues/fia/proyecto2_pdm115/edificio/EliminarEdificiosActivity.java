package ues.fia.proyecto2_pdm115.edificio;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import ues.fia.proyecto2_pdm115.R;
import android.database.Cursor;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import ues.fia.proyecto2_pdm115.controlDBLabCare;

public class EliminarEdificiosActivity extends AppCompatActivity {

    private controlDBLabCare helper;
    private EditText editBuscarCodigo;
    private TextView txtNombre, txtDireccion, txtCoordenadas;
    private Button btnBuscar, btnEliminar, btnVolver;
    private int idEdificioSeleccionado = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eliminar_edificios);

        helper = new controlDBLabCare(this);

        editBuscarCodigo = findViewById(R.id.editEliminarCodEdificio);
        txtNombre = findViewById(R.id.txtEliminarNombreEdificio);
        txtDireccion = findViewById(R.id.txtEliminarDireccionEdificio);
        txtCoordenadas = findViewById(R.id.txtEliminarCoordenadasEdificio);

        btnBuscar = findViewById(R.id.btnBuscarEliminarEdificio);
        btnEliminar = findViewById(R.id.btnConfirmarEliminarEdificio);
        btnVolver = findViewById(R.id.btnVolverEliminarEdificio);

        btnBuscar.setOnClickListener(v -> buscarEdificio());
        btnEliminar.setOnClickListener(v -> eliminarEdificio());
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
                idEdificioSeleccionado = cursor.getInt(cursor.getColumnIndexOrThrow("id_edificio"));
                String nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"));

                String direccion = "No asignada";
                try {
                    direccion = cursor.getString(cursor.getColumnIndexOrThrow("direccion"));
                    if(direccion == null || direccion.isEmpty()) direccion = "No asignada";
                } catch(Exception e) { }

                Double lat = cursor.isNull(cursor.getColumnIndexOrThrow("latitud")) ? null : cursor.getDouble(cursor.getColumnIndexOrThrow("latitud"));
                Double lon = cursor.isNull(cursor.getColumnIndexOrThrow("longitud")) ? null : cursor.getDouble(cursor.getColumnIndexOrThrow("longitud"));
                String coords = (lat != null && lon != null) ?  "\n" + "Latitud: " + lat +  "\n" +"Longitud: " + lon : "No asignadas";

                txtNombre.setText("Nombre: " + nombre);
                txtDireccion.setText("Dirección: " + direccion);
                txtCoordenadas.setText("Ubicación: " + coords);

                Toast.makeText(this, "Edificio cargado para eliminación.", Toast.LENGTH_SHORT).show();
                cursor.close();
            } else {
                idEdificioSeleccionado = -1;
                txtNombre.setText("Nombre: (No encontrado)");
                txtDireccion.setText("Dirección: (No encontrado)");
                txtCoordenadas.setText("Ubicación: (No encontrado)");
                Toast.makeText(this, "No se encontró ningún edificio con ese código.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error al buscar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            helper.cerrar();
        }
    }

    private void eliminarEdificio() {
        if (idEdificioSeleccionado == -1) {
            Toast.makeText(this, "Debe buscar y seleccionar un edificio válido primero.", Toast.LENGTH_SHORT).show();
            return;
        }

        helper.abrir();

        String resultado = helper.eliminarEdificio(idEdificioSeleccionado);
        helper.cerrar();

        Toast.makeText(this, resultado, Toast.LENGTH_LONG).show();
        if (resultado.contains("correctamente") || !resultado.contains("Error")) {
            finish();
        }
    }
}