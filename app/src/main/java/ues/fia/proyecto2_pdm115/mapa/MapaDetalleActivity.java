package ues.fia.proyecto2_pdm115.mapa;

import android.app.Activity;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;

import ues.fia.proyecto2_pdm115.R;
import ues.fia.proyecto2_pdm115.controlDBLabCare;

public class MapaDetalleActivity extends Activity {

    private static final GeoPoint UBICACION_DEFAULT = new GeoPoint(13.7167, -89.2033);
    private static final int REQUEST_LOCATION_PERMISSION = 3001;
    private MyLocationNewOverlay ubicacionUsuarioOverlay;

    private MapView mapView;
    private controlDBLabCare db;

    private TextView txtTituloMapa;
    private TextView txtTotalMarcadores;
    private TextView txtMensajeMapa;
    private Button btnCentrarMapa;
    private Button btnVolverMapa;

    private String tipoMapa;
    private String tituloMapa;
    private final ArrayList<MarcadorMapa> marcadores = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().load(
                getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
        );
        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.activity_mapa_detalle);

        tipoMapa = getIntent().getStringExtra(MapasMenuActivity.EXTRA_TIPO_MAPA);
        tituloMapa = getIntent().getStringExtra(MapasMenuActivity.EXTRA_TITULO_MAPA);

        if (tipoMapa == null || tipoMapa.trim().isEmpty()) {
            tipoMapa = MapasMenuActivity.TIPO_INCIDENCIAS;
        }

        if (tituloMapa == null || tituloMapa.trim().isEmpty()) {
            tituloMapa = "Mapa";
        }

        enlazarVistas();
        abrirBaseDatos();
        configurarEventos();
        configurarMapaBase();
        cargarMarcadoresSegunTipo();
        pintarMarcadores();
        activarUbicacionUsuario();
    }

    private void enlazarVistas() {
        txtTituloMapa = findViewById(R.id.txtTituloMapa);
        txtTotalMarcadores = findViewById(R.id.txtTotalMarcadores);
        txtMensajeMapa = findViewById(R.id.txtMensajeMapa);
        btnCentrarMapa = findViewById(R.id.btnCentrarMapa);
        btnVolverMapa = findViewById(R.id.btnVolverMapa);
        mapView = findViewById(R.id.mapViewLabCare);

        txtTituloMapa.setText(tituloMapa);
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
        btnCentrarMapa.setOnClickListener(v -> centrarMapa());
        btnVolverMapa.setOnClickListener(v -> finish());
    }

    private void configurarMapaBase() {
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.setBuiltInZoomControls(true);
        mapView.getController().setZoom(15.0);
        mapView.getController().setCenter(UBICACION_DEFAULT);
    }
    private void activarUbicacionUsuario() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    REQUEST_LOCATION_PERMISSION
            );
            return;
        }

        GpsMyLocationProvider proveedorGps = new GpsMyLocationProvider(this);
        proveedorGps.addLocationSource(android.location.LocationManager.GPS_PROVIDER);
        proveedorGps.addLocationSource(android.location.LocationManager.NETWORK_PROVIDER);

        ubicacionUsuarioOverlay = new MyLocationNewOverlay(proveedorGps, mapView);
        ubicacionUsuarioOverlay.enableMyLocation();
        ubicacionUsuarioOverlay.enableFollowLocation();

        mapView.getOverlays().add(ubicacionUsuarioOverlay);

        ubicacionUsuarioOverlay.runOnFirstFix(() -> runOnUiThread(() -> {
            if (ubicacionUsuarioOverlay.getMyLocation() != null) {
                mapView.getController().animateTo(ubicacionUsuarioOverlay.getMyLocation());
                mapView.getController().setZoom(17.0);
            }
        }));

        mapView.invalidate();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            boolean permisoConcedido = false;

            for (int resultado : grantResults) {
                if (resultado == PackageManager.PERMISSION_GRANTED) {
                    permisoConcedido = true;
                    break;
                }
            }

            if (permisoConcedido) {
                activarUbicacionUsuario();
            } else {
                android.widget.Toast.makeText(
                        this,
                        "No se concedió permiso de ubicación.",
                        android.widget.Toast.LENGTH_LONG
                ).show();
            }
        }
    }

    private void cargarMarcadoresSegunTipo() {
        marcadores.clear();

        if (db == null || db.getDb() == null || !db.getDb().isOpen()) {
            abrirBaseDatos();
        }

        if (MapasMenuActivity.TIPO_INCIDENCIAS.equals(tipoMapa)) {
            cargarIncidencias();
        } else if (MapasMenuActivity.TIPO_EDIFICIOS.equals(tipoMapa)) {
            cargarEdificios();
        } else if (MapasMenuActivity.TIPO_LABORATORIOS.equals(tipoMapa)) {
            cargarLaboratorios();
        } else if (MapasMenuActivity.TIPO_MANTENIMIENTOS.equals(tipoMapa)) {
            cargarMantenimientos();
        } else {
            cargarIncidencias();
        }

        txtTotalMarcadores.setText("Puntos encontrados: " + marcadores.size());
    }

    private void cargarIncidencias() {
        Cursor cursor = null;

        try {
            cursor = db.getDb().rawQuery(
                    "SELECT i.id_incidencia, i.titulo, i.descripcion, i.prioridad, " +
                            "i.estado_incidencia, i.origen_registro, i.latitud, i.longitud, " +
                            "ti.nombre AS tipo_incidencia, eq.nombre AS equipo, lab.nombre AS laboratorio " +
                            "FROM incidencias i " +
                            "INNER JOIN tipos_incidencia ti ON ti.id_tipo_incidencia = i.id_tipo_incidencia " +
                            "INNER JOIN equipos eq ON eq.id_equipo = i.id_equipo " +
                            "INNER JOIN laboratorios lab ON lab.id_laboratorio = eq.id_laboratorio " +
                            "WHERE i.latitud IS NOT NULL AND i.longitud IS NOT NULL " +
                            "ORDER BY i.fecha_reporte DESC, i.id_incidencia DESC",
                    null
            );

            if (cursor.moveToFirst()) {
                do {
                    Double latitud = doubleCursor(cursor, "latitud");
                    Double longitud = doubleCursor(cursor, "longitud");

                    if (latitud == null || longitud == null) continue;

                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("id_incidencia"));
                    String titulo = textoCursor(cursor, "titulo");
                    String tipo = textoCursor(cursor, "tipo_incidencia");
                    String estado = textoCursor(cursor, "estado_incidencia");
                    String prioridad = textoCursor(cursor, "prioridad");
                    String equipo = textoCursor(cursor, "equipo");
                    String laboratorio = textoCursor(cursor, "laboratorio");

                    marcadores.add(new MarcadorMapa(
                            latitud,
                            longitud,
                            "⚠ Incidencia #" + id + ": " + titulo,
                            "Tipo: " + tipo + "\nEstado: " + estado +
                                    "\nPrioridad: " + prioridad +
                                    "\nEquipo: " + equipo +
                                    "\nLaboratorio: " + laboratorio
                    ));

                } while (cursor.moveToNext());
            }

        } catch (Exception e) {
            mostrarMensaje("Error al cargar incidencias: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private void cargarEdificios() {
        Cursor cursor = null;

        try {
            cursor = db.getDb().rawQuery(
                    "SELECT id_edificio, nombre, codigo, latitud, longitud " +
                            "FROM edificios " +
                            "WHERE latitud IS NOT NULL AND longitud IS NOT NULL " +
                            "ORDER BY nombre ASC",
                    null
            );

            if (cursor.moveToFirst()) {
                do {
                    Double latitud = doubleCursor(cursor, "latitud");
                    Double longitud = doubleCursor(cursor, "longitud");

                    if (latitud == null || longitud == null) continue;

                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("id_edificio"));
                    String nombre = textoCursor(cursor, "nombre");
                    String codigo = textoCursor(cursor, "codigo");

                    marcadores.add(new MarcadorMapa(
                            latitud,
                            longitud,
                            "🏢 Edificio #" + id + ": " + nombre,
                            "Código: " + codigo
                    ));

                } while (cursor.moveToNext());
            }

        } catch (Exception e) {
            mostrarMensaje("Error al cargar edificios: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private void cargarLaboratorios() {
        Cursor cursor = null;

        try {
            cursor = db.getDb().rawQuery(
                    "SELECT lab.id_laboratorio, lab.nombre, lab.codigo, lab.piso, " +
                            "lab.latitud, lab.longitud, ed.nombre AS edificio " +
                            "FROM laboratorios lab " +
                            "INNER JOIN edificios ed ON ed.id_edificio = lab.id_edificio " +
                            "WHERE lab.latitud IS NOT NULL AND lab.longitud IS NOT NULL " +
                            "ORDER BY lab.nombre ASC",
                    null
            );

            if (cursor.moveToFirst()) {
                do {
                    Double latitud = doubleCursor(cursor, "latitud");
                    Double longitud = doubleCursor(cursor, "longitud");

                    if (latitud == null || longitud == null) continue;

                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("id_laboratorio"));
                    String nombre = textoCursor(cursor, "nombre");
                    String codigo = textoCursor(cursor, "codigo");
                    String piso = textoCursor(cursor, "piso");
                    String edificio = textoCursor(cursor, "edificio");

                    marcadores.add(new MarcadorMapa(
                            latitud,
                            longitud,
                            "🔬 Laboratorio #" + id + ": " + nombre,
                            "Código: " + codigo +
                                    "\nPiso: " + piso +
                                    "\nEdificio: " + edificio
                    ));

                } while (cursor.moveToNext());
            }

        } catch (Exception e) {
            mostrarMensaje("Error al cargar laboratorios: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private void cargarMantenimientos() {
        Cursor cursor = null;

        try {
            cursor = db.getDb().rawQuery(
                    "SELECT m.id_mantenimiento, m.tipo_mantenimiento, m.estado_mantenimiento, " +
                            "m.fecha_inicio, m.fecha_fin, eq.nombre AS equipo, eq.codigo_inventario, " +
                            "lab.nombre AS laboratorio, i.titulo AS incidencia, " +
                            "COALESCE(i.latitud, lab.latitud) AS latitud, " +
                            "COALESCE(i.longitud, lab.longitud) AS longitud " +
                            "FROM mantenimientos m " +
                            "INNER JOIN equipos eq ON eq.id_equipo = m.id_equipo " +
                            "INNER JOIN laboratorios lab ON lab.id_laboratorio = eq.id_laboratorio " +
                            "LEFT JOIN incidencias i ON i.id_incidencia = m.id_incidencia " +
                            "WHERE COALESCE(i.latitud, lab.latitud) IS NOT NULL " +
                            "AND COALESCE(i.longitud, lab.longitud) IS NOT NULL " +
                            "ORDER BY m.id_mantenimiento DESC",
                    null
            );

            if (cursor.moveToFirst()) {
                do {
                    Double latitud = doubleCursor(cursor, "latitud");
                    Double longitud = doubleCursor(cursor, "longitud");

                    if (latitud == null || longitud == null) continue;

                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("id_mantenimiento"));
                    String tipo = textoCursor(cursor, "tipo_mantenimiento");
                    String estado = textoCursor(cursor, "estado_mantenimiento");
                    String equipo = textoCursor(cursor, "equipo");
                    String codigo = textoCursor(cursor, "codigo_inventario");
                    String laboratorio = textoCursor(cursor, "laboratorio");
                    String incidencia = textoCursor(cursor, "incidencia");

                    if (incidencia.trim().isEmpty()) {
                        incidencia = "Sin incidencia relacionada";
                    }

                    marcadores.add(new MarcadorMapa(
                            latitud,
                            longitud,
                            "🔧 Mantenimiento #" + id + ": " + tipo,
                            "Estado: " + estado +
                                    "\nEquipo: " + equipo + " (" + codigo + ")" +
                                    "\nLaboratorio: " + laboratorio +
                                    "\nIncidencia: " + incidencia
                    ));

                } while (cursor.moveToNext());
            }

        } catch (Exception e) {
            mostrarMensaje("Error al cargar mantenimientos: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private void pintarMarcadores() {
        if (mapView == null) {
            return;
        }

        mapView.getOverlays().clear();

        if (marcadores.isEmpty()) {
            mostrarMensaje("No hay registros con coordenadas para mostrar en este mapa.");
            mapView.getController().setZoom(15.0);
            mapView.getController().setCenter(UBICACION_DEFAULT);
            mapView.invalidate();
            return;
        }

        txtMensajeMapa.setVisibility(View.GONE);

        for (MarcadorMapa marcador : marcadores) {
            Marker marker = new Marker(mapView);
            marker.setPosition(new GeoPoint(marcador.latitud, marcador.longitud));
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setTitle(marcador.titulo);
            marker.setSubDescription(marcador.descripcion);
            marker.setPanToView(true);
            mapView.getOverlays().add(marker);
        }

        mapView.invalidate();
        centrarMapa();
    }

    private void centrarMapa() {
        if (mapView == null) return;

        if (marcadores.isEmpty()) {
            mapView.getController().setZoom(15.0);
            mapView.getController().setCenter(UBICACION_DEFAULT);
            return;
        }

        if (marcadores.size() == 1) {
            MarcadorMapa marcador = marcadores.get(0);
            mapView.getController().setZoom(17.0);
            mapView.getController().animateTo(new GeoPoint(marcador.latitud, marcador.longitud));
            return;
        }

        double norte = -90.0;
        double sur = 90.0;
        double este = -180.0;
        double oeste = 180.0;

        for (MarcadorMapa marcador : marcadores) {
            norte = Math.max(norte, marcador.latitud);
            sur = Math.min(sur, marcador.latitud);
            este = Math.max(este, marcador.longitud);
            oeste = Math.min(oeste, marcador.longitud);
        }

        BoundingBox boundingBox = new BoundingBox(norte, este, sur, oeste);
        mapView.post(() -> mapView.zoomToBoundingBox(boundingBox, true, 90));
    }

    private void mostrarMensaje(String mensaje) {
        txtMensajeMapa.setText(mensaje);
        txtMensajeMapa.setVisibility(View.VISIBLE);
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
    }

    private String textoCursor(Cursor cursor, String columna) {
        int index = cursor.getColumnIndex(columna);
        if (index < 0 || cursor.isNull(index)) return "";
        return cursor.getString(index);
    }

    private Double doubleCursor(Cursor cursor, String columna) {
        int index = cursor.getColumnIndex(columna);
        if (index < 0 || cursor.isNull(index)) return null;
        return cursor.getDouble(index);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
        if (ubicacionUsuarioOverlay != null) {
            ubicacionUsuarioOverlay.enableMyLocation();
            ubicacionUsuarioOverlay.enableFollowLocation();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
        if (ubicacionUsuarioOverlay != null) {
            ubicacionUsuarioOverlay.disableFollowLocation();
            ubicacionUsuarioOverlay.disableMyLocation();
        }
    }

    @Override
    protected void onDestroy() {
        if (mapView != null) {
            mapView.onDetach();
        }

        if (db != null) {
            db.cerrar();
            db = null;
        }

        super.onDestroy();
    }

    private static class MarcadorMapa {
        double latitud;
        double longitud;
        String titulo;
        String descripcion;

        MarcadorMapa(double latitud, double longitud, String titulo, String descripcion) {
            this.latitud = latitud;
            this.longitud = longitud;
            this.titulo = titulo;
            this.descripcion = descripcion;
        }
    }
}
