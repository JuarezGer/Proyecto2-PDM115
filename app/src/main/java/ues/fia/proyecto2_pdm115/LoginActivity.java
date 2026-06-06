package ues.fia.proyecto2_pdm115;

import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import ues.fia.proyecto2_pdm115.utils.*;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    private EditText edtCorreo;
    private EditText edtClave;
    private CheckBox chkMostrarClave;
    private Button btnIngresar;
    private Button btnBiometria;
    private TextView txtMensajeLogin;

    private controlDBLabCare db;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(this);

        if (sessionManager.sesionActiva()) {
            abrirMain();
            return;
        }

        enlazarVistas();
        abrirBaseDatos();
        cargarUltimoCorreo();
        configurarEventos();
    }

    private void enlazarVistas() {
        edtCorreo = findViewById(R.id.edtCorreoLogin);
        edtClave = findViewById(R.id.edtClaveLogin);
        chkMostrarClave = findViewById(R.id.chkMostrarClave);
        btnIngresar = findViewById(R.id.btnIngresarLogin);
        btnBiometria = findViewById(R.id.btnBiometriaLogin);
        txtMensajeLogin = findViewById(R.id.txtMensajeLogin);
    }

    private void abrirBaseDatos() {
        db = new controlDBLabCare(this);
        try {
            db.abrir();
        } catch (SQLException e) {
            Toast.makeText(this, "Error al abrir la base de datos: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void cargarUltimoCorreo() {
        String ultimoCorreo = sessionManager.getUltimoCorreo();
        if (ultimoCorreo != null && !ultimoCorreo.trim().isEmpty()) {
            edtCorreo.setText(ultimoCorreo);
        }
    }

    private void configurarEventos() {
        btnIngresar.setOnClickListener(v -> iniciarSesionConClave());

        btnBiometria.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, LoginBiometricoActivity.class);

            String correo = edtCorreo.getText().toString().trim();
            if (!correo.isEmpty()) {
                intent.putExtra("correo", correo);
            }

            startActivity(intent);
        });

        chkMostrarClave.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                edtClave.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                edtClave.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            edtClave.setSelection(edtClave.getText().length());
        });
    }

    private void iniciarSesionConClave() {
        String correo = edtCorreo.getText().toString().trim();
        String clave = edtClave.getText().toString();

        if (correo.isEmpty()) {
            edtCorreo.setError("Ingrese el correo");
            edtCorreo.requestFocus();
            return;
        }

        if (clave.isEmpty()) {
            edtClave.setError("Ingrese la clave");
            edtClave.requestFocus();
            return;
        }

        if (db == null || db.getDb() == null || !db.getDb().isOpen()) {
            abrirBaseDatos();
        }

        boolean loginValido = db.validarLogin(correo, clave);

        if (loginValido) {
            guardarSesionDesdeCorreo(correo);
            abrirMain();
        } else {
            txtMensajeLogin.setText("Correo o clave incorrectos, o usuario inactivo.");
            txtMensajeLogin.setVisibility(View.VISIBLE);
        }
    }

    private void guardarSesionDesdeCorreo(String correo) {
        HashMap<String, String> usuario = db.consultarUsuarioPorCorreo(correo);

        if (usuario == null) {
            Toast.makeText(this, "No se encontró el usuario.", Toast.LENGTH_LONG).show();
            return;
        }

        int idUsuario = Integer.parseInt(usuario.get("id_usuario"));
        String nombre = usuario.get("nombres") + " " + usuario.get("apellidos");
        String rol = usuario.get("rol");

        sessionManager.guardarSesion(idUsuario, correo, nombre, rol);
    }

    private void abrirMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null) {
            db.cerrar();
            db = null;
        }
    }
}
