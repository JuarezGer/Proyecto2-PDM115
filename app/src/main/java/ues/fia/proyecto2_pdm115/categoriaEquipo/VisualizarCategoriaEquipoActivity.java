package ues.fia.proyecto2_pdm115.categoriaEquipo;

import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import ues.fia.proyecto2_pdm115.R;
import ues.fia.proyecto2_pdm115.controlDBLabCare;

public class VisualizarCategoriaEquipoActivity extends AppCompatActivity {

    private ListView listCategoriasEquipo;
    private TextView txtTotalCategoriasEquipo;
    private TextView txtMensajeVisualizarCategoriaEquipo;
    private Button btnActualizarListaCategoriaEquipo;
    private Button btnVolverVisualizarCategoriaEquipo;

    private controlDBLabCare db;
    private final ArrayList<CategoriaEquipoAdapter.Categoria> datosPantalla = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualizar_categoria_equipo);

        enlazarVistas();
        abrirBaseDatos();
        configurarEventos();
        cargarCategoriasEquipo();
    }

    private void enlazarVistas() {
        listCategoriasEquipo = findViewById(R.id.listCategoriasEquipo);
        txtTotalCategoriasEquipo = findViewById(R.id.txtTotalCategoriasEquipo);
        txtMensajeVisualizarCategoriaEquipo = findViewById(R.id.txtMensajeVisualizarCategoriaEquipo);
        btnActualizarListaCategoriaEquipo = findViewById(R.id.btnActualizarListaCategoriaEquipo);
        btnVolverVisualizarCategoriaEquipo = findViewById(R.id.btnVolverVisualizarCategoriaEquipo);
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
        btnActualizarListaCategoriaEquipo.setOnClickListener(v -> cargarCategoriasEquipo());

        btnVolverVisualizarCategoriaEquipo.setOnClickListener(v -> finish());
    }

    private void cargarCategoriasEquipo() {
        datosPantalla.clear();
        Cursor cursor = null;

        try {

            if (db == null || db.getDb() == null || !db.getDb().isOpen()) {
                abrirBaseDatos();
            }

            cursor = db.consultarCategoriasEquipoCursor();

            if (cursor != null && cursor.moveToFirst()) {

                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("id_categoria"));
                    String nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"));

                    datosPantalla.add(
                            new CategoriaEquipoAdapter.Categoria(id, nombre)
                    );

                } while (cursor.moveToNext());

            }

            CategoriaEquipoAdapter adapter =
                    new CategoriaEquipoAdapter(this, datosPantalla);

            listCategoriasEquipo.setAdapter(adapter);

            txtTotalCategoriasEquipo.setText(
                    "Total: " + datosPantalla.size()
            );

            txtMensajeVisualizarCategoriaEquipo.setVisibility(View.GONE);

        } catch (Exception e) {

            mostrarMensaje("Error al cargar categorías: " + e.getMessage());

        } finally {

            if (cursor != null) {
                cursor.close();
            }

        }
    }

    private void mostrarMensaje(String mensaje) {
        txtMensajeVisualizarCategoriaEquipo.setText(mensaje);
        txtMensajeVisualizarCategoriaEquipo.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (db != null) {
            db.cerrar();
            db = null;
        }
    }
}