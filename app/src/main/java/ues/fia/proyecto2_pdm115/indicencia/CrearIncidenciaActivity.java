package ues.fia.proyecto2_pdm115.indicencia;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
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
import java.util.Locale;

import ues.fia.proyecto2_pdm115.R;
import ues.fia.proyecto2_pdm115.controlDBLabCare;

public class CrearIncidenciaActivity extends AppCompatActivity {

    private static final int REQUEST_RECONOCER_VOZ = 3001;
    private static final int REQUEST_UBICACION = 3002;

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
    private Button btnDictarDescripcion;
    private Button btnObtenerUbicacion;
    private Button btnGuardar;
    private Button btnLimpiar;
    private Button btnVolver;

    private controlDBLabCare db;
    private FusedLocationProviderClient fusedLocationClient;
    private ArrayList<ItemSpinner> listaEquipos;
    private ArrayList<ItemSpinner> listaUsuarios;
    private ArrayList<ItemSpinner> listaTipos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_incidencia);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        enlazarVistas();
        abrirBaseDatos();
        cargarCombos();
        configurarEventos();
    }

    private void enlazarVistas() {
        spEquipo = findViewById(R.id.spEquipoIncidenciaCrear);
        spUsuarioReporta = findViewById(R.id.spUsuarioReportaIncidenciaCrear);
        spTipoIncidencia = findViewById(R.id.spTipoIncidenciaCrear);
        spPrioridad = findViewById(R.id.spPrioridadIncidenciaCrear);
        spEstado = findViewById(R.id.spEstadoIncidenciaCrear);
        spOrigen = findViewById(R.id.spOrigenIncidenciaCrear);
        edtTitulo = findViewById(R.id.edtTituloIncidenciaCrear);
        edtDescripcion = findViewById(R.id.edtDescripcionIncidenciaCrear);
        edtTextoVozOriginal = findViewById(R.id.edtTextoVozOriginalIncidenciaCrear);
        edtLatitud = findViewById(R.id.edtLatitudIncidenciaCrear);
        edtLongitud = findViewById(R.id.edtLongitudIncidenciaCrear);
        txtMensaje = findViewById(R.id.txtMensajeCrearIncidencia);
        btnDictarDescripcion = findViewById(R.id.btnDictarDescripcionIncidencia);
        btnObtenerUbicacion = findViewById(R.id.btnObtenerUbicacionIncidenciaCrear);
        btnGuardar = findViewById(R.id.btnGuardarIncidencia);
        btnLimpiar = findViewById(R.id.btnLimpiarIncidencia);
        btnVolver = findViewById(R.id.btnVolverCrearIncidencia);
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
        btnDictarDescripcion.setOnClickListener(v -> {
            iniciarReconocimientoVoz();
        });
        btnObtenerUbicacion.setOnClickListener(v -> {
            obtenerUbicacionActual();
        });
        btnGuardar.setOnClickListener(v -> guardarIncidencia());
        btnLimpiar.setOnClickListener(v -> limpiarCampos());
        btnVolver.setOnClickListener(v -> finish());
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
            mostrarMensaje("Error al cargar tipos de incidencia: " + e.getMessage());
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

    private void iniciarReconocimientoVoz() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, new Locale("es", "SV"));
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Describe la incidencia del equipo");

        try {
            startActivityForResult(intent, REQUEST_RECONOCER_VOZ);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "El dispositivo no tiene reconocimiento de voz disponible.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_RECONOCER_VOZ && resultCode == Activity.RESULT_OK && data != null) {
            ArrayList<String> resultados = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (resultados != null && !resultados.isEmpty()) {
                String textoReconocido = resultados.get(0);
                edtDescripcion.setText(textoReconocido);
                edtTextoVozOriginal.setText(textoReconocido);
                seleccionarSpinnerTexto(spOrigen, "voz");
                mostrarMensaje("Descripción agregada por voz.");
            }
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

    private void guardarIncidencia() {
        if (listaEquipos == null || listaEquipos.isEmpty()) {
            mostrarMensaje("Debe registrar equipos antes de crear una incidencia.");
            mostrarAnimacionError();
            return;
        }

        if (listaUsuarios == null || listaUsuarios.isEmpty()) {
            mostrarMensaje("Debe registrar usuarios antes de crear una incidencia.");
            mostrarAnimacionError();
            return;
        }

        if (listaTipos == null || listaTipos.isEmpty()) {
            mostrarMensaje("Debe registrar tipos de incidencia antes de crear una incidencia.");
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

        int idEquipo = ((ItemSpinner) spEquipo.getSelectedItem()).id;
        int idUsuario = ((ItemSpinner) spUsuarioReporta.getSelectedItem()).id;
        int idTipo = ((ItemSpinner) spTipoIncidencia.getSelectedItem()).id;

        if (db == null || db.getDb() == null || !db.getDb().isOpen()) {
            abrirBaseDatos();
        }

        String mensaje = db.insertarIncidencia(
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

        mostrarMensaje(mensaje);

        if (mensaje.toLowerCase().contains("correctamente")) {
            mostrarAnimacionExito();
            limpiarCampos();
        } else {
            mostrarAnimacionError();
        }
    }

    private void mostrarAnimacionExito() {
        // El feedback principal se muestra con el mensaje de pantalla.
    }

    private void mostrarAnimacionError() {
        // El feedback de error se muestra con el mensaje de pantalla y los errores de campo.
    }

    private void limpiarCampos() {
        edtTitulo.setText("");
        edtDescripcion.setText("");
        edtTextoVozOriginal.setText("");
        edtLatitud.setText("");
        edtLongitud.setText("");
        spPrioridad.setSelection(0);
        spEstado.setSelection(0);
        spOrigen.setSelection(0);
        edtTitulo.requestFocus();
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

    private void seleccionarSpinnerTexto(Spinner spinner, String texto) {
        for (int i = 0; i < spinner.getCount(); i++) {
            Object item = spinner.getItemAtPosition(i);
            if (item != null && item.toString().equalsIgnoreCase(texto)) {
                spinner.setSelection(i);
                return;
            }
        }
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
}
