package ues.fia.proyecto2_pdm115.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "labcare_sesion";
    private static final String KEY_LOGGED = "logged";
    private static final String KEY_ID_USUARIO = "id_usuario";
    private static final String KEY_CORREO = "correo";
    private static final String KEY_NOMBRE = "nombre";
    private static final String KEY_ROL = "rol";
    private static final String KEY_LAST_EMAIL = "last_email";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void guardarSesion(int idUsuario, String correo, String nombre, String rol) {
        prefs.edit()
                .putBoolean(KEY_LOGGED, true)
                .putInt(KEY_ID_USUARIO, idUsuario)
                .putString(KEY_CORREO, correo)
                .putString(KEY_NOMBRE, nombre)
                .putString(KEY_ROL, rol)
                .putString(KEY_LAST_EMAIL, correo)
                .apply();
    }

    public boolean sesionActiva() {
        return prefs.getBoolean(KEY_LOGGED, false);
    }

    public int getIdUsuario() {
        return prefs.getInt(KEY_ID_USUARIO, -1);
    }

    public String getCorreo() {
        return prefs.getString(KEY_CORREO, "");
    }

    public String getNombre() {
        return prefs.getString(KEY_NOMBRE, "");
    }

    public String getRol() {
        return prefs.getString(KEY_ROL, "");
    }

    public String getUltimoCorreo() {
        return prefs.getString(KEY_LAST_EMAIL, "");
    }

    public void guardarUltimoCorreo(String correo) {
        prefs.edit().putString(KEY_LAST_EMAIL, correo).apply();
    }

    public void cerrarSesion() {
        String ultimoCorreo = getUltimoCorreo();
        prefs.edit().clear().putString(KEY_LAST_EMAIL, ultimoCorreo).apply();
    }
}
