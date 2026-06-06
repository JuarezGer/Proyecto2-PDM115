package ues.fia.proyecto2_pdm115;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class CrudHelper {

    public static class SpinnerItem {
        public int id;
        public String texto;

        public SpinnerItem(int id, String texto) {
            this.id = id;
            this.texto = texto;
        }

        @Override
        public String toString() {
            return texto;
        }
    }

    public static String texto(EditText editText) {
        return editText.getText().toString().trim();
    }

    public static int entero(EditText editText) {
        String valor = texto(editText);
        if (valor.isEmpty()) return 0;
        return Integer.parseInt(valor);
    }

    public static Integer enteroNullable(EditText editText) {
        String valor = texto(editText);
        if (valor.isEmpty()) return null;
        return Integer.parseInt(valor);
    }

    public static Double doubleNullable(EditText editText) {
        String valor = texto(editText);
        if (valor.isEmpty()) return null;
        return Double.parseDouble(valor);
    }

    public static void mensaje(Context context, String texto) {
        Toast.makeText(context, texto, Toast.LENGTH_LONG).show();
    }

    public static int idSeleccionado(Spinner spinner) {
        Object item = spinner.getSelectedItem();
        if (item instanceof SpinnerItem) {
            return ((SpinnerItem) item).id;
        }
        return -1;
    }

    public static Integer idSeleccionadoNullable(Spinner spinner) {
        int id = idSeleccionado(spinner);
        return id <= 0 ? null : id;
    }

    public static void cargarSpinner(Context context, Spinner spinner, Cursor cursor,
                                     String columnaId, String... columnasTexto) {
        ArrayList<SpinnerItem> items = new ArrayList<>();
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    do {
                        int id = cursor.getInt(cursor.getColumnIndexOrThrow(columnaId));
                        String texto = construirTexto(cursor, columnasTexto);
                        items.add(new SpinnerItem(id, texto));
                    } while (cursor.moveToNext());
                }
            } finally {
                cursor.close();
            }
        }
        ArrayAdapter<SpinnerItem> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    public static void cargarSpinnerConNulo(Context context, Spinner spinner, Cursor cursor,
                                            String columnaId, String textoNulo, String... columnasTexto) {
        ArrayList<SpinnerItem> items = new ArrayList<>();
        items.add(new SpinnerItem(0, textoNulo));
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    do {
                        int id = cursor.getInt(cursor.getColumnIndexOrThrow(columnaId));
                        String texto = construirTexto(cursor, columnasTexto);
                        items.add(new SpinnerItem(id, texto));
                    } while (cursor.moveToNext());
                }
            } finally {
                cursor.close();
            }
        }
        ArrayAdapter<SpinnerItem> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    public static void cargarSpinnerTexto(Context context, Spinner spinner, String... valores) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, valores);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    public static String textoSeleccionado(Spinner spinner) {
        Object item = spinner.getSelectedItem();
        return item == null ? "" : item.toString();
    }

    public static int enteroSeleccionado(Spinner spinner) {
        String valor = textoSeleccionado(spinner);
        return valor.startsWith("Sí") || valor.startsWith("Activo") || valor.equals("1") ? 1 : 0;
    }

    public static void seleccionarSpinnerPorId(Spinner spinner, int id) {
        for (int i = 0; i < spinner.getCount(); i++) {
            Object item = spinner.getItemAtPosition(i);
            if (item instanceof SpinnerItem && ((SpinnerItem) item).id == id) {
                spinner.setSelection(i);
                return;
            }
        }
    }

    public static void seleccionarSpinnerPorTexto(Spinner spinner, String texto) {
        if (texto == null) return;
        for (int i = 0; i < spinner.getCount(); i++) {
            Object item = spinner.getItemAtPosition(i);
            if (item != null && item.toString().equalsIgnoreCase(texto)) {
                spinner.setSelection(i);
                return;
            }
        }
    }

    public static void seleccionarSiNo(Spinner spinner, String valor) {
        if ("1".equals(valor) || "true".equalsIgnoreCase(valor)) spinner.setSelection(0);
        else spinner.setSelection(1);
    }

    public static void poner(EditText editText, HashMap<String, String> mapa, String clave) {
        editText.setText(mapa.get(clave) == null ? "" : mapa.get(clave));
    }

    public static int enteroMapa(HashMap<String, String> mapa, String clave) {
        try {
            String valor = mapa.get(clave);
            if (valor == null || valor.trim().isEmpty()) return 0;
            return Integer.parseInt(valor);
        } catch (Exception e) {
            return 0;
        }
    }

    public static HashMap<String, String> consultarPorId(SQLiteDatabase db, String tabla, String columnaId, int id) {
        Cursor cursor = null;
        try {
            cursor = db.query(tabla, null, columnaId + " = ?", new String[]{String.valueOf(id)}, null, null, null);
            if (cursor.moveToFirst()) return cursorAMap(cursor);
            return null;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    public static void mostrarCursorEnTextView(TextView textView, Cursor cursor) {
        StringBuilder sb = new StringBuilder();
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    int contador = 1;
                    do {
                        sb.append("Registro #").append(contador++).append("\n");
                        for (int i = 0; i < cursor.getColumnCount(); i++) {
                            sb.append(cursor.getColumnName(i))
                                    .append(": ")
                                    .append(cursor.isNull(i) ? "" : cursor.getString(i))
                                    .append("\n");
                        }
                        sb.append("\n-----------------------------\n\n");
                    } while (cursor.moveToNext());
                } else {
                    sb.append("No hay registros para mostrar.");
                }
            } finally {
                cursor.close();
            }
        } else {
            sb.append("No se pudo obtener la información.");
        }
        textView.setText(sb.toString());
    }

    private static String construirTexto(Cursor cursor, String... columnasTexto) {
        StringBuilder sb = new StringBuilder();
        for (String columna : columnasTexto) {
            int index = cursor.getColumnIndex(columna);
            if (index >= 0 && !cursor.isNull(index)) {
                if (sb.length() > 0) sb.append(" - ");
                sb.append(cursor.getString(index));
            }
        }
        return sb.toString();
    }

    private static HashMap<String, String> cursorAMap(Cursor cursor) {
        HashMap<String, String> map = new HashMap<>();
        for (int i = 0; i < cursor.getColumnCount(); i++) {
            map.put(cursor.getColumnName(i), cursor.isNull(i) ? "" : cursor.getString(i));
        }
        return map;
    }
}
