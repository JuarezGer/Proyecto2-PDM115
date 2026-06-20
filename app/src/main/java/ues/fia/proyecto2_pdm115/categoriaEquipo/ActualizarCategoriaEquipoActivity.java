package ues.fia.proyecto2_pdm115.categoriaEquipo;

import android.database.SQLException;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import ues.fia.proyecto2_pdm115.R;
import ues.fia.proyecto2_pdm115.controlDBLabCare;

public class ActualizarCategoriaEquipoActivity extends AppCompatActivity {

    private EditText edtNombre;
    private Button btnActualizar;
    private Button btnVolver;

    private controlDBLabCare db;

    private int idCategoria;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actualizar_categoria_equipo);

        edtNombre = findViewById(R.id.edtNombreCategoriaActualizar);
        btnActualizar = findViewById(R.id.btnActualizarCategoria);
        btnVolver = findViewById(R.id.btnVolverActualizarCategoria);

        db = new controlDBLabCare(this);

        try {
            db.abrir();
        } catch (SQLException e) {
            Toast.makeText(this,
                    "Error al abrir base de datos: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }

        idCategoria = getIntent().getIntExtra("id_categoria", -1);
        String nombre = getIntent().getStringExtra("nombre");

        edtNombre.setText(nombre);

        btnActualizar.setOnClickListener(v -> actualizarCategoria());

        btnVolver.setOnClickListener(v -> finish());
    }

    private void actualizarCategoria() {

        String nuevoNombre = edtNombre.getText().toString().trim();

        if (nuevoNombre.isEmpty()) {
            edtNombre.setError("Ingrese el nombre");
            edtNombre.requestFocus();
            return;
        }

        String mensaje =
                db.actualizarCategoriaEquipo(idCategoria, nuevoNombre);

        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();

        if (mensaje.contains("correctamente")) {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (db != null) {
            db.cerrar();
        }
    }
}