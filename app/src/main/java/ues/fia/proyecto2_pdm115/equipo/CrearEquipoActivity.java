package ues.fia.proyecto2_pdm115.equipo;

import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

import ues.fia.proyecto2_pdm115.R;
import ues.fia.proyecto2_pdm115.controlDBLabCare;


public class CrearEquipoActivity extends AppCompatActivity {

    private Spinner spLaboratorio;
    private Spinner spCategoria;
    private Spinner spEstado;
    private ArrayList<String> listaEstados;
    private EditText edtCodigoInventario;
    private EditText edtCodigoQr;
    private EditText edtNombre;
    private EditText edtMarca;
    private EditText edtModelo;

    private TextView txtMensaje;

    private Button btnGuardar;
    private Button btnLimpiar;
    private Button btnVolver;

    private controlDBLabCare db;

    private ArrayList<String> listaLaboratorios;
    private ArrayList<Integer> listaIdLaboratorios;

    private ArrayList<String> listaCategorias;
    private ArrayList<Integer> listaIdCategorias;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_equipo);

        enlazarVistas();
        abrirBaseDatos();
        configurarEventos();
        cargarLaboratorios();
        cargarCategorias();
        cargarEstados();
    }
    private void configurarEventos() {
        btnGuardar.setOnClickListener(v -> guardarEquipo());
        btnLimpiar.setOnClickListener(v -> limpiarCampos());
        btnVolver.setOnClickListener(v -> finish());
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

    private void mostrarMensaje(String mensaje) {
        txtMensaje.setText(mensaje);
        txtMensaje.setVisibility(View.VISIBLE);
    }
    private void enlazarVistas() {

        spLaboratorio = findViewById(R.id.spLaboratorioCrear);
        spCategoria = findViewById(R.id.spCategoriaCrear);

        edtCodigoInventario = findViewById(R.id.edtCodigoInventarioCrear);
        edtCodigoQr = findViewById(R.id.edtCodigoQrCrear);
        edtNombre = findViewById(R.id.edtNombreEquipoCrear);
        edtMarca = findViewById(R.id.edtMarcaCrear);
        edtModelo = findViewById(R.id.edtModeloCrear);
        spEstado = findViewById(R.id.spEstadoCrear);

        txtMensaje = findViewById(R.id.txtMensajeCrearEquipo);

        btnGuardar = findViewById(R.id.btnGuardarEquipo);
        btnLimpiar = findViewById(R.id.btnLimpiarEquipo);
        btnVolver = findViewById(R.id.btnVolverCrearEquipo);
    }

    private void cargarEstados() {

        listaEstados = new ArrayList<>();

        listaEstados.add("activo");
        listaEstados.add("en_mantenimiento");
        listaEstados.add("fuera_servicio");
        listaEstados.add("baja");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                listaEstados
        );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spEstado.setAdapter(adapter);
    }
    private void cargarCategorias() {

        listaCategorias = new ArrayList<>();
        listaIdCategorias = new ArrayList<>();

        Cursor cursor = null;

        try {

            cursor = db.consultarCategoriasEquipoCursor();

            if (cursor != null && cursor.moveToFirst()) {

                do {

                    listaIdCategorias.add(
                            cursor.getInt(cursor.getColumnIndexOrThrow("id_categoria"))
                    );

                    listaCategorias.add(
                            cursor.getString(cursor.getColumnIndexOrThrow("nombre"))
                    );

                } while (cursor.moveToNext());

            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_spinner_item,
                    listaCategorias
            );

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            spCategoria.setAdapter(adapter);

        } catch (Exception e) {

            mostrarMensaje("Error al cargar categorías: " + e.getMessage());

        } finally {

            if (cursor != null)
                cursor.close();

        }
    }
    private void cargarLaboratorios() {

        listaLaboratorios = new ArrayList<>();
        listaIdLaboratorios = new ArrayList<>();

        Cursor cursor = null;

        try {

            cursor = db.consultarLaboratoriosCursor();

            if (cursor != null && cursor.moveToFirst()) {

                do {

                    listaIdLaboratorios.add(
                            cursor.getInt(cursor.getColumnIndexOrThrow("id_laboratorio"))
                    );

                    listaLaboratorios.add(
                            cursor.getString(cursor.getColumnIndexOrThrow("nombre"))
                    );

                } while (cursor.moveToNext());

            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_spinner_item,
                    listaLaboratorios
            );

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            spLaboratorio.setAdapter(adapter);

        } catch (Exception e) {

            mostrarMensaje("Error al cargar laboratorios: " + e.getMessage());

        } finally {

            if (cursor != null)
                cursor.close();

        }
    }
    private void limpiarCampos() {

        edtCodigoInventario.setText("");
        edtCodigoQr.setText("");
        edtNombre.setText("");
        edtMarca.setText("");
        edtModelo.setText("");

        spLaboratorio.setSelection(0);
        spCategoria.setSelection(0);
        spEstado.setSelection(0);

        edtCodigoInventario.requestFocus();

    }
    @Override
    protected void onDestroy() {

        super.onDestroy();

        if (db != null) {
            db.cerrar();
            db = null;
        }

    }
    private void guardarEquipo() {

        int idLaboratorio = listaIdLaboratorios.get(spLaboratorio.getSelectedItemPosition());
        int idCategoria = listaIdCategorias.get(spCategoria.getSelectedItemPosition());

        String codigoInventario = edtCodigoInventario.getText().toString().trim();
        String codigoQr = edtCodigoQr.getText().toString().trim();
        String nombre = edtNombre.getText().toString().trim();
        String marca = edtMarca.getText().toString().trim();
        String modelo = edtModelo.getText().toString().trim();
        String estadoEquipo = spEstado.getSelectedItem().toString();

        if (codigoInventario.isEmpty()) {
            edtCodigoInventario.setError("Ingrese el código de inventario");
            edtCodigoInventario.requestFocus();
            return;
        }

        if (nombre.isEmpty()) {
            edtNombre.setError("Ingrese el nombre");
            edtNombre.requestFocus();
            return;
        }

        if (marca.isEmpty()) {
            edtMarca.setError("Ingrese la marca");
            edtMarca.requestFocus();
            return;
        }

        if (modelo.isEmpty()) {
            edtModelo.setError("Ingrese el modelo");
            edtModelo.requestFocus();
            return;
        }

        String mensaje = db.insertarEquipo(
                idLaboratorio,
                idCategoria,
                codigoInventario,
               codigoQr,
                nombre,
                marca,
                modelo,
                estadoEquipo
        );

        mostrarMensaje(mensaje);

        if (mensaje.toLowerCase().contains("correctamente")) {
            limpiarCampos();
        }
    }
}
