package ues.fia.proyecto2_pdm115.indicencia;

import android.app.AlertDialog;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;

import ues.fia.proyecto2_pdm115.R;
import ues.fia.proyecto2_pdm115.controlDBLabCare;

public class EliminarIncidenciaActivity extends AppCompatActivity {

    private Spinner spIncidencia;
    private TextView txtDetalle;
    private TextView txtMensaje;
    private Button btnEliminar;
    private Button btnLimpiar;
    private Button btnVolver;

    private controlDBLabCare db;
    private ArrayList<IncidenciaItem> listaIncidencias;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eliminar_incidencia);

        enlazarVistas();
        abrirBaseDatos();
        cargarIncidencias();
        configurarEventos();
    }

    private void enlazarVistas() {
        spIncidencia = findViewById(R.id.spIncidenciaEliminar);
        txtDetalle = findViewById(R.id.txtDetalleIncidenciaEliminar);
        txtMensaje = findViewById(R.id.txtMensajeEliminarIncidencia);
        btnEliminar = findViewById(R.id.btnEliminarIncidencia);
        btnLimpiar = findViewById(R.id.btnLimpiarEliminarIncidencia);
        btnVolver = findViewById(R.id.btnVolverEliminarIncidencia);
    }

    private void abrirBaseDatos() {
        db = new controlDBLabCare(this);
        try {
            db.abrir();
        } catch (SQLException e) {
            mostrarMensaje("Error al abrir la base de datos: " + e.getMessage());
        } catch (Exception e) {
            mostrarMensaje("Error: " + e.getMessage());
        }
    }

    private void configurarEventos() {
        spIncidencia.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (listaIncidencias != null && !listaIncidencias.isEmpty()) {
                    mostrarDetalle(listaIncidencias.get(position).id);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        btnEliminar.setOnClickListener(v -> confirmarEliminacion());
        btnLimpiar.setOnClickListener(v -> txtDetalle.setText("Seleccione una incidencia para eliminar."));
        btnVolver.setOnClickListener(v -> finish());
    }

    private void cargarIncidencias() {
        listaIncidencias = new ArrayList<>();
        Cursor cursor = null;

        try {
            cursor = db.consultarIncidenciasCursor();
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("id_incidencia"));
                    String titulo = cursor.getString(cursor.getColumnIndexOrThrow("titulo"));
                    String estado = cursor.getString(cursor.getColumnIndexOrThrow("estado_incidencia"));
                    listaIncidencias.add(new IncidenciaItem(id, titulo, estado));
                } while (cursor.moveToNext());
            }

            ArrayAdapter<IncidenciaItem> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listaIncidencias);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spIncidencia.setAdapter(adapter);

            if (listaIncidencias.isEmpty()) {
                txtDetalle.setText("No hay incidencias registradas.");
                btnEliminar.setEnabled(false);
            } else {
                btnEliminar.setEnabled(true);
                mostrarDetalle(listaIncidencias.get(0).id);
            }
        } catch (Exception e) {
            mostrarMensaje("Error al cargar incidencias: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private void mostrarDetalle(int idIncidencia) {
        HashMap<String, String> datos = db.consultarIncidencia(idIncidencia);
        if (datos == null) {
            txtDetalle.setText("No se encontró la incidencia seleccionada.");
            return;
        }

        txtDetalle.setText(
                "ID: " + valor(datos, "id_incidencia") + "\n" +
                        "Título: " + valor(datos, "titulo") + "\n" +
                        "Equipo: " + valor(datos, "equipo") + "\n" +
                        "Tipo: " + valor(datos, "tipo_incidencia") + "\n" +
                        "Prioridad: " + valor(datos, "prioridad") + "\n" +
                        "Estado: " + valor(datos, "estado_incidencia") + "\n" +
                        "Fecha: " + valor(datos, "fecha_reporte")
        );
    }

    private void confirmarEliminacion() {
        if (listaIncidencias == null || listaIncidencias.isEmpty()) {
            mostrarMensaje("No hay registros para eliminar.");
            return;
        }

        IncidenciaItem seleccionado = (IncidenciaItem) spIncidencia.getSelectedItem();

        new AlertDialog.Builder(this)
                .setTitle("Confirmar eliminación")
                .setMessage("¿Está seguro de eliminar la incidencia?\n\n" + seleccionado.titulo)
                .setPositiveButton("Sí, eliminar", (dialog, which) -> eliminarIncidencia(seleccionado.id))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarIncidencia(int idIncidencia) {
        String mensaje = db.eliminarIncidencia(idIncidencia);
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
        mostrarMensaje(mensaje);
        cargarIncidencias();
    }

    private String valor(HashMap<String, String> datos, String clave) {
        String valor = datos.get(clave);
        return valor == null ? "" : valor;
    }

    private void mostrarMensaje(String mensaje) {
        txtMensaje.setText(mensaje);
        txtMensaje.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null) {
            db.cerrar();
            db = null;
        }
    }

    private static class IncidenciaItem {
        int id;
        String titulo;
        String estado;

        IncidenciaItem(int id, String titulo, String estado) {
            this.id = id;
            this.titulo = titulo;
            this.estado = estado;
        }

        @Override
        public String toString() {
            return id + " - " + titulo + " (" + estado + ")";
        }
    }
}
