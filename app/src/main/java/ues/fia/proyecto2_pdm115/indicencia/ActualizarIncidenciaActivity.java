package ues.fia.proyecto2_pdm115.indicencia;

import android.Manifest;
import android.database.Cursor;
import android.database.SQLException;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.ArrayList;
import java.util.HashMap;

import ues.fia.proyecto2_pdm115.R;
import ues.fia.proyecto2_pdm115.controlDBLabCare;

public class ActualizarIncidenciaActivity extends AppCompatActivity {

    private static final int REQUEST_UBICACION = 3102;

    private Spinner spIncidencia;
    private Spinner spEquipo;
    private Spinner spUsuarioReporta;
    private Spinner spTipoIncidencia;
    private Spinner spPrioridad;
    private Spinner spEstado;
    private Spinner spOrigen;
    private EditText edtTitulo;
    private EditText edtDescripcion;
    private EditText edtTextoVozOriginal;
    private EditText edtLatitud;
    private EditText edtLongitud;
    private TextView txtMensaje;
    private Button btnObtenerUbicacion;
    private Button btnActualizar;
    private Button btnLimpiar;
    private Button btnVolver;

    private controlDBLabCare db;
    private FusedLocationProviderClient fusedLocationClient;
    private ArrayList<IncidenciaItem> listaIncidencias;
    private ArrayList<ItemSpinner> listaEquipos;
    private ArrayList<ItemSpinner> listaUsuarios;
    private ArrayList<ItemSpinner> listaTipos;
    private boolean cargandoFormulario = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actualizar_incidencia);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        enlazarVistas();
        abrirBaseDatos();
        cargarCombos();
        cargarIncidencias();
        configurarEventos();
    }

    private void enlazarVistas() {
        spIncidencia = findViewById(R.id.spIncidenciaActualizar);
        spEquipo = findViewById(R.id.spEquipoIncidenciaActualizar);
        spUsuarioReporta = findViewById(R.id.spUsuarioReportaIncidenciaActualizar);
        spTipoIncidencia = findViewById(R.id.spTipoIncidenciaActualizarIncidencia);
        spPrioridad = findViewById(R.id.spPrioridadIncidenciaActualizar);
        spEstado = findViewById(R.id.spEstadoIncidenciaActualizar);
        spOrigen = findViewById(R.id.spOrigenIncidenciaActualizar);
        edtTitulo = findViewById(R.id.edtTituloIncidenciaActualizar);
        edtDescripcion = findViewById(R.id.edtDescripcionIncidenciaActualizar);
        edtTextoVozOriginal = findViewById(R.id.edtTextoVozOriginalIncidenciaActualizar);
        edtLatitud = findViewById(R.id.edtLatitudIncidenciaActualizar);
        edtLongitud = findViewById(R.id.edtLongitudIncidenciaActualizar);
        txtMensaje = findViewById(R.id.txtMensajeActualizarIncidencia);
        btnObtenerUbicacion = findViewById(R.id.btnObtenerUbicacionIncidenciaActualizar);
        btnActualizar = findViewById(R.id.btnActualizarIncidencia);
        btnLimpiar = findViewById(R.id.btnLimpiarActualizarIncidencia);
        btnVolver = findViewById(R.id.btnVolverActualizarIncidencia);
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

    private void cargarCombos() {
        cargarEquipos();
        cargarUsuarios();
        cargarTiposIncidencia();
        cargarSpinnerTexto(spPrioridad, "baja", "media", "alta", "critica");
        cargarSpinnerTexto(spEstado, "pendiente", "en_proceso", "resuelta", "cancelada");
        cargarSpinnerTexto(spOrigen, "manual", "voz", "qr", "web");
    }

    private void configurarEventos() {
        spIncidencia.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!cargandoFormulario && listaIncidencias != null && !listaIncidencias.isEmpty()) {
                    cargarDatosIncidencia(listaIncidencias.get(position).id);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        btnObtenerUbicacion.setOnClickListener(v -> {
            obtenerUbicacionActual();
        });
        btnActualizar.setOnClickListener(v -> actualizarIncidencia());
        btnLimpiar.setOnClickListener(v -> limpiarCamposEditables());
        btnVolver.setOnClickListener(v -> finish());
    }

    private void cargarIncidencias() {
        listaIncidencias = new ArrayList<>();
        Cursor cursor = null;

        try {
            cursor = db.consultarIncidenciasCursor();
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("id_incidencia"));
                    String titulo = cursor.getString(cursor.getColumnIndexOrThrow("titulo"));
                    String estado = cursor.getString(cursor.getColumnIndexOrThrow("estado_incidencia"));
                    listaIncidencias.add(new IncidenciaItem(id, titulo, estado));
                } while (cursor.moveToNext());
            }

            ArrayAdapter<IncidenciaItem> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listaIncidencias);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spIncidencia.setAdapter(adapter);

            if (listaIncidencias.isEmpty()) {
                mostrarMensaje("No hay incidencias registradas.");
                btnActualizar.setEnabled(false);
            } else {
                btnActualizar.setEnabled(true);
                cargarDatosIncidencia(listaIncidencias.get(0).id);
            }
        } catch (Exception e) {
            mostrarMensaje("Error al cargar incidencias: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private void cargarDatosIncidencia(int idIncidencia) {
        try {
            cargandoFormulario = true;
            HashMap<String, String> datos = db.consultarIncidencia(idIncidencia);

            if (datos == null) {
                mostrarMensaje("No se encontró la incidencia seleccionada.");
                return;
            }

            seleccionarSpinnerPorId(spEquipo, enteroMapa(datos, "id_equipo"));
            seleccionarSpinnerPorId(spUsuarioReporta, enteroMapa(datos, "id_usuario_reporta"));
            seleccionarSpinnerPorId(spTipoIncidencia, enteroMapa(datos, "id_tipo_incidencia"));
            seleccionarSpinnerTexto(spPrioridad, datos.get("prioridad"));
            seleccionarSpinnerTexto(spEstado, datos.get("estado_incidencia"));
            seleccionarSpinnerTexto(spOrigen, datos.get("origen_registro"));

            edtTitulo.setText(valor(datos, "titulo"));
            edtDescripcion.setText(valor(datos, "descripcion"));
            edtTextoVozOriginal.setText(valor(datos, "texto_voz_original"));
            edtLatitud.setText(valor(datos, "latitud"));
            edtLongitud.setText(valor(datos, "longitud"));
            txtMensaje.setVisibility(View.GONE);
        } finally {
            cargandoFormulario = false;
        }
    }


    private void obtenerUbicacionActual() {
        if (!permisoUbicacionConcedido()) {
            pedirPermisoUbicacion();
            return;
        }

        if (fusedLocationClient == null) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        }

        mostrarMensaje("Obteniendo ubicación GPS...");

        try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            colocarUbicacion(location);
                        } else {
                            obtenerUltimaUbicacionDisponible();
                        }
                    })
                    .addOnFailureListener(e -> {
                        mostrarMensaje("No se pudo obtener la ubicación: " + e.getMessage());
                        mostrarAnimacionError();
                    });
        } catch (SecurityException e) {
            mostrarMensaje("Permiso de ubicación no concedido.");
            mostrarAnimacionError();
            mostrarAnimacionError();
        }
    }

    private void obtenerUltimaUbicacionDisponible() {
        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            colocarUbicacion(location);
                        } else {
                            mostrarMensaje("No se pudo obtener la ubicación. Active el GPS e inténtelo nuevamente.");
                            mostrarAnimacionError();
                        }
                    })
                    .addOnFailureListener(e -> {
                        mostrarMensaje("No se pudo obtener la última ubicación: " + e.getMessage());
                        mostrarAnimacionError();
                    });
        } catch (SecurityException e) {
            mostrarMensaje("Permiso de ubicación no concedido.");
        }
    }

    private void colocarUbicacion(Location location) {
        edtLatitud.setText(String.valueOf(location.getLatitude()));
        edtLongitud.setText(String.valueOf(location.getLongitude()));
        mostrarMensaje("Ubicación agregada correctamente.");
    }

    private boolean permisoUbicacionConcedido() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void pedirPermisoUbicacion() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                REQUEST_UBICACION
        );
    }

    private void actualizarIncidencia() {
        if (listaIncidencias == null || listaIncidencias.isEmpty()) {
            mostrarMensaje("No hay registros para actualizar.");
            mostrarAnimacionError();
            return;
        }

        String titulo = texto(edtTitulo);
        String descripcion = texto(edtDescripcion);
        String textoVozOriginal = texto(edtTextoVozOriginal);
        String prioridad = textoSeleccionado(spPrioridad);
        String estado = textoSeleccionado(spEstado);
        String origen = textoSeleccionado(spOrigen);
        Double latitud = doubleNullable(edtLatitud);
        Double longitud = doubleNullable(edtLongitud);

        if (titulo.isEmpty()) {
            edtTitulo.setError("Ingrese el título");
            edtTitulo.requestFocus();
            mostrarAnimacionError();
            return;
        }

        if (descripcion.isEmpty()) {
            edtDescripcion.setError("Ingrese la descripción");
            edtDescripcion.requestFocus();
            mostrarAnimacionError();
            return;
        }

        if ("voz".equals(origen) && textoVozOriginal.isEmpty()) {
            edtTextoVozOriginal.setError("Si el origen es voz, debe existir texto original");
            edtTextoVozOriginal.requestFocus();
            mostrarAnimacionError();
            return;
        }

        IncidenciaItem incidencia = (IncidenciaItem) spIncidencia.getSelectedItem();
        int idEquipo = ((ItemSpinner) spEquipo.getSelectedItem()).id;
        int idUsuario = ((ItemSpinner) spUsuarioReporta.getSelectedItem()).id;
        int idTipo = ((ItemSpinner) spTipoIncidencia.getSelectedItem()).id;

        String mensaje = db.actualizarIncidencia(
                incidencia.id,
                idEquipo,
                idUsuario,
                idTipo,
                titulo,
                descripcion,
                prioridad,
                estado,
                origen,
                textoVozOriginal.isEmpty() ? null : textoVozOriginal,
                latitud,
                longitud
        );

        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
        mostrarMensaje(mensaje);

        if (mensaje.toLowerCase().contains("correctamente")) {
        } else {
            mostrarAnimacionError();
        }

        cargarIncidencias();
    }

    private void limpiarCamposEditables() {
        edtTitulo.setText("");
        edtDescripcion.setText("");
        edtTextoVozOriginal.setText("");
        edtLatitud.setText("");
        edtLongitud.setText("");
        spPrioridad.setSelection(0);
        spEstado.setSelection(0);
        spOrigen.setSelection(0);
    }

    private void cargarEquipos() {
        listaEquipos = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.consultarEquiposCursor();
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("id_equipo"));
                    String codigo = cursor.getString(cursor.getColumnIndexOrThrow("codigo_inventario"));
                    String nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"));
                    listaEquipos.add(new ItemSpinner(id, codigo + " - " + nombre));
                } while (cursor.moveToNext());
            }
            cargarAdapter(spEquipo, listaEquipos);
        } catch (Exception e) {
            mostrarMensaje("Error al cargar equipos: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private void cargarUsuarios() {
        listaUsuarios = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.consultarUsuariosCursor();
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("id_usuario"));
                    String nombres = cursor.getString(cursor.getColumnIndexOrThrow("nombres"));
                    String apellidos = cursor.getString(cursor.getColumnIndexOrThrow("apellidos"));
                    listaUsuarios.add(new ItemSpinner(id, nombres + " " + apellidos));
                } while (cursor.moveToNext());
            }
            cargarAdapter(spUsuarioReporta, listaUsuarios);
        } catch (Exception e) {
            mostrarMensaje("Error al cargar usuarios: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private void cargarTiposIncidencia() {
        listaTipos = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.consultarTiposIncidenciaCursor();
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("id_tipo_incidencia"));
                    String nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"));
                    listaTipos.add(new ItemSpinner(id, nombre));
                } while (cursor.moveToNext());
            }
            cargarAdapter(spTipoIncidencia, listaTipos);
        } catch (Exception e) {
            mostrarMensaje("Error al cargar tipos: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private void cargarAdapter(Spinner spinner, ArrayList<ItemSpinner> lista) {
        ArrayAdapter<ItemSpinner> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, lista);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void cargarSpinnerTexto(Spinner spinner, String... valores) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, valores);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void seleccionarSpinnerPorId(Spinner spinner, int id) {
        for (int i = 0; i < spinner.getCount(); i++) {
            Object item = spinner.getItemAtPosition(i);
            if (item instanceof ItemSpinner && ((ItemSpinner) item).id == id) {
                spinner.setSelection(i);
                return;
            }
        }
    }

    private void seleccionarSpinnerTexto(Spinner spinner, String texto) {
        if (texto == null) return;
        for (int i = 0; i < spinner.getCount(); i++) {
            Object item = spinner.getItemAtPosition(i);
            if (item != null && item.toString().equalsIgnoreCase(texto)) {
                spinner.setSelection(i);
                return;
            }
        }
    }

    private String texto(EditText editText) {
        return editText.getText().toString().trim();
    }

    private String textoSeleccionado(Spinner spinner) {
        Object item = spinner.getSelectedItem();
        return item == null ? "" : item.toString();
    }

    private Double doubleNullable(EditText editText) {
        String valor = texto(editText);
        if (valor.isEmpty()) return null;
        try {
            return Double.parseDouble(valor);
        } catch (Exception e) {
            editText.setError("Valor numérico no válido");
            editText.requestFocus();
            return null;
        }
    }

    private int enteroMapa(HashMap<String, String> mapa, String clave) {
        try {
            return Integer.parseInt(valor(mapa, clave));
        } catch (Exception e) {
            return 0;
        }
    }

    private String valor(HashMap<String, String> mapa, String clave) {
        String valor = mapa.get(clave);
        return valor == null ? "" : valor;
    }

    private void mostrarAnimacionError() {
        // El feedback de error se muestra con el mensaje de pantalla y los errores de campo.
    }

    private void mostrarMensaje(String mensaje) {
        txtMensaje.setText(mensaje);
        txtMensaje.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_UBICACION) {
            boolean permisoConcedido = false;

            for (int resultado : grantResults) {
                if (resultado == PackageManager.PERMISSION_GRANTED) {
                    permisoConcedido = true;
                    break;
                }
            }

            if (permisoConcedido) {
                obtenerUbicacionActual();
            } else {
                mostrarMensaje("Permiso de ubicación denegado. Puede escribir las coordenadas manualmente.");
                mostrarAnimacionError();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null) {
            db.cerrar();
            db = null;
        }
    }

    private static class ItemSpinner {
        int id;
        String texto;

        ItemSpinner(int id, String texto) {
            this.id = id;
            this.texto = texto;
        }

        @Override
        public String toString() {
            return texto;
        }
    }

    private static class IncidenciaItem {
        int id;
        String titulo;
        String estado;

        IncidenciaItem(int id, String titulo, String estado) {
            this.id = id;
            this.titulo = titulo;
            this.estado = estado;
        }

        @Override
        public String toString() {
            return id + " - " + titulo + " (" + estado + ")";
        }
    }
}
