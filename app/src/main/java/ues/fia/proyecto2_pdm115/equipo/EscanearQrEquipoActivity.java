package ues.fia.proyecto2_pdm115.equipo;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import org.json.JSONObject;

import java.util.HashMap;

import ues.fia.proyecto2_pdm115.R;
import ues.fia.proyecto2_pdm115.controlDBLabCare;

public class EscanearQrEquipoActivity extends AppCompatActivity {

    private controlDBLabCare db;
    private TextView txtResultadoQrEquipo;
    private Button btnIniciarEscaneoQr;
    private Button btnVolverEscanerQr;

    private ActivityResultLauncher<ScanOptions> qrLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_escanear_qr_equipo);

        enlazarVistas();
        configurarBaseDatos();
        configurarEscaner();
        configurarEventos();

        new Handler(Looper.getMainLooper()).postDelayed(this::iniciarEscaneo, 350);
    }

    private void enlazarVistas() {
        txtResultadoQrEquipo = findViewById(R.id.txtResultadoQrEquipo);
        btnIniciarEscaneoQr = findViewById(R.id.btnIniciarEscaneoQr);
        btnVolverEscanerQr = findViewById(R.id.btnVolverEscanerQr);
    }

    private void configurarBaseDatos() {
        db = new controlDBLabCare(this);
        try {
            db.abrir();
        } catch (SQLException e) {
            Toast.makeText(this, "Error al abrir base de datos: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void configurarEscaner() {
        qrLauncher = registerForActivityResult(new ScanContract(), result -> {
            if (result.getContents() == null) {
                txtResultadoQrEquipo.setText("Escaneo cancelado.");
                Toast.makeText(this, "Escaneo cancelado", Toast.LENGTH_SHORT).show();
                return;
            }

            String contenidoQr = result.getContents().trim();
            txtResultadoQrEquipo.setText("Código leído: " + contenidoQr);
            buscarEquipoDesdeQr(contenidoQr);
        });
    }

    private void configurarEventos() {
        btnIniciarEscaneoQr.setOnClickListener(v -> iniciarEscaneo());
        btnVolverEscanerQr.setOnClickListener(v -> finish());
    }

    private void iniciarEscaneo() {
        ScanOptions options = new ScanOptions();
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        options.setPrompt("Escanea el código QR del equipo");
        options.setCameraId(0);
        options.setBeepEnabled(true);
        options.setBarcodeImageEnabled(false);
        options.setOrientationLocked(false);
        qrLauncher.launch(options);
    }

    private void buscarEquipoDesdeQr(String contenidoQr) {
        if (db == null || db.getDb() == null || !db.getDb().isOpen()) {
            configurarBaseDatos();
        }

        HashMap<String, String> equipo = null;

        String codigo = extraerCodigoDesdeQr(contenidoQr);
        if (codigo != null && !codigo.trim().isEmpty()) {
            equipo = db.consultarEquipoPorQR(codigo.trim());
        }

        if (equipo == null) {
            Integer idEquipo = extraerIdEquipoDesdeQr(contenidoQr);
            if (idEquipo != null) {
                equipo = db.consultarEquipo(idEquipo);
            }
        }

        if (equipo == null) {
            Toast.makeText(this, "No se encontró ningún equipo para ese QR", Toast.LENGTH_LONG).show();
            txtResultadoQrEquipo.setText("No se encontró equipo. QR leído: " + contenidoQr);
            return;
        }

        mostrarModalEquipo(equipo);
    }

    private String extraerCodigoDesdeQr(String contenidoQr) {
        try {
            JSONObject json = new JSONObject(contenidoQr);

            if (json.has("codigo_qr")) {
                return json.optString("codigo_qr", "");
            }

            if (json.has("codigo_inventario")) {
                return json.optString("codigo_inventario", "");
            }

            if (json.has("codigo")) {
                return json.optString("codigo", "");
            }

        } catch (Exception ignored) {
            // Si no es JSON, se toma el contenido completo como código QR o código inventario.
        }

        return contenidoQr;
    }

    private Integer extraerIdEquipoDesdeQr(String contenidoQr) {
        try {
            JSONObject json = new JSONObject(contenidoQr);
            if (json.has("id_equipo")) {
                return json.optInt("id_equipo", -1) == -1 ? null : json.optInt("id_equipo");
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    private void mostrarModalEquipo(HashMap<String, String> equipo) {
        String detalle = construirDetalleEquipo(equipo);

        TextView txtDetalle = new TextView(this);
        txtDetalle.setText(detalle);
        txtDetalle.setTextSize(15);
        txtDetalle.setTextColor(getResources().getColor(R.color.blue_gray_900));
        txtDetalle.setPadding(dp(20), dp(16), dp(20), dp(16));
        txtDetalle.setGravity(Gravity.START);

        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(txtDetalle);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Datos del equipo")
                .setView(scrollView)
                .setPositiveButton("Actualizar", (d, which) -> abrirActualizarEquipo(equipo))
                .setNeutralButton("Escanear otro", (d, which) -> iniciarEscaneo())
                .setNegativeButton("Cerrar", null)
                .create();

        dialog.show();
    }

    private String construirDetalleEquipo(HashMap<String, String> e) {
        return "ID Equipo: " + valor(e, "id_equipo") + "\n\n" +
                "ID Laboratorio: " + valor(e, "id_laboratorio") + "\n" +
                "Laboratorio: " + valor(e, "laboratorio") + "\n" +
                "Edificio: " + valor(e, "edificio") + "\n\n" +
                "ID Categoría: " + valor(e, "id_categoria") + "\n" +
                "Categoría: " + valor(e, "categoria") + "\n\n" +
                "Código inventario: " + valor(e, "codigo_inventario") + "\n" +
                "Código QR: " + valor(e, "codigo_qr") + "\n\n" +
                "Nombre: " + valor(e, "nombre") + "\n" +
                "Marca: " + valor(e, "marca") + "\n" +
                "Modelo: " + valor(e, "modelo") + "\n" +
                "Estado: " + valor(e, "estado_equipo");
    }

    private void abrirActualizarEquipo(HashMap<String, String> equipo) {
        Intent intent = new Intent(this, ActualizarEquipoActivity.class);
        intent.putExtra("id_equipo", entero(valor(equipo, "id_equipo")));
        intent.putExtra("codigo_inventario", textoIntent(equipo, "codigo_inventario"));
        intent.putExtra("codigo_qr", textoIntent(equipo, "codigo_qr"));
        intent.putExtra("nombre", textoIntent(equipo, "nombre"));
        intent.putExtra("marca", textoIntent(equipo, "marca"));
        intent.putExtra("modelo", textoIntent(equipo, "modelo"));
        intent.putExtra("laboratorio", textoIntent(equipo, "laboratorio"));
        intent.putExtra("categoria", textoIntent(equipo, "categoria"));
        intent.putExtra("estado_equipo", textoIntent(equipo, "estado_equipo"));
        startActivity(intent);
    }

    private String textoIntent(HashMap<String, String> map, String key) {
        String value = map.get(key);
        return value == null ? "" : value;
    }

    private String valor(HashMap<String, String> map, String key) {
        String value = map.get(key);
        if (value == null || value.trim().isEmpty()) return "—";
        return value;
    }

    private int entero(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return -1;
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
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
