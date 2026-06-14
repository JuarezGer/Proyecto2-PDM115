package ues.fia.proyecto2_pdm115.usuario;

import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;

import ues.fia.proyecto2_pdm115.R;
import ues.fia.proyecto2_pdm115.controlDBLabCare;

public class ActualizarUsuarioActivity extends AppCompatActivity {

    private Spinner spUsuarioActualizar;
    private Spinner spRolUsuarioActualizar;
    private Spinner spUsaBiometriaUsuarioActualizar;
    private Spinner spActivoUsuarioActualizar;
    private EditText edtNombresUsuarioActualizar;
    private EditText edtApellidosUsuarioActualizar;
    private EditText edtCorreoUsuarioActualizar;
    private EditText edtClaveUsuarioActualizar;
    private TextView txtMensajeActualizarUsuario;
    private Button btnActualizarUsuario;
    private Button btnLimpiarActualizarUsuario;
    private Button btnVolverActualizarUsuario;

    private controlDBLabCare db;
    private ArrayList<UsuarioItem> listaUsuarios;
    private ArrayList<RolItem> listaRoles;
    private String claveActual = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actualizar_usuario);

        enlazarVistas();
        abrirBaseDatos();
        cargarRoles();
        cargarSpinnersEstado();
        cargarUsuarios();
        configurarEventos();
    }

    private void enlazarVistas() {
        spUsuarioActualizar = findViewById(R.id.spUsuarioActualizar);
        spRolUsuarioActualizar = findViewById(R.id.spRolUsuarioActualizar);
        spUsaBiometriaUsuarioActualizar = findViewById(R.id.spUsaBiometriaUsuarioActualizar);
        spActivoUsuarioActualizar = findViewById(R.id.spActivoUsuarioActualizar);
        edtNombresUsuarioActualizar = findViewById(R.id.edtNombresUsuarioActualizar);
        edtApellidosUsuarioActualizar = findViewById(R.id.edtApellidosUsuarioActualizar);
        edtCorreoUsuarioActualizar = findViewById(R.id.edtCorreoUsuarioActualizar);
        edtClaveUsuarioActualizar = findViewById(R.id.edtClaveUsuarioActualizar);
        txtMensajeActualizarUsuario = findViewById(R.id.txtMensajeActualizarUsuario);
        btnActualizarUsuario = findViewById(R.id.btnActualizarUsuario);
        btnLimpiarActualizarUsuario = findViewById(R.id.btnLimpiarActualizarUsuario);
        btnVolverActualizarUsuario = findViewById(R.id.btnVolverActualizarUsuario);
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
        spUsuarioActualizar.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                cargarDatosUsuarioSeleccionado();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        btnActualizarUsuario.setOnClickListener(v -> actualizarUsuario());
        btnLimpiarActualizarUsuario.setOnClickListener(v -> cargarDatosUsuarioSeleccionado());
        btnVolverActualizarUsuario.setOnClickListener(v -> finish());
    }

    private void cargarRoles() {
        listaRoles = new ArrayList<>();
        Cursor cursor = null;

        try {
            if (db == null || db.getDb() == null || !db.getDb().isOpen()) abrirBaseDatos();

            cursor = db.consultarRolesCursor();
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("id_rol"));
                    String nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"));
                    listaRoles.add(new RolItem(id, nombre));
                } while (cursor.moveToNext());
            }

            ArrayAdapter<RolItem> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listaRoles);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spRolUsuarioActualizar.setAdapter(adapter);

        } catch (Exception e) {
            mostrarMensaje("Error al cargar roles: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private void cargarSpinnersEstado() {
        ArrayAdapter<String> adapterBiometria = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"No", "Sí"});
        adapterBiometria.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spUsaBiometriaUsuarioActualizar.setAdapter(adapterBiometria);

        ArrayAdapter<String> adapterActivo = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Activo", "Inactivo"});
        adapterActivo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spActivoUsuarioActualizar.setAdapter(adapterActivo);
    }

    private void cargarUsuarios() {
        listaUsuarios = new ArrayList<>();
        Cursor cursor = null;

        try {
            if (db == null || db.getDb() == null || !db.getDb().isOpen()) abrirBaseDatos();

            cursor = db.consultarUsuariosCursor();
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("id_usuario"));
                    String nombres = cursor.getString(cursor.getColumnIndexOrThrow("nombres"));
                    String apellidos = cursor.getString(cursor.getColumnIndexOrThrow("apellidos"));
                    String correo = cursor.getString(cursor.getColumnIndexOrThrow("correo"));
                    listaUsuarios.add(new UsuarioItem(id, nombres, apellidos, correo));
                } while (cursor.moveToNext());
            }

            ArrayAdapter<UsuarioItem> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listaUsuarios);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spUsuarioActualizar.setAdapter(adapter);

            if (listaUsuarios.isEmpty()) {
                mostrarMensaje("No hay usuarios registrados.");
                btnActualizarUsuario.setEnabled(false);
            } else {
                btnActualizarUsuario.setEnabled(true);
            }

        } catch (Exception e) {
            mostrarMensaje("Error al cargar usuarios: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private void cargarDatosUsuarioSeleccionado() {
        if (listaUsuarios == null || listaUsuarios.isEmpty()) {
            return;
        }

        UsuarioItem seleccionado = (UsuarioItem) spUsuarioActualizar.getSelectedItem();
        if (seleccionado == null) return;

        try {
            HashMap<String, String> usuario = db.consultarUsuario(seleccionado.id);
            if (usuario == null) {
                mostrarMensaje("No se encontró el usuario seleccionado.");
                return;
            }

            edtNombresUsuarioActualizar.setText(valor(usuario, "nombres"));
            edtApellidosUsuarioActualizar.setText(valor(usuario, "apellidos"));
            edtCorreoUsuarioActualizar.setText(valor(usuario, "correo"));
            edtClaveUsuarioActualizar.setText("");
            claveActual = valor(usuario, "clave_hash");

            seleccionarRol(entero(usuario, "id_rol"));
            seleccionarSiNo(spUsaBiometriaUsuarioActualizar, entero(usuario, "usa_biometria"));
            seleccionarActivo(entero(usuario, "activo"));
            txtMensajeActualizarUsuario.setVisibility(View.GONE);

        } catch (Exception e) {
            mostrarMensaje("Error al cargar datos del usuario: " + e.getMessage());
        }
    }

    private void actualizarUsuario() {
        if (listaUsuarios == null || listaUsuarios.isEmpty()) {
            Toast.makeText(this, "No hay registros para actualizar.", Toast.LENGTH_SHORT).show();
            return;
        }

        UsuarioItem usuarioSeleccionado = (UsuarioItem) spUsuarioActualizar.getSelectedItem();
        RolItem rolSeleccionado = (RolItem) spRolUsuarioActualizar.getSelectedItem();

        if (usuarioSeleccionado == null || rolSeleccionado == null) {
            mostrarMensaje("Seleccione usuario y rol.");
            return;
        }

        String nombres = edtNombresUsuarioActualizar.getText().toString().trim();
        String apellidos = edtApellidosUsuarioActualizar.getText().toString().trim();
        String correo = edtCorreoUsuarioActualizar.getText().toString().trim();
        String claveNueva = edtClaveUsuarioActualizar.getText().toString().trim();
        String claveEnviar = claveNueva.isEmpty() ? claveActual : claveNueva;
        int usaBiometria = obtenerValorSiNo(spUsaBiometriaUsuarioActualizar);
        int activo = obtenerValorActivo(spActivoUsuarioActualizar);

        if (!validarCampos(nombres, apellidos, correo)) {
            return;
        }

        String mensaje = db.actualizarUsuario(
                usuarioSeleccionado.id,
                rolSeleccionado.id,
                nombres,
                apellidos,
                correo,
                claveEnviar,
                usaBiometria,
                activo
        );

        mostrarMensaje(mensaje);

        if (mensaje.toLowerCase().contains("correctamente")) {
            cargarUsuarios();
        }
    }

    private boolean validarCampos(String nombres, String apellidos, String correo) {
        if (nombres.isEmpty()) {
            edtNombresUsuarioActualizar.setError("Ingrese los nombres");
            edtNombresUsuarioActualizar.requestFocus();
            return false;
        }
        if (apellidos.isEmpty()) {
            edtApellidosUsuarioActualizar.setError("Ingrese los apellidos");
            edtApellidosUsuarioActualizar.requestFocus();
            return false;
        }
        if (correo.isEmpty()) {
            edtCorreoUsuarioActualizar.setError("Ingrese el correo");
            edtCorreoUsuarioActualizar.requestFocus();
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            edtCorreoUsuarioActualizar.setError("Ingrese un correo válido");
            edtCorreoUsuarioActualizar.requestFocus();
            return false;
        }
        return true;
    }

    private String valor(HashMap<String, String> mapa, String clave) {
        String valor = mapa.get(clave);
        return valor == null ? "" : valor;
    }

    private int entero(HashMap<String, String> mapa, String clave) {
        try {
            String valor = mapa.get(clave);
            if (valor == null || valor.trim().isEmpty()) return 0;
            return Integer.parseInt(valor);
        } catch (Exception e) {
            return 0;
        }
    }

    private void seleccionarRol(int idRol) {
        for (int i = 0; i < spRolUsuarioActualizar.getCount(); i++) {
            RolItem item = (RolItem) spRolUsuarioActualizar.getItemAtPosition(i);
            if (item.id == idRol) {
                spRolUsuarioActualizar.setSelection(i);
                return;
            }
        }
    }

    private void seleccionarSiNo(Spinner spinner, int valor) {
        spinner.setSelection(valor == 1 ? 1 : 0);
    }

    private void seleccionarActivo(int valor) {
        spActivoUsuarioActualizar.setSelection(valor == 1 ? 0 : 1);
    }

    private int obtenerValorSiNo(Spinner spinner) {
        Object item = spinner.getSelectedItem();
        return item != null && item.toString().equalsIgnoreCase("Sí") ? 1 : 0;
    }

    private int obtenerValorActivo(Spinner spinner) {
        Object item = spinner.getSelectedItem();
        return item != null && item.toString().equalsIgnoreCase("Activo") ? 1 : 0;
    }

    private void mostrarMensaje(String mensaje) {
        txtMensajeActualizarUsuario.setText(mensaje);
        txtMensajeActualizarUsuario.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null) {
            db.cerrar();
            db = null;
        }
    }

    private static class UsuarioItem {
        int id;
        String nombres;
        String apellidos;
        String correo;

        UsuarioItem(int id, String nombres, String apellidos, String correo) {
            this.id = id;
            this.nombres = nombres;
            this.apellidos = apellidos;
            this.correo = correo;
        }

        @Override
        public String toString() {
            return id + " - " + nombres + " " + apellidos;
        }
    }

    private static class RolItem {
        int id;
        String nombre;

        RolItem(int id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }

        @Override
        public String toString() {
            return id + " - " + nombre;
        }
    }
}
