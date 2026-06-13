package ues.fia.proyecto2_pdm115.usuario;

import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import ues.fia.proyecto2_pdm115.R;
import ues.fia.proyecto2_pdm115.controlDBLabCare;

public class CrearUsuarioActivity extends AppCompatActivity {

    private Spinner spRolUsuario;
    private Spinner spUsaBiometriaUsuario;
    private Spinner spActivoUsuario;
    private EditText edtNombresUsuario;
    private EditText edtApellidosUsuario;
    private EditText edtCorreoUsuario;
    private EditText edtClaveUsuario;
    private TextView txtMensajeCrearUsuario;
    private Button btnGuardarUsuario;
    private Button btnLimpiarUsuario;
    private Button btnVolverCrearUsuario;

    private controlDBLabCare db;
    private ArrayList<RolItem> listaRoles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_usuario);

        enlazarVistas();
        abrirBaseDatos();
        configurarEventos();
        cargarRoles();
        cargarSpinnersEstado();
    }

    private void enlazarVistas() {
        spRolUsuario = findViewById(R.id.spRolUsuario);
        spUsaBiometriaUsuario = findViewById(R.id.spUsaBiometriaUsuario);
        spActivoUsuario = findViewById(R.id.spActivoUsuario);
        edtNombresUsuario = findViewById(R.id.edtNombresUsuario);
        edtApellidosUsuario = findViewById(R.id.edtApellidosUsuario);
        edtCorreoUsuario = findViewById(R.id.edtCorreoUsuario);
        edtClaveUsuario = findViewById(R.id.edtClaveUsuario);
        txtMensajeCrearUsuario = findViewById(R.id.txtMensajeCrearUsuario);
        btnGuardarUsuario = findViewById(R.id.btnGuardarUsuario);
        btnLimpiarUsuario = findViewById(R.id.btnLimpiarUsuario);
        btnVolverCrearUsuario = findViewById(R.id.btnVolverCrearUsuario);
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
        btnGuardarUsuario.setOnClickListener(v -> guardarUsuario());
        btnLimpiarUsuario.setOnClickListener(v -> limpiarCampos());
        btnVolverCrearUsuario.setOnClickListener(v -> finish());
    }

    private void cargarRoles() {
        listaRoles = new ArrayList<>();
        Cursor cursor = null;

        try {
            if (db == null || db.getDb() == null || !db.getDb().isOpen()) {
                abrirBaseDatos();
            }

            cursor = db.consultarRolesCursor();

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("id_rol"));
                    String nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"));
                    listaRoles.add(new RolItem(id, nombre));
                } while (cursor.moveToNext());
            }

            ArrayAdapter<RolItem> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_spinner_item,
                    listaRoles
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spRolUsuario.setAdapter(adapter);

            if (listaRoles.isEmpty()) {
                mostrarMensaje("No hay roles registrados. Cree un rol antes de registrar usuarios.");
                btnGuardarUsuario.setEnabled(false);
            } else {
                btnGuardarUsuario.setEnabled(true);
            }

        } catch (Exception e) {
            mostrarMensaje("Error al cargar roles: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private void cargarSpinnersEstado() {
        ArrayAdapter<String> adapterBiometria = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"No", "Sí"}
        );
        adapterBiometria.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spUsaBiometriaUsuario.setAdapter(adapterBiometria);

        ArrayAdapter<String> adapterActivo = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Activo", "Inactivo"}
        );
        adapterActivo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spActivoUsuario.setAdapter(adapterActivo);
    }

    private void guardarUsuario() {
        if (listaRoles == null || listaRoles.isEmpty()) {
            mostrarMensaje("No hay roles disponibles para asignar al usuario.");
            return;
        }

        RolItem rolSeleccionado = (RolItem) spRolUsuario.getSelectedItem();
        String nombres = edtNombresUsuario.getText().toString().trim();
        String apellidos = edtApellidosUsuario.getText().toString().trim();
        String correo = edtCorreoUsuario.getText().toString().trim();
        String clave = edtClaveUsuario.getText().toString().trim();
        int usaBiometria = obtenerValorSiNo(spUsaBiometriaUsuario);
        int activo = obtenerValorActivo(spActivoUsuario);

        if (!validarCampos(nombres, apellidos, correo, clave)) {
            return;
        }

        if (db == null || db.getDb() == null || !db.getDb().isOpen()) {
            abrirBaseDatos();
        }

        String mensaje = db.insertarUsuario(
                rolSeleccionado.id,
                nombres,
                apellidos,
                correo,
                clave,
                usaBiometria,
                activo
        );

        mostrarMensaje(mensaje);

        if (mensaje.toLowerCase().contains("correctamente")) {
            limpiarCampos();
        }
    }

    private boolean validarCampos(String nombres, String apellidos, String correo, String clave) {
        if (nombres.isEmpty()) {
            edtNombresUsuario.setError("Ingrese los nombres");
            edtNombresUsuario.requestFocus();
            return false;
        }
        if (apellidos.isEmpty()) {
            edtApellidosUsuario.setError("Ingrese los apellidos");
            edtApellidosUsuario.requestFocus();
            return false;
        }
        if (correo.isEmpty()) {
            edtCorreoUsuario.setError("Ingrese el correo");
            edtCorreoUsuario.requestFocus();
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            edtCorreoUsuario.setError("Ingrese un correo válido");
            edtCorreoUsuario.requestFocus();
            return false;
        }
        if (clave.isEmpty()) {
            edtClaveUsuario.setError("Ingrese la clave");
            edtClaveUsuario.requestFocus();
            return false;
        }
        return true;
    }

    private int obtenerValorSiNo(Spinner spinner) {
        Object item = spinner.getSelectedItem();
        return item != null && item.toString().equalsIgnoreCase("Sí") ? 1 : 0;
    }

    private int obtenerValorActivo(Spinner spinner) {
        Object item = spinner.getSelectedItem();
        return item != null && item.toString().equalsIgnoreCase("Activo") ? 1 : 0;
    }

    private void limpiarCampos() {
        edtNombresUsuario.setText("");
        edtApellidosUsuario.setText("");
        edtCorreoUsuario.setText("");
        edtClaveUsuario.setText("");
        if (spRolUsuario.getCount() > 0) spRolUsuario.setSelection(0);
        if (spUsaBiometriaUsuario.getCount() > 0) spUsaBiometriaUsuario.setSelection(0);
        if (spActivoUsuario.getCount() > 0) spActivoUsuario.setSelection(0);
        edtNombresUsuario.requestFocus();
    }

    private void mostrarMensaje(String mensaje) {
        txtMensajeCrearUsuario.setText(mensaje);
        txtMensajeCrearUsuario.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null) {
            db.cerrar();
            db = null;
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
