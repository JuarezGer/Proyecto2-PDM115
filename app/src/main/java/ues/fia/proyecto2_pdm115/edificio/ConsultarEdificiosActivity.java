package ues.fia.proyecto2_pdm115.edificio;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import ues.fia.proyecto2_pdm115.R;
import ues.fia.proyecto2_pdm115.controlDBLabCare;

import android.database.Cursor;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ConsultarEdificiosActivity extends AppCompatActivity {

    private controlDBLabCare helper;
    private ListView listViewEdificios;
    private TextView txtTotal, txtMensaje;
    private Button btnActualizar, btnVolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consultar_edificios);

        helper = new controlDBLabCare(this);

        listViewEdificios = findViewById(R.id.listEdificios);
        txtTotal = findViewById(R.id.txtTotalEdificios);
        txtMensaje = findViewById(R.id.txtMensajeEdificios);
        btnActualizar = findViewById(R.id.btnActualizarListaEdificios);
        btnVolver = findViewById(R.id.btnVolverEdificios);

        cargarEdificios();

        btnActualizar.setOnClickListener(v -> cargarEdificios());
        btnVolver.setOnClickListener(v -> finish());
    }

    private void cargarEdificios() {
        helper.abrir();
        Cursor cursor = helper.consultarEdificiosCursor();

        List<String> listaFormat = new ArrayList<>();
        int total = 0;

        if (cursor != null) {
            total = cursor.getCount();
            if (cursor.moveToFirst()) {
                do {
                    String nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"));
                    String codigo = cursor.getString(cursor.getColumnIndexOrThrow("codigo"));

                    int idxDireccion = cursor.getColumnIndexOrThrow("direccion");
                    String direccion = cursor.isNull(idxDireccion) ? "No especificada" : cursor.getString(idxDireccion);

                    int idxLat = cursor.getColumnIndexOrThrow("latitud");
                    int idxLon = cursor.getColumnIndexOrThrow("longitud");

                    Double latitud = cursor.isNull(idxLat) ? null : cursor.getDouble(idxLat);
                    Double longitud = cursor.isNull(idxLon) ? null : cursor.getDouble(idxLon);

                    String latStr = (latitud != null) ? String.valueOf(latitud) : "No asignada";
                    String lonStr = (longitud != null) ? String.valueOf(longitud) : "No asignada";

                    String fichaEdificio = "Código: " + codigo + "\n" +
                            "Nombre del Edificio: " + nombre + "\n" +
                            "Dirección: " + direccion + "\n" +
                            "Ubicación Geográfica:\n" + "Latitud: " + latStr +"\n" + "Longitud: " + lonStr;

                    listaFormat.add(fichaEdificio);

                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        helper.cerrar();

        txtTotal.setText("Total: " + total);

        if (total == 0) {
            txtMensaje.setText("No hay edificios registrados en la base de datos.");
            txtMensaje.setVisibility(View.VISIBLE);
        } else {
            txtMensaje.setVisibility(View.GONE);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaFormat);
        listViewEdificios.setAdapter(adapter);
    }
}