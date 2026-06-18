package ues.fia.proyecto2_pdm115.edificio;

import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import ues.fia.proyecto2_pdm115.R;
import ues.fia.proyecto2_pdm115.controlDBLabCare;

public class ConsultarEdificiosActivity extends AppCompatActivity {

    private controlDBLabCare helper;
    private ListView listViewEdificios;
    private TextView txtTotal;
    private TextView txtMensaje;
    private Button btnActualizar;
    private Button btnVolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consultar_edificios);

        helper = new controlDBLabCare(this);

        vincularVistas();
        configurarEventos();
        cargarEdificios();
    }

    private void vincularVistas() {
        listViewEdificios = findViewById(R.id.listEdificios);
        txtTotal = findViewById(R.id.txtTotalEdificios);
        txtMensaje = findViewById(R.id.txtMensajeEdificios);
        btnActualizar = findViewById(R.id.btnActualizarListaEdificios);
        btnVolver = findViewById(R.id.btnVolverEdificios);
    }

    private void configurarEventos() {
        btnActualizar.setOnClickListener(v -> cargarEdificios());
        btnVolver.setOnClickListener(v -> finish());
    }

    private void cargarEdificios() {
        List<String> listaFormat = new ArrayList<>();
        Cursor cursor = null;
        int total = 0;

        try {
            helper.abrir();
            cursor = helper.consultarEdificiosCursor();

            if (cursor != null) {
                total = cursor.getCount();

                if (cursor.moveToFirst()) {
                    do {
                        int id = cursor.getInt(cursor.getColumnIndexOrThrow("id_edificio"));
                        String nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"));
                        String codigo = cursor.getString(cursor.getColumnIndexOrThrow("codigo"));

                        int idxLat = cursor.getColumnIndexOrThrow("latitud");
                        int idxLon = cursor.getColumnIndexOrThrow("longitud");

                        String latStr = cursor.isNull(idxLat) ? "No asignada" : String.valueOf(cursor.getDouble(idxLat));
                        String lonStr = cursor.isNull(idxLon) ? "No asignada" : String.valueOf(cursor.getDouble(idxLon));

                        String fichaEdificio = "ID: " + id + "\n" +
                                "Código: " + codigo + "\n" +
                                "Nombre del edificio: " + nombre + "\n" +
                                "Ubicación geográfica:\n" +
                                "Latitud: " + latStr + "\n" +
                                "Longitud: " + lonStr;

                        listaFormat.add(fichaEdificio);
                    } while (cursor.moveToNext());
                }
            }

            txtTotal.setText("Total: " + total);

            if (total == 0) {
                txtMensaje.setText("No hay edificios registrados en la base de datos.");
                txtMensaje.setVisibility(View.VISIBLE);
            } else {
                txtMensaje.setVisibility(View.GONE);
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_list_item_1,
                    listaFormat
            );
            listViewEdificios.setAdapter(adapter);

        } catch (SQLException e) {
            Toast.makeText(this, "Error al abrir la base de datos: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error al cargar edificios: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            if (cursor != null) cursor.close();
            helper.cerrar();
        }
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
