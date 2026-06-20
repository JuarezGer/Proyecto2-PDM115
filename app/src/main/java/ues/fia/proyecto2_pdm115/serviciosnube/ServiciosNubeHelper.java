package ues.fia.proyecto2_pdm115.serviciosnube;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;

import ues.fia.proyecto2_pdm115.controlDBLabCare;

public class ServiciosNubeHelper {

    public interface SyncCallback {
        void onSuccess(String mensaje);
        void onError(String error);
    }

    private final Context context;

    public ServiciosNubeHelper(Context context) {
        this.context = context;
    }

    public void probarConexion(SyncCallback callback) {
        ApiServiciosNubeClient.get(ConfigServiciosNube.URL_PROBAR_CONEXION, new ApiServiciosNubeClient.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject json = new JSONObject(response);
                    callback.onSuccess(json.optString("mensaje", "Conexión correcta."));
                } catch (Exception e) {
                    callback.onSuccess(response);
                }
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    public void guardarRegistrosEnServidor(SyncCallback callback) {
        JSONObject datos = construirJsonDesdeSQLite();

        if (datos == null) {
            callback.onError("No se pudieron leer los registros locales.");
            return;
        }

        ApiServiciosNubeClient.postJson(ConfigServiciosNube.URL_GUARDAR_TODO, datos, new ApiServiciosNubeClient.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject json = new JSONObject(response);
                    callback.onSuccess(json.optString("mensaje", "Registros guardados correctamente."));
                } catch (Exception e) {
                    callback.onSuccess(response);
                }
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    public void restaurarRegistrosDesdeServidor(SyncCallback callback) {
        ApiServiciosNubeClient.get(ConfigServiciosNube.URL_RESTAURAR_TODO, new ApiServiciosNubeClient.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject json = new JSONObject(response);

                    if (!json.optBoolean("ok", false)) {
                        callback.onError(json.optString("mensaje", "No se pudo restaurar."));
                        return;
                    }

                    JSONObject datos = json.getJSONObject("datos");
                    restaurarEnSQLite(datos);

                    callback.onSuccess("Registros restaurados en SQLite correctamente.");

                } catch (Exception e) {
                    callback.onError("Error al procesar restauración: " + e.getMessage());
                }
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    private JSONObject construirJsonDesdeSQLite() {
        controlDBLabCare helper = new controlDBLabCare(context);

        try {
            helper.abrir();
            SQLiteDatabase db = helper.getDb();

            JSONObject json = new JSONObject();
            json.put("roles", tablaAJsonArray(db, "roles"));
            json.put("usuarios", tablaAJsonArray(db, "usuarios"));

            // Dependencias necesarias para que equipos/incidencias/mantenimientos funcionen.
            json.put("edificios", tablaAJsonArray(db, "edificios"));
            json.put("laboratorios", tablaAJsonArray(db, "laboratorios"));
            json.put("categorias_equipo", tablaAJsonArray(db, "categorias_equipo"));
            json.put("equipos", tablaAJsonArray(db, "equipos"));
            json.put("tipos_incidencia", tablaAJsonArray(db, "tipos_incidencia"));

            json.put("incidencias", tablaAJsonArray(db, "incidencias"));
            json.put("mantenimientos", tablaAJsonArray(db, "mantenimientos"));

            return json;

        } catch (Exception e) {
            return null;
        } finally {
            helper.cerrar();
        }
    }

    private JSONArray tablaAJsonArray(SQLiteDatabase db, String tabla) throws Exception {
        JSONArray array = new JSONArray();
        Cursor cursor = null;

        try {
            cursor = db.rawQuery("SELECT * FROM " + tabla, null);

            if (cursor.moveToFirst()) {
                do {
                    JSONObject fila = new JSONObject();

                    for (int i = 0; i < cursor.getColumnCount(); i++) {
                        String columna = cursor.getColumnName(i);

                        if (cursor.isNull(i)) {
                            fila.put(columna, JSONObject.NULL);
                        } else {
                            fila.put(columna, cursor.getString(i));
                        }
                    }

                    array.put(fila);

                } while (cursor.moveToNext());
            }

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return array;
    }

    private void restaurarEnSQLite(JSONObject datos) throws Exception {
        controlDBLabCare helper = new controlDBLabCare(context);
        helper.abrir();
        SQLiteDatabase db = helper.getDb();

        db.beginTransaction();

        try {
            upsertFilas(db, "roles", "id_rol", datos.optJSONArray("roles"));
            upsertFilas(db, "usuarios", "id_usuario", datos.optJSONArray("usuarios"));

            // Dependencias necesarias.
            upsertFilas(db, "edificios", "id_edificio", datos.optJSONArray("edificios"));
            upsertFilas(db, "laboratorios", "id_laboratorio", datos.optJSONArray("laboratorios"));
            upsertFilas(db, "categorias_equipo", "id_categoria", datos.optJSONArray("categorias_equipo"));
            upsertFilas(db, "equipos", "id_equipo", datos.optJSONArray("equipos"));
            upsertFilas(db, "tipos_incidencia", "id_tipo_incidencia", datos.optJSONArray("tipos_incidencia"));

            upsertFilas(db, "incidencias", "id_incidencia", datos.optJSONArray("incidencias"));
            upsertFilas(db, "mantenimientos", "id_mantenimiento", datos.optJSONArray("mantenimientos"));

            db.setTransactionSuccessful();

        } finally {
            db.endTransaction();
            helper.cerrar();
        }
    }

    private void upsertFilas(SQLiteDatabase db, String tabla, String pk, JSONArray filas) throws Exception {
        if (filas == null) return;

        for (int i = 0; i < filas.length(); i++) {
            JSONObject objeto = filas.getJSONObject(i);
            ContentValues valores = jsonAContentValues(objeto);

            if (!objeto.has(pk) || objeto.isNull(pk)) {
                continue;
            }

            String id = objeto.optString(pk);

            int actualizadas = db.update(tabla, valores, pk + " = ?", new String[]{id});

            if (actualizadas == 0) {
                db.insertWithOnConflict(tabla, null, valores, SQLiteDatabase.CONFLICT_IGNORE);
            }
        }
    }

    private ContentValues jsonAContentValues(JSONObject objeto) throws Exception {
        ContentValues valores = new ContentValues();
        Iterator<String> keys = objeto.keys();

        while (keys.hasNext()) {
            String key = keys.next();

            if (objeto.isNull(key)) {
                valores.putNull(key);
            } else {
                valores.put(key, objeto.optString(key));
            }
        }

        return valores;
    }
}
