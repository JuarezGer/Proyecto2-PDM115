package ues.fia.proyecto2_pdm115.reporte;

import android.Manifest;
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
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
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
import java.util.Locale;

import ues.fia.proyecto2_pdm115.R;
import ues.fia.proyecto2_pdm115.controlDBLabCare;

public class VisualizarReporteGeneralActivity extends AppCompatActivity {

    private static final int REQUEST_WRITE_STORAGE = 3101;

    private File archivoTemporalPendiente;
    private String nombreArchivoPendiente;

    private ListView listReporteGeneral;
    private TextView txtTotalReporteGeneral;
    private TextView txtMensajeReporteGeneral;
    private Button btnActualizarReporteGeneral;
    private Button btnExportarReporteGeneralPdf;
    private Button btnVolverReporteGeneral;

    private controlDBLabCare db;
    private final ArrayList<String> datosPantalla = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualizar_reporte_general);

        enlazarVistas();
        abrirBaseDatos();
        configurarEventos();
        cargarReporteGeneral();
    }

    private void enlazarVistas() {
        listReporteGeneral = findViewById(R.id.listReporteGeneral);
        txtTotalReporteGeneral = findViewById(R.id.txtTotalReporteGeneral);
        txtMensajeReporteGeneral = findViewById(R.id.txtMensajeReporteGeneral);
        btnActualizarReporteGeneral = findViewById(R.id.btnActualizarReporteGeneral);
        btnExportarReporteGeneralPdf = findViewById(R.id.btnExportarReporteGeneralPdf);
        btnVolverReporteGeneral = findViewById(R.id.btnVolverReporteGeneral);
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
        btnActualizarReporteGeneral.setOnClickListener(v -> cargarReporteGeneral());
        btnExportarReporteGeneralPdf.setOnClickListener(v -> exportarReporteGeneralPdf());
        btnVolverReporteGeneral.setOnClickListener(v -> finish());
    }

    private void cargarReporteGeneral() {
        datosPantalla.clear();

        try {
            if (db == null || db.getDb() == null || !db.getDb().isOpen()) {
                abrirBaseDatos();
            }

            agregarSeccionPantalla("RESUMEN GENERAL", obtenerConsultaResumenGeneral());

            agregarSeccionPantalla("INCIDENCIAS POR TIPO",
                    "SELECT ti.nombre AS categoria, COUNT(*) AS total " +
                            "FROM incidencias i " +
                            "INNER JOIN tipos_incidencia ti ON ti.id_tipo_incidencia = i.id_tipo_incidencia " +
                            "GROUP BY ti.nombre ORDER BY total DESC");

            agregarSeccionPantalla("INCIDENCIAS POR ESTADO",
                    "SELECT estado_incidencia AS categoria, COUNT(*) AS total " +
                            "FROM incidencias GROUP BY estado_incidencia ORDER BY total DESC");

            agregarSeccionPantalla("INCIDENCIAS POR PRIORIDAD",
                    "SELECT prioridad AS categoria, COUNT(*) AS total " +
                            "FROM incidencias GROUP BY prioridad " +
                            "ORDER BY CASE prioridad " +
                            "WHEN 'critica' THEN 1 " +
                            "WHEN 'alta' THEN 2 " +
                            "WHEN 'media' THEN 3 " +
                            "WHEN 'baja' THEN 4 " +
                            "ELSE 5 END");

            agregarSeccionPantalla("INCIDENCIAS POR ORIGEN",
                    "SELECT origen_registro AS categoria, COUNT(*) AS total " +
                            "FROM incidencias GROUP BY origen_registro ORDER BY total DESC");

            agregarSeccionPantalla("MANTENIMIENTOS POR ESTADO",
                    "SELECT estado_mantenimiento AS categoria, COUNT(*) AS total " +
                            "FROM mantenimientos GROUP BY estado_mantenimiento ORDER BY total DESC");

            agregarSeccionPantalla("MANTENIMIENTOS POR TIPO",
                    "SELECT tipo_mantenimiento AS categoria, COUNT(*) AS total " +
                            "FROM mantenimientos GROUP BY tipo_mantenimiento ORDER BY total DESC");

            agregarSeccionPantalla("MANTENIMIENTOS POR TÉCNICO",
                    "SELECT ut.nombres || ' ' || ut.apellidos AS categoria, COUNT(*) AS total " +
                            "FROM mantenimientos m " +
                            "INNER JOIN usuarios ut ON ut.id_usuario = m.id_usuario_tecnico " +
                            "GROUP BY ut.id_usuario, ut.nombres, ut.apellidos ORDER BY total DESC");

            agregarSeccionPantalla("MANTENIMIENTOS POR EQUIPO",
                    "SELECT eq.nombre || ' (' || eq.codigo_inventario || ')' AS categoria, COUNT(*) AS total " +
                            "FROM mantenimientos m " +
                            "INNER JOIN equipos eq ON eq.id_equipo = m.id_equipo " +
                            "GROUP BY eq.id_equipo, eq.nombre, eq.codigo_inventario ORDER BY total DESC");

            if (datosPantalla.isEmpty()) {
                datosPantalla.add("No hay información estadística para mostrar.");
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_list_item_1,
                    datosPantalla
            );

            listReporteGeneral.setAdapter(adapter);
            txtTotalReporteGeneral.setText("Indicadores: " + contarIndicadoresReales());
            txtMensajeReporteGeneral.setVisibility(View.GONE);

        } catch (Exception e) {
            mostrarMensaje("Error al cargar reporte: " + e.getMessage());
        }
    }

    private String obtenerConsultaResumenGeneral() {
        return "SELECT 'Equipos registrados' AS categoria, COUNT(*) AS total FROM equipos " +
                "UNION ALL SELECT 'Incidencias registradas', COUNT(*) FROM incidencias " +
                "UNION ALL SELECT 'Incidencias abiertas', COUNT(*) FROM incidencias " +
                "WHERE estado_incidencia IN ('pendiente', 'abierta', 'en_proceso') " +
                "UNION ALL SELECT 'Mantenimientos registrados', COUNT(*) FROM mantenimientos " +
                "UNION ALL SELECT 'Mantenimientos pendientes o en proceso', COUNT(*) FROM mantenimientos " +
                "WHERE estado_mantenimiento IN ('pendiente', 'en_proceso')";
    }

    private void agregarSeccionPantalla(String titulo, String consulta) {
        Cursor cursor = null;
        boolean tieneDatos = false;

        try {
            cursor = db.getDb().rawQuery(consulta, null);
            datosPantalla.add("===== " + titulo + " =====");

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String categoria = cursor.getString(cursor.getColumnIndexOrThrow("categoria"));
                    int total = cursor.getInt(cursor.getColumnIndexOrThrow("total"));
                    datosPantalla.add(formatearEtiqueta(categoria) + ": " + total);
                    tieneDatos = true;
                } while (cursor.moveToNext());
            }

            if (!tieneDatos) {
                datosPantalla.add("Sin datos.");
            }

            datosPantalla.add("");
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private int contarIndicadoresReales() {
        int total = 0;
        for (String linea : datosPantalla) {
            if (!linea.trim().isEmpty() && !linea.startsWith("=====") && !linea.equals("Sin datos.")) {
                total++;
            }
        }
        return total;
    }

    private ArrayList<ReporteFila> obtenerFilasReporte() {
        ArrayList<ReporteFila> filas = new ArrayList<>();

        agregarFilasReporte(filas, "Resumen general", obtenerConsultaResumenGeneral());

        agregarFilasReporte(filas, "Incidencias por tipo",
                "SELECT ti.nombre AS categoria, COUNT(*) AS total " +
                        "FROM incidencias i " +
                        "INNER JOIN tipos_incidencia ti ON ti.id_tipo_incidencia = i.id_tipo_incidencia " +
                        "GROUP BY ti.nombre ORDER BY total DESC");

        agregarFilasReporte(filas, "Incidencias por estado",
                "SELECT estado_incidencia AS categoria, COUNT(*) AS total " +
                        "FROM incidencias GROUP BY estado_incidencia ORDER BY total DESC");

        agregarFilasReporte(filas, "Incidencias por prioridad",
                "SELECT prioridad AS categoria, COUNT(*) AS total " +
                        "FROM incidencias GROUP BY prioridad " +
                        "ORDER BY CASE prioridad " +
                        "WHEN 'critica' THEN 1 " +
                        "WHEN 'alta' THEN 2 " +
                        "WHEN 'media' THEN 3 " +
                        "WHEN 'baja' THEN 4 " +
                        "ELSE 5 END");

        agregarFilasReporte(filas, "Incidencias por origen",
                "SELECT origen_registro AS categoria, COUNT(*) AS total " +
                        "FROM incidencias GROUP BY origen_registro ORDER BY total DESC");

        agregarFilasReporte(filas, "Mantenimientos por estado",
                "SELECT estado_mantenimiento AS categoria, COUNT(*) AS total " +
                        "FROM mantenimientos GROUP BY estado_mantenimiento ORDER BY total DESC");

        agregarFilasReporte(filas, "Mantenimientos por tipo",
                "SELECT tipo_mantenimiento AS categoria, COUNT(*) AS total " +
                        "FROM mantenimientos GROUP BY tipo_mantenimiento ORDER BY total DESC");

        agregarFilasReporte(filas, "Mantenimientos por técnico",
                "SELECT ut.nombres || ' ' || ut.apellidos AS categoria, COUNT(*) AS total " +
                        "FROM mantenimientos m " +
                        "INNER JOIN usuarios ut ON ut.id_usuario = m.id_usuario_tecnico " +
                        "GROUP BY ut.id_usuario, ut.nombres, ut.apellidos ORDER BY total DESC");

        agregarFilasReporte(filas, "Mantenimientos por equipo",
                "SELECT eq.nombre || ' (' || eq.codigo_inventario || ')' AS categoria, COUNT(*) AS total " +
                        "FROM mantenimientos m " +
                        "INNER JOIN equipos eq ON eq.id_equipo = m.id_equipo " +
                        "GROUP BY eq.id_equipo, eq.nombre, eq.codigo_inventario ORDER BY total DESC");

        return filas;
    }

    private void agregarFilasReporte(ArrayList<ReporteFila> filas, String grupo, String consulta) {
        Cursor cursor = null;

        try {
            cursor = db.getDb().rawQuery(consulta, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String categoria = cursor.getString(cursor.getColumnIndexOrThrow("categoria"));
                    int total = cursor.getInt(cursor.getColumnIndexOrThrow("total"));
                    filas.add(new ReporteFila(grupo, formatearEtiqueta(categoria), total));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private void exportarReporteGeneralPdf() {
        try {
            if (db == null || db.getDb() == null || !db.getDb().isOpen()) {
                abrirBaseDatos();
            }

            ArrayList<ReporteFila> filas = obtenerFilasReporte();

            if (filas.isEmpty()) {
                Toast.makeText(this, "No hay datos para exportar.", Toast.LENGTH_LONG).show();
                return;
            }

            File carpetaTemporal = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);

            if (carpetaTemporal == null) {
                Toast.makeText(this, "No se pudo acceder a documentos temporales.", Toast.LENGTH_LONG).show();
                return;
            }

            if (!carpetaTemporal.exists()) {
                carpetaTemporal.mkdirs();
            }

            String fecha = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String nombreArchivo = "reporte_estadistico_labcare_" + fecha + ".pdf";
            File archivoTemporal = new File(carpetaTemporal, nombreArchivo);

            crearPdfReporte(archivoTemporal, filas);
            guardarArchivoEnDocumentos(archivoTemporal, nombreArchivo);

        } catch (Throwable e) {
            Toast.makeText(this, "No se pudo exportar PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void crearPdfReporte(File archivoPdf, ArrayList<ReporteFila> filas) throws Exception {
        PdfWriter writer = new PdfWriter(new FileOutputStream(archivoPdf));
        PdfDocument pdfDocument = new PdfDocument(writer);
        Document document = new Document(pdfDocument, PageSize.A4);
        document.setMargins(42, 36, 36, 36);

        PdfFont fontNormal = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        DeviceRgb colorEncabezado = new DeviceRgb(84, 110, 122);

        document.add(new Paragraph("Reporte estadístico LabCare")
                .setFont(fontBold)
                .setFontSize(18)
                .setFontColor(ColorConstants.BLACK)
                .setTextAlignment(TextAlignment.CENTER));

        document.add(new Paragraph("Generado: " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date()))
                .setFont(fontNormal)
                .setFontSize(10)
                .setFontColor(ColorConstants.DARK_GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(12));

        document.add(new Paragraph("Resumen de incidencias y mantenimientos registrado en la base de datos local de la aplicación.")
                .setFont(fontNormal)
                .setFontSize(10)
                .setFontColor(ColorConstants.DARK_GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(18));

        agregarTablaResumen(document, filas, fontNormal, fontBold, colorEncabezado);

        document.add(new Paragraph("Detalle estadístico")
                .setFont(fontBold)
                .setFontSize(12)
                .setMarginTop(16)
                .setMarginBottom(6));

        Table tabla = new Table(UnitValue.createPercentArray(new float[]{3f, 4f, 1.2f}))
                .useAllAvailableWidth()
                .setMarginTop(8);

        agregarCeldaEncabezado(tabla, "Grupo", fontBold, colorEncabezado);
        agregarCeldaEncabezado(tabla, "Categoría", fontBold, colorEncabezado);
        agregarCeldaEncabezado(tabla, "Total", fontBold, colorEncabezado);

        for (ReporteFila fila : filas) {
            agregarCeldaNormal(tabla, fila.grupo, fontNormal, TextAlignment.LEFT);
            agregarCeldaNormal(tabla, fila.categoria, fontNormal, TextAlignment.LEFT);
            agregarCeldaNormal(tabla, String.valueOf(fila.total), fontNormal, TextAlignment.CENTER);
        }

        document.add(tabla);
        document.close();
    }

    private void agregarTablaResumen(Document document, ArrayList<ReporteFila> filas,
                                     PdfFont fontNormal, PdfFont fontBold,
                                     DeviceRgb colorEncabezado) {
        document.add(new Paragraph("Indicadores principales")
                .setFont(fontBold)
                .setFontSize(12)
                .setMarginTop(8)
                .setMarginBottom(8));

        Table tablaResumen = new Table(UnitValue.createPercentArray(new float[]{4f, 1.2f}))
                .useAllAvailableWidth();

        agregarCeldaEncabezado(tablaResumen, "Indicador", fontBold, colorEncabezado);
        agregarCeldaEncabezado(tablaResumen, "Total", fontBold, colorEncabezado);

        for (ReporteFila fila : filas) {
            if ("Resumen general".equalsIgnoreCase(fila.grupo)) {
                agregarCeldaNormal(tablaResumen, fila.categoria, fontNormal, TextAlignment.LEFT);
                agregarCeldaNormal(tablaResumen, String.valueOf(fila.total), fontNormal, TextAlignment.CENTER);
            }
        }

        document.add(tablaResumen);
    }

    private void agregarCeldaEncabezado(Table tabla, String texto, PdfFont font, DeviceRgb colorEncabezado) {
        Cell celda = new Cell()
                .add(new Paragraph(texto).setFont(font).setFontSize(9).setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(colorEncabezado)
                .setTextAlignment(TextAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPadding(6);
        tabla.addCell(celda);
    }

    private void agregarCeldaNormal(Table tabla, String texto, PdfFont font, TextAlignment alineacion) {
        Cell celda = new Cell()
                .add(new Paragraph(texto == null ? "" : texto).setFont(font).setFontSize(9).setFontColor(ColorConstants.BLACK))
                .setTextAlignment(alineacion)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPadding(5);
        tabla.addCell(celda);
    }

    private void guardarArchivoEnDocumentos(File archivoTemporal, String nombreArchivo) {
        if (archivoTemporal == null || !archivoTemporal.exists()) {
            runOnUiThread(() -> Toast.makeText(this, "No se encontró el PDF temporal.", Toast.LENGTH_LONG).show());
            return;
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                guardarConMediaStoreDocumentos(archivoTemporal, nombreArchivo);
            } else {
                guardarEnDocumentosLegacyConPermiso(archivoTemporal, nombreArchivo);
            }

        } catch (Exception e) {
            runOnUiThread(() -> Toast.makeText(
                    this,
                    "Error al guardar PDF en Documentos: " + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show());
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

        if (uri == null) {
            throw new Exception("No se pudo crear el PDF en Documentos.");
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

        runOnUiThread(() -> Toast.makeText(
                this,
                "PDF guardado en Documentos: " + nombreArchivo,
                Toast.LENGTH_LONG
        ).show());
    }

    private void guardarEnDocumentosLegacyConPermiso(File archivoTemporal, String nombreArchivo) throws Exception {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            archivoTemporalPendiente = archivoTemporal;
            nombreArchivoPendiente = nombreArchivo;

            runOnUiThread(() -> ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_STORAGE
            ));

            return;
        }

        guardarEnDocumentosLegacy(archivoTemporal, nombreArchivo);
    }

    private void guardarEnDocumentosLegacy(File archivoTemporal, String nombreArchivo) throws Exception {
        File carpetaDocumentos = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);

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

        runOnUiThread(() -> Toast.makeText(
                this,
                "PDF guardado en Documentos: " + nombreArchivo,
                Toast.LENGTH_LONG
        ).show());
    }

    private String formatearEtiqueta(String texto) {
        if (texto == null || texto.trim().isEmpty()) return "Sin dato";
        return texto.replace("_", " ");
    }

    private void mostrarMensaje(String mensaje) {
        txtMensajeReporteGeneral.setText(mensaje);
        txtMensajeReporteGeneral.setVisibility(View.VISIBLE);
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
                            guardarEnDocumentosLegacy(archivoTemporalPendiente, nombreArchivoPendiente);
                        } catch (Exception e) {
                            runOnUiThread(() -> Toast.makeText(
                                    this,
                                    "Error al guardar PDF: " + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show());
                        }
                    }).start();
                }
            } else {
                Toast.makeText(this, "Permiso denegado. No se pudo guardar el PDF en Documentos.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private static class ReporteFila {
        String grupo;
        String categoria;
        int total;

        ReporteFila(String grupo, String categoria, int total) {
            this.grupo = grupo;
            this.categoria = categoria;
            this.total = total;
        }
    }
}
