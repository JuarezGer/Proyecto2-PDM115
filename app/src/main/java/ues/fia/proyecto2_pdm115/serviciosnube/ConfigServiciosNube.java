package ues.fia.proyecto2_pdm115.serviciosnube;

public class ConfigServiciosNube {

    // Emulador Android Studio:
    public static final String BASE_URL = "http://192.168.18.12/labcare_api/";

    // Celular físico conectado a la misma red Wi-Fi que la PC:
    // public static final String BASE_URL = "http://192.168.1.10/labcare_api/";

    public static final String URL_PROBAR_CONEXION = BASE_URL + "probar_conexion.php";
    public static final String URL_DATOS_NUBE = BASE_URL + "sync/datos_nube.php";
    public static final String URL_GUARDAR_TODO = BASE_URL + "sync/guardar_todo.php";
    public static final String URL_RESTAURAR_TODO = BASE_URL + "sync/restaurar_todo.php";
    public static final String URL_ROLES_USUARIOS = BASE_URL + "sync/roles_usuarios.php";
    public static final String URL_ACTUALIZAR_ROL = BASE_URL + "sync/actualizar_rol.php";
    public static final String URL_ACTUALIZAR_USUARIO = BASE_URL + "sync/actualizar_usuario.php";
}
