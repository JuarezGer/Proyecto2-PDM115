package ues.fia.proyecto2_pdm115;

import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import ues.fia.proyecto2_pdm115.utils.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import java.util.HashMap;
import java.util.concurrent.Executor;

public class LoginBiometricoActivity extends AppCompatActivity {

    private EditText edtCorreoBiometria;
    private TextView txtMensajeBiometria;
    private Button btnContinuarBiometria;
    private Button btnVolverLogin;

    private controlDBLabCare db;
    private SessionManager sessionManager;

    private final int autenticadores = BiometricManager.Authenticators.BIOMETRIC_WEAK
            | BiometricManager.Authenticators.DEVICE_CREDENTIAL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_biometrico);

        sessionManager = new SessionManager(this);

        if (sessionManager.sesionActiva()) {
            abrirMain();
            return;
        }

        enlazarVistas();
        abrirBaseDatos();
        cargarCorreoSugerido();
        configurarEventos();
        validarDisponibilidadBiometrica();
    }

    private void enlazarVistas() {
        edtCorreoBiometria = findViewById(R.id.edtCorreoBiometria);
        txtMensajeBiometria = findViewById(R.id.txtMensajeBiometria);
        btnContinuarBiometria = findViewById(R.id.btnContinuarBiometria);
        btnVolverLogin = findViewById(R.id.btnVolverLogin);
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

    private void cargarCorreoSugerido() {
        String correoIntent = getIntent().getStringExtra("correo");

        if (correoIntent != null && !correoIntent.trim().isEmpty()) {
            edtCorreoBiometria.setText(correoIntent.trim());
            return;
        }

        String ultimoCorreo = sessionManager.getUltimoCorreo();
        if (ultimoCorreo != null && !ultimoCorreo.trim().isEmpty()) {
            edtCorreoBiometria.setText(ultimoCorreo.trim());
        }
    }

    private void configurarEventos() {
        btnContinuarBiometria.setOnClickListener(v -> validarCorreoYAutenticar());

        btnVolverLogin.setOnClickListener(v -> finish());
    }

    private void validarDisponibilidadBiometrica() {
        BiometricManager biometricManager = BiometricManager.from(this);
        int estado = biometricManager.canAuthenticate(autenticadores);

        if (estado == BiometricManager.BIOMETRIC_SUCCESS) {
            btnContinuarBiometria.setEnabled(true);
            txtMensajeBiometria.setVisibility(View.GONE);
        } else {
            btnContinuarBiometria.setEnabled(false);
            mostrarMensaje("La biometría o credencial del dispositivo no está disponible. Configura huella, rostro, PIN o patrón en el dispositivo.");
        }
    }

    private void validarCorreoYAutenticar() {
        String correo = edtCorreoBiometria.getText().toString().trim();

        if (correo.isEmpty()) {
            edtCorreoBiometria.setError("Ingrese el correo");
            edtCorreoBiometria.requestFocus();
            return;
        }

        if (db == null || db.getDb() == null || !db.getDb().isOpen()) {
            abrirBaseDatos();
        }

        HashMap<String, String> usuario = db.consultarUsuarioPorCorreo(correo);

        if (usuario == null) {
            mostrarMensaje("No existe un usuario activo con ese correo.");
            return;
        }

        if (!db.usuarioUsaBiometria(correo)) {
            mostrarMensaje("Este usuario existe, pero no tiene activado el inicio con biometría.");
            return;
        }

        mostrarPromptBiometrico(correo);
    }

    private void mostrarPromptBiometrico(String correo) {
        Executor executor = ContextCompat.getMainExecutor(this);

        BiometricPrompt biometricPrompt = new BiometricPrompt(
                this,
                executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        Toast.makeText(LoginBiometricoActivity.this, errString, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        guardarSesionDesdeCorreo(correo);
                        abrirMain();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        Toast.makeText(LoginBiometricoActivity.this, "No se pudo validar la biometría.", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Ingreso biométrico")
                .setSubtitle("Confirma tu identidad para entrar a LabCare")
                .setAllowedAuthenticators(autenticadores)
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    private void guardarSesionDesdeCorreo(String correo) {
        HashMap<String, String> usuario = db.consultarUsuarioPorCorreo(correo);

        if (usuario == null) {
            mostrarMensaje("No se encontró el usuario.");
            return;
        }

        int idUsuario = Integer.parseInt(usuario.get("id_usuario"));
        String nombre = usuario.get("nombres") + " " + usuario.get("apellidos");
        String rol = usuario.get("rol");

        sessionManager.guardarSesion(idUsuario, correo, nombre, rol);
    }

    private void mostrarMensaje(String mensaje) {
        txtMensajeBiometria.setText(mensaje);
        txtMensajeBiometria.setVisibility(View.VISIBLE);
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
