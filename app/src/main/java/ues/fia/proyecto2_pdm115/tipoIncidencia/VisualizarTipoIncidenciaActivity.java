package ues.fia.proyecto2_pdm115.tipoIncidencia;

import android.Manifest;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.net.Uri;
import android.os.Build;
import android.content.pm.PackageManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import androidx.appcompat.app.AppCompatActivity;

import com.ajts.androidmads.library.SQLiteToExcel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.ArrayList;

import ues.fia.proyecto2_pdm115.R;
import ues.fia.proyecto2_pdm115.controlDBLabCare;

public class VisualizarTipoIncidenciaActivity extends AppCompatActivity {

    private static final String NOMBRE_BASE_DATOS = "labcare.db";
    private static final String TABLA_TIPOS_INCIDENCIA = "tipos_incidencia";
    private static final String ARCHIVO_EXCEL = "tipos_incidencia.xls";
    private static final int REQUEST_WRITE_STORAGE = 2001;

    private File archivoTemporalPendiente;
    private String nombreArchivoPendiente;

    private ListView listTiposIncidencia;
    private TextView txtTotalTiposIncidencia;
    private TextView txtMensajeVisualizarTipoIncidencia;
    private Button btnActualizarListaTipoIncidencia;
    private Button btnExportarTiposIncidenciaExcel;
    private Button btnVolverVisualizarTipoIncidencia;

