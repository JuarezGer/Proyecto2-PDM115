package ues.fia.proyecto2_pdm115.serviciosnube;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import ues.fia.proyecto2_pdm115.R;

public class FusionarDatosActivity extends AppCompatActivity {

    private ServiciosNubeHelper helper;
    private TextView txtEstado;
    private LinearLayout contenedorDiferencias;
    private Button btnSubirLocal, btnBajarRemoto;

    private final String[] TABLAS_COMPARAR = {"usuarios", "incidencias", "mantenimientos"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fusionar_datos);

        helper = new ServiciosNubeHelper(this);
        txtEstado = findViewById(R.id.txtEstadoFusionarDatos);
        contenedorDiferencias = findViewById(R.id.contenedorDiferenciasFusionar);
        btnSubirLocal = findViewById(R.id.btnFusionarLocalRemoto);
        btnBajarRemoto = findViewById(R.id.btnFusionarRemotoLocal);

        btnSubirLocal.setEnabled(false);
        btnBajarRemoto.setEnabled(false);

        findViewById(R.id.btnCompararFusionar).setOnClickListener(v -> compararDatos());
        btnSubirLocal.setOnClickListener(v -> confirmarSubida());
        btnBajarRemoto.setOnClickListener(v -> confirmarBajada());
        findViewById(R.id.btnVolverFusionarDatos).setOnClickListener(v -> finish());

        compararDatos();
    }

    private void compararDatos() {
        txtEstado.setText("Comparando base local con base remota...");
        contenedorDiferencias.removeAllViews();
        btnSubirLocal.setEnabled(false);
        btnBajarRemoto.setEnabled(false);

        helper.compararConServidor(TABLAS_COMPARAR, new ServiciosNubeHelper.ComparacionCallback() {
            @Override
            public void onSuccess(JSONObject comparacion, boolean hayDiferencias) {
                try {
                    if (!hayDiferencias) {
                        txtEstado.setText("No se encontraron diferencias en Usuarios, Incidencias y Mantenimientos.");
                        agregarMensaje("Las bases están sincronizadas para las tablas comparadas.");
                        return;
                    }

                    txtEstado.setText("Se encontraron cambios entre la base local y la base remota.");
                    mostrarDiferencias(comparacion);
                    btnSubirLocal.setEnabled(true);
                    btnBajarRemoto.setEnabled(true);
                } catch (Exception e) {
                    txtEstado.setText("Error mostrando diferencias: " + e.getMessage());
                }
            }

            @Override
            public void onError(String error) {
                txtEstado.setText(error);
                Toast.makeText(FusionarDatosActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void mostrarDiferencias(JSONObject comparacion) throws Exception {
        contenedorDiferencias.removeAllViews();

        for (String tabla : TABLAS_COMPARAR) {
            JSONArray difs = comparacion.optJSONArray(tabla);
            int total = difs == null ? 0 : difs.length();

            TextView tituloTabla = new TextView(this);
            tituloTabla.setText(tabla.toUpperCase() + " - diferencias: " + total);
            tituloTabla.setTextSize(17);
            tituloTabla.setTypeface(null, android.graphics.Typeface.BOLD);
            tituloTabla.setPadding(0, ServiciosNubeUiHelper.dp(this, 16), 0, ServiciosNubeUiHelper.dp(this, 4));
            contenedorDiferencias.addView(tituloTabla);

            if (total == 0) {
                agregarMensaje("Sin diferencias en " + tabla + ".");
                continue;
            }

            for (int i = 0; i < total; i++) {
                JSONObject dif = difs.getJSONObject(i);
                String tipo = dif.optString("tipo");
                String id = dif.optString("id");
                String subtitulo = "Tipo de diferencia: " + tipo + " | ID: " + id;
                ServiciosNubeUiHelper.agregarCard(this, contenedorDiferencias,
                        tabla + " #" + id,
                        subtitulo,
                        "⇄",
                        v -> mostrarDetalleDiferencia(tabla, dif));
            }
        }
    }

    private void mostrarDetalleDiferencia(String tabla, JSONObject dif) {
        try {
            String detalle = "TABLA: " + tabla + "\n" +
                    "TIPO: " + dif.optString("tipo") + "\n" +
                    "ID: " + dif.optString("id") + "\n\n" +
                    "===== LOCAL =====\n" + valorObjeto(dif, "local") + "\n\n" +
                    "===== REMOTO =====\n" + valorObjeto(dif, "remoto");

            new AlertDialog.Builder(this)
                    .setTitle("Detalle de diferencia")
                    .setMessage(detalle)
                    .setPositiveButton("Cerrar", null)
                    .show();
        } catch (Exception e) {
            Toast.makeText(this, "Error al mostrar diferencia: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private String valorObjeto(JSONObject dif, String key) throws Exception {
        if (!dif.has(key) || dif.isNull(key)) return "No existe registro.";
        return ServiciosNubeUiHelper.jsonBonito(dif.getJSONObject(key));
    }

    private void confirmarSubida() {
        new AlertDialog.Builder(this)
                .setTitle("Fusionar datos")
                .setMessage("Esto guardará los registros locales en la base remota de XAMPP. ¿Deseas continuar?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Fusionar", (dialog, which) -> subirLocalAlServidor())
                .show();
    }

    private void confirmarBajada() {
        new AlertDialog.Builder(this)
                .setTitle("Fusionar datos")
                .setMessage("Esto extraerá los registros de la base remota y los guardará en la base local SQLite. ¿Deseas continuar?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Fusionar", (dialog, which) -> bajarRemotoALocal())
                .show();
    }

    private void subirLocalAlServidor() {
        txtEstado.setText("Guardando base local en XAMPP...");
        helper.guardarLocalEnServidor(new ServiciosNubeHelper.SyncCallback() {
            @Override
            public void onSuccess(String mensaje) {
                Toast.makeText(FusionarDatosActivity.this, mensaje, Toast.LENGTH_LONG).show();
                compararDatos();
            }

            @Override
            public void onError(String error) {
                txtEstado.setText(error);
                Toast.makeText(FusionarDatosActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void bajarRemotoALocal() {
        txtEstado.setText("Restaurando base remota en SQLite...");
        helper.restaurarRemotoEnLocal(new ServiciosNubeHelper.SyncCallback() {
            @Override
            public void onSuccess(String mensaje) {
                Toast.makeText(FusionarDatosActivity.this, mensaje, Toast.LENGTH_LONG).show();
                compararDatos();
            }

            @Override
            public void onError(String error) {
                txtEstado.setText(error);
                Toast.makeText(FusionarDatosActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void agregarMensaje(String mensaje) {
        TextView tv = new TextView(this);
        tv.setText(mensaje);
        tv.setTextSize(14);
        tv.setPadding(0, ServiciosNubeUiHelper.dp(this, 8), 0, ServiciosNubeUiHelper.dp(this, 8));
        contenedorDiferencias.addView(tv);
    }
}
