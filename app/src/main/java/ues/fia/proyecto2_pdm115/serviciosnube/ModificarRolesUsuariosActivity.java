package ues.fia.proyecto2_pdm115.serviciosnube;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import ues.fia.proyecto2_pdm115.R;

public class ModificarRolesUsuariosActivity extends AppCompatActivity {

    private ServiciosNubeHelper helper;
    private TextView txtEstado;
    private LinearLayout contenedorRolesLocal, contenedorRolesRemoto, contenedorUsuariosLocal, contenedorUsuariosRemoto;
    private Button btnSubirLocal, btnBajarRemoto;
    private boolean puedeEditar = false;

    private JSONObject datosLocales;
    private JSONObject datosRemotos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modificar_roles_usuarios);

        helper = new ServiciosNubeHelper(this);
        txtEstado = findViewById(R.id.txtEstadoModificarRolesUsuarios);
        contenedorRolesLocal = findViewById(R.id.contenedorRolesLocal);
        contenedorRolesRemoto = findViewById(R.id.contenedorRolesRemoto);
        contenedorUsuariosLocal = findViewById(R.id.contenedorUsuariosLocal);
        contenedorUsuariosRemoto = findViewById(R.id.contenedorUsuariosRemoto);
        btnSubirLocal = findViewById(R.id.btnFusionarLocalRemotoRolesUsuarios);
        btnBajarRemoto = findViewById(R.id.btnFusionarRemotoLocalRolesUsuarios);

        findViewById(R.id.btnCompararRolesUsuarios).setOnClickListener(v -> cargarYComparar());
        btnSubirLocal.setOnClickListener(v -> confirmarSubida());
        btnBajarRemoto.setOnClickListener(v -> confirmarBajada());
        findViewById(R.id.btnVolverModificarRolesUsuarios).setOnClickListener(v -> finish());

        cargarYComparar();
    }

    private void cargarYComparar() {
        txtEstado.setText("Cargando roles y usuarios locales/remotos...");
        limpiar();
        puedeEditar = false;

        try {
            datosLocales = helper.obtenerRolesUsuariosLocales();
        } catch (Exception e) {
            txtEstado.setText("Error leyendo base local: " + e.getMessage());
            return;
        }

        helper.obtenerRolesUsuariosRemotos(new ServiciosNubeHelper.JsonCallback() {
            @Override
            public void onSuccess(JSONObject remoto) {
                try {
                    datosRemotos = remoto;
                    JSONObject comparacion = helper.compararRolesUsuariosLocalRemoto(remoto);
                    boolean hayDiferencias = comparacion.optInt("total_diferencias", 0) > 0;
                    puedeEditar = !hayDiferencias;

                    mostrarListados();

                    if (hayDiferencias) {
                        txtEstado.setText("Hay diferencias entre roles/usuarios locales y remotos. Primero debes fusionar para poder modificar.");
                        btnSubirLocal.setEnabled(true);
                        btnBajarRemoto.setEnabled(true);
                    } else {
                        txtEstado.setText("Roles y usuarios están iguales en ambas bases. Puedes tocar un registro local para modificarlo.");
                        btnSubirLocal.setEnabled(false);
                        btnBajarRemoto.setEnabled(false);
                    }
                } catch (Exception e) {
                    txtEstado.setText("Error comparando roles y usuarios: " + e.getMessage());
                }
            }

            @Override
            public void onError(String error) {
                txtEstado.setText(error);
                Toast.makeText(ModificarRolesUsuariosActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void mostrarListados() throws Exception {
        limpiar();
        cargarRoles(datosLocales.optJSONArray("roles"), contenedorRolesLocal, true);
        cargarRoles(datosRemotos.optJSONArray("roles"), contenedorRolesRemoto, false);
        cargarUsuarios(datosLocales.optJSONArray("usuarios"), contenedorUsuariosLocal, true);
        cargarUsuarios(datosRemotos.optJSONArray("usuarios"), contenedorUsuariosRemoto, false);
    }

    private void cargarRoles(JSONArray roles, LinearLayout contenedor, boolean esLocal) throws Exception {
        if (roles == null || roles.length() == 0) {
            agregarTexto(contenedor, "No hay roles registrados.");
            return;
        }

        for (int i = 0; i < roles.length(); i++) {
            JSONObject rol = roles.getJSONObject(i);
            String titulo = "Rol #" + rol.optString("id_rol") + " - " + rol.optString("nombre");
            String subtitulo = rol.optString("descripcion", "Sin descripción");
            ServiciosNubeUiHelper.agregarCard(this, contenedor, titulo, subtitulo, "🛡", v -> {
                if (esLocal && puedeEditar) mostrarDialogoEditarRol(rol);
                else ServiciosNubeUiHelper.mostrarDetalleJson(this, "Detalle de rol", rol);
            });
        }
    }

    private void cargarUsuarios(JSONArray usuarios, LinearLayout contenedor, boolean esLocal) throws Exception {
        if (usuarios == null || usuarios.length() == 0) {
            agregarTexto(contenedor, "No hay usuarios registrados.");
            return;
        }

        for (int i = 0; i < usuarios.length(); i++) {
            JSONObject usuario = usuarios.getJSONObject(i);
            String titulo = "Usuario #" + usuario.optString("id_usuario") + " - " + usuario.optString("nombres") + " " + usuario.optString("apellidos");
            String subtitulo = "Correo: " + usuario.optString("correo") + " | Rol ID: " + usuario.optString("id_rol") + " | Activo: " + usuario.optString("activo");
            ServiciosNubeUiHelper.agregarCard(this, contenedor, titulo, subtitulo, "👤", v -> {
                if (esLocal && puedeEditar) mostrarDialogoEditarUsuario(usuario);
                else ServiciosNubeUiHelper.mostrarDetalleJson(this, "Detalle de usuario", usuario);
            });
        }
    }

    private void mostrarDialogoEditarRol(JSONObject rol) {
        try {
            LinearLayout layout = crearLayoutDialogo();
            EditText edtNombre = crearEditText("Nombre", rol.optString("nombre"));
            EditText edtDescripcion = crearEditText("Descripción", rol.optString("descripcion"));
            layout.addView(edtNombre);
            layout.addView(edtDescripcion);

            new AlertDialog.Builder(this)
                    .setTitle("Modificar rol #" + rol.optString("id_rol"))
                    .setView(layout)
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Guardar", (dialog, which) -> {
                        try {
                            String nombre = edtNombre.getText().toString().trim();
                            if (nombre.isEmpty()) {
                                Toast.makeText(this, "El nombre es obligatorio.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            JSONObject nuevo = new JSONObject(rol.toString());
                            nuevo.put("nombre", nombre);
                            nuevo.put("descripcion", edtDescripcion.getText().toString().trim());
                            guardarRol(nuevo);
                        } catch (Exception e) {
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })
                    .show();
        } catch (Exception e) {
            Toast.makeText(this, "Error abriendo edición: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void mostrarDialogoEditarUsuario(JSONObject usuario) {
        try {
            LinearLayout layout = crearLayoutDialogo();
            EditText edtIdRol = crearEditText("ID rol", usuario.optString("id_rol"));
            edtIdRol.setInputType(InputType.TYPE_CLASS_NUMBER);
            EditText edtNombres = crearEditText("Nombres", usuario.optString("nombres"));
            EditText edtApellidos = crearEditText("Apellidos", usuario.optString("apellidos"));
            EditText edtCorreo = crearEditText("Correo", usuario.optString("correo"));
            EditText edtBiometria = crearEditText("Usa biometría (0 o 1)", usuario.optString("usa_biometria"));
            edtBiometria.setInputType(InputType.TYPE_CLASS_NUMBER);
            EditText edtActivo = crearEditText("Activo (0 o 1)", usuario.optString("activo"));
            edtActivo.setInputType(InputType.TYPE_CLASS_NUMBER);

            layout.addView(edtIdRol);
            layout.addView(edtNombres);
            layout.addView(edtApellidos);
            layout.addView(edtCorreo);
            layout.addView(edtBiometria);
            layout.addView(edtActivo);

            new AlertDialog.Builder(this)
                    .setTitle("Modificar usuario #" + usuario.optString("id_usuario"))
                    .setMessage("La clave hash se conserva sin cambios.")
                    .setView(layout)
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Guardar", (dialog, which) -> {
                        try {
                            JSONObject nuevo = new JSONObject(usuario.toString());
                            nuevo.put("id_rol", edtIdRol.getText().toString().trim());
                            nuevo.put("nombres", edtNombres.getText().toString().trim());
                            nuevo.put("apellidos", edtApellidos.getText().toString().trim());
                            nuevo.put("correo", edtCorreo.getText().toString().trim());
                            nuevo.put("usa_biometria", edtBiometria.getText().toString().trim());
                            nuevo.put("activo", edtActivo.getText().toString().trim());

                            if (nuevo.optString("nombres").isEmpty() || nuevo.optString("apellidos").isEmpty() || nuevo.optString("correo").isEmpty()) {
                                Toast.makeText(this, "Nombres, apellidos y correo son obligatorios.", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            guardarUsuario(nuevo);
                        } catch (Exception e) {
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })
                    .show();
        } catch (Exception e) {
            Toast.makeText(this, "Error abriendo edición: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void guardarRol(JSONObject rol) {
        txtEstado.setText("Guardando rol en base local y remota...");
        helper.actualizarRolAmbasBases(rol, new ServiciosNubeHelper.SyncCallback() {
            @Override
            public void onSuccess(String mensaje) {
                Toast.makeText(ModificarRolesUsuariosActivity.this, mensaje, Toast.LENGTH_LONG).show();
                cargarYComparar();
            }

            @Override
            public void onError(String error) {
                txtEstado.setText(error);
                Toast.makeText(ModificarRolesUsuariosActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void guardarUsuario(JSONObject usuario) {
        txtEstado.setText("Guardando usuario en base local y remota...");
        helper.actualizarUsuarioAmbasBases(usuario, new ServiciosNubeHelper.SyncCallback() {
            @Override
            public void onSuccess(String mensaje) {
                Toast.makeText(ModificarRolesUsuariosActivity.this, mensaje, Toast.LENGTH_LONG).show();
                cargarYComparar();
            }

            @Override
            public void onError(String error) {
                txtEstado.setText(error);
                Toast.makeText(ModificarRolesUsuariosActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void confirmarSubida() {
        new AlertDialog.Builder(this)
                .setTitle("Fusionar roles y usuarios")
                .setMessage("Se guardarán los datos locales en la base remota. Después podrás modificar cuando ambas bases estén iguales.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Fusionar", (dialog, which) -> helper.guardarLocalEnServidor(new ServiciosNubeHelper.SyncCallback() {
                    @Override
                    public void onSuccess(String mensaje) {
                        Toast.makeText(ModificarRolesUsuariosActivity.this, mensaje, Toast.LENGTH_LONG).show();
                        cargarYComparar();
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(ModificarRolesUsuariosActivity.this, error, Toast.LENGTH_LONG).show();
                    }
                }))
                .show();
    }

    private void confirmarBajada() {
        new AlertDialog.Builder(this)
                .setTitle("Fusionar roles y usuarios")
                .setMessage("Se restaurarán los datos remotos en la base local. Después podrás modificar cuando ambas bases estén iguales.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Fusionar", (dialog, which) -> helper.restaurarRemotoEnLocal(new ServiciosNubeHelper.SyncCallback() {
                    @Override
                    public void onSuccess(String mensaje) {
                        Toast.makeText(ModificarRolesUsuariosActivity.this, mensaje, Toast.LENGTH_LONG).show();
                        cargarYComparar();
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(ModificarRolesUsuariosActivity.this, error, Toast.LENGTH_LONG).show();
                    }
                }))
                .show();
    }

    private LinearLayout crearLayoutDialogo() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = ServiciosNubeUiHelper.dp(this, 16);
        layout.setPadding(pad, pad, pad, pad);
        return layout;
    }

    private EditText crearEditText(String hint, String texto) {
        EditText edt = new EditText(this);
        edt.setHint(hint);
        edt.setText(texto == null ? "" : texto);
        edt.setSingleLine(false);
        edt.setPadding(0, ServiciosNubeUiHelper.dp(this, 8), 0, ServiciosNubeUiHelper.dp(this, 8));
        return edt;
    }

    private void limpiar() {
        contenedorRolesLocal.removeAllViews();
        contenedorRolesRemoto.removeAllViews();
        contenedorUsuariosLocal.removeAllViews();
        contenedorUsuariosRemoto.removeAllViews();
    }

    private void agregarTexto(LinearLayout contenedor, String texto) {
        TextView tv = new TextView(this);
        tv.setText(texto);
        tv.setTextSize(14);
        tv.setPadding(0, ServiciosNubeUiHelper.dp(this, 8), 0, ServiciosNubeUiHelper.dp(this, 8));
        contenedor.addView(tv);
    }
}
