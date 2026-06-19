package ues.fia.proyecto2_pdm115.serviciosnube;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import ues.fia.proyecto2_pdm115.R;

public class ServiciosNubeMenuActivity extends AppCompatActivity {

    private ServiciosNubeHelper helper;
    private TextView txtEstado;
    private Button btnGuardar, btnRestaurar, btnVerificar, btnVolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_servicios_nube_menu);

        helper = new ServiciosNubeHelper(this);

        txtEstado = findViewById(R.id.txtEstadoServiciosNube);
        btnGuardar = findViewById(R.id.btnGuardarServidor);
        btnRestaurar = findViewById(R.id.btnRestaurarServidor);
        btnVerificar = findViewById(R.id.btnVerificarConexion);
        btnVolver = findViewById(R.id.btnVolverServiciosNube);

        btnVerificar.setOnClickListener(v -> verificarConexion());
        btnGuardar.setOnClickListener(v -> confirmarGuardar());
        btnRestaurar.setOnClickListener(v -> confirmarRestaurar());
        btnVolver.setOnClickListener(v -> finish());
    }

    private void verificarConexion() {
        bloquearBotones(true);
        txtEstado.setText("Verificando conexión...");

        helper.probarConexion(new ServiciosNubeHelper.SyncCallback() {
            @Override
            public void onSuccess(String mensaje) {
                bloquearBotones(false);
                txtEstado.setText(mensaje);
            }

            @Override
            public void onError(String error) {
                bloquearBotones(false);
                txtEstado.setText("Error: " + error);
            }
        });
    }

    private void confirmarGuardar() {
        new AlertDialog.Builder(this)
                .setTitle("Guardar en servidor")
                .setMessage("Se enviarán a XAMPP los registros locales de roles, usuarios, equipos, incidencias y mantenimientos. También se enviarán catálogos relacionados necesarios.")
                .setPositiveButton("Guardar", (dialog, which) -> guardarEnServidor())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void guardarEnServidor() {
        bloquearBotones(true);
        txtEstado.setText("Guardando registros en servidor...");

        helper.guardarRegistrosEnServidor(new ServiciosNubeHelper.SyncCallback() {
            @Override
            public void onSuccess(String mensaje) {
                bloquearBotones(false);
                txtEstado.setText(mensaje);
                mostrarMensaje("Éxito", mensaje);
            }

            @Override
            public void onError(String error) {
                bloquearBotones(false);
                txtEstado.setText("Error: " + error);
                mostrarMensaje("Error", error);
            }
        });
    }

    private void confirmarRestaurar() {
        new AlertDialog.Builder(this)
                .setTitle("Restaurar desde servidor")
                .setMessage("Se descargarán registros desde XAMPP y se insertarán/actualizarán en SQLite local. No borra registros locales que no estén en el servidor.")
                .setPositiveButton("Restaurar", (dialog, which) -> restaurarDesdeServidor())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void restaurarDesdeServidor() {
        bloquearBotones(true);
        txtEstado.setText("Restaurando registros desde servidor...");

        helper.restaurarRegistrosDesdeServidor(new ServiciosNubeHelper.SyncCallback() {
            @Override
            public void onSuccess(String mensaje) {
                bloquearBotones(false);
                txtEstado.setText(mensaje);
                mostrarMensaje("Éxito", mensaje);
            }

            @Override
            public void onError(String error) {
                bloquearBotones(false);
                txtEstado.setText("Error: " + error);
                mostrarMensaje("Error", error);
            }
        });
    }

    private void bloquearBotones(boolean bloquear) {
        btnGuardar.setEnabled(!bloquear);
        btnRestaurar.setEnabled(!bloquear);
        btnVerificar.setEnabled(!bloquear);
        btnVolver.setEnabled(!bloquear);
        txtEstado.setVisibility(View.VISIBLE);
    }

    private void mostrarMensaje(String titulo, String mensaje) {
        new AlertDialog.Builder(this)
                .setTitle(titulo)
                .setMessage(mensaje)
                .setPositiveButton("Aceptar", null)
                .show();
    }
}
