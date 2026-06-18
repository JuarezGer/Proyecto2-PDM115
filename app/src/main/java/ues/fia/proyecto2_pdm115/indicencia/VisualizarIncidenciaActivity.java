package ues.fia.proyecto2_pdm115.indicencia;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import ues.fia.proyecto2_pdm115.R;
import ues.fia.proyecto2_pdm115.controlDBLabCare;

public class VisualizarIncidenciaActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private static final int REQUEST_WRITE_STORAGE = 4101;

    private File archivoTemporalPendiente;
    private String nombreArchivoPendiente;

    private ListView listIncidencias;
    private TextView txtTotalIncidencias;
    private TextView txtMensaje;
    private Button btnActualizarLista;
    private Button btnVolver;

    private controlDBLabCare db;
    private TextToSpeech textToSpeech;
    private boolean ttsDisponible = false;

    private final ArrayList<IncidenciaItem> listaIncidencias = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualizar_incidencia);

        enlazarVistas();
        abrirBaseDatos();
        textToSpeech = new TextToSpeech(this, this);
        configurarEventos();
        cargarIncidencias();
    }

    private void enlazarVistas() {
        listIncidencias = findViewById(R.id.listIncidencias);
        txtTotalIncidencias = findViewById(R.id.txtTotalIncidencias);
        txtMensaje = findViewById(R.id.txtMensajeVisualizarIncidencia);
        btnActualizarLista = findViewById(R.id.btnActualizarListaIncidencia);
        btnVolver = findViewById(R.id.btnVolverVisualizarIncidencia);
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
        btnActualizarLista.setOnClickListener(v -> cargarIncidencias());
        btnVolver.setOnClickListener(v -> finish());

        listIncidencias.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < listaIncidencias.size()) {
                IncidenciaItem item = listaIncidencias.get(position);
                if (item.id > 0) {
                    mostrarModalIncidencia(item.id);
                }
            }
        });
    }

    private void cargarIncidencias() {
        listaIncidencias.clear();
        Cursor cursor = null;

        try {
            if (db == null || db.getDb() == null || !db.getDb().isOpen()) {
                abrirBaseDatos();
            }

            cursor = db.consultarIncidenciasCursor();
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("id_incidencia"));
                    String titulo = obtenerString(cursor, "titulo");
                    String equipo = obtenerString(cursor, "equipo");
                    String tipo = obtenerString(cursor, "tipo_incidencia");
                    String prioridad = obtenerString(cursor, "prioridad");
                    String estado = obtenerString(cursor, "estado_incidencia");
                    String fecha = obtenerString(cursor, "fecha_reporte");
                    listaIncidencias.add(new IncidenciaItem(id, titulo, equipo, tipo, prioridad, estado, fecha));
                } while (cursor.moveToNext());
            }

            if (listaIncidencias.isEmpty()) {
                listaIncidencias.add(new IncidenciaItem(0, "No hay incidencias registradas.", "", "", "", "", ""));
            } else {
            }

            ArrayAdapter<IncidenciaItem> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaIncidencias);
            listIncidencias.setAdapter(adapter);

            int total = listaIncidencias.get(0).id == 0 ? 0 : listaIncidencias.size();
            txtTotalIncidencias.setText("Total: " + total);
            txtMensaje.setVisibility(View.GONE);
        } catch (Exception e) {
            mostrarMensaje("Error al cargar incidencias: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private String obtenerString(Cursor cursor, String columna) {
        int index = cursor.getColumnIndex(columna);
        if (index < 0 || cursor.isNull(index)) return "";
        return cursor.getString(index);
    }

    private void mostrarModalIncidencia(int idIncidencia) {
        HashMap<String, String> datos = db.consultarIncidencia(idIncidencia);

        if (datos == null) {
            Toast.makeText(this, "No se encontró la incidencia seleccionada.", Toast.LENGTH_LONG).show();
            return;
        }

        ScrollView scrollView = new ScrollView(this);
        LinearLayout contenedor = new LinearLayout(this);
        contenedor.setOrientation(LinearLayout.VERTICAL);
        contenedor.setPadding(24, 16, 24, 16);
        scrollView.addView(contenedor);

        TextView txtDetalle = new TextView(this);
        txtDetalle.setText(construirDetalle(datos));
        txtDetalle.setTextSize(15f);
        txtDetalle.setTextColor(ContextCompat.getColor(this, R.color.blue_gray_900));
        txtDetalle.setLineSpacing(2f, 1.1f);
        contenedor.addView(txtDetalle);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Detalle de incidencia")
                .setView(scrollView)
                .setPositiveButton("Exportar PDF", null)
                .setNeutralButton("Leer", null)
                .setNegativeButton("Cerrar", null)
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> exportarIncidenciaPdf(datos));
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> leerIncidencia(datos));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v -> {
                detenerLectura();
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private String construirDetalle(HashMap<String, String> datos) {
        return "ID: " + valor(datos, "id_incidencia") + "\n" +
                "Título: " + valor(datos, "titulo") + "\n" +
                "Descripción: " + valor(datos, "descripcion") + "\n" +
                "Equipo: " + valor(datos, "equipo") + "\n" +
                "Código inventario: " + valor(datos, "codigo_inventario") + "\n" +
                "Tipo: " + valor(datos, "tipo_incidencia") + "\n" +
                "Usuario reporta: " + valor(datos, "usuario_reporta") + "\n" +
                "Prioridad: " + valor(datos, "prioridad") + "\n" +
                "Estado: " + valor(datos, "estado_incidencia") + "\n" +
                "Origen: " + valor(datos, "origen_registro") + "\n" +
                "Texto voz original: " + valor(datos, "texto_voz_original") + "\n" +
                "Latitud: " + valor(datos, "latitud") + "\n" +
                "Longitud: " + valor(datos, "longitud") + "\n" +
                "Fecha reporte: " + valor(datos, "fecha_reporte");
    }

    private void leerIncidencia(HashMap<String, String> datos) {
        if (!ttsDisponible || textToSpeech == null) {
            Toast.makeText(this, "TextToSpeech no está disponible todavía.", Toast.LENGTH_SHORT).show();
            return;
        }

        String texto = "Incidencia " + valor(datos, "id_incidencia") + ". " +
                "Título: " + valor(datos, "titulo") + ". " +
                "Equipo: " + valor(datos, "equipo") + ". " +
                "Tipo: " + valor(datos, "tipo_incidencia") + ". " +
                "Prioridad: " + valor(datos, "prioridad") + ". " +
                "Estado: " + valor(datos, "estado_incidencia") + ". " +
                "Descripción: " + valor(datos, "descripcion") + ".";
        textToSpeech.speak(texto, TextToSpeech.QUEUE_FLUSH, null, "INCIDENCIA_" + valor(datos, "id_incidencia"));
    }

    private void detenerLectura() {
        if (textToSpeech != null) {
            textToSpeech.stop();
        }
    }

    private void exportarIncidenciaPdf(HashMap<String, String> datos) {
        try {
            File carpetaTemporal = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            if (carpetaTemporal == null) {
                Toast.makeText(this, "No se pudo acceder a documentos temporales.", Toast.LENGTH_LONG).show();
                return;
            }

            if (!carpetaTemporal.exists()) carpetaTemporal.mkdirs();

            String fecha = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String nombreArchivo = "incidencia_" + valor(datos, "id_incidencia") + "_" + fecha + ".pdf";
            File archivoTemporal = new File(carpetaTemporal, nombreArchivo);

            generarPdf(archivoTemporal, datos);
            guardarArchivoEnDocumentos(archivoTemporal, nombreArchivo);
        } catch (Exception e) {
            Toast.makeText(this, "No se pudo exportar el PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void generarPdf(File archivoTemporal, HashMap<String, String> datos) throws Exception {
        PdfWriter writer = new PdfWriter(archivoTemporal.getAbsolutePath());
        PdfDocument pdfDocument = new PdfDocument(writer);
        Document document = new Document(pdfDocument, PageSize.A4);
        document.setMargins(36, 32, 36, 32);

        PdfFont fontNormal = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        DeviceRgb colorHeader = new DeviceRgb(84, 110, 122);

        Paragraph titulo = new Paragraph("LabCare - Reporte de Incidencia")
                .setFont(fontBold)
                .setFontSize(18)
                .setFontColor(colorHeader)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(titulo);

        Paragraph subtitulo = new Paragraph("Generado el " + new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date()))
                .setFont(fontNormal)
                .setFontSize(10)
                .setFontColor(ColorConstants.DARK_GRAY)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(subtitulo);

        document.add(new Paragraph(" "));

        Table tabla = new Table(UnitValue.createPercentArray(new float[]{35, 65}));
        tabla.setWidth(UnitValue.createPercentValue(100));

        agregarFila(tabla, fontBold, fontNormal, "ID", valor(datos, "id_incidencia"));
        agregarFila(tabla, fontBold, fontNormal, "Título", valor(datos, "titulo"));
        agregarFila(tabla, fontBold, fontNormal, "Descripción", valor(datos, "descripcion"));
        agregarFila(tabla, fontBold, fontNormal, "Equipo", valor(datos, "equipo"));
        agregarFila(tabla, fontBold, fontNormal, "Código inventario", valor(datos, "codigo_inventario"));
        agregarFila(tabla, fontBold, fontNormal, "Tipo de incidencia", valor(datos, "tipo_incidencia"));
        agregarFila(tabla, fontBold, fontNormal, "Usuario reporta", valor(datos, "usuario_reporta"));
        agregarFila(tabla, fontBold, fontNormal, "Prioridad", valor(datos, "prioridad"));
        agregarFila(tabla, fontBold, fontNormal, "Estado", valor(datos, "estado_incidencia"));
        agregarFila(tabla, fontBold, fontNormal, "Origen", valor(datos, "origen_registro"));
        agregarFila(tabla, fontBold, fontNormal, "Texto voz original", valor(datos, "texto_voz_original"));
        agregarFila(tabla, fontBold, fontNormal, "Latitud", valor(datos, "latitud"));
        agregarFila(tabla, fontBold, fontNormal, "Longitud", valor(datos, "longitud"));
        agregarFila(tabla, fontBold, fontNormal, "Fecha reporte", valor(datos, "fecha_reporte"));

        document.add(tabla);
        document.close();
    }

    private void agregarFila(Table tabla, PdfFont fontBold, PdfFont fontNormal, String etiqueta, String valor) {
        Cell celdaEtiqueta = new Cell()
                .add(new Paragraph(etiqueta).setFont(fontBold).setFontSize(10))
                .setBackgroundColor(new DeviceRgb(236, 239, 241))
                .setBorder(new SolidBorder(new DeviceRgb(207, 216, 220), 0.7f))
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPadding(6);

        Cell celdaValor = new Cell()
                .add(new Paragraph(valor == null || valor.trim().isEmpty() ? "-" : valor).setFont(fontNormal).setFontSize(10))
                .setBorder(new SolidBorder(new DeviceRgb(207, 216, 220), 0.7f))
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPadding(6);

        tabla.addCell(celdaEtiqueta);
        tabla.addCell(celdaValor);
    }

    private void guardarArchivoEnDocumentos(File archivoTemporal, String nombreArchivo) {
        if (archivoTemporal == null || !archivoTemporal.exists()) {
            Toast.makeText(this, "No se encontró el archivo temporal generado.", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                guardarConMediaStoreDocumentos(archivoTemporal, nombreArchivo);
            } else {
                guardarEnDocumentosLegacyConPermiso(archivoTemporal, nombreArchivo);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error al guardar en Documentos: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void guardarConMediaStoreDocumentos(File archivoTemporal, String nombreArchivo) throws Exception {
        ContentResolver resolver = getContentResolver();

        ContentValues valores = new ContentValues();
        valores.put(MediaStore.MediaColumns.DISPLAY_NAME, nombreArchivo);
        valores.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
        valores.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS);
        valores.put(MediaStore.MediaColumns.IS_PENDING, 1);

        Uri uri = resolver.insert(MediaStore.Files.getContentUri("external"), valores);
        if (uri == null) throw new Exception("No se pudo crear el archivo en Documentos.");

        try (FileInputStream inputStream = new FileInputStream(archivoTemporal);
             OutputStream outputStream = resolver.openOutputStream(uri)) {
            if (outputStream == null) throw new Exception("No se pudo abrir el archivo de destino.");

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
        Toast.makeText(this, "PDF guardado en Documentos: " + nombreArchivo, Toast.LENGTH_LONG).show();
    }

    private void guardarEnDocumentosLegacyConPermiso(File archivoTemporal, String nombreArchivo) throws Exception {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            archivoTemporalPendiente = archivoTemporal;
            nombreArchivoPendiente = nombreArchivo;
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
            return;
        }

        guardarEnDocumentosLegacy(archivoTemporal, nombreArchivo);
    }

    private void guardarEnDocumentosLegacy(File archivoTemporal, String nombreArchivo) throws Exception {
        File carpetaDocumentos = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        if (!carpetaDocumentos.exists()) carpetaDocumentos.mkdirs();

        File archivoDestino = new File(carpetaDocumentos, nombreArchivo);
        try (FileInputStream inputStream = new FileInputStream(archivoTemporal);
             FileOutputStream outputStream = new FileOutputStream(archivoDestino)) {
            byte[] buffer = new byte[4096];
            int bytesLeidos;
            while ((bytesLeidos = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesLeidos);
            }
            outputStream.flush();
        }
        Toast.makeText(this, "PDF guardado en Documentos: " + nombreArchivo, Toast.LENGTH_LONG).show();
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
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int resultado = textToSpeech.setLanguage(new Locale("es", "SV"));
            ttsDisponible = resultado != TextToSpeech.LANG_MISSING_DATA && resultado != TextToSpeech.LANG_NOT_SUPPORTED;
        } else {
            ttsDisponible = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        detenerLectura();

        if (textToSpeech != null) {
            textToSpeech.shutdown();
            textToSpeech = null;
        }

        if (db != null) {
            db.cerrar();
            db = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_WRITE_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (archivoTemporalPendiente != null && nombreArchivoPendiente != null) {
                    try {
                        guardarEnDocumentosLegacy(archivoTemporalPendiente, nombreArchivoPendiente);
                    } catch (Exception e) {
                        Toast.makeText(this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            } else {
                Toast.makeText(this, "Permiso denegado. No se pudo guardar el PDF.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private static class IncidenciaItem {
        int id;
        String titulo;
        String equipo;
        String tipo;
        String prioridad;
        String estado;
        String fecha;

        IncidenciaItem(int id, String titulo, String equipo, String tipo, String prioridad, String estado, String fecha) {
            this.id = id;
            this.titulo = titulo;
            this.equipo = equipo;
            this.tipo = tipo;
            this.prioridad = prioridad;
            this.estado = estado;
            this.fecha = fecha;
        }

        @Override
        public String toString() {
            if (id == 0) return titulo;
            return "ID: " + id + "\n" +
                    "Título: " + titulo + "\n" +
                    "Equipo: " + equipo + "\n" +
                    "Tipo: " + tipo + " | Prioridad: " + prioridad + "\n" +
                    "Estado: " + estado + " | Fecha: " + fecha;
        }
    }
}
