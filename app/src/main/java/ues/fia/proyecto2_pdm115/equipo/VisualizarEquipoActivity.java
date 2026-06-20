package ues.fia.proyecto2_pdm115.equipo;

import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;


import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import ues.fia.proyecto2_pdm115.R;
import ues.fia.proyecto2_pdm115.controlDBLabCare;

public class VisualizarEquipoActivity extends AppCompatActivity {

    private ListView listEquipos;
    private TextView txtTotalEquipos;
    private TextView txtMensajeVisualizarEquipo;
    private Button btnActualizarListaEquipo;
    private Button btnVolverVisualizarEquipo;

    private controlDBLabCare db;

    private final ArrayList<EquipoAdapter.Equipo> datosPantalla =
            new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualizar_equipo);

        enlazarVistas();
        abrirBaseDatos();
        configurarEventos();
        cargarEquipos();
    }
    private void enlazarVistas() {

        listEquipos = findViewById(R.id.listEquipos);

        txtTotalEquipos = findViewById(R.id.txtTotalEquipos);

        txtMensajeVisualizarEquipo =
                findViewById(R.id.txtMensajeVisualizarEquipo);

        btnActualizarListaEquipo =
                findViewById(R.id.btnActualizarListaEquipo);

        btnVolverVisualizarEquipo =
                findViewById(R.id.btnVolverVisualizarEquipo);

    }
    private void abrirBaseDatos() {

        db = new controlDBLabCare(this);

        try {

            db.abrir();

        } catch (SQLException e) {

            mostrarMensaje(
                    "Error al abrir la base de datos: "
                            + e.getMessage()
            );

        } catch (Exception e) {

            mostrarMensaje(
                    "Error: " + e.getMessage()
            );

        }

    }
    private void configurarEventos() {

        btnActualizarListaEquipo.setOnClickListener(
                v -> cargarEquipos()
        );

        btnVolverVisualizarEquipo.setOnClickListener(
                v -> finish()
        );

    }
    private void mostrarMensaje(String mensaje) {

        txtMensajeVisualizarEquipo.setText(mensaje);

        txtMensajeVisualizarEquipo.setVisibility(
                View.VISIBLE
        );
    }
    @Override
    protected void onDestroy() {

        super.onDestroy();

        if (db != null) {

            db.cerrar();
            db = null;

        }

    }
    private void cargarEquipos() {

        datosPantalla.clear();

        Cursor cursor = null;

        try {

            if (db == null || db.getDb() == null || !db.getDb().isOpen()) {
                abrirBaseDatos();
            }

            cursor = db.consultarEquiposCursor();

            if (cursor != null && cursor.moveToFirst()) {

                do {

                    int id = cursor.getInt(
                            cursor.getColumnIndexOrThrow("id_equipo")
                    );

                    String codigoInventario = cursor.getString(
                            cursor.getColumnIndexOrThrow("codigo_inventario")
                    );

                    String codigoQr = cursor.getString(
                            cursor.getColumnIndexOrThrow("codigo_qr")
                    );

                    String nombre = cursor.getString(
                            cursor.getColumnIndexOrThrow("nombre")
                    );

                    String marca = cursor.getString(
                            cursor.getColumnIndexOrThrow("marca")
                    );

                    String modelo = cursor.getString(
                            cursor.getColumnIndexOrThrow("modelo")
                    );

                    String estado = cursor.getString(
                            cursor.getColumnIndexOrThrow("estado_equipo")
                    );

                    String laboratorio = cursor.getString(
                            cursor.getColumnIndexOrThrow("laboratorio")
                    );

                    String categoria = cursor.getString(
                            cursor.getColumnIndexOrThrow("categoria")
                    );

                    String edificio = cursor.getString(
                            cursor.getColumnIndexOrThrow("edificio")
                    );

                    datosPantalla.add(

                            new EquipoAdapter.Equipo(
                                    id,
                                    codigoInventario,
                                    codigoQr,
                                    nombre,
                                    marca,
                                    modelo,
                                    estado,
                                    laboratorio,
                                    categoria,
                                    edificio
                            )

                    );

                } while (cursor.moveToNext());

            }

            EquipoAdapter adapter =
                    new EquipoAdapter(this, datosPantalla);

            listEquipos.setAdapter(adapter);

            txtTotalEquipos.setText(
                    "Total: " + datosPantalla.size()
            );

            txtMensajeVisualizarEquipo.setVisibility(
                    View.GONE
            );

        } catch (Exception e) {

            mostrarMensaje(
                    "Error al cargar equipos: " + e.getMessage()
            );

        } finally {

            if (cursor != null) {
                cursor.close();
            }

        }

    }
}