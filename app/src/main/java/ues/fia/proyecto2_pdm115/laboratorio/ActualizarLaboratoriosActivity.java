package ues.fia.proyecto2_pdm115.laboratorio;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import ues.fia.proyecto2_pdm115.R;
import ues.fia.proyecto2_pdm115.controlDBLabCare;
import android.database.Cursor;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import ues.fia.proyecto2_pdm115.VerMapaActivity;


public class ActualizarLaboratoriosActivity extends AppCompatActivity {

    private controlDBLabCare helper;
    private EditText editBuscarCodigo, editNombre, editPiso, editLatitud, editLongitud;
    private Button btnBuscar, btnGpsSimular, btnActualizar, btnVolver;
    private int idEdificioAsociado = -1;
    private ActivityResultLauncher<Intent> mapaLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actualizar_laboratorios);

        helper = new controlDBLabCare(this);

        editBuscarCodigo = findViewById(R.id.editBuscarCodLab);
        editNombre = findViewById(R.id.editActNombreLab);
        editPiso = findViewById(R.id.editActPisoLab);
        editLatitud = findViewById(R.id.editActLatLab);
        editLongitud = findViewById(R.id.editActLonLab);

        btnBuscar = findViewById(R.id.btnBuscarLab);
        btnGpsSimular = findViewById(R.id.btnGpsActLab);
        btnActualizar = findViewById(R.id.btnConfirmarActLab);
        btnVolver = findViewById(R.id.btnVolverActLab);


        mapaLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {

                        double lat = result.getData().getDoubleExtra("LATITUD", 0.0);
                        double lon = result.getData().getDoubleExtra("LONGITUD", 0.0);


                        editLatitud.setText(String.valueOf(lat));
                        editLongitud.setText(String.valueOf(lon));

                        Toast.makeText(this, "Nuevas coordenadas del laboratorio cargadas.", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        btnBuscar.setOnClickListener(v -> buscarLaboratorio());


        btnGpsSimular.setOnClickListener(v -> {
            Intent intent = new Intent(this, VerMapaActivity.class);
            mapaLauncher.launch(intent);
        });

        btnActualizar.setOnClickListener(v -> actualizarLaboratorio());
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

                editNombre.setText(cursor.getString(cursor.getColumnIndexOrThrow("nombre")));

                editPiso.setText(cursor.getString(cursor.getColumnIndexOrThrow("piso")));

                idEdificioAsociado = cursor.getInt(cursor.getColumnIndexOrThrow("id_edificio"));

                int latIndex = cursor.getColumnIndexOrThrow("latitud");
                int lonIndex = cursor.getColumnIndexOrThrow("longitud");
                editLatitud.setText(cursor.isNull(latIndex) ? "" : String.valueOf(cursor.getDouble(latIndex)));
                editLongitud.setText(cursor.isNull(lonIndex) ? "" : String.valueOf(cursor.getDouble(lonIndex)));

                Toast.makeText(this, "Laboratorio localizado.", Toast.LENGTH_SHORT).show();
                cursor.close();
            } else {
                Toast.makeText(this, "No se encontró el laboratorio.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error en los campos al buscar: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } finally {
            helper.cerrar();
        }
    }

    private void actualizarLaboratorio() {
        try {
            String codigo = editBuscarCodigo.getText().toString().trim();
            String nombre = editNombre.getText().toString().trim();
            String pisoStr = editPiso.getText().toString().trim();
            String latStr = editLatitud.getText().toString().trim();
            String lonStr = editLongitud.getText().toString().trim();

            if (codigo.isEmpty() || nombre.isEmpty() || idEdificioAsociado == -1) {
                Toast.makeText(this, "Complete los campos y busque un laboratorio válido primero.", Toast.LENGTH_SHORT).show();
                return;
            }

            Double latitud = null;
            Double longitud = null;
            try {
                if (!latStr.isEmpty()) latitud = Double.parseDouble(latStr);
                if (!lonStr.isEmpty()) longitud = Double.parseDouble(lonStr);
            } catch (NumberFormatException nfe) {
                Toast.makeText(this, "Las coordenadas ingresadas no tienen un formato numérico válido.", Toast.LENGTH_SHORT).show();
                return;
            }

            int idLaboratorio = -1;
            helper.abrir();

            Cursor cursor = helper.getDb().query("laboratorios", null, "codigo = ?", new String[]{codigo}, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                idLaboratorio = cursor.getInt(cursor.getColumnIndexOrThrow("id_laboratorio"));
                cursor.close();
            }

            if (idLaboratorio == -1) {
                Toast.makeText(this, "Error: No se encontró el ID único de este laboratorio.", Toast.LENGTH_SHORT).show();
                helper.cerrar();
                return;
            }

            String resultado = helper.actualizarLaboratorio(
                    idLaboratorio,
                    idEdificioAsociado,
                    nombre,
                    codigo,
                    pisoStr,
                    latitud,
                    longitud
            );
            helper.cerrar();

            Toast.makeText(this, resultado, Toast.LENGTH_LONG).show();
            if (resultado.contains("correctamente")) {
                finish();
            }

        } catch (Exception e) {
            Toast.makeText(this, "Fallo crítico al actualizar: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
            if (helper != null) {
                helper.cerrar();
            }
        }
    }
}