package ues.fia.proyecto2_pdm115.mantenimiento;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.core.content.FileProvider;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.File;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PdfMantenimientoHelper {

    private final Context context;

    public PdfMantenimientoHelper(Context context) {
        this.context = context;
    }

    // Método principal — Ahora devuelve el String de la URI o Ruta Absoluta
    public String generarPdf(Cursor cursor, int idMantenimiento) {
        if (cursor == null || !cursor.moveToFirst()) return null;

        String nombreArchivo = "mantenimiento_" + idMantenimiento + "_" +
                new SimpleDateFormat("yyyyMMdd_HHmmss",
                        Locale.getDefault()).format(new Date()) + ".pdf";

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                return guardarConMediaStore(cursor, nombreArchivo);
            } else {
                return guardarConFile(cursor, nombreArchivo);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            cursor.close();
        }
    }

    // Android 10+
    private String guardarConMediaStore(Cursor cursor, String nombreArchivo) throws Exception {
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, nombreArchivo);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS);

        Uri uri = context.getContentResolver()
                .insert(MediaStore.Files.getContentUri("external"), values);
        if (uri == null) return null;

        try (OutputStream os = context.getContentResolver().openOutputStream(uri)) {
            escribirPdf(cursor, os);
        }
        return uri.toString(); // Retorna "content://..."
    }

    // Android 7, 8, 9
    private String guardarConFile(Cursor cursor, String nombreArchivo) throws Exception {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        if (!dir.exists()) dir.mkdirs();
        File archivo = new File(dir, nombreArchivo);
        try (java.io.FileOutputStream fos = new java.io.FileOutputStream(archivo)) {
            escribirPdf(cursor, fos);
        }
        return archivo.getAbsolutePath(); // Retorna "/storage/emulated/0/..."
    }

    // Construcción del PDF con iText
    private void escribirPdf(Cursor cursor, OutputStream os) throws Exception {
        PdfWriter writer = new PdfWriter(os);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        // — Encabezado —
        document.add(new Paragraph("Reporte de Mantenimiento")
                .setFontSize(20)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER));

        document.add(new Paragraph("ID: " + obtenerStringSeguro(cursor, "id_mantenimiento") +
                "    Generado: " + new SimpleDateFormat("dd/MM/yyyy HH:mm",
                Locale.getDefault()).format(new Date()))
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER));

        agregarSeparador(document);

        // — Datos del mantenimiento —
        agregarTituloSeccion(document, "Datos del Mantenimiento");
        agregarTabla(document, new String[][]{
                {"Tipo", obtenerStringSeguro(cursor, "tipo_mantenimiento")},
                {"Estado", obtenerStringSeguro(cursor, "estado_mantenimiento")},
                {"Diagnóstico", obtenerStringSeguro(cursor, "diagnostico")},
                {"Solución aplicada", obtenerStringSeguro(cursor, "solucion_aplicada")},
                {"Fecha inicio", obtenerStringSeguro(cursor, "fecha_inicio")},
                {"Fecha fin", obtenerStringSeguro(cursor, "fecha_fin")}
        });

        agregarSeparador(document);

        // — Datos del equipo —
        agregarTituloSeccion(document, "Equipo");
        agregarTabla(document, new String[][]{
                {"Nombre", obtenerStringSeguro(cursor, "nombre_equipo")},
                {"Código inventario", obtenerStringSeguro(cursor, "codigo_inventario")},
                {"Marca", obtenerStringSeguro(cursor, "marca")},
                {"Modelo", obtenerStringSeguro(cursor, "modelo")},
                {"Estado", obtenerStringSeguro(cursor, "estado_equipo")}
        });

        agregarSeparador(document);

        // — Ubicación —
        agregarTituloSeccion(document, "Ubicación");
        agregarTabla(document, new String[][]{
                {"Laboratorio", obtenerStringSeguro(cursor, "nombre_laboratorio")},
                {"Piso", obtenerStringSeguro(cursor, "piso")},
                {"Edificio", obtenerStringSeguro(cursor, "nombre_edificio")}
        });

        agregarSeparador(document);

        // — Incidencia —
        agregarTituloSeccion(document, "Incidencia Relacionada");
        String tituloInc = cursor.getString(cursor.getColumnIndexOrThrow("titulo_incidencia"));

        if (tituloInc == null || tituloInc.trim().isEmpty()) {
            document.add(new Paragraph("Sin incidencia asociada")
                    .setItalic().setFontSize(11));
        } else {
            agregarTabla(document, new String[][]{
                    {"Título", tituloInc},
                    {"Descripción", obtenerStringSeguro(cursor, "descripcion_incidencia")},
                    {"Prioridad", obtenerStringSeguro(cursor, "prioridad")},
                    {"Fecha reporte", obtenerStringSeguro(cursor, "fecha_reporte")}
            });
        }

        agregarSeparador(document);

        // — Personal —
        agregarTituloSeccion(document, "Personal");
        agregarTabla(document, new String[][]{
                {"Creado por", obtenerStringSeguro(cursor, "nombre_creador")},
                {"Técnico asignado", obtenerStringSeguro(cursor, "nombre_tecnico")}
        });

        document.close();
    }

    // Evita crashes si el valor en la BD es null
    private String obtenerStringSeguro(Cursor cursor, String columna) {
        int index = cursor.getColumnIndexOrThrow(columna);
        if (cursor.isNull(index)) {
            return "—";
        }
        return cursor.getString(index);
    }

    private void agregarTituloSeccion(Document doc, String titulo) {
        doc.add(new Paragraph(titulo)
                .setFontSize(13)
                .setBold()
                .setFontColor(ColorConstants.DARK_GRAY));
    }

    private void agregarSeparador(Document doc) {
        doc.add(new Paragraph(" "));
    }

    private void agregarTabla(Document doc, String[][] filas) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
                .setWidth(UnitValue.createPercentValue(100));
        for (String[] fila : filas) {
            table.addCell(new Cell().add(new Paragraph(fila[0]).setBold())
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY));
            String valor = fila[1] != null ? fila[1] : "—";
            table.addCell(new Cell().add(new Paragraph(valor)));
        }
        doc.add(table);
    }

    // Método polimórfico universal para abrir el archivo (soporta rutas y URIs de MediaStore)
    public void abrirPdf(String rutaORuri) {
        if (rutaORuri == null) return;

        Uri uri;
        if (rutaORuri.startsWith("content://")) {
            uri = Uri.parse(rutaORuri);
        } else {
            File file = new File(rutaORuri);
            uri = FileProvider.getUriForFile(context,
                    context.getPackageName() + ".fileprovider", file);
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/pdf");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}