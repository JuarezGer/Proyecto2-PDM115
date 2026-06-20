package ues.fia.proyecto2_pdm115.serviciosnube;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeSet;

import ues.fia.proyecto2_pdm115.controlDBLabCare;

public class ServiciosNubeHelper {

    public interface SyncCallback {
        void onSuccess(String mensaje);
        void onError(String error);
    }

    public interface JsonCallback {
        void onSuccess(JSONObject json);
        void onError(String error);
    }

    public interface ComparacionCallback {
        void onSuccess(JSONObject comparacion, boolean hayDiferencias);
        void onError(String error);
    }

    private final Context context;

    private static final String[] ORDEN_TABLAS = {
            "roles", "usuarios", "edificios", "laboratorios", "categorias_equipo",
            "equipos", "tipos_incidencia", "incidencias", "mantenimientos"
    };

    private static final Map<String, String> PKS = new HashMap<>();

    static {
        PKS.put("roles", "id_rol");
        PKS.put("usuarios", "id_usuario");
        PKS.put("edificios", "id_edificio");
        PKS.put("laboratorios", "id_laboratorio");
        PKS.put("categorias_equipo", "id_categoria");
        PKS.put("equipos", "id_equipo");
        PKS.put("tipos_incidencia", "id_tipo_incidencia");
        PKS.put("incidencias", "id_incidencia");
        PKS.put("mantenimientos", "id_mantenimiento");
    }

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

