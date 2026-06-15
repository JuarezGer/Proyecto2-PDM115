package ues.fia.proyecto2_pdm115.evidencia;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import com.google.android.material.textfield.TextInputEditText;
import ues.fia.proyecto2_pdm115.R;
import ues.fia.proyecto2_pdm115.controlDBLabCare;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class InsertarEvidenciaActivity extends AppCompatActivity {

    // Vistas
    private Spinner spinnerMantenimiento, spinnerIncidencia;
    private ImageView imgPreview;
    private TextInputEditText editDescripcion;
    private Button btnCapturarArchivo, btnGuardar, btnCancelar;

    // Datos de los spinners
    private List<Integer> listaIdMantenimientos = new ArrayList<>();
    private List<Integer> listaIdIncidencias = new ArrayList<>();

    // Archivo capturado
    private Uri uriFotoActual;
    private String rutaArchivoFinal;

    // Controlador BD
    private controlDBLabCare db;

    // Launchers de cámara y galería
    private ActivityResultLauncher<Uri> launcherCamara;
    private ActivityResultLauncher<String> launcherGaleria;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insertar_evidencia);

        db = new controlDBLabCare(this);

        vincularVistas();
        configurarToolbar();
        configurarLaunchers();
        cargarSpinnerMantenimientos();
        configurarListeners();
    }

    private void vincularVistas() {
        spinnerMantenimiento = findViewById(R.id.spinnerMantenimiento);
        spinnerIncidencia    = findViewById(R.id.spinnerIncidencia);
        imgPreview           = findViewById(R.id.imgPreview);
        editDescripcion      = findViewById(R.id.editDescripcion);
        btnCapturarArchivo   = findViewById(R.id.btnCapturarArchivo);
        btnGuardar           = findViewById(R.id.btnGuardar);
        btnCancelar          = findViewById(R.id.btnCancelar);
    }

    private void configurarToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    // =========================================================
    // LAUNCHERS
    // =========================================================

    private void configurarLaunchers() {
        launcherCamara = registerForActivityResult(
                new ActivityResultContracts.TakePicture(), success -> {
                    if (success && uriFotoActual != null) {
                        rutaArchivoFinal = copiarImagenAAlmacenamientoInterno(uriFotoActual);
                        imgPreview.setImageURI(uriFotoActual);
                        imgPreview.setVisibility(android.view.View.VISIBLE);
                    } else {
                        Toast.makeText(this,
                                "No se capturó ninguna foto",
                                Toast.LENGTH_SHORT).show();
                    }
                });

        launcherGaleria = registerForActivityResult(
                new ActivityResultContracts.GetContent(), uri -> {
                    if (uri != null) {
                        rutaArchivoFinal = copiarImagenAAlmacenamientoInterno(uri);
                        imgPreview.setImageURI(uri);
                        imgPreview.setVisibility(android.view.View.VISIBLE);
                    } else {
                        Toast.makeText(this,
                                "No se seleccionó ningún archivo",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // =========================================================
    // SPINNERS
    // =========================================================

    private void cargarSpinnerMantenimientos() {
        List<String> etiquetas = new ArrayList<>();
        listaIdMantenimientos.clear();
        etiquetas.add("Selecciona un mantenimiento");
        listaIdMantenimientos.add(-1);

        try {
            db.abrir();
            Cursor cursor = db.obtenerMantenimientosParaSpinner();
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(
                            cursor.getColumnIndexOrThrow("id_mantenimiento"));
                    String tipo = cursor.getString(
                            cursor.getColumnIndexOrThrow("tipo_mantenimiento"));
                    String estado = cursor.getString(
                            cursor.getColumnIndexOrThrow("estado_mantenimiento"));
                    etiquetas.add("#" + id + " — " + tipo + " (" + estado + ")");
                    listaIdMantenimientos.add(id);
                }
                cursor.close();
            }
        } finally {
            db.cerrar();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, etiquetas);
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        spinnerMantenimiento.setAdapter(adapter);

        // Primera opción deshabilitada
        spinnerMantenimiento.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(
                            android.widget.AdapterView<?> parent,
                            android.view.View view, int position, long id) {
                        if (position == 0) {
                            cargarSpinnerIncidencias(-1);
                        } else {
                            int idMantenimiento = listaIdMantenimientos.get(position);
                            cargarSpinnerIncidencias(idMantenimiento);
                        }
                    }
                    @Override
                    public void onNothingSelected(
                            android.widget.AdapterView<?> parent) {}
                });

        // Cargar incidencias vacío por defecto
        cargarSpinnerIncidencias(-1);
    }

    private void cargarSpinnerIncidencias(int idMantenimiento) {
        List<String> etiquetas = new ArrayList<>();
        listaIdIncidencias.clear();
        etiquetas.add("Sin incidencia");
        listaIdIncidencias.add(-1);

        if (idMantenimiento != -1) {
            try {
                db.abrir();
                Cursor cursor = db.obtenerIncidenciasPorMantenimiento(
                        idMantenimiento);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        int id = cursor.getInt(
                                cursor.getColumnIndexOrThrow("id_incidencia"));
                        String titulo = cursor.getString(
                                cursor.getColumnIndexOrThrow("titulo"));
                        etiquetas.add("#" + id + " — " + titulo);
                        listaIdIncidencias.add(id);
                    }
                    cursor.close();
                }
            } finally {
                db.cerrar();
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, etiquetas);
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        spinnerIncidencia.setAdapter(adapter);
    }

    // =========================================================
    // LISTENERS
    // =========================================================

    private void configurarListeners() {
        btnCapturarArchivo.setOnClickListener(v -> mostrarDialogoCaptura());
        btnGuardar.setOnClickListener(v -> validarYGuardar());
        btnCancelar.setOnClickListener(v -> finish());
    }

    private void mostrarDialogoCaptura() {
        new AlertDialog.Builder(this)
                .setTitle("Capturar evidencia")
                .setItems(new String[]{"Tomar foto", "Elegir de galería"},
                        (dialog, which) -> {
                            if (which == 0) abrirCamara();
                            else abrirGaleria();
                        })
                .show();
    }

    private void abrirCamara() {
        try {
            File foto = crearArchivoTemporal();
            uriFotoActual = FileProvider.getUriForFile(this,
                    getPackageName() + ".fileprovider", foto);
            launcherCamara.launch(uriFotoActual);
        } catch (IOException e) {
            Toast.makeText(this,
                    "Error al crear archivo de foto",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void abrirGaleria() {
        launcherGaleria.launch("image/*");
    }

    private File crearArchivoTemporal() throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile("EVD_" + timestamp, ".jpg", dir);
    }

    private String getRutaDesdeUri(Uri uri) {
        return uri.toString();
    }

    // =========================================================
    // VALIDACIÓN Y GUARDADO
    // =========================================================

    private void validarYGuardar() {
        int posMantenimiento = spinnerMantenimiento.getSelectedItemPosition();
        if (posMantenimiento == 0) {
            Toast.makeText(this,
                    "Selecciona un mantenimiento",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (rutaArchivoFinal == null || rutaArchivoFinal.isEmpty()) {
            Toast.makeText(this,
                    "Debes capturar o seleccionar un archivo de evidencia",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        int idMantenimiento = listaIdMantenimientos.get(posMantenimiento);
        int posIncidencia = spinnerIncidencia.getSelectedItemPosition();
        Integer idIncidencia = listaIdIncidencias.get(posIncidencia) == -1
                ? null : listaIdIncidencias.get(posIncidencia);

        String descripcion = editDescripcion.getText() != null
                ? editDescripcion.getText().toString().trim() : "";

        try {
            db.abrir();
            String resultado = db.insertarEvidencia(
                    idMantenimiento,
                    idIncidencia,
                    "imagen",
                    rutaArchivoFinal,
                    descripcion.isEmpty() ? null : descripcion
            );
            if (resultado.contains("correctamente")) {
                Toast.makeText(this, resultado, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, resultado, Toast.LENGTH_LONG).show();
            }
        } finally {
            db.cerrar();
        }
    }

    private String copiarImagenAAlmacenamientoInterno(Uri uri) {
        try {
            File dir = new File(getFilesDir(), "evidencias");
            if (!dir.exists()) dir.mkdirs();
            String nombre = "EVD_" + System.currentTimeMillis() + ".jpg";
            File destino = new File(dir, nombre);
            java.io.InputStream in = getContentResolver().openInputStream(uri);
            java.io.FileOutputStream out = new java.io.FileOutputStream(destino);
            byte[] buffer = new byte[4096];
            int len;
            while ((len = in.read(buffer)) != -1) out.write(buffer, 0, len);
            in.close();
            out.close();
            return destino.getAbsolutePath(); // ruta permanente
        } catch (Exception e) {
            return null;
        }
    }
}