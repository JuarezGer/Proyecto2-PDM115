package ues.fia.proyecto2_pdm115.laboratorio;

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

public class EliminarLaboratoriosActivity extends AppCompatActivity {

    private controlDBLabCare helper;
    private EditText editBuscarCodigo;
    private TextView txtNombre, txtPiso, txtCoordenadas;
    private Button btnBuscar, btnEliminar, btnVolver;
    private int idLabSeleccionado = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eliminar_laboratorios);

        helper = new controlDBLabCare(this);

        editBuscarCodigo = findViewById(R.id.editEliminarCodLab);
        txtNombre = findViewById(R.id.txtEliminarNombreLab);
        txtPiso = findViewById(R.id.txtEliminarPisoLab);
        txtCoordenadas = findViewById(R.id.txtEliminarCoordenadasLab);

        btnBuscar = findViewById(R.id.btnBuscarEliminarLab);
        btnEliminar = findViewById(R.id.btnConfirmarEliminarLab);
        btnVolver = findViewById(R.id.btnVolverEliminarLab);

        btnBuscar.setOnClickListener(v -> buscarLaboratorio());
        btnEliminar.setOnClickListener(v -> eliminarLaboratorio());
        btnVolver.setOnClickListener(v -> finish());
    }

    private void buscarLaboratorio() {
        String codBuscar = editBuscarCodigo.getText().toString().trim();
        if (codBuscar.isEmpty()) {
            Toast.makeText(this, "Por favor ingrese un código.", Toast.LENGTH_SHORT).show();
            return;
        }

        helper.abrir();
        try {
            Cursor cursor = helper.getDb().query("laboratorios", null, "codigo = ?", new String[]{codBuscar}, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                // Usamos id_lab que es la llave primaria en el script de tu grupo
                idLabSeleccionado = cursor.getInt(cursor.getColumnIndexOrThrow("id_laboratorio"));
                String nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"));
                String piso = cursor.getString(cursor.getColumnIndexOrThrow("piso"));

                Double lat = cursor.isNull(cursor.getColumnIndexOrThrow("latitud")) ? null : cursor.getDouble(cursor.getColumnIndexOrThrow("latitud"));
                Double lon = cursor.isNull(cursor.getColumnIndexOrThrow("longitud")) ? null : cursor.getDouble(cursor.getColumnIndexOrThrow("longitud"));
                String coords = (lat != null && lon != null) ? "\n" + "Latitud: " + lat + "\n" +"Longitud: " + lon : "No asignadas";

                txtNombre.setText("Laboratorio: " + nombre);
                txtPiso.setText("Ubicado en: " + piso);
                txtCoordenadas.setText("Coordenadas GPS: " + coords);

                Toast.makeText(this, "Laboratorio listo para remover.", Toast.LENGTH_SHORT).show();
                cursor.close();
            } else {
                idLabSeleccionado = -1;
                txtNombre.setText("Laboratorio: (No encontrado)");
                txtPiso.setText("Ubicado en: (No encontrado)");
                txtCoordenadas.setText("Coordenadas GPS: (No encontrado)");
                Toast.makeText(this, "No se encontró el laboratorio.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error en los campos al buscar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            helper.cerrar();
        }
    }

    private void eliminarLaboratorio() {
        if (idLabSeleccionado == -1) {
            Toast.makeText(this, "Busque un laboratorio válido primero.", Toast.LENGTH_SHORT).show();
            return;
        }

        helper.abrir();
        // Llamamos al método del helper de tu grupo
        String resultado = helper.eliminarLaboratorio(idLabSeleccionado);
        helper.cerrar();

        Toast.makeText(this, resultado, Toast.LENGTH_LONG).show();
        if (resultado.contains("correctamente") || !resultado.contains("Error")) {
            finish();
        }
    }
}