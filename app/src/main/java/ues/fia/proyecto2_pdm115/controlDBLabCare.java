package ues.fia.proyecto2_pdm115;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import ues.fia.proyecto2_pdm115.utils.*;

import java.util.ArrayList;
import java.util.HashMap;

public class controlDBLabCare {

    private static final String BASE_DATOS = "labcare.db";
    private static final int VERSION = 2;

    private final Context context;
    private DatabaseHelper DBHelper;
    private SQLiteDatabase db;

    public controlDBLabCare(Context ctx) {
        this.context = ctx;
        DBHelper = new DatabaseHelper(context);
    }

    // =========================================================
    // APERTURA / CIERRE
    // =========================================================

    public void abrir() throws SQLException {
        db = DBHelper.getWritableDatabase();
        db.execSQL("PRAGMA foreign_keys=ON;");
    }

    public void cerrar() {
        if (DBHelper != null) {
            DBHelper.close();
        }
        db = null;
    }

    public SQLiteDatabase getDb() {
        return db;
    }

    // =========================================================
    // LOGIN
    // =========================================================

    public boolean validarLogin(String correo, String clavePlano) {
        String claveHash = SeguridadUtils.sha256SiNecesario(clavePlano);
        return validarLoginHash(correo, claveHash);
    }

    public boolean validarLoginHash(String correo, String claveHash) {
        Cursor cursor = null;
        try {
            cursor = db.query(
                    "usuarios",
                    null,
                    "correo = ? AND clave_hash = ? AND activo = 1",
                    new String[]{correo, claveHash},
                    null,
                    null,
                    null
            );
            return cursor.moveToFirst();
        } catch (Exception e) {
            return false;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    public boolean usuarioUsaBiometria(String correo) {
        Cursor cursor = null;
        try {
            cursor = db.query(
                    "usuarios",
                    new String[]{"usa_biometria"},
                    "correo = ? AND activo = 1",
                    new String[]{correo},
                    null,
                    null,
                    null
            );
            if (cursor.moveToFirst()) {
                return cursor.getInt(cursor.getColumnIndexOrThrow("usa_biometria")) == 1;
            }
            return false;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    public HashMap<String, String> consultarUsuarioPorCorreo(String correo) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(
                    "SELECT u.*, r.nombre AS rol " +
                            "FROM usuarios u " +
                            "INNER JOIN roles r ON r.id_rol = u.id_rol " +
                            "WHERE u.correo = ? AND u.activo = 1",
                    new String[]{correo}
            );
            if (cursor.moveToFirst()) {
                return cursorAMap(cursor);
            }
            return null;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    public String activarBiometriaUsuario(int idUsuario, boolean activar) {
        try {
            ContentValues valores = new ContentValues();
            valores.put("usa_biometria", activar ? 1 : 0);
            int filas = db.update(
                    "usuarios",
                    valores,
                    "id_usuario = ?",
                    new String[]{String.valueOf(idUsuario)}
            );
            return filas > 0 ? "Preferencia biométrica actualizada." : "No se encontró el usuario.";
        } catch (Exception e) {
            return "Error al actualizar biometría: " + e.getMessage();
        }
    }

    public int obtenerIdUsuario(String correo) {
        Cursor cursor = null;
        try {
            cursor = db.query(
                    "usuarios",
                    new String[]{"id_usuario"},
                    "correo = ?",
                    new String[]{correo},
                    null,
                    null,
                    null
            );
            if (cursor.moveToFirst()) {
                return cursor.getInt(cursor.getColumnIndexOrThrow("id_usuario"));
            }
            return -1;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    public String obtenerNombreUsuario(String correo) {
        Cursor cursor = null;
        try {
            cursor = db.query(
                    "usuarios",
                    new String[]{"nombres", "apellidos"},
                    "correo = ?",
                    new String[]{correo},
                    null,
                    null,
                    null
            );
            if (cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndexOrThrow("nombres")) + " " +
                        cursor.getString(cursor.getColumnIndexOrThrow("apellidos"));
            }
            return "";
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    public String obtenerRolUsuario(String correo) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(
                    "SELECT r.nombre " +
                            "FROM usuarios u " +
                            "INNER JOIN roles r ON r.id_rol = u.id_rol " +
                            "WHERE u.correo = ?",
                    new String[]{correo}
            );
            if (cursor.moveToFirst()) {
                return cursor.getString(0);
            }
            return "";
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context) {
            super(context, BASE_DATOS, null, VERSION);
        }

        @Override
        public void onConfigure(SQLiteDatabase db) {
            super.onConfigure(db);
            db.setForeignKeyConstraintsEnabled(true);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                db.beginTransaction();
                crearTablas(db);
                crearTriggers(db);
                llenarDatosIniciales(db);
                db.setTransactionSuccessful();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                if (db.inTransaction()) {
                    db.endTransaction();
                }
            }
        }

        private void crearTablas(SQLiteDatabase db) {

            db.execSQL("CREATE TABLE roles (" +
                    "id_rol INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "nombre TEXT NOT NULL UNIQUE" +
                    ");");

            db.execSQL("CREATE TABLE usuarios (" +
                    "id_usuario INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "id_rol INTEGER NOT NULL, " +
                    "nombres TEXT NOT NULL, " +
                    "apellidos TEXT NOT NULL, " +
                    "correo TEXT NOT NULL UNIQUE, " +
                    "clave_hash TEXT NOT NULL, " +
                    "usa_biometria INTEGER NOT NULL DEFAULT 0, " +
                    "activo INTEGER NOT NULL DEFAULT 1, " +
                    "FOREIGN KEY (id_rol) REFERENCES roles(id_rol) " +
                    "ON UPDATE CASCADE ON DELETE RESTRICT" +
                    ");");

            db.execSQL("CREATE TABLE edificios (" +
                    "id_edificio INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "nombre TEXT NOT NULL, " +
                    "codigo TEXT NOT NULL UNIQUE, " +
                    "latitud REAL, " +
                    "longitud REAL" +
                    ");");

            db.execSQL("CREATE TABLE laboratorios (" +
                    "id_laboratorio INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "id_edificio INTEGER NOT NULL, " +
                    "nombre TEXT NOT NULL, " +
                    "codigo TEXT NOT NULL UNIQUE, " +
                    "piso TEXT, " +
                    "latitud REAL, " +
                    "longitud REAL, " +
                    "FOREIGN KEY (id_edificio) REFERENCES edificios(id_edificio) " +
                    "ON UPDATE CASCADE ON DELETE RESTRICT" +
                    ");");

            db.execSQL("CREATE TABLE categorias_equipo (" +
                    "id_categoria INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "nombre TEXT NOT NULL UNIQUE" +
                    ");");

            db.execSQL("CREATE TABLE equipos (" +
                    "id_equipo INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "id_laboratorio INTEGER NOT NULL, " +
                    "id_categoria INTEGER NOT NULL, " +
                    "codigo_inventario TEXT NOT NULL UNIQUE, " +
                    "codigo_qr TEXT UNIQUE, " +
                    "nombre TEXT NOT NULL, " +
                    "marca TEXT, " +
                    "modelo TEXT, " +
                    "estado_equipo TEXT NOT NULL, " +
                    "FOREIGN KEY (id_laboratorio) REFERENCES laboratorios(id_laboratorio) " +
                    "ON UPDATE CASCADE ON DELETE RESTRICT, " +
                    "FOREIGN KEY (id_categoria) REFERENCES categorias_equipo(id_categoria) " +
                    "ON UPDATE CASCADE ON DELETE RESTRICT" +
                    ");");

            db.execSQL("CREATE TABLE tipos_incidencia (" +
                    "id_tipo_incidencia INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "nombre TEXT NOT NULL UNIQUE" +
                    ");");

            db.execSQL("CREATE TABLE incidencias (" +
                    "id_incidencia INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "id_equipo INTEGER NOT NULL, " +
                    "id_usuario_reporta INTEGER NOT NULL, " +
                    "id_tipo_incidencia INTEGER NOT NULL, " +
                    "titulo TEXT NOT NULL, " +
                    "descripcion TEXT, " +
                    "prioridad TEXT NOT NULL, " +
                    "estado_incidencia TEXT NOT NULL, " +
                    "origen_registro TEXT NOT NULL, " +
                    "texto_voz_original TEXT, " +
                    "latitud REAL, " +
                    "longitud REAL, " +
                    "fecha_reporte TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (id_equipo) REFERENCES equipos(id_equipo) " +
                    "ON UPDATE CASCADE ON DELETE RESTRICT, " +
                    "FOREIGN KEY (id_usuario_reporta) REFERENCES usuarios(id_usuario) " +
                    "ON UPDATE CASCADE ON DELETE RESTRICT, " +
                    "FOREIGN KEY (id_tipo_incidencia) REFERENCES tipos_incidencia(id_tipo_incidencia) " +
                    "ON UPDATE CASCADE ON DELETE RESTRICT" +
                    ");");

            db.execSQL("CREATE TABLE mantenimientos (" +
                    "id_mantenimiento INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "id_equipo INTEGER NOT NULL, " +
                    "id_incidencia INTEGER, " +
                    "id_usuario_crea INTEGER NOT NULL, " +
                    "id_usuario_tecnico INTEGER NOT NULL, " +
                    "tipo_mantenimiento TEXT NOT NULL, " +
                    "estado_mantenimiento TEXT NOT NULL, " +
                    "diagnostico TEXT, " +
                    "solucion_aplicada TEXT, " +
                    "fecha_inicio TEXT, " +
                    "fecha_fin TEXT, " +
                    "FOREIGN KEY (id_equipo) REFERENCES equipos(id_equipo) " +
                    "ON UPDATE CASCADE ON DELETE RESTRICT, " +
                    "FOREIGN KEY (id_incidencia) REFERENCES incidencias(id_incidencia) " +
                    "ON UPDATE CASCADE ON DELETE SET NULL, " +
                    "FOREIGN KEY (id_usuario_crea) REFERENCES usuarios(id_usuario) " +
                    "ON UPDATE CASCADE ON DELETE RESTRICT, " +
                    "FOREIGN KEY (id_usuario_tecnico) REFERENCES usuarios(id_usuario) " +
                    "ON UPDATE CASCADE ON DELETE RESTRICT" +
                    ");");
        }

        private void crearTriggers(SQLiteDatabase db) {

            // Validaciones generales de booleanos
            db.execSQL("CREATE TRIGGER trg_usuarios_bi_valores " +
                    "BEFORE INSERT ON usuarios " +
                    "FOR EACH ROW " +
                    "WHEN NEW.usa_biometria NOT IN (0, 1) OR NEW.activo NOT IN (0, 1) " +
                    "BEGIN " +
                    "SELECT RAISE(ABORT, 'usa_biometria y activo solo aceptan 0 o 1'); " +
                    "END;");

            db.execSQL("CREATE TRIGGER trg_usuarios_bu_valores " +
                    "BEFORE UPDATE ON usuarios " +
                    "FOR EACH ROW " +
                    "WHEN NEW.usa_biometria NOT IN (0, 1) OR NEW.activo NOT IN (0, 1) " +
                    "BEGIN " +
                    "SELECT RAISE(ABORT, 'usa_biometria y activo solo aceptan 0 o 1'); " +
                    "END;");

            // Validar estados de equipos
            db.execSQL("CREATE TRIGGER trg_equipos_bi_estado " +
                    "BEFORE INSERT ON equipos " +
                    "FOR EACH ROW " +
                    "WHEN NEW.estado_equipo NOT IN ('activo', 'en_mantenimiento', 'fuera_servicio', 'baja') " +
                    "BEGIN " +
                    "SELECT RAISE(ABORT, 'Estado de equipo no valido'); " +
                    "END;");

            db.execSQL("CREATE TRIGGER trg_equipos_bu_estado " +
                    "BEFORE UPDATE ON equipos " +
                    "FOR EACH ROW " +
                    "WHEN NEW.estado_equipo NOT IN ('activo', 'en_mantenimiento', 'fuera_servicio', 'baja') " +
                    "BEGIN " +
                    "SELECT RAISE(ABORT, 'Estado de equipo no valido'); " +
                    "END;");

            // Si no se manda codigo_qr, se copia codigo_inventario como identificador para QR.
            db.execSQL("CREATE TRIGGER trg_equipos_ai_qr_auto " +
                    "AFTER INSERT ON equipos " +
                    "FOR EACH ROW " +
                    "WHEN NEW.codigo_qr IS NULL OR trim(NEW.codigo_qr) = '' " +
                    "BEGIN " +
                    "UPDATE equipos SET codigo_qr = NEW.codigo_inventario " +
                    "WHERE id_equipo = NEW.id_equipo; " +
                    "END;");

            // Validar valores de incidencia
            db.execSQL("CREATE TRIGGER trg_incidencias_bi_valores " +
                    "BEFORE INSERT ON incidencias " +
                    "FOR EACH ROW " +
                    "WHEN NEW.prioridad NOT IN ('baja', 'media', 'alta', 'critica') " +
                    "OR NEW.estado_incidencia NOT IN ('pendiente', 'en_proceso', 'resuelta', 'cancelada') " +
                    "OR NEW.origen_registro NOT IN ('manual', 'voz', 'qr', 'web') " +
                    "BEGIN " +
                    "SELECT RAISE(ABORT, 'Valores no validos para prioridad, estado u origen de incidencia'); " +
                    "END;");

            db.execSQL("CREATE TRIGGER trg_incidencias_bu_valores " +
                    "BEFORE UPDATE ON incidencias " +
                    "FOR EACH ROW " +
                    "WHEN NEW.prioridad NOT IN ('baja', 'media', 'alta', 'critica') " +
                    "OR NEW.estado_incidencia NOT IN ('pendiente', 'en_proceso', 'resuelta', 'cancelada') " +
                    "OR NEW.origen_registro NOT IN ('manual', 'voz', 'qr', 'web') " +
                    "BEGIN " +
                    "SELECT RAISE(ABORT, 'Valores no validos para prioridad, estado u origen de incidencia'); " +
                    "END;");

            // Si el origen es voz, debe existir texto_voz_original.
            db.execSQL("CREATE TRIGGER trg_incidencias_voz_bi " +
                    "BEFORE INSERT ON incidencias " +
                    "FOR EACH ROW " +
                    "WHEN NEW.origen_registro = 'voz' " +
                    "AND (NEW.texto_voz_original IS NULL OR trim(NEW.texto_voz_original) = '') " +
                    "BEGIN " +
                    "SELECT RAISE(ABORT, 'Si el origen del registro es voz, debe guardar texto_voz_original'); " +
                    "END;");

            db.execSQL("CREATE TRIGGER trg_incidencias_voz_bu " +
                    "BEFORE UPDATE ON incidencias " +
                    "FOR EACH ROW " +
                    "WHEN NEW.origen_registro = 'voz' " +
                    "AND (NEW.texto_voz_original IS NULL OR trim(NEW.texto_voz_original) = '') " +
                    "BEGIN " +
                    "SELECT RAISE(ABORT, 'Si el origen del registro es voz, debe guardar texto_voz_original'); " +
                    "END;");

            // Validar valores de mantenimiento
            db.execSQL("CREATE TRIGGER trg_mantenimientos_bi_valores " +
                    "BEFORE INSERT ON mantenimientos " +
                    "FOR EACH ROW " +
                    "WHEN NEW.tipo_mantenimiento NOT IN ('preventivo', 'correctivo') " +
                    "OR NEW.estado_mantenimiento NOT IN ('pendiente', 'en_proceso', 'finalizado', 'cancelado') " +
                    "BEGIN " +
                    "SELECT RAISE(ABORT, 'Valores no validos para tipo o estado de mantenimiento'); " +
                    "END;");

            db.execSQL("CREATE TRIGGER trg_mantenimientos_bu_valores " +
                    "BEFORE UPDATE ON mantenimientos " +
                    "FOR EACH ROW " +
                    "WHEN NEW.tipo_mantenimiento NOT IN ('preventivo', 'correctivo') " +
                    "OR NEW.estado_mantenimiento NOT IN ('pendiente', 'en_proceso', 'finalizado', 'cancelado') " +
                    "BEGIN " +
                    "SELECT RAISE(ABORT, 'Valores no validos para tipo o estado de mantenimiento'); " +
                    "END;");

            // La incidencia relacionada debe pertenecer al mismo equipo del mantenimiento.
            db.execSQL("CREATE TRIGGER trg_mantenimientos_equipo_incidencia_bi " +
                    "BEFORE INSERT ON mantenimientos " +
                    "FOR EACH ROW " +
                    "WHEN NEW.id_incidencia IS NOT NULL " +
                    "AND (SELECT id_equipo FROM incidencias WHERE id_incidencia = NEW.id_incidencia) <> NEW.id_equipo " +
                    "BEGIN " +
                    "SELECT RAISE(ABORT, 'La incidencia no pertenece al mismo equipo del mantenimiento'); " +
                    "END;");

            db.execSQL("CREATE TRIGGER trg_mantenimientos_equipo_incidencia_bu " +
                    "BEFORE UPDATE ON mantenimientos " +
                    "FOR EACH ROW " +
                    "WHEN NEW.id_incidencia IS NOT NULL " +
                    "AND (SELECT id_equipo FROM incidencias WHERE id_incidencia = NEW.id_incidencia) <> NEW.id_equipo " +
                    "BEGIN " +
                    "SELECT RAISE(ABORT, 'La incidencia no pertenece al mismo equipo del mantenimiento'); " +
                    "END;");

            // fecha_fin no puede ser menor que fecha_inicio.
            db.execSQL("CREATE TRIGGER trg_mantenimientos_fechas_bi " +
                    "BEFORE INSERT ON mantenimientos " +
                    "FOR EACH ROW " +
                    "WHEN NEW.fecha_inicio IS NOT NULL " +
                    "AND NEW.fecha_fin IS NOT NULL " +
                    "AND datetime(NEW.fecha_fin) < datetime(NEW.fecha_inicio) " +
                    "BEGIN " +
                    "SELECT RAISE(ABORT, 'fecha_fin no puede ser menor que fecha_inicio'); " +
                    "END;");

            db.execSQL("CREATE TRIGGER trg_mantenimientos_fechas_bu " +
                    "BEFORE UPDATE ON mantenimientos " +
                    "FOR EACH ROW " +
                    "WHEN NEW.fecha_inicio IS NOT NULL " +
                    "AND NEW.fecha_fin IS NOT NULL " +
                    "AND datetime(NEW.fecha_fin) < datetime(NEW.fecha_inicio) " +
                    "BEGIN " +
                    "SELECT RAISE(ABORT, 'fecha_fin no puede ser menor que fecha_inicio'); " +
                    "END;");

            // Al crear mantenimiento, el equipo queda en mantenimiento.
            db.execSQL("CREATE TRIGGER trg_mantenimientos_ai_estado_equipo " +
                    "AFTER INSERT ON mantenimientos " +
                    "FOR EACH ROW " +
                    "WHEN NEW.estado_mantenimiento IN ('pendiente', 'en_proceso') " +
                    "BEGIN " +
                    "UPDATE equipos " +
                    "SET estado_equipo = 'en_mantenimiento' " +
                    "WHERE id_equipo = NEW.id_equipo; " +
                    "END;");

// Al crear mantenimiento, la incidencia queda en proceso.
            db.execSQL("CREATE TRIGGER trg_mantenimientos_ai_estado_incidencia " +
                    "AFTER INSERT ON mantenimientos " +
                    "FOR EACH ROW " +
                    "WHEN NEW.id_incidencia IS NOT NULL " +
                    "BEGIN " +
                    "UPDATE incidencias " +
                    "SET estado_incidencia = 'en_proceso' " +
                    "WHERE id_incidencia = NEW.id_incidencia; " +
                    "END;");

// Al finalizar mantenimiento, el equipo vuelve a activo.
            db.execSQL("CREATE TRIGGER trg_mantenimientos_au_estado_equipo " +
                    "AFTER UPDATE ON mantenimientos " +
                    "FOR EACH ROW " +
                    "WHEN NEW.estado_mantenimiento = 'finalizado' " +
                    "BEGIN " +
                    "UPDATE equipos " +
                    "SET estado_equipo = 'activo' " +
                    "WHERE id_equipo = NEW.id_equipo; " +
                    "END;");

// Al finalizar mantenimiento, la incidencia queda resuelta.
            db.execSQL("CREATE TRIGGER trg_mantenimientos_au_estado_incidencia " +
                    "AFTER UPDATE ON mantenimientos " +
                    "FOR EACH ROW " +
                    "WHEN NEW.estado_mantenimiento = 'finalizado' " +
                    "AND NEW.id_incidencia IS NOT NULL " +
                    "BEGIN " +
                    "UPDATE incidencias " +
                    "SET estado_incidencia = 'resuelta' " +
                    "WHERE id_incidencia = NEW.id_incidencia; " +
                    "END;");
        }

        private void llenarDatosIniciales(SQLiteDatabase db) {

            insertarRolInicial(db, "Administrador");
            insertarRolInicial(db, "Tecnico");
            insertarRolInicial(db, "Reportante");

            insertarUsuarioInicial(db, 1, "Admin", "Sistema", "admin@labcare.com", SeguridadUtils.sha256("admin123"), 1, 1);
            insertarUsuarioInicial(db, 2, "Carlos", "Tecnico", "tecnico@labcare.com", SeguridadUtils.sha256("tec123"), 0, 1);
            insertarUsuarioInicial(db, 3, "Ana", "Docente", "reportante@labcare.com", SeguridadUtils.sha256("rep123"), 0, 1);

            insertarEdificioInicial(db, "Edificio de Ingenieria", "EING", 13.7167, -89.2033);
            insertarEdificioInicial(db, "Edificio de Ciencias", "ECIE", 13.7170, -89.2040);

            insertarLaboratorioInicial(db, 1, "Laboratorio de Informatica 1", "LAB-INF-01", "Piso 1", 13.7168, -89.2035);
            insertarLaboratorioInicial(db, 1, "Laboratorio de Redes", "LAB-RED-01", "Piso 2", 13.7169, -89.2036);
            insertarLaboratorioInicial(db, 2, "Laboratorio de Electronica", "LAB-ELE-01", "Piso 1", 13.7171, -89.2042);

            insertarCategoriaInicial(db, "Computadora");
            insertarCategoriaInicial(db, "Proyector");
            insertarCategoriaInicial(db, "Router");
            insertarCategoriaInicial(db, "Impresora");
            insertarCategoriaInicial(db, "Equipo de laboratorio");

            insertarEquipoInicial(db, 1, 1, "INV-PC-001", "QR-PC-001", "Computadora Dell OptiPlex", "Dell", "OptiPlex 7090", "activo");
            insertarEquipoInicial(db, 1, 2, "INV-PRO-001", "QR-PRO-001", "Proyector Epson", "Epson", "X49", "activo");
            insertarEquipoInicial(db, 2, 3, "INV-ROU-001", "QR-ROU-001", "Router Cisco", "Cisco", "ISR 1100", "activo");
            insertarEquipoInicial(db, 3, 5, "INV-OSC-001", "QR-OSC-001", "Osciloscopio", "Rigol", "DS1054Z", "activo");

            insertarTipoIncidenciaInicial(db, "Hardware");
            insertarTipoIncidenciaInicial(db, "Software");
            insertarTipoIncidenciaInicial(db, "Red");
            insertarTipoIncidenciaInicial(db, "Mantenimiento preventivo");
            insertarTipoIncidenciaInicial(db, "Otro");

            insertarIncidenciaInicial(db, 1, 3, 1, "Equipo no enciende", "La computadora no inicia correctamente.", "alta", "pendiente", "manual", null, 13.7168, -89.2035);
            insertarIncidenciaInicial(db, 2, 3, 1, "Proyector sin imagen", "El proyector enciende, pero no muestra imagen.", "media", "pendiente", "voz", "El proyector no muestra imagen", 13.7168, -89.2035);
        }

        private void insertarRolInicial(SQLiteDatabase db, String nombre) {
            ContentValues valores = new ContentValues();
            valores.put("nombre", nombre);
            db.insertWithOnConflict("roles", null, valores, SQLiteDatabase.CONFLICT_IGNORE);
        }

        private void insertarUsuarioInicial(SQLiteDatabase db, int idRol, String nombres, String apellidos,
                                            String correo, String claveHash, int usaBiometria, int activo) {
            ContentValues valores = new ContentValues();
            valores.put("id_rol", idRol);
            valores.put("nombres", nombres);
            valores.put("apellidos", apellidos);
            valores.put("correo", correo);
            valores.put("clave_hash", SeguridadUtils.sha256SiNecesario(claveHash));
            valores.put("usa_biometria", usaBiometria);
            valores.put("activo", activo);
            db.insertWithOnConflict("usuarios", null, valores, SQLiteDatabase.CONFLICT_IGNORE);
        }

        private void insertarEdificioInicial(SQLiteDatabase db, String nombre, String codigo, Double latitud, Double longitud) {
            ContentValues valores = new ContentValues();
            valores.put("nombre", nombre);
            valores.put("codigo", codigo);
            putDoubleOrNull(valores, "latitud", latitud);
            putDoubleOrNull(valores, "longitud", longitud);
            db.insertWithOnConflict("edificios", null, valores, SQLiteDatabase.CONFLICT_IGNORE);
        }

        private void insertarLaboratorioInicial(SQLiteDatabase db, int idEdificio, String nombre, String codigo,
                                                String piso, Double latitud, Double longitud) {
            ContentValues valores = new ContentValues();
            valores.put("id_edificio", idEdificio);
            valores.put("nombre", nombre);
            valores.put("codigo", codigo);
            valores.put("piso", piso);
            putDoubleOrNull(valores, "latitud", latitud);
            putDoubleOrNull(valores, "longitud", longitud);
            db.insertWithOnConflict("laboratorios", null, valores, SQLiteDatabase.CONFLICT_IGNORE);
        }

        private void insertarCategoriaInicial(SQLiteDatabase db, String nombre) {
            ContentValues valores = new ContentValues();
            valores.put("nombre", nombre);
            db.insertWithOnConflict("categorias_equipo", null, valores, SQLiteDatabase.CONFLICT_IGNORE);
        }

        private void insertarEquipoInicial(SQLiteDatabase db, int idLaboratorio, int idCategoria, String codigoInventario,
                                           String codigoQr, String nombre, String marca, String modelo, String estadoEquipo) {
            ContentValues valores = new ContentValues();
            valores.put("id_laboratorio", idLaboratorio);
            valores.put("id_categoria", idCategoria);
            valores.put("codigo_inventario", codigoInventario);
            valores.put("codigo_qr", codigoQr);
            valores.put("nombre", nombre);
            valores.put("marca", marca);
            valores.put("modelo", modelo);
            valores.put("estado_equipo", estadoEquipo);
            db.insertWithOnConflict("equipos", null, valores, SQLiteDatabase.CONFLICT_IGNORE);
        }

        private void insertarTipoIncidenciaInicial(SQLiteDatabase db, String nombre) {
            ContentValues valores = new ContentValues();
            valores.put("nombre", nombre);
            db.insertWithOnConflict("tipos_incidencia", null, valores, SQLiteDatabase.CONFLICT_IGNORE);
        }

        private void insertarIncidenciaInicial(SQLiteDatabase db, int idEquipo, int idUsuarioReporta, int idTipoIncidencia,
                                               String titulo, String descripcion, String prioridad, String estadoIncidencia,
                                               String origenRegistro, String textoVozOriginal, Double latitud, Double longitud) {
            ContentValues valores = new ContentValues();
            valores.put("id_equipo", idEquipo);
            valores.put("id_usuario_reporta", idUsuarioReporta);
            valores.put("id_tipo_incidencia", idTipoIncidencia);
            valores.put("titulo", titulo);
            valores.put("descripcion", descripcion);
            valores.put("prioridad", prioridad);
            valores.put("estado_incidencia", estadoIncidencia);
            valores.put("origen_registro", origenRegistro);
            valores.put("texto_voz_original", textoVozOriginal);
            putDoubleOrNull(valores, "latitud", latitud);
            putDoubleOrNull(valores, "longitud", longitud);
            db.insertWithOnConflict("incidencias", null, valores, SQLiteDatabase.CONFLICT_IGNORE);
        }

        private static void putDoubleOrNull(ContentValues valores, String columna, Double valor) {
            if (valor == null) valores.putNull(columna);
            else valores.put(columna, valor);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TRIGGER IF EXISTS trg_usuarios_bi_valores");
            db.execSQL("DROP TRIGGER IF EXISTS trg_usuarios_bu_valores");
            db.execSQL("DROP TRIGGER IF EXISTS trg_equipos_bi_estado");
            db.execSQL("DROP TRIGGER IF EXISTS trg_equipos_bu_estado");
            db.execSQL("DROP TRIGGER IF EXISTS trg_equipos_ai_qr_auto");
            db.execSQL("DROP TRIGGER IF EXISTS trg_incidencias_bi_valores");
            db.execSQL("DROP TRIGGER IF EXISTS trg_incidencias_bu_valores");
            db.execSQL("DROP TRIGGER IF EXISTS trg_incidencias_voz_bi");
            db.execSQL("DROP TRIGGER IF EXISTS trg_incidencias_voz_bu");
            db.execSQL("DROP TRIGGER IF EXISTS trg_mantenimientos_bi_valores");
            db.execSQL("DROP TRIGGER IF EXISTS trg_mantenimientos_bu_valores");
            db.execSQL("DROP TRIGGER IF EXISTS trg_mantenimientos_equipo_incidencia_bi");
            db.execSQL("DROP TRIGGER IF EXISTS trg_mantenimientos_equipo_incidencia_bu");
            db.execSQL("DROP TRIGGER IF EXISTS trg_mantenimientos_fechas_bi");
            db.execSQL("DROP TRIGGER IF EXISTS trg_mantenimientos_fechas_bu");
            db.execSQL("DROP TRIGGER IF EXISTS trg_mantenimientos_ai_estado_equipo");
            db.execSQL("DROP TRIGGER IF EXISTS trg_mantenimientos_au_finalizado");
            db.execSQL("DROP TRIGGER IF EXISTS trg_mantenimientos_ai_estado_equipo");
            db.execSQL("DROP TRIGGER IF EXISTS trg_mantenimientos_ai_estado_incidencia");
            db.execSQL("DROP TRIGGER IF EXISTS trg_mantenimientos_au_finalizado");
            db.execSQL("DROP TRIGGER IF EXISTS trg_mantenimientos_au_estado_equipo");
            db.execSQL("DROP TRIGGER IF EXISTS trg_mantenimientos_au_estado_incidencia");

            db.execSQL("DROP TABLE IF EXISTS mantenimientos");
            db.execSQL("DROP TABLE IF EXISTS incidencias");
            db.execSQL("DROP TABLE IF EXISTS tipos_incidencia");
            db.execSQL("DROP TABLE IF EXISTS equipos");
            db.execSQL("DROP TABLE IF EXISTS categorias_equipo");
            db.execSQL("DROP TABLE IF EXISTS laboratorios");
            db.execSQL("DROP TABLE IF EXISTS edificios");
            db.execSQL("DROP TABLE IF EXISTS usuarios");
            db.execSQL("DROP TABLE IF EXISTS roles");

            onCreate(db);
        }
    }

    // =========================================================
    // CRUD ROLES
    // =========================================================

    public String insertarRol(String nombre) {
        try {
            ContentValues valores = new ContentValues();
            valores.put("nombre", nombre);
            long resultado = db.insertOrThrow("roles", null, valores);
            return resultado == -1 ? "Error al insertar rol." : "Rol insertado correctamente.";
        } catch (SQLiteConstraintException e) {
            return "Error de integridad: El rol ya existe.";
        } catch (Exception e) {
            return "Error al insertar rol: " + e.getMessage();
        }
    }

    public Cursor consultarRolesCursor() {
        return db.query("roles", null, null, null, null, null, "nombre ASC");
    }

    public String actualizarRol(int idRol, String nombre) {
        try {
            ContentValues valores = new ContentValues();
            valores.put("nombre", nombre);
            int filas = db.update("roles", valores, "id_rol = ?", new String[]{String.valueOf(idRol)});
            return filas > 0 ? "Rol actualizado correctamente." : "No se encontró el rol.";
        } catch (SQLiteConstraintException e) {
            return "Error de integridad: El rol ya existe.";
        } catch (Exception e) {
            return "Error al actualizar rol: " + e.getMessage();
        }
    }

    public String eliminarRol(int idRol) {
        try {
            int filas = db.delete("roles", "id_rol = ?", new String[]{String.valueOf(idRol)});
            return filas > 0 ? "Rol eliminado correctamente." : "No se encontró el rol.";
        } catch (SQLiteConstraintException e) {
            return "No se puede eliminar el rol porque tiene usuarios relacionados.";
        } catch (Exception e) {
            return "Error al eliminar rol: " + e.getMessage();
        }
    }

    // =========================================================
    // CRUD USUARIOS
    // =========================================================

    public String insertarUsuario(int idRol, String nombres, String apellidos, String correo,
                                  String claveHash, int usaBiometria, int activo) {
        try {
            ContentValues valores = new ContentValues();
            valores.put("id_rol", idRol);
            valores.put("nombres", nombres);
            valores.put("apellidos", apellidos);
            valores.put("correo", correo);
            valores.put("clave_hash", SeguridadUtils.sha256SiNecesario(claveHash));
            valores.put("usa_biometria", usaBiometria);
            valores.put("activo", activo);
            long resultado = db.insertOrThrow("usuarios", null, valores);
            return resultado == -1 ? "Error al insertar usuario." : "Usuario insertado correctamente.";
        } catch (SQLiteConstraintException e) {
            return "Error de integridad: Verifique que el rol exista y que el correo no esté repetido. " + e.getMessage();
        } catch (Exception e) {
            return "Error al insertar usuario: " + e.getMessage();
        }
    }

    public Cursor consultarUsuariosCursor() {
        return db.rawQuery(
                "SELECT u.id_usuario, u.nombres, u.apellidos, u.correo, u.usa_biometria, u.activo, " +
                        "r.nombre AS rol " +
                        "FROM usuarios u " +
                        "INNER JOIN roles r ON r.id_rol = u.id_rol " +
                        "ORDER BY u.apellidos ASC, u.nombres ASC",
                null
        );
    }

    public Cursor consultarTecnicosCursor() {
        return db.rawQuery(
                "SELECT u.id_usuario, u.nombres, u.apellidos, u.correo " +
                        "FROM usuarios u " +
                        "INNER JOIN roles r ON r.id_rol = u.id_rol " +
                        "WHERE r.nombre = 'Tecnico' AND u.activo = 1 " +
                        "ORDER BY u.nombres ASC",
                null
        );
    }

    public HashMap<String, String> consultarUsuario(int idUsuario) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(
                    "SELECT u.*, r.nombre AS rol " +
                            "FROM usuarios u " +
                            "INNER JOIN roles r ON r.id_rol = u.id_rol " +
                            "WHERE u.id_usuario = ?",
                    new String[]{String.valueOf(idUsuario)}
            );
            if (cursor.moveToFirst()) {
                return cursorAMap(cursor);
            }
            return null;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    public String actualizarUsuario(int idUsuario, int idRol, String nombres, String apellidos,
                                    String correo, String claveHash, int usaBiometria, int activo) {
        try {
            ContentValues valores = new ContentValues();
            valores.put("id_rol", idRol);
            valores.put("nombres", nombres);
            valores.put("apellidos", apellidos);
            valores.put("correo", correo);
            valores.put("clave_hash", SeguridadUtils.sha256SiNecesario(claveHash));
            valores.put("usa_biometria", usaBiometria);
            valores.put("activo", activo);
            int filas = db.update("usuarios", valores, "id_usuario = ?", new String[]{String.valueOf(idUsuario)});
            return filas > 0 ? "Usuario actualizado correctamente." : "No se encontró el usuario.";
        } catch (SQLiteConstraintException e) {
            return "Error de integridad: Verifique rol y correo. " + e.getMessage();
        } catch (Exception e) {
            return "Error al actualizar usuario: " + e.getMessage();
        }
    }

    public String eliminarUsuario(int idUsuario) {
        try {
            int filas = db.delete("usuarios", "id_usuario = ?", new String[]{String.valueOf(idUsuario)});
            return filas > 0 ? "Usuario eliminado correctamente." : "No se encontró el usuario.";
        } catch (SQLiteConstraintException e) {
            return "No se puede eliminar el usuario porque tiene incidencias o mantenimientos relacionados.";
        } catch (Exception e) {
            return "Error al eliminar usuario: " + e.getMessage();
        }
    }

    // =========================================================
    // CRUD EDIFICIOS
    // =========================================================

    public String insertarEdificio(String nombre, String codigo, Double latitud, Double longitud) {
        try {
            ContentValues valores = new ContentValues();
            valores.put("nombre", nombre);
            valores.put("codigo", codigo);
            putDoubleOrNull(valores, "latitud", latitud);
            putDoubleOrNull(valores, "longitud", longitud);
            long resultado = db.insertOrThrow("edificios", null, valores);
            return resultado == -1 ? "Error al insertar edificio." : "Edificio insertado correctamente.";
        } catch (SQLiteConstraintException e) {
            return "Error de integridad: El código del edificio ya existe.";
        } catch (Exception e) {
            return "Error al insertar edificio: " + e.getMessage();
        }
    }

    public Cursor consultarEdificiosCursor() {
        return db.query("edificios", null, null, null, null, null, "nombre ASC");
    }

    public HashMap<String, String> consultarEdificio(int idEdificio) {
        Cursor cursor = null;
        try {
            cursor = db.query("edificios", null, "id_edificio = ?", new String[]{String.valueOf(idEdificio)}, null, null, null);
            if (cursor.moveToFirst()) return cursorAMap(cursor);
            return null;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    public String actualizarEdificio(int idEdificio, String nombre, String codigo, Double latitud, Double longitud) {
        try {
            ContentValues valores = new ContentValues();
            valores.put("nombre", nombre);
            valores.put("codigo", codigo);
            putDoubleOrNull(valores, "latitud", latitud);
            putDoubleOrNull(valores, "longitud", longitud);
            int filas = db.update("edificios", valores, "id_edificio = ?", new String[]{String.valueOf(idEdificio)});
            return filas > 0 ? "Edificio actualizado correctamente." : "No se encontró el edificio.";
        } catch (SQLiteConstraintException e) {
            return "Error de integridad: El código del edificio ya existe.";
        } catch (Exception e) {
            return "Error al actualizar edificio: " + e.getMessage();
        }
    }

    public String eliminarEdificio(int idEdificio) {
        try {
            int filas = db.delete("edificios", "id_edificio = ?", new String[]{String.valueOf(idEdificio)});
            return filas > 0 ? "Edificio eliminado correctamente." : "No se encontró el edificio.";
        } catch (SQLiteConstraintException e) {
            return "No se puede eliminar el edificio porque tiene laboratorios relacionados.";
        } catch (Exception e) {
            return "Error al eliminar edificio: " + e.getMessage();
        }
    }

    // =========================================================
    // CRUD LABORATORIOS
    // =========================================================

    public String insertarLaboratorio(int idEdificio, String nombre, String codigo,
                                      String piso, Double latitud, Double longitud) {
        try {
            ContentValues valores = new ContentValues();
            valores.put("id_edificio", idEdificio);
            valores.put("nombre", nombre);
            valores.put("codigo", codigo);
            valores.put("piso", piso);
            putDoubleOrNull(valores, "latitud", latitud);
            putDoubleOrNull(valores, "longitud", longitud);
            long resultado = db.insertOrThrow("laboratorios", null, valores);
            return resultado == -1 ? "Error al insertar laboratorio." : "Laboratorio insertado correctamente.";
        } catch (SQLiteConstraintException e) {
            return "Error de integridad: Verifique el edificio y que el código no esté repetido. " + e.getMessage();
        } catch (Exception e) {
            return "Error al insertar laboratorio: " + e.getMessage();
        }
    }

    public Cursor consultarLaboratoriosCursor() {
        return db.rawQuery(
                "SELECT l.id_laboratorio, l.nombre, l.codigo, l.piso, l.latitud, l.longitud, " +
                        "e.nombre AS edificio " +
                        "FROM laboratorios l " +
                        "INNER JOIN edificios e ON e.id_edificio = l.id_edificio " +
                        "ORDER BY l.nombre ASC",
                null
        );
    }

    public HashMap<String, String> consultarLaboratorio(int idLaboratorio) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(
                    "SELECT l.*, e.nombre AS edificio " +
                            "FROM laboratorios l " +
                            "INNER JOIN edificios e ON e.id_edificio = l.id_edificio " +
                            "WHERE l.id_laboratorio = ?",
                    new String[]{String.valueOf(idLaboratorio)}
            );
            if (cursor.moveToFirst()) return cursorAMap(cursor);
            return null;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    public String actualizarLaboratorio(int idLaboratorio, int idEdificio, String nombre, String codigo,
                                        String piso, Double latitud, Double longitud) {
        try {
            ContentValues valores = new ContentValues();
            valores.put("id_edificio", idEdificio);
            valores.put("nombre", nombre);
            valores.put("codigo", codigo);
            valores.put("piso", piso);
            putDoubleOrNull(valores, "latitud", latitud);
            putDoubleOrNull(valores, "longitud", longitud);
            int filas = db.update("laboratorios", valores, "id_laboratorio = ?", new String[]{String.valueOf(idLaboratorio)});
            return filas > 0 ? "Laboratorio actualizado correctamente." : "No se encontró el laboratorio.";
        } catch (SQLiteConstraintException e) {
            return "Error de integridad: Verifique el edificio y el código. " + e.getMessage();
        } catch (Exception e) {
            return "Error al actualizar laboratorio: " + e.getMessage();
        }
    }

    public String eliminarLaboratorio(int idLaboratorio) {
        try {
            int filas = db.delete("laboratorios", "id_laboratorio = ?", new String[]{String.valueOf(idLaboratorio)});
            return filas > 0 ? "Laboratorio eliminado correctamente." : "No se encontró el laboratorio.";
        } catch (SQLiteConstraintException e) {
            return "No se puede eliminar el laboratorio porque tiene equipos relacionados.";
        } catch (Exception e) {
            return "Error al eliminar laboratorio: " + e.getMessage();
        }
    }

    // =========================================================
    // CRUD CATEGORIAS DE EQUIPO
    // =========================================================

    public String insertarCategoriaEquipo(String nombre) {
        try {
            ContentValues valores = new ContentValues();
            valores.put("nombre", nombre);
            long resultado = db.insertOrThrow("categorias_equipo", null, valores);
            return resultado == -1 ? "Error al insertar categoría." : "Categoría insertada correctamente.";
        } catch (SQLiteConstraintException e) {
            return "Error de integridad: La categoría ya existe.";
        } catch (Exception e) {
            return "Error al insertar categoría: " + e.getMessage();
        }
    }

    public Cursor consultarCategoriasEquipoCursor() {
        return db.query("categorias_equipo", null, null, null, null, null, "nombre ASC");
    }

    public String actualizarCategoriaEquipo(int idCategoria, String nombre) {
        try {
            ContentValues valores = new ContentValues();
            valores.put("nombre", nombre);
            int filas = db.update("categorias_equipo", valores, "id_categoria = ?", new String[]{String.valueOf(idCategoria)});
            return filas > 0 ? "Categoría actualizada correctamente." : "No se encontró la categoría.";
        } catch (SQLiteConstraintException e) {
            return "Error de integridad: La categoría ya existe.";
        } catch (Exception e) {
            return "Error al actualizar categoría: " + e.getMessage();
        }
    }

    public String eliminarCategoriaEquipo(int idCategoria) {
        try {
            int filas = db.delete("categorias_equipo", "id_categoria = ?", new String[]{String.valueOf(idCategoria)});
            return filas > 0 ? "Categoría eliminada correctamente." : "No se encontró la categoría.";
        } catch (SQLiteConstraintException e) {
            return "No se puede eliminar la categoría porque tiene equipos relacionados.";
        } catch (Exception e) {
            return "Error al eliminar categoría: " + e.getMessage();
        }
    }

    // =========================================================
    // CRUD EQUIPOS
    // =========================================================

    public String insertarEquipo(int idLaboratorio, int idCategoria, String codigoInventario, String codigoQr,
                                 String nombre, String marca, String modelo, String estadoEquipo) {
        try {
            ContentValues valores = new ContentValues();
            valores.put("id_laboratorio", idLaboratorio);
            valores.put("id_categoria", idCategoria);
            valores.put("codigo_inventario", codigoInventario);
            if (codigoQr == null || codigoQr.trim().isEmpty()) valores.putNull("codigo_qr");
            else valores.put("codigo_qr", codigoQr);
            valores.put("nombre", nombre);
            valores.put("marca", marca);
            valores.put("modelo", modelo);
            valores.put("estado_equipo", estadoEquipo);
            long resultado = db.insertOrThrow("equipos", null, valores);
            return resultado == -1 ? "Error al insertar equipo." : "Equipo insertado correctamente.";
        } catch (SQLiteConstraintException e) {
            return "Error de integridad: Verifique laboratorio, categoría, código inventario o QR. " + e.getMessage();
        } catch (Exception e) {
            return "Error al insertar equipo: " + e.getMessage();
        }
    }

    public Cursor consultarEquiposCursor() {
        return db.rawQuery(
                "SELECT eq.id_equipo, eq.codigo_inventario, eq.codigo_qr, eq.nombre, eq.marca, eq.modelo, eq.estado_equipo, " +
                        "lab.nombre AS laboratorio, cat.nombre AS categoria, ed.nombre AS edificio " +
                        "FROM equipos eq " +
                        "INNER JOIN laboratorios lab ON lab.id_laboratorio = eq.id_laboratorio " +
                        "INNER JOIN edificios ed ON ed.id_edificio = lab.id_edificio " +
                        "INNER JOIN categorias_equipo cat ON cat.id_categoria = eq.id_categoria " +
                        "ORDER BY eq.nombre ASC",
                null
        );
    }

    public Cursor consultarEquiposActivosCursor() {
        return db.rawQuery(
                "SELECT id_equipo, codigo_inventario, nombre, estado_equipo " +
                        "FROM equipos " +
                        "WHERE estado_equipo IN ('activo', 'fuera_servicio') " +
                        "ORDER BY nombre ASC",
                null
        );
    }

    public HashMap<String, String> consultarEquipo(int idEquipo) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(
                    "SELECT eq.*, lab.nombre AS laboratorio, cat.nombre AS categoria, ed.nombre AS edificio " +
                            "FROM equipos eq " +
                            "INNER JOIN laboratorios lab ON lab.id_laboratorio = eq.id_laboratorio " +
                            "INNER JOIN edificios ed ON ed.id_edificio = lab.id_edificio " +
                            "INNER JOIN categorias_equipo cat ON cat.id_categoria = eq.id_categoria " +
                            "WHERE eq.id_equipo = ?",
                    new String[]{String.valueOf(idEquipo)}
            );
            if (cursor.moveToFirst()) return cursorAMap(cursor);
            return null;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    public HashMap<String, String> consultarEquipoPorQR(String codigoQr) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(
                    "SELECT eq.*, lab.nombre AS laboratorio, cat.nombre AS categoria, ed.nombre AS edificio " +
                            "FROM equipos eq " +
                            "INNER JOIN laboratorios lab ON lab.id_laboratorio = eq.id_laboratorio " +
                            "INNER JOIN edificios ed ON ed.id_edificio = lab.id_edificio " +
                            "INNER JOIN categorias_equipo cat ON cat.id_categoria = eq.id_categoria " +
                            "WHERE eq.codigo_qr = ? OR eq.codigo_inventario = ?",
                    new String[]{codigoQr, codigoQr}
            );
            if (cursor.moveToFirst()) return cursorAMap(cursor);
            return null;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    public String actualizarEquipo(int idEquipo, int idLaboratorio, int idCategoria, String codigoInventario,
                                   String codigoQr, String nombre, String marca, String modelo, String estadoEquipo) {
        try {
            ContentValues valores = new ContentValues();
            valores.put("id_laboratorio", idLaboratorio);
            valores.put("id_categoria", idCategoria);
            valores.put("codigo_inventario", codigoInventario);
            if (codigoQr == null || codigoQr.trim().isEmpty()) valores.putNull("codigo_qr");
            else valores.put("codigo_qr", codigoQr);
            valores.put("nombre", nombre);
            valores.put("marca", marca);
            valores.put("modelo", modelo);
            valores.put("estado_equipo", estadoEquipo);
            int filas = db.update("equipos", valores, "id_equipo = ?", new String[]{String.valueOf(idEquipo)});
            return filas > 0 ? "Equipo actualizado correctamente." : "No se encontró el equipo.";
        } catch (SQLiteConstraintException e) {
            return "Error de integridad: Verifique laboratorio, categoría, código inventario o QR. " + e.getMessage();
        } catch (Exception e) {
            return "Error al actualizar equipo: " + e.getMessage();
        }
    }

    public String eliminarEquipo(int idEquipo) {
        try {
            int filas = db.delete("equipos", "id_equipo = ?", new String[]{String.valueOf(idEquipo)});
            return filas > 0 ? "Equipo eliminado correctamente." : "No se encontró el equipo.";
        } catch (SQLiteConstraintException e) {
            return "No se puede eliminar el equipo porque tiene incidencias o mantenimientos relacionados.";
        } catch (Exception e) {
            return "Error al eliminar equipo: " + e.getMessage();
        }
    }

    // =========================================================
    // CRUD TIPOS DE INCIDENCIA
    // =========================================================

    public String insertarTipoIncidencia(String nombre) {
        try {
            ContentValues valores = new ContentValues();
            valores.put("nombre", nombre);
            long resultado = db.insertOrThrow("tipos_incidencia", null, valores);
            return resultado == -1 ? "Error al insertar tipo de incidencia." : "Tipo de incidencia insertado correctamente.";
        } catch (SQLiteConstraintException e) {
            return "Error de integridad: El tipo de incidencia ya existe.";
        } catch (Exception e) {
            return "Error al insertar tipo de incidencia: " + e.getMessage();
        }
    }

    public Cursor consultarTiposIncidenciaCursor() {
        return db.query("tipos_incidencia", null, null, null, null, null, "nombre ASC");
    }

    public String actualizarTipoIncidencia(int idTipoIncidencia, String nombre) {
        try {
            ContentValues valores = new ContentValues();
            valores.put("nombre", nombre);
            int filas = db.update("tipos_incidencia", valores, "id_tipo_incidencia = ?", new String[]{String.valueOf(idTipoIncidencia)});
            return filas > 0 ? "Tipo de incidencia actualizado correctamente." : "No se encontró el tipo de incidencia.";
        } catch (SQLiteConstraintException e) {
            return "Error de integridad: El tipo de incidencia ya existe.";
        } catch (Exception e) {
            return "Error al actualizar tipo de incidencia: " + e.getMessage();
        }
    }

    public String eliminarTipoIncidencia(int idTipoIncidencia) {
        try {
            int filas = db.delete("tipos_incidencia", "id_tipo_incidencia = ?", new String[]{String.valueOf(idTipoIncidencia)});
            return filas > 0 ? "Tipo de incidencia eliminado correctamente." : "No se encontró el tipo de incidencia.";
        } catch (SQLiteConstraintException e) {
            return "No se puede eliminar el tipo porque tiene incidencias relacionadas.";
        } catch (Exception e) {
            return "Error al eliminar tipo de incidencia: " + e.getMessage();
        }
    }

    // =========================================================
    // CRUD INCIDENCIAS
    // =========================================================

    public String insertarIncidencia(int idEquipo, int idUsuarioReporta, int idTipoIncidencia,
                                     String titulo, String descripcion, String prioridad,
                                     String estadoIncidencia, String origenRegistro,
                                     String textoVozOriginal, Double latitud, Double longitud) {
        try {
            ContentValues valores = new ContentValues();
            valores.put("id_equipo", idEquipo);
            valores.put("id_usuario_reporta", idUsuarioReporta);
            valores.put("id_tipo_incidencia", idTipoIncidencia);
            valores.put("titulo", titulo);
            valores.put("descripcion", descripcion);
            valores.put("prioridad", prioridad);
            valores.put("estado_incidencia", estadoIncidencia);
            valores.put("origen_registro", origenRegistro);
            valores.put("texto_voz_original", textoVozOriginal);
            putDoubleOrNull(valores, "latitud", latitud);
            putDoubleOrNull(valores, "longitud", longitud);
            long resultado = db.insertOrThrow("incidencias", null, valores);
            return resultado == -1 ? "Error al insertar incidencia." : "Incidencia insertada correctamente.";
        } catch (SQLiteConstraintException e) {
            return "Error de integridad: Verifique equipo, usuario, tipo de incidencia y reglas de voz. " + e.getMessage();
        } catch (Exception e) {
            return "Error al insertar incidencia: " + e.getMessage();
        }
    }

    public Cursor consultarIncidenciasCursor() {
        return db.rawQuery(
                "SELECT i.id_incidencia, i.titulo, i.descripcion, i.prioridad, i.estado_incidencia, " +
                        "i.origen_registro, i.texto_voz_original, i.latitud, i.longitud, i.fecha_reporte, " +
                        "eq.nombre AS equipo, eq.codigo_inventario, ti.nombre AS tipo_incidencia, " +
                        "u.nombres || ' ' || u.apellidos AS usuario_reporta " +
                        "FROM incidencias i " +
                        "INNER JOIN equipos eq ON eq.id_equipo = i.id_equipo " +
                        "INNER JOIN tipos_incidencia ti ON ti.id_tipo_incidencia = i.id_tipo_incidencia " +
                        "INNER JOIN usuarios u ON u.id_usuario = i.id_usuario_reporta " +
                        "ORDER BY i.fecha_reporte DESC, i.id_incidencia DESC",
                null
        );
    }

    public Cursor consultarIncidenciasPendientesCursor() {
        return db.rawQuery(
                "SELECT i.id_incidencia, i.titulo, i.prioridad, i.estado_incidencia, " +
                        "i.id_equipo, eq.nombre AS equipo, eq.codigo_inventario " +
                        "FROM incidencias i " +
                        "INNER JOIN equipos eq ON eq.id_equipo = i.id_equipo " +
                        "WHERE i.estado_incidencia IN ('pendiente', 'en_proceso') " +
                        "ORDER BY i.fecha_reporte DESC",
                null
        );
    }

    public HashMap<String, String> consultarIncidencia(int idIncidencia) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(
                    "SELECT i.*, eq.nombre AS equipo, eq.codigo_inventario, ti.nombre AS tipo_incidencia, " +
                            "u.nombres || ' ' || u.apellidos AS usuario_reporta " +
                            "FROM incidencias i " +
                            "INNER JOIN equipos eq ON eq.id_equipo = i.id_equipo " +
                            "INNER JOIN tipos_incidencia ti ON ti.id_tipo_incidencia = i.id_tipo_incidencia " +
                            "INNER JOIN usuarios u ON u.id_usuario = i.id_usuario_reporta " +
                            "WHERE i.id_incidencia = ?",
                    new String[]{String.valueOf(idIncidencia)}
            );
            if (cursor.moveToFirst()) return cursorAMap(cursor);
            return null;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    public String actualizarIncidencia(int idIncidencia, int idEquipo, int idUsuarioReporta, int idTipoIncidencia,
                                       String titulo, String descripcion, String prioridad,
                                       String estadoIncidencia, String origenRegistro,
                                       String textoVozOriginal, Double latitud, Double longitud) {
        try {
            ContentValues valores = new ContentValues();
            valores.put("id_equipo", idEquipo);
            valores.put("id_usuario_reporta", idUsuarioReporta);
            valores.put("id_tipo_incidencia", idTipoIncidencia);
            valores.put("titulo", titulo);
            valores.put("descripcion", descripcion);
            valores.put("prioridad", prioridad);
            valores.put("estado_incidencia", estadoIncidencia);
            valores.put("origen_registro", origenRegistro);
            valores.put("texto_voz_original", textoVozOriginal);
            putDoubleOrNull(valores, "latitud", latitud);
            putDoubleOrNull(valores, "longitud", longitud);
            int filas = db.update("incidencias", valores, "id_incidencia = ?", new String[]{String.valueOf(idIncidencia)});
            return filas > 0 ? "Incidencia actualizada correctamente." : "No se encontró la incidencia.";
        } catch (SQLiteConstraintException e) {
            return "Error de integridad: Verifique equipo, usuario, tipo de incidencia y reglas de voz. " + e.getMessage();
        } catch (Exception e) {
            return "Error al actualizar incidencia: " + e.getMessage();
        }
    }

    public String eliminarIncidencia(int idIncidencia) {
        try {
            int filas = db.delete("incidencias", "id_incidencia = ?", new String[]{String.valueOf(idIncidencia)});
            return filas > 0 ? "Incidencia eliminada correctamente." : "No se encontró la incidencia.";
        } catch (SQLiteConstraintException e) {
            return "No se puede eliminar la incidencia porque tiene mantenimientos relacionados.";
        } catch (Exception e) {
            return "Error al eliminar incidencia: " + e.getMessage();
        }
    }

    // =========================================================
    // CRUD MANTENIMIENTOS
    // =========================================================

    public String insertarMantenimiento(int idEquipo, Integer idIncidencia, int idUsuarioCrea,
                                        int idUsuarioTecnico, String tipoMantenimiento,
                                        String estadoMantenimiento, String diagnostico,
                                        String solucionAplicada, String fechaInicio, String fechaFin) {
        try {
            ContentValues valores = new ContentValues();
            valores.put("id_equipo", idEquipo);
            if (idIncidencia == null) valores.putNull("id_incidencia");
            else valores.put("id_incidencia", idIncidencia);
            valores.put("id_usuario_crea", idUsuarioCrea);
            valores.put("id_usuario_tecnico", idUsuarioTecnico);
            valores.put("tipo_mantenimiento", tipoMantenimiento);
            valores.put("estado_mantenimiento", estadoMantenimiento);
            valores.put("diagnostico", diagnostico);
            valores.put("solucion_aplicada", solucionAplicada);
            valores.put("fecha_inicio", fechaInicio);
            valores.put("fecha_fin", fechaFin);
            long resultado = db.insertOrThrow("mantenimientos", null, valores);
            return resultado == -1 ? "Error al insertar mantenimiento." : "Mantenimiento insertado correctamente.";
        } catch (SQLiteConstraintException e) {
            return "Error de integridad: Verifique equipo, incidencia, usuarios y fechas. " + e.getMessage();
        } catch (Exception e) {
            return "Error al insertar mantenimiento: " + e.getMessage();
        }
    }

    public Cursor consultarMantenimientosCursor() {
        return db.rawQuery(
                "SELECT m.id_mantenimiento, m.tipo_mantenimiento, m.estado_mantenimiento, " +
                        "m.diagnostico, m.solucion_aplicada, m.fecha_inicio, m.fecha_fin, " +
                        "eq.nombre AS equipo, eq.codigo_inventario, " +
                        "i.titulo AS incidencia, " +
                        "uc.nombres || ' ' || uc.apellidos AS usuario_crea, " +
                        "ut.nombres || ' ' || ut.apellidos AS usuario_tecnico " +
                        "FROM mantenimientos m " +
                        "INNER JOIN equipos eq ON eq.id_equipo = m.id_equipo " +
                        "LEFT JOIN incidencias i ON i.id_incidencia = m.id_incidencia " +
                        "INNER JOIN usuarios uc ON uc.id_usuario = m.id_usuario_crea " +
                        "INNER JOIN usuarios ut ON ut.id_usuario = m.id_usuario_tecnico " +
                        "ORDER BY m.id_mantenimiento DESC",
                null
        );
    }

    public HashMap<String, String> consultarMantenimiento(int idMantenimiento) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(
                    "SELECT m.*, eq.nombre AS equipo, eq.codigo_inventario, i.titulo AS incidencia, " +
                            "uc.nombres || ' ' || uc.apellidos AS usuario_crea, " +
                            "ut.nombres || ' ' || ut.apellidos AS usuario_tecnico " +
                            "FROM mantenimientos m " +
                            "INNER JOIN equipos eq ON eq.id_equipo = m.id_equipo " +
                            "LEFT JOIN incidencias i ON i.id_incidencia = m.id_incidencia " +
                            "INNER JOIN usuarios uc ON uc.id_usuario = m.id_usuario_crea " +
                            "INNER JOIN usuarios ut ON ut.id_usuario = m.id_usuario_tecnico " +
                            "WHERE m.id_mantenimiento = ?",
                    new String[]{String.valueOf(idMantenimiento)}
            );
            if (cursor.moveToFirst()) return cursorAMap(cursor);
            return null;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    public String actualizarMantenimiento(int idMantenimiento, int idEquipo, Integer idIncidencia,
                                          int idUsuarioCrea, int idUsuarioTecnico,
                                          String tipoMantenimiento, String estadoMantenimiento,
                                          String diagnostico, String solucionAplicada,
                                          String fechaInicio, String fechaFin) {
        try {
            ContentValues valores = new ContentValues();
            valores.put("id_equipo", idEquipo);
            if (idIncidencia == null) valores.putNull("id_incidencia");
            else valores.put("id_incidencia", idIncidencia);
            valores.put("id_usuario_crea", idUsuarioCrea);
            valores.put("id_usuario_tecnico", idUsuarioTecnico);
            valores.put("tipo_mantenimiento", tipoMantenimiento);
            valores.put("estado_mantenimiento", estadoMantenimiento);
            valores.put("diagnostico", diagnostico);
            valores.put("solucion_aplicada", solucionAplicada);
            valores.put("fecha_inicio", fechaInicio);
            valores.put("fecha_fin", fechaFin);
            int filas = db.update("mantenimientos", valores, "id_mantenimiento = ?", new String[]{String.valueOf(idMantenimiento)});
            return filas > 0 ? "Mantenimiento actualizado correctamente." : "No se encontró el mantenimiento.";
        } catch (SQLiteConstraintException e) {
            return "Error de integridad: Verifique equipo, incidencia, usuarios y fechas. " + e.getMessage();
        } catch (Exception e) {
            return "Error al actualizar mantenimiento: " + e.getMessage();
        }
    }

    public String finalizarMantenimiento(int idMantenimiento, String diagnostico, String solucionAplicada, String fechaFin) {
        try {
            ContentValues valores = new ContentValues();
            valores.put("estado_mantenimiento", "finalizado");
            valores.put("diagnostico", diagnostico);
            valores.put("solucion_aplicada", solucionAplicada);
            valores.put("fecha_fin", fechaFin);
            int filas = db.update("mantenimientos", valores, "id_mantenimiento = ?", new String[]{String.valueOf(idMantenimiento)});
            return filas > 0 ? "Mantenimiento finalizado correctamente." : "No se encontró el mantenimiento.";
        } catch (SQLiteConstraintException e) {
            return "Error de integridad: " + e.getMessage();
        } catch (Exception e) {
            return "Error al finalizar mantenimiento: " + e.getMessage();
        }
    }

    public String eliminarMantenimiento(int idMantenimiento) {
        try {
            int filas = db.delete("mantenimientos", "id_mantenimiento = ?", new String[]{String.valueOf(idMantenimiento)});
            return filas > 0 ? "Mantenimiento eliminado correctamente." : "No se encontró el mantenimiento.";
        } catch (SQLiteConstraintException e) {
            return "No se puede eliminar el mantenimiento porque tiene registros relacionados.";
        } catch (Exception e) {
            return "Error al eliminar mantenimiento: " + e.getMessage();
        }
    }

    // =========================================================
    // CONSULTAS PARA REPORTES / ESTADÍSTICAS
    // =========================================================

    public Cursor consultarEstadisticaIncidenciasPorEstadoCursor() {
        return db.rawQuery(
                "SELECT estado_incidencia, COUNT(*) AS total " +
                        "FROM incidencias " +
                        "GROUP BY estado_incidencia " +
                        "ORDER BY total DESC",
                null
        );
    }

    public Cursor consultarEstadisticaIncidenciasPorTipoCursor() {
        return db.rawQuery(
                "SELECT ti.nombre AS tipo_incidencia, COUNT(*) AS total " +
                        "FROM incidencias i " +
                        "INNER JOIN tipos_incidencia ti ON ti.id_tipo_incidencia = i.id_tipo_incidencia " +
                        "GROUP BY ti.nombre " +
                        "ORDER BY total DESC",
                null
        );
    }

    public Cursor consultarEquiposPorLaboratorioCursor(int idLaboratorio) {
        return db.rawQuery(
                "SELECT eq.id_equipo, eq.codigo_inventario, eq.nombre, eq.estado_equipo, cat.nombre AS categoria " +
                        "FROM equipos eq " +
                        "INNER JOIN categorias_equipo cat ON cat.id_categoria = eq.id_categoria " +
                        "WHERE eq.id_laboratorio = ? " +
                        "ORDER BY eq.nombre ASC",
                new String[]{String.valueOf(idLaboratorio)}
        );
    }

    public Cursor consultarIncidenciasPorEquipoCursor(int idEquipo) {
        return db.rawQuery(
                "SELECT i.id_incidencia, i.titulo, i.prioridad, i.estado_incidencia, i.fecha_reporte, ti.nombre AS tipo_incidencia " +
                        "FROM incidencias i " +
                        "INNER JOIN tipos_incidencia ti ON ti.id_tipo_incidencia = i.id_tipo_incidencia " +
                        "WHERE i.id_equipo = ? " +
                        "ORDER BY i.fecha_reporte DESC",
                new String[]{String.valueOf(idEquipo)}
        );
    }

    public Cursor consultarMantenimientosPorTecnicoCursor(int idUsuarioTecnico) {
        return db.rawQuery(
                "SELECT m.id_mantenimiento, m.tipo_mantenimiento, m.estado_mantenimiento, " +
                        "eq.nombre AS equipo, eq.codigo_inventario, m.fecha_inicio, m.fecha_fin " +
                        "FROM mantenimientos m " +
                        "INNER JOIN equipos eq ON eq.id_equipo = m.id_equipo " +
                        "WHERE m.id_usuario_tecnico = ? " +
                        "ORDER BY m.id_mantenimiento DESC",
                new String[]{String.valueOf(idUsuarioTecnico)}
        );
    }

    // =========================================================
    // MÉTODOS GENÉRICOS
    // =========================================================

    public String insertarRegistro(String tabla, ContentValues valores) {
        try {
            long resultado = db.insertOrThrow(tabla, null, valores);
            return resultado == -1 ? "Error al insertar registro." : "Registro insertado correctamente. ID/Fila: " + resultado;
        } catch (SQLiteConstraintException e) {
            return "Error de integridad: " + e.getMessage();
        } catch (Exception e) {
            return "Error al insertar: " + e.getMessage();
        }
    }

    public Cursor consultarRegistros(String tabla, String[] columnas, String where, String[] whereArgs, String orderBy) {
        return db.query(tabla, columnas, where, whereArgs, null, null, orderBy);
    }

    public String actualizarRegistro(String tabla, ContentValues valores, String where, String[] whereArgs) {
        try {
            int filas = db.update(tabla, valores, where, whereArgs);
            return filas > 0 ? "Registro actualizado correctamente. Filas afectadas: " + filas : "No se encontró ningún registro para actualizar.";
        } catch (SQLiteConstraintException e) {
            return "Error de integridad: " + e.getMessage();
        } catch (Exception e) {
            return "Error al actualizar: " + e.getMessage();
        }
    }

    public String eliminarRegistro(String tabla, String where, String[] whereArgs) {
        try {
            int filas = db.delete(tabla, where, whereArgs);
            return filas > 0 ? "Registro eliminado correctamente. Filas afectadas: " + filas : "No se encontró ningún registro para eliminar.";
        } catch (SQLiteConstraintException e) {
            return "Error de integridad: " + e.getMessage();
        } catch (Exception e) {
            return "Error al eliminar: " + e.getMessage();
        }
    }

    public boolean existeRegistro(String tabla, String where, String[] whereArgs) {
        Cursor cursor = null;
        try {
            cursor = db.query(tabla, new String[]{"1"}, where, whereArgs, null, null, null);
            return cursor.moveToFirst();
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    public int contarRegistros(String tabla) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT COUNT(*) FROM " + tabla, null);
            if (cursor.moveToFirst()) return cursor.getInt(0);
            return 0;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    public String llenarDatosIniciales() {
        try {
            if (db == null || !db.isOpen()) {
                return "La base de datos no está abierta.";
            }
            if (contarRegistros("roles") > 0 && contarRegistros("usuarios") > 0 && contarRegistros("equipos") > 0) {
                return "Los datos iniciales ya estaban cargados.";
            }
            db.beginTransaction();
            DBHelper.llenarDatosIniciales(db);
            db.setTransactionSuccessful();
            return "Datos iniciales cargados correctamente.";
        } catch (Exception e) {
            return "Error al cargar datos iniciales: " + e.getMessage();
        } finally {
            if (db != null && db.inTransaction()) {
                db.endTransaction();
            }
        }
    }

    private static void putDoubleOrNull(ContentValues valores, String columna, Double valor) {
        if (valor == null) valores.putNull(columna);
        else valores.put(columna, valor);
    }

    private HashMap<String, String> cursorAMap(Cursor cursor) {
        HashMap<String, String> map = new HashMap<>();
        for (int i = 0; i < cursor.getColumnCount(); i++) {
            String columna = cursor.getColumnName(i);
            if (cursor.isNull(i)) {
                map.put(columna, "");
            } else {
                map.put(columna, cursor.getString(i));
            }
        }
        return map;
    }

    public ArrayList<HashMap<String, String>> cursorAListaMap(Cursor cursor) {
        ArrayList<HashMap<String, String>> lista = new ArrayList<>();
        if (cursor == null) return lista;

        try {
            if (cursor.moveToFirst()) {
                do {
                    lista.add(cursorAMap(cursor));
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }
        return lista;
    }
}