    public void obtenerDatosNube(JsonCallback callback) {
        ApiServiciosNubeClient.get(ConfigServiciosNube.URL_DATOS_NUBE, new ApiServiciosNubeClient.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject json = new JSONObject(response);
                    if (!json.optBoolean("ok", false)) {
                        callback.onError(json.optString("mensaje", "No se pudieron obtener datos."));
                        return;
                    }
                    callback.onSuccess(json.getJSONObject("datos"));
                } catch (Exception e) {
                    callback.onError("Error procesando datos de nube: " + e.getMessage());
                }
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    public void obtenerDatosRemotos(JsonCallback callback) {
        ApiServiciosNubeClient.get(ConfigServiciosNube.URL_RESTAURAR_TODO, new ApiServiciosNubeClient.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject json = new JSONObject(response);
                    if (!json.optBoolean("ok", false)) {
                        callback.onError(json.optString("mensaje", "No se pudo descargar la base remota."));
                        return;
                    }
                    callback.onSuccess(json.getJSONObject("datos"));
                } catch (Exception e) {
                    callback.onError("Error procesando base remota: " + e.getMessage());
                }
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    public void obtenerRolesUsuariosRemotos(JsonCallback callback) {
        ApiServiciosNubeClient.get(ConfigServiciosNube.URL_ROLES_USUARIOS, new ApiServiciosNubeClient.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject json = new JSONObject(response);
                    if (!json.optBoolean("ok", false)) {
                        callback.onError(json.optString("mensaje", "No se pudieron obtener roles y usuarios."));
                        return;
                    }
                    callback.onSuccess(json.getJSONObject("datos"));
                } catch (Exception e) {
                    callback.onError("Error procesando roles y usuarios remotos: " + e.getMessage());
                }
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    public JSONObject obtenerRolesUsuariosLocales() throws Exception {
        controlDBLabCare helper = new controlDBLabCare(context);
        try {
            helper.abrir();
            SQLiteDatabase db = helper.getDb();
            JSONObject json = new JSONObject();
            json.put("roles", tablaAJsonArray(db, "roles"));
            json.put("usuarios", tablaAJsonArray(db, "usuarios"));
            return json;
        } finally {
            helper.cerrar();
        }
    }

    public void guardarLocalEnServidor(SyncCallback callback) {
        JSONObject datos;
        try {
            datos = construirJsonDesdeSQLite();
        } catch (Exception e) {
            callback.onError("No se pudieron leer los registros locales: " + e.getMessage());
            return;
        }

        ApiServiciosNubeClient.postJson(ConfigServiciosNube.URL_GUARDAR_TODO, datos, new ApiServiciosNubeClient.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject json = new JSONObject(response);
                    callback.onSuccess(json.optString("mensaje", "Registros guardados en servidor."));
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

    public void restaurarRemotoEnLocal(SyncCallback callback) {
        obtenerDatosRemotos(new JsonCallback() {
            @Override
            public void onSuccess(JSONObject datos) {
                try {
                    restaurarEnSQLite(datos);
                    callback.onSuccess("Registros remotos restaurados en SQLite local correctamente.");
                } catch (Exception e) {
                    callback.onError("Error al guardar remoto en SQLite: " + e.getMessage());
                }
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    public void compararConServidor(String[] tablas, ComparacionCallback callback) {
        obtenerDatosRemotos(new JsonCallback() {
            @Override
            public void onSuccess(JSONObject remoto) {
                try {
                    JSONObject local = construirJsonDesdeSQLite();
                    JSONObject comparacion = compararJson(local, remoto, tablas);
                    boolean hayDiferencias = comparacion.optInt("total_diferencias", 0) > 0;
                    callback.onSuccess(comparacion, hayDiferencias);
                } catch (Exception e) {
                    callback.onError("Error al comparar datos: " + e.getMessage());
                }
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    public JSONObject compararRolesUsuariosLocalRemoto(JSONObject remoto) throws Exception {
        JSONObject local = obtenerRolesUsuariosLocales();
        return compararJson(local, remoto, new String[]{"roles", "usuarios"});
    }

    public void actualizarRolAmbasBases(JSONObject rol, SyncCallback callback) {
        ApiServiciosNubeClient.postJson(ConfigServiciosNube.URL_ACTUALIZAR_ROL, rol, new ApiServiciosNubeClient.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject json = new JSONObject(response);
                    if (!json.optBoolean("ok", false)) {
                        callback.onError(json.optString("mensaje", "No se actualizó el rol remoto."));
                        return;
                    }
                    actualizarRolLocal(rol);
                    callback.onSuccess("Rol actualizado en base local y remota.");
                } catch (Exception e) {
                    callback.onError("Error actualizando rol local: " + e.getMessage());
                }
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    public void actualizarUsuarioAmbasBases(JSONObject usuario, SyncCallback callback) {
        ApiServiciosNubeClient.postJson(ConfigServiciosNube.URL_ACTUALIZAR_USUARIO, usuario, new ApiServiciosNubeClient.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject json = new JSONObject(response);
                    if (!json.optBoolean("ok", false)) {
                        callback.onError(json.optString("mensaje", "No se actualizó el usuario remoto."));
                        return;
                    }
                    actualizarUsuarioLocal(usuario);
                    callback.onSuccess("Usuario actualizado en base local y remota.");
                } catch (Exception e) {
                    callback.onError("Error actualizando usuario local: " + e.getMessage());
                }
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    private void actualizarRolLocal(JSONObject rol) throws Exception {
        controlDBLabCare helper = new controlDBLabCare(context);
        try {
            helper.abrir();
            ContentValues valores = new ContentValues();
            valores.put("nombre", rol.optString("nombre", ""));
            valores.put("descripcion", rol.optString("descripcion", ""));
            helper.getDb().update("roles", valores, "id_rol = ?", new String[]{rol.optString("id_rol")});
        } finally {
            helper.cerrar();
        }
    }

    private void actualizarUsuarioLocal(JSONObject usuario) throws Exception {
        controlDBLabCare helper = new controlDBLabCare(context);
        try {
            helper.abrir();
            ContentValues valores = new ContentValues();
            valores.put("id_rol", usuario.optString("id_rol"));
            valores.put("nombres", usuario.optString("nombres", ""));
            valores.put("apellidos", usuario.optString("apellidos", ""));
            valores.put("correo", usuario.optString("correo", ""));
            valores.put("clave_hash", usuario.optString("clave_hash", ""));
            valores.put("usa_biometria", usuario.optString("usa_biometria", "0"));
            valores.put("activo", usuario.optString("activo", "1"));
            helper.getDb().update("usuarios", valores, "id_usuario = ?", new String[]{usuario.optString("id_usuario")});
        } finally {
            helper.cerrar();
        }
    }

    private JSONObject construirJsonDesdeSQLite() throws Exception {
        controlDBLabCare helper = new controlDBLabCare(context);
        try {
            helper.abrir();
            SQLiteDatabase db = helper.getDb();
            JSONObject json = new JSONObject();
            for (String tabla : ORDEN_TABLAS) {
                json.put(tabla, tablaAJsonArray(db, tabla));
            }
            return json;
        } finally {
            helper.cerrar();
        }
    }

    private JSONArray tablaAJsonArray(SQLiteDatabase db, String tabla) throws Exception {
        JSONArray array = new JSONArray();
        Cursor cursor = null;
        String pk = PKS.get(tabla);
        String orderBy = pk == null ? "" : " ORDER BY " + pk + " ASC";

        try {
            cursor = db.rawQuery("SELECT * FROM " + tabla + orderBy, null);
            if (cursor.moveToFirst()) {
                do {
                    JSONObject fila = new JSONObject();
                    for (int i = 0; i < cursor.getColumnCount(); i++) {
                        String columna = cursor.getColumnName(i);
                        if (cursor.isNull(i)) fila.put(columna, JSONObject.NULL);
                        else fila.put(columna, cursor.getString(i));
                    }
                    array.put(fila);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return array;
    }

    private void restaurarEnSQLite(JSONObject datos) throws Exception {
        controlDBLabCare helper = new controlDBLabCare(context);
        helper.abrir();
        SQLiteDatabase db = helper.getDb();
        db.beginTransaction();

        try {
            for (String tabla : ORDEN_TABLAS) {
                upsertFilas(db, tabla, PKS.get(tabla), datos.optJSONArray(tabla));
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            helper.cerrar();
        }
    }

    private void upsertFilas(SQLiteDatabase db, String tabla, String pk, JSONArray filas) throws Exception {
        if (filas == null || pk == null) return;

        for (int i = 0; i < filas.length(); i++) {
            JSONObject objeto = filas.getJSONObject(i);
            ContentValues valores = jsonAContentValues(objeto);
            if (!objeto.has(pk) || objeto.isNull(pk)) continue;
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
            if (objeto.isNull(key)) valores.putNull(key);
            else valores.put(key, objeto.optString(key));
        }
        return valores;
    }

    private JSONObject compararJson(JSONObject local, JSONObject remoto, String[] tablas) throws Exception {
        JSONObject resultado = new JSONObject();
        int totalDiferencias = 0;

        for (String tabla : tablas) {
            String pk = PKS.get(tabla);
            if (pk == null) continue;

            JSONArray difs = compararTabla(local.optJSONArray(tabla), remoto.optJSONArray(tabla), pk);
            resultado.put(tabla, difs);
            totalDiferencias += difs.length();
        }

        resultado.put("total_diferencias", totalDiferencias);
        resultado.put("tablas", new JSONArray(Arrays.asList(tablas)));
        return resultado;
    }

    private JSONArray compararTabla(JSONArray local, JSONArray remoto, String pk) throws Exception {
        JSONArray diferencias = new JSONArray();
        Map<String, JSONObject> mapLocal = arrayAMap(local, pk);
        Map<String, JSONObject> mapRemoto = arrayAMap(remoto, pk);
        TreeSet<String> ids = new TreeSet<>();
        ids.addAll(mapLocal.keySet());
        ids.addAll(mapRemoto.keySet());

        for (String id : ids) {
            JSONObject l = mapLocal.get(id);
            JSONObject r = mapRemoto.get(id);
            JSONObject dif = new JSONObject();
            dif.put("id", id);

            if (l == null) {
                dif.put("tipo", "solo_remoto");
                dif.put("local", JSONObject.NULL);
                dif.put("remoto", r);
                diferencias.put(dif);
            } else if (r == null) {
                dif.put("tipo", "solo_local");
                dif.put("local", l);
                dif.put("remoto", JSONObject.NULL);
                diferencias.put(dif);
            } else if (!firmaObjeto(l).equals(firmaObjeto(r))) {
                dif.put("tipo", "diferente");
                dif.put("local", l);
                dif.put("remoto", r);
                diferencias.put(dif);
            }
        }

        return diferencias;
    }

    private Map<String, JSONObject> arrayAMap(JSONArray array, String pk) throws Exception {
        Map<String, JSONObject> map = new LinkedHashMap<>();
        if (array == null) return map;

        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            if (obj.has(pk) && !obj.isNull(pk)) {
                map.put(obj.optString(pk), obj);
            }
        }
        return map;
    }

    private String firmaObjeto(JSONObject obj) throws Exception {
        TreeSet<String> keys = new TreeSet<>();
        Iterator<String> it = obj.keys();
        while (it.hasNext()) keys.add(it.next());

        StringBuilder sb = new StringBuilder();
        for (String key : keys) {
            Object value = obj.isNull(key) ? "" : obj.optString(key, "");
            sb.append(key).append('=').append(String.valueOf(value).trim()).append(';');
        }
        return sb.toString();
    }
}
