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
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class ConsultarLaboratorioActivity extends AppCompatActivity {

    private controlDBLabCare helper;
    private ListView listViewLaboratorios;
    private TextView txtTotal, txtMensaje;
    private Button btnActualizar, btnVolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consultar_laboratorio);

        helper = new controlDBLabCare(this);

        listViewLaboratorios = findViewById(R.id.listLaboratorios);
        txtTotal = findViewById(R.id.txtTotalLaboratorios);
        txtMensaje = findViewById(R.id.txtMensajeLaboratorios);
        btnActualizar = findViewById(R.id.btnActualizarListaLaboratorios);
        btnVolver = findViewById(R.id.btnVolverLaboratorios);

        cargarLaboratorios();

        btnActualizar.setOnClickListener(v -> cargarLaboratorios());
        btnVolver.setOnClickListener(v -> finish());
    }

    private void cargarLaboratorios() {
        helper.abrir();
        Cursor cursor = helper.consultarLaboratoriosCursor();

        List<String> listaFormat = new ArrayList<>();
        int total = 0;

        if (cursor != null) {
            total = cursor.getCount();
            if (cursor.moveToFirst()) {
                do {

                    String labNombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"));
                    String labCodigo = cursor.getString(cursor.getColumnIndexOrThrow("codigo"));

                    String piso = cursor.getString(cursor.getColumnIndexOrThrow("piso"));


                    String edificioPertenece = "";
                    try {
                        edificioPertenece = cursor.getString(cursor.getColumnIndexOrThrow("edificio"));
                    } catch (Exception e) {
                        // Por si en el query de tu grupo la columna se llama diferente (ej: nombre_edificio)
                        int index = cursor.getColumnIndex("nombre_edificio");
                        if(index != -1) edificioPertenece = cursor.getString(index);
                        else edificioPertenece = "No especificado";
                    }

                    // Ubicación geográfica del laboratorio
                    Double latitud = cursor.isNull(cursor.getColumnIndexOrThrow("latitud")) ? null : cursor.getDouble(cursor.getColumnIndexOrThrow("latitud"));
                    Double longitud = cursor.isNull(cursor.getColumnIndexOrThrow("longitud")) ? null : cursor.getDouble(cursor.getColumnIndexOrThrow("longitud"));

                    String latStr = (latitud != null) ? String.valueOf(latitud) : "No asignada";
                    String lonStr = (longitud != null) ? String.valueOf(longitud) : "No asignada";

                    // Construimos la ficha detallada del laboratorio con saltos de línea (\n)
                    String fichaLaboratorio = "Código: " + labCodigo + "\n" +
                            "Laboratorio: " + labNombre + "\n" +
                             piso + "\n" +"Pertenece a: " + edificioPertenece + "\n" +
                            "Ubicación Geográfica:" + "\n" +"Latitud: " + latStr + "\n" +"Longitud: " + lonStr;

                    listaFormat.add(fichaLaboratorio);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        helper.cerrar();

        // Actualizar estadísticas superiores (Total: X)
        txtTotal.setText("Total: " + total);

        if (total == 0) {
            txtMensaje.setText("No hay laboratorios registrados en la base de datos.");
            txtMensaje.setVisibility(View.VISIBLE);
        } else {
            txtMensaje.setVisibility(View.GONE);
        }

        // Adaptador para inflar el texto multilínea en el ListView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaFormat);
        listViewLaboratorios.setAdapter(adapter);
    }
}