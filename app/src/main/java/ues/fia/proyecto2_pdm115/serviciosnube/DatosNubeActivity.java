package ues.fia.proyecto2_pdm115.serviciosnube;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import ues.fia.proyecto2_pdm115.R;

public class DatosNubeActivity extends AppCompatActivity {

    private ServiciosNubeHelper helper;
    private LinearLayout contenedorUsuarios, contenedorIncidencias, contenedorMantenimientos;
    private TextView txtEstado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_datos_nube);

        helper = new ServiciosNubeHelper(this);
        txtEstado = findViewById(R.id.txtEstadoDatosNube);
        contenedorUsuarios = findViewById(R.id.contenedorUsuariosNube);
        contenedorIncidencias = findViewById(R.id.contenedorIncidenciasNube);
        contenedorMantenimientos = findViewById(R.id.contenedorMantenimientosNube);

        findViewById(R.id.btnActualizarDatosNube).setOnClickListener(v -> cargarDatos());
        findViewById(R.id.btnVolverDatosNube).setOnClickListener(v -> finish());

        cargarDatos();
    }

    private void cargarDatos() {
        txtEstado.setText("Cargando datos desde XAMPP...");
        limpiarContenedores();

        helper.obtenerDatosNube(new ServiciosNubeHelper.JsonCallback() {
            @Override
            public void onSuccess(JSONObject datos) {
                try {
                    JSONArray usuarios = datos.optJSONArray("usuarios");
                    JSONArray incidencias = datos.optJSONArray("incidencias");
                    JSONArray mantenimientos = datos.optJSONArray("mantenimientos");

                    cargarUsuarios(usuarios);
                    cargarIncidencias(incidencias);
                    cargarMantenimientos(mantenimientos);

                    int total = total(usuarios) + total(incidencias) + total(mantenimientos);
                    txtEstado.setText("Datos cargados desde la base remota. Total de registros visibles: " + total);
                } catch (Exception e) {
                    txtEstado.setText("Error al mostrar datos: " + e.getMessage());
                }
            }

            @Override
            public void onError(String error) {
                txtEstado.setText(error);
                Toast.makeText(DatosNubeActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void cargarUsuarios(JSONArray array) throws Exception {
        if (array == null || array.length() == 0) {
            agregarVacio(contenedorUsuarios, "No hay usuarios en la base remota.");
            return;
        }

        for (int i = 0; i < array.length(); i++) {
            JSONObject u = array.getJSONObject(i);
            String titulo = "Usuario #" + u.optString("id_usuario") + " - " + u.optString("nombres") + " " + u.optString("apellidos");
            String subtitulo = "Correo: " + u.optString("correo") + " | Rol: " + u.optString("rol") + " | Activo: " + u.optString("activo");
            ServiciosNubeUiHelper.agregarCard(this, contenedorUsuarios, titulo, subtitulo, "👤", v ->
                    ServiciosNubeUiHelper.mostrarDetalleJson(this, "Detalle de usuario", u));
        }
    }

    private void cargarIncidencias(JSONArray array) throws Exception {
        if (array == null || array.length() == 0) {
            agregarVacio(contenedorIncidencias, "No hay incidencias en la base remota.");
            return;
        }

        for (int i = 0; i < array.length(); i++) {
            JSONObject inc = array.getJSONObject(i);
            String titulo = "Incidencia #" + inc.optString("id_incidencia") + " - " + inc.optString("titulo");
            String subtitulo = "Equipo: " + inc.optString("equipo") + " | Estado: " + inc.optString("estado_incidencia") + " | Prioridad: " + inc.optString("prioridad");
            ServiciosNubeUiHelper.agregarCard(this, contenedorIncidencias, titulo, subtitulo, "⚠", v ->
                    ServiciosNubeUiHelper.mostrarDetalleJson(this, "Detalle de incidencia", inc));
        }
    }

    private void cargarMantenimientos(JSONArray array) throws Exception {
        if (array == null || array.length() == 0) {
            agregarVacio(contenedorMantenimientos, "No hay mantenimientos en la base remota.");
            return;
        }

        for (int i = 0; i < array.length(); i++) {
            JSONObject m = array.getJSONObject(i);
            String titulo = "Mantenimiento #" + m.optString("id_mantenimiento") + " - " + m.optString("tipo_mantenimiento");
            String subtitulo = "Equipo: " + m.optString("equipo") + " | Técnico: " + m.optString("usuario_tecnico") + " | Estado: " + m.optString("estado_mantenimiento");
            ServiciosNubeUiHelper.agregarCard(this, contenedorMantenimientos, titulo, subtitulo, "🔧", v ->
                    ServiciosNubeUiHelper.mostrarDetalleJson(this, "Detalle de mantenimiento", m));
        }
    }

    private void agregarVacio(LinearLayout contenedor, String texto) {
        TextView tv = new TextView(this);
        tv.setText(texto);
        tv.setTextSize(14);
        tv.setPadding(0, ServiciosNubeUiHelper.dp(this, 8), 0, ServiciosNubeUiHelper.dp(this, 8));
        contenedor.addView(tv);
    }

    private int total(JSONArray array) {
        return array == null ? 0 : array.length();
    }

    private void limpiarContenedores() {
        contenedorUsuarios.removeAllViews();
        contenedorIncidencias.removeAllViews();
        contenedorMantenimientos.removeAllViews();
    }
}
