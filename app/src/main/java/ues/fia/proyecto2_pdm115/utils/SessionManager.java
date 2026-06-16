package ues.fia.proyecto2_pdm115.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "labcare_session";

    private static final String KEY_SESION_ACTIVA = "sesion_activa";
    private static final String KEY_ID_USUARIO = "id_usuario";
    private static final String KEY_CORREO = "correo";
    private static final String KEY_NOMBRE_USUARIO = "nombre_usuario";
    private static final String KEY_ROL_USUARIO = "rol_usuario";
    private static final String KEY_ULTIMO_CORREO = "ultimo_correo";

    private final SharedPreferences preferences;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    public void guardarSesion(int idUsuario, String correo, String nombreUsuario, String rolUsuario) {
        editor.putBoolean(KEY_SESION_ACTIVA, true);
        editor.putInt(KEY_ID_USUARIO, idUsuario);
        editor.putString(KEY_CORREO, correo);
        editor.putString(KEY_NOMBRE_USUARIO, nombreUsuario);
        editor.putString(KEY_ROL_USUARIO, rolUsuario);
        editor.putString(KEY_ULTIMO_CORREO, correo);
        editor.apply();
    }

    public boolean sesionActiva() {
        return preferences.getBoolean(KEY_SESION_ACTIVA, false);
    }

    public void cerrarSesion() {
        String ultimoCorreo = getUltimoCorreo();

        editor.clear();

        if (ultimoCorreo != null && !ultimoCorreo.trim().isEmpty()) {
            editor.putString(KEY_ULTIMO_CORREO, ultimoCorreo);
        }

        editor.apply();
    }

    public int getIdUsuario() {
        return preferences.getInt(KEY_ID_USUARIO, -1);
    }

    public String getCorreo() {
        return preferences.getString(KEY_CORREO, "");
    }

    public String getCorreoUsuario() {
        return getCorreo();
    }

    public String getNombreUsuario() {
        return preferences.getString(KEY_NOMBRE_USUARIO, "");
    }

    public String getNombre() {
        return getNombreUsuario();
    }

    public String getRolUsuario() {
        return preferences.getString(KEY_ROL_USUARIO, "");
    }

    public String getRol() {
        return getRolUsuario();
    }

    public String getUltimoCorreo() {
        return preferences.getString(KEY_ULTIMO_CORREO, "");
    }

    public String getPrimerNombreUsuario() {
        String nombreCompleto = getNombreUsuario();

        if (nombreCompleto == null) {
            return "";
        }

        nombreCompleto = nombreCompleto.trim();

        if (nombreCompleto.isEmpty()) {
            return "";
        }

        String[] partes = nombreCompleto.split("\\s+");
        return partes.length > 0 ? partes[0] : nombreCompleto;
    }
}