    private controlDBLabCare db;
    private final ArrayList<String> datosPantalla = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualizar_tipo_incidencia);

        enlazarVistas();
        abrirBaseDatos();
        configurarEventos();
        cargarTiposIncidencia();
    }

    private void enlazarVistas() {
        listTiposIncidencia = findViewById(R.id.listTiposIncidencia);
        txtTotalTiposIncidencia = findViewById(R.id.txtTotalTiposIncidencia);
        txtMensajeVisualizarTipoIncidencia = findViewById(R.id.txtMensajeVisualizarTipoIncidencia);
        btnActualizarListaTipoIncidencia = findViewById(R.id.btnActualizarListaTipoIncidencia);
        btnExportarTiposIncidenciaExcel = findViewById(R.id.btnExportarTiposIncidenciaExcel);
        btnVolverVisualizarTipoIncidencia = findViewById(R.id.btnVolverVisualizarTipoIncidencia);
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
        btnActualizarListaTipoIncidencia.setOnClickListener(v -> cargarTiposIncidencia());
        btnExportarTiposIncidenciaExcel.setOnClickListener(v -> exportarTiposIncidenciaExcel());
        btnVolverVisualizarTipoIncidencia.setOnClickListener(v -> finish());
    }

    private void cargarTiposIncidencia() {
        datosPantalla.clear();
        Cursor cursor = null;

        try {
            if (db == null || db.getDb() == null || !db.getDb().isOpen()) {
                abrirBaseDatos();
            }

            cursor = db.consultarTiposIncidenciaCursor();

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("id_tipo_incidencia"));
                    String nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"));
                    datosPantalla.add("ID: " + id + "\nTipo: " + nombre);
                } while (cursor.moveToNext());
            }

            if (datosPantalla.isEmpty()) {
                datosPantalla.add("No hay tipos de incidencia registrados.");
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_list_item_1,
                    datosPantalla
            );

            listTiposIncidencia.setAdapter(adapter);
            txtTotalTiposIncidencia.setText("Total: " + (datosPantalla.get(0).startsWith("No hay") ? 0 : datosPantalla.size()));
            txtMensajeVisualizarTipoIncidencia.setVisibility(View.GONE);

        } catch (Exception e) {
            mostrarMensaje("Error al cargar tipos de incidencia: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }


    private void exportarTiposIncidenciaExcel() {
        try {
            File carpetaTemporal = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);

            if (carpetaTemporal == null) {
                Toast.makeText(this, "No se pudo acceder a documentos temporales.", Toast.LENGTH_LONG).show();
                return;
            }

            if (!carpetaTemporal.exists()) {
                carpetaTemporal.mkdirs();
            }

            String fecha = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                    .format(new Date());

            String nombreArchivo = "tipos_incidencia_" + fecha + ".xls";

            SQLiteToExcel sqliteToExcel = new SQLiteToExcel(
                    this,
                    "labcare.db",
                    carpetaTemporal.getAbsolutePath()
            );

            sqliteToExcel.exportSingleTable(
                    "tipos_incidencia",
                    nombreArchivo,
                    new SQLiteToExcel.ExportListener() {
                        @Override
                        public void onStart() {
                            runOnUiThread(() ->
                                    Toast.makeText(
                                            VisualizarTipoIncidenciaActivity.this,
                                            "Exportando a Excel...",
                                            Toast.LENGTH_SHORT
                                    ).show()
                            );
                        }

                        @Override
                        public void onCompleted(String filePath) {
                            File archivoTemporal = new File(filePath);

                            if (!archivoTemporal.exists()) {
                                archivoTemporal = new File(carpetaTemporal, nombreArchivo);
                            }

                            guardarArchivoEnDocumentos(archivoTemporal, nombreArchivo);
                        }

                        @Override
                        public void onError(Exception e) {
                            runOnUiThread(() ->
                                    Toast.makeText(
                                            VisualizarTipoIncidenciaActivity.this,
                                            "Error al exportar: " + e.getMessage(),
                                            Toast.LENGTH_LONG
                                    ).show()
                            );
                        }
                    }
            );

        } catch (Throwable e) {
            Toast.makeText(
                    this,
                    "No se pudo exportar: " + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void guardarArchivoEnDocumentos(File archivoTemporal, String nombreArchivo) {
        if (archivoTemporal == null || !archivoTemporal.exists()) {
            runOnUiThread(() ->
                    Toast.makeText(
                            this,
                            "No se encontró el archivo temporal exportado.",
                            Toast.LENGTH_LONG
                    ).show()
            );
            return;
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                guardarConMediaStoreDocumentos(archivoTemporal, nombreArchivo);
            } else {
                guardarEnDocumentosLegacyConPermiso(archivoTemporal, nombreArchivo);
            }

        } catch (Exception e) {
            runOnUiThread(() ->
                    Toast.makeText(
                            this,
                            "Error al guardar en Documentos: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show()
            );
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void guardarConMediaStoreDocumentos(File archivoTemporal, String nombreArchivo) throws Exception {
        ContentResolver resolver = getContentResolver();

        ContentValues valores = new ContentValues();
        valores.put(MediaStore.MediaColumns.DISPLAY_NAME, nombreArchivo);
        valores.put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.ms-excel");
        valores.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS);
        valores.put(MediaStore.MediaColumns.IS_PENDING, 1);

        Uri uri = resolver.insert(
                MediaStore.Files.getContentUri("external"),
                valores
        );

        if (uri == null) {
            throw new Exception("No se pudo crear el archivo en Documentos.");
        }

        try (
                FileInputStream inputStream = new FileInputStream(archivoTemporal);
                OutputStream outputStream = resolver.openOutputStream(uri)
        ) {
            if (outputStream == null) {
                throw new Exception("No se pudo abrir el archivo de destino.");
            }

            byte[] buffer = new byte[4096];
            int bytesLeidos;

            while ((bytesLeidos = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesLeidos);
            }

            outputStream.flush();
        }

        ContentValues finalizar = new ContentValues();
        finalizar.put(MediaStore.MediaColumns.IS_PENDING, 0);
        resolver.update(uri, finalizar, null, null);

        runOnUiThread(() ->
                Toast.makeText(
                        this,
                        "Excel guardado en Documentos: " + nombreArchivo,
                        Toast.LENGTH_LONG
                ).show()
        );
    }

    private void guardarEnDocumentosLegacyConPermiso(File archivoTemporal, String nombreArchivo) throws Exception {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            archivoTemporalPendiente = archivoTemporal;
            nombreArchivoPendiente = nombreArchivo;

            runOnUiThread(() ->
                    ActivityCompat.requestPermissions(
                            this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_WRITE_STORAGE
                    )
            );

            return;
        }

        guardarEnDocumentosLegacy(archivoTemporal, nombreArchivo);
    }

    private void guardarEnDocumentosLegacy(File archivoTemporal, String nombreArchivo) throws Exception {
        File carpetaDocumentos = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS
        );


        if (!carpetaDocumentos.exists()) {
            carpetaDocumentos.mkdirs();
        }

        File archivoDestino = new File(carpetaDocumentos, nombreArchivo);

        try (
                FileInputStream inputStream = new FileInputStream(archivoTemporal);
                FileOutputStream outputStream = new FileOutputStream(archivoDestino)
        ) {
            byte[] buffer = new byte[4096];
            int bytesLeidos;

            while ((bytesLeidos = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesLeidos);
            }

            outputStream.flush();
        }

        runOnUiThread(() ->
                Toast.makeText(
                        this,
                        "Excel guardado en Documentos: " + nombreArchivo,
                        Toast.LENGTH_LONG
                ).show()
        );
    }

    private void mostrarMensaje(String mensaje) {
        txtMensajeVisualizarTipoIncidencia.setText(mensaje);
        txtMensajeVisualizarTipoIncidencia.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null) {
            db.cerrar();
            db = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_WRITE_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (archivoTemporalPendiente != null && nombreArchivoPendiente != null) {
                    new Thread(() -> {
                        try {
                            guardarEnDocumentosLegacy(
                                    archivoTemporalPendiente,
                                    nombreArchivoPendiente
                            );
                        } catch (Exception e) {
                            runOnUiThread(() ->
                                    Toast.makeText(
                                            this,
                                            "Error al guardar: " + e.getMessage(),
                                            Toast.LENGTH_LONG
                                    ).show()
                            );
                        }
                    }).start();
                }
            } else {
                Toast.makeText(
                        this,
                        "Permiso denegado. No se pudo guardar en Documentos.",
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }
}
