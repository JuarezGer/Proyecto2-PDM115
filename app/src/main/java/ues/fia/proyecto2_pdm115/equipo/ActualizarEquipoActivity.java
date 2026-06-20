package ues.fia.proyecto2_pdm115.equipo;

import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

import ues.fia.proyecto2_pdm115.R;
import ues.fia.proyecto2_pdm115.controlDBLabCare;

public class ActualizarEquipoActivity extends AppCompatActivity {

    private EditText edtCodigoInventario;
    private EditText edtCodigoQr;
    private EditText edtNombre;
    private EditText edtMarca;
    private EditText edtModelo;

    private Spinner spLaboratorio;
    private Spinner spCategoria;
    private Spinner spEstado;

    private Button btnActualizar;
    private Button btnVolver;

    private controlDBLabCare db;

    private int idEquipo;

    private ArrayList<String> listaLaboratorios;
    private ArrayList<Integer> listaIdLaboratorios;

    private ArrayList<String> listaCategorias;
    private ArrayList<Integer> listaIdCategorias;

    private String laboratorioActual;
    private String categoriaActual;
    private String estadoActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actualizar_equipo);

        enlazarVistas();

        db = new controlDBLabCare(this);

        try {
            db.abrir();
            cargarLaboratorios();
            cargarCategorias();
            cargarEstados();

            recibirDatos();
        } catch (SQLException e) {

            Toast.makeText(
                    this,
                    "Error al abrir base de datos: " + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();

        }

        recibirDatos();

        btnActualizar.setOnClickListener(
                v -> actualizarEquipo()
        );

        btnVolver.setOnClickListener(
                v -> finish()
        );
    }
    private void enlazarVistas() {

        edtCodigoInventario =
                findViewById(R.id.edtCodigoInventarioActualizar);

        edtCodigoQr =
                findViewById(R.id.edtCodigoQrActualizar);

        edtNombre =
                findViewById(R.id.edtNombreEquipoActualizar);

        edtMarca =
                findViewById(R.id.edtMarcaActualizar);

        edtModelo =
                findViewById(R.id.edtModeloActualizar);

        spLaboratorio =
                findViewById(R.id.spLaboratorioActualizar);

        spCategoria =
                findViewById(R.id.spCategoriaActualizar);

        spEstado =
                findViewById(R.id.spEstadoActualizar);

        btnActualizar =
                findViewById(R.id.btnActualizarEquipo);

        btnVolver =
                findViewById(R.id.btnVolverActualizarEquipo);

    }
    private void recibirDatos() {

        idEquipo =
                getIntent().getIntExtra(
                        "id_equipo",
                        -1
                );

        edtCodigoInventario.setText(
                getIntent().getStringExtra(
                        "codigo_inventario"
                )
        );

        edtCodigoQr.setText(
                getIntent().getStringExtra(
                        "codigo_qr"
                )
        );

        edtNombre.setText(
                getIntent().getStringExtra(
                        "nombre"
                )
        );

        edtMarca.setText(
                getIntent().getStringExtra(
                        "marca"
                )
        );

        edtModelo.setText(
                getIntent().getStringExtra(
                        "modelo"
                )
        );

        laboratorioActual =
                getIntent().getStringExtra(
                        "laboratorio"
                );

        categoriaActual =
                getIntent().getStringExtra(
                        "categoria"
                );

        estadoActual =
                getIntent().getStringExtra(
                        "estado_equipo"
                );

        seleccionarSpinner(
                spLaboratorio,
                laboratorioActual
        );

        seleccionarSpinner(
                spCategoria,
                categoriaActual
        );

        seleccionarSpinner(
                spEstado,
                estadoActual
        );

    }

    private void seleccionarSpinner(
            Spinner spinner,
            String valor
    ) {

        ArrayAdapter adapter =
                (ArrayAdapter) spinner.getAdapter();

        for (int i = 0; i < adapter.getCount(); i++) {

            if (adapter.getItem(i)
                    .toString()
                    .equals(valor)) {

                spinner.setSelection(i);

                break;
            }

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
                            cursor.getInt(
                                    cursor.getColumnIndexOrThrow(
                                            "id_laboratorio"
                                    )
                            )
                    );

                    listaLaboratorios.add(
                            cursor.getString(
                                    cursor.getColumnIndexOrThrow(
                                            "nombre"
                                    )
                            )
                    );

                } while (cursor.moveToNext());

            }

            ArrayAdapter<String> adapter =
                    new ArrayAdapter<>(
                            this,
                            android.R.layout.simple_spinner_item,
                            listaLaboratorios
                    );

            adapter.setDropDownViewResource(
                    android.R.layout.simple_spinner_dropdown_item
            );

            spLaboratorio.setAdapter(adapter);

        } finally {

            if (cursor != null)
                cursor.close();

        }

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
                            cursor.getInt(
                                    cursor.getColumnIndexOrThrow(
                                            "id_categoria"
                                    )
                            )
                    );

                    listaCategorias.add(
                            cursor.getString(
                                    cursor.getColumnIndexOrThrow(
                                            "nombre"
                                    )
                            )
                    );

                } while (cursor.moveToNext());

            }

            ArrayAdapter<String> adapter =
                    new ArrayAdapter<>(
                            this,
                            android.R.layout.simple_spinner_item,
                            listaCategorias
                    );

            adapter.setDropDownViewResource(
                    android.R.layout.simple_spinner_dropdown_item
            );

            spCategoria.setAdapter(adapter);

        } finally {

            if (cursor != null)
                cursor.close();

        }

    }
    private void cargarEstados() {

        ArrayList<String> estados = new ArrayList<>();

        estados.add("activo");
        estados.add("en_mantenimiento");
        estados.add("fuera_servicio");
        estados.add("baja");

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        estados
                );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spEstado.setAdapter(adapter);

    }
    private void actualizarEquipo() {

        String codigoInventario =
                edtCodigoInventario.getText().toString().trim();

        String codigoQr =
                edtCodigoQr.getText().toString().trim();

        String nombre =
                edtNombre.getText().toString().trim();

        String marca =
                edtMarca.getText().toString().trim();

        String modelo =
                edtModelo.getText().toString().trim();

        String estado =
                spEstado.getSelectedItem().toString();

        if (codigoInventario.isEmpty()) {

            edtCodigoInventario.setError(
                    "Ingrese el código de inventario"
            );

            edtCodigoInventario.requestFocus();

            return;

        }

        if (nombre.isEmpty()) {

            edtNombre.setError(
                    "Ingrese el nombre"
            );

            edtNombre.requestFocus();

            return;

        }

        int idLaboratorio =
                listaIdLaboratorios.get(
                        spLaboratorio.getSelectedItemPosition()
                );

        int idCategoria =
                listaIdCategorias.get(
                        spCategoria.getSelectedItemPosition()
                );

        String mensaje =
                db.actualizarEquipo(
                        idEquipo,
                        idLaboratorio,
                        idCategoria,
                        codigoInventario,
                        codigoQr,
                        nombre,
                        marca,
                        modelo,
                        estado
                );

        Toast.makeText(
                this,
                mensaje,
                Toast.LENGTH_LONG
        ).show();

        if (mensaje.contains("correctamente")) {

            finish();

        }

    }
}