package ues.fia.proyecto2_pdm115;

import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import ues.fia.proyecto2_pdm115.categoriaEquipo.CategoriaEquipoMenuActivity;
import ues.fia.proyecto2_pdm115.edificio.EdificioMenuActivity;
import ues.fia.proyecto2_pdm115.equipo.EquipoMenuActivity;
import ues.fia.proyecto2_pdm115.evidencia.EvidenciaMenuActivity;
import ues.fia.proyecto2_pdm115.indicencia.IncidenciaMenuActivity;
import ues.fia.proyecto2_pdm115.laboratorio.LaboratorioMenuActivity;
import ues.fia.proyecto2_pdm115.mantenimiento.MantenimientoMenuActivity;
import ues.fia.proyecto2_pdm115.mapa.MapasMenuActivity;
import ues.fia.proyecto2_pdm115.reporte.ReportesMenuActivity;
import ues.fia.proyecto2_pdm115.rol.RolMenuActivity;
import ues.fia.proyecto2_pdm115.serviciosnube.ServiciosNubeMenuActivity;
import ues.fia.proyecto2_pdm115.tipoIncidencia.TipoIncidenciaMenuActivity;
import ues.fia.proyecto2_pdm115.usuario.UsuarioMenuActivity;
import ues.fia.proyecto2_pdm115.utils.SessionManager;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private controlDBLabCare db;
    private TextView txtTotalEquipos;
    private TextView txtIncidenciasAbiertas;
    private TextView txtMantenimientos;
    private TextView txtNombreUsuarioHeader;
    private SessionManager sessionManager;

    private LinearLayout contenedorMenuPrincipal;
    private LinearLayout contenedorAlertasDispositivo;

    private View cardRoles;
    private View cardUsuarios;
    private View cardEdificios;
    private View cardLaboratorios;
    private View cardCategoriasEquipo;
    private View cardEquipos;
    private View cardTiposIncidencia;
    private View cardIncidencias;
    private View cardMantenimientos;
    private View cardEvidencias;
    private View cardReportes;
    private View cardMapas;
    private View cardServiciosNube;

    private String rolActualNormalizado;

    private boolean alertaGpsCerrada = false;
    private boolean alertaInternetCerrada = false;
    private boolean ultimoGpsActivo = true;
    private boolean ultimoInternetActivo = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        if (!sessionManager.sesionActiva()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        enlazarVistas();

        rolActualNormalizado = normalizarRol(sessionManager.getRolUsuario());
        cargarNombreUsuarioHeader();

        db = new controlDBLabCare(this);
        abrirBaseDatos();
        configurarMenuPrincipal();
        configurarBarraInferior();
        aplicarPermisosPorRol();
        cargarResumenRapido();
        verificarEstadoDispositivoYMostrarAlertas();
    }

    private void enlazarVistas() {
        txtTotalEquipos = findViewById(R.id.txtTotalEquipos);
        txtIncidenciasAbiertas = findViewById(R.id.txtIncidenciasAbiertas);
        txtMantenimientos = findViewById(R.id.txtMantenimientos);
        txtNombreUsuarioHeader = findViewById(R.id.txtNombreUsuarioHeader);
        contenedorMenuPrincipal = findViewById(R.id.contenedorMenuPrincipal);

        cardRoles = findViewById(R.id.cardRoles);
        cardUsuarios = findViewById(R.id.cardUsuarios);
        cardEdificios = findViewById(R.id.cardEdificios);
        cardLaboratorios = findViewById(R.id.cardLaboratorios);
        cardCategoriasEquipo = findViewById(R.id.cardCategoriasEquipo);
        cardEquipos = findViewById(R.id.cardEquipos);
        cardTiposIncidencia = findViewById(R.id.cardTiposIncidencia);
        cardIncidencias = findViewById(R.id.cardIncidencias);
        cardMantenimientos = findViewById(R.id.cardMantenimientos);
        cardEvidencias = findViewById(R.id.cardEvidencias);
        cardReportes = findViewById(R.id.cardReportes);
        cardMapas = findViewById(R.id.cardMapas);
        cardServiciosNube = findViewById(R.id.cardServiciosNube);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (db == null) {
            db = new controlDBLabCare(this);
        }
        abrirBaseDatos();
        cargarResumenRapido();
        verificarEstadoDispositivoYMostrarAlertas();
    }

    private void abrirBaseDatos() {
        try {
            db.abrir();
        } catch (SQLException e) {
            Toast.makeText(this, "Error al abrir la base de datos: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void cargarNombreUsuarioHeader() {
        String primerNombre = sessionManager.getPrimerNombreUsuario();

        if (primerNombre == null || primerNombre.trim().isEmpty()) {
            txtNombreUsuarioHeader.setText("Hola");
        } else {
            txtNombreUsuarioHeader.setText("Hola, " + primerNombre);
        }
    }

    private void configurarMenuPrincipal() {
        cardRoles.setOnClickListener(v -> abrirMenuSiTienePermiso("roles", RolMenuActivity.class));
        cardUsuarios.setOnClickListener(v -> abrirMenuSiTienePermiso("usuarios", UsuarioMenuActivity.class));
        cardEdificios.setOnClickListener(v -> abrirMenuSiTienePermiso("edificios", EdificioMenuActivity.class));
        cardLaboratorios.setOnClickListener(v -> abrirMenuSiTienePermiso("laboratorios", LaboratorioMenuActivity.class));
        cardCategoriasEquipo.setOnClickListener(v -> abrirMenuSiTienePermiso("categorias", CategoriaEquipoMenuActivity.class));
        cardEquipos.setOnClickListener(v -> abrirMenuSiTienePermiso("equipos", EquipoMenuActivity.class));
        cardTiposIncidencia.setOnClickListener(v -> abrirMenuSiTienePermiso("tipos_incidencia", TipoIncidenciaMenuActivity.class));
        cardIncidencias.setOnClickListener(v -> abrirMenuSiTienePermiso("incidencias", IncidenciaMenuActivity.class));
        cardMantenimientos.setOnClickListener(v -> abrirMenuSiTienePermiso("mantenimientos", MantenimientoMenuActivity.class));
        cardEvidencias.setOnClickListener(v -> abrirMenuSiTienePermiso("evidencias", EvidenciaMenuActivity.class));
        cardReportes.setOnClickListener(v -> abrirMenuSiTienePermiso("reportes", ReportesMenuActivity.class));
        cardMapas.setOnClickListener(v -> abrirMenuSiTienePermiso("mapas", MapasMenuActivity.class));
        cardServiciosNube.setOnClickListener(v -> abrirMenuSiTienePermiso("serviciosnube", ServiciosNubeMenuActivity.class));

        findViewById(R.id.btnCerrarSesion).setOnClickListener(v -> {
            sessionManager.cerrarSesion();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void configurarBarraInferior() {
        findViewById(R.id.navHome).setOnClickListener(v -> {
            ScrollView scrollView = findViewById(R.id.mainScroll);
            scrollView.smoothScrollTo(0, 0);
        });

        findViewById(R.id.navEquipos).setOnClickListener(v ->
                abrirMenuSiTienePermiso("equipos", EquipoMenuActivity.class)
        );

        findViewById(R.id.navReportes).setOnClickListener(v ->
                abrirMenuSiTienePermiso("reportes", ReportesMenuActivity.class)
        );

        findViewById(R.id.navPerfil).setOnClickListener(v ->
                abrirMenuSiTienePermiso("usuarios", UsuarioMenuActivity.class)
        );
    }

    private void aplicarPermisosPorRol() {
        ocultarTodasLasTarjetas();

        if (esRol("administrador")) {
            mostrarTarjetas(
                    cardRoles,
                    cardUsuarios,
                    cardEdificios,
                    cardLaboratorios,
                    cardCategoriasEquipo,
                    cardEquipos,
                    cardTiposIncidencia,
                    cardIncidencias,
                    cardMantenimientos,
                    cardEvidencias,
                    cardReportes,
                    cardMapas,
                    cardServiciosNube
            );
        } else if (esRol("supervisor")) {
            mostrarTarjetas(
                    cardEdificios,
                    cardLaboratorios,
                    cardCategoriasEquipo,
                    cardEquipos,
                    cardTiposIncidencia,
                    cardIncidencias,
                    cardMantenimientos,
                    cardEvidencias,
                    cardReportes,
                    cardMapas,
                    cardServiciosNube
            );
        } else if (esRol("tecnico")) {
            mostrarTarjetas(
                    cardEquipos,
                    cardIncidencias,
                    cardMantenimientos,
                    cardEvidencias,
                    cardMapas
            );
        } else if (esRol("reportante")) {
            mostrarTarjetas(
                    cardIncidencias,
                    cardMapas
            );
        } else {
            mostrarTarjetas(cardIncidencias);
        }

        reorganizarTarjetasVisibles();
    }

    private void ocultarTodasLasTarjetas() {
        View[] tarjetas = obtenerTodasLasTarjetas();
        for (View tarjeta : tarjetas) {
            if (tarjeta != null) {
                tarjeta.setVisibility(View.GONE);
            }
        }
    }

    private void mostrarTarjetas(View... tarjetas) {
        for (View tarjeta : tarjetas) {
            if (tarjeta != null) {
                tarjeta.setVisibility(View.VISIBLE);
            }
        }
    }

    private void reorganizarTarjetasVisibles() {
        if (contenedorMenuPrincipal == null) {
            return;
        }

        ArrayList<View> tarjetasVisibles = new ArrayList<>();
        View[] todasLasTarjetas = obtenerTodasLasTarjetas();

        for (View tarjeta : todasLasTarjetas) {
            if (tarjeta != null && tarjeta.getVisibility() == View.VISIBLE) {
                tarjetasVisibles.add(tarjeta);
            }
        }

        for (View tarjeta : todasLasTarjetas) {
            if (tarjeta != null) {
                ViewParent parent = tarjeta.getParent();
                if (parent instanceof ViewGroup) {
                    ((ViewGroup) parent).removeView(tarjeta);
                }
            }
        }

        contenedorMenuPrincipal.removeAllViews();

        for (int i = 0; i < tarjetasVisibles.size(); i += 2) {
            LinearLayout fila = new LinearLayout(this);
            fila.setOrientation(LinearLayout.HORIZONTAL);
            fila.setBaselineAligned(false);
            fila.setWeightSum(2);

            LinearLayout.LayoutParams paramsFila = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            paramsFila.setMargins(0, i == 0 ? dp(12) : dp(14), 0, 0);
            fila.setLayoutParams(paramsFila);

            boolean hayDerecha = i + 1 < tarjetasVisibles.size();

            View tarjetaIzquierda = tarjetasVisibles.get(i);
            fila.addView(tarjetaIzquierda, crearParametrosTarjeta(true, hayDerecha));

            if (hayDerecha) {
                View tarjetaDerecha = tarjetasVisibles.get(i + 1);
                fila.addView(tarjetaDerecha, crearParametrosTarjeta(false, true));
            } else {
                View espacio = new View(this);
                fila.addView(espacio, crearParametrosTarjeta(false, false));
            }

            contenedorMenuPrincipal.addView(fila);
        }
    }

    private View[] obtenerTodasLasTarjetas() {
        return new View[]{
                cardRoles,
                cardUsuarios,
                cardEdificios,
                cardLaboratorios,
                cardCategoriasEquipo,
                cardEquipos,
                cardTiposIncidencia,
                cardIncidencias,
                cardMantenimientos,
                cardEvidencias,
                cardReportes,
                cardMapas,
                cardServiciosNube
        };
    }

    private LinearLayout.LayoutParams crearParametrosTarjeta(boolean izquierda, boolean tienePareja) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0,
                dp(124),
                1
        );

        if (izquierda && tienePareja) {
            params.setMargins(0, 0, dp(8), 0);
        } else if (!izquierda && tienePareja) {
            params.setMargins(dp(8), 0, 0, 0);
        } else {
            params.setMargins(0, 0, 0, 0);
        }

        return params;
    }

    private boolean tienePermiso(String modulo) {
        if (esRol("administrador")) {
            return true;
        }

        switch (modulo) {
            case "roles":
            case "usuarios":
                return false;

            case "edificios":
            case "laboratorios":
            case "categorias":
            case "tipos_incidencia":
            case "reportes":
            case "serviciosnube":
                return esRol("supervisor");

            case "equipos":
            case "mantenimientos":
            case "evidencias":
                return esRol("supervisor") || esRol("tecnico");

            case "incidencias":
            case "mapas":
                return esRol("supervisor") || esRol("tecnico") || esRol("reportante");

            default:
                return false;
        }
    }

    private boolean esRol(String rol) {
        return rolActualNormalizado.equals(normalizarRol(rol));
    }

    private String normalizarRol(String rol) {
        if (rol == null) {
            return "";
        }

        return rol.trim()
                .toLowerCase()
                .replace("á", "a")
                .replace("é", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ú", "u");
    }

    private void abrirMenuSiTienePermiso(String modulo, Class<?> activityDestino) {
        if (tienePermiso(modulo)) {
            abrirMenu(activityDestino);
        } else {
            Toast.makeText(
                    this,
                    "Tu rol no tiene acceso a este apartado.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void abrirMenu(Class<?> activityDestino) {
        startActivity(new Intent(this, activityDestino));
    }

    private void cargarResumenRapido() {
        if (db == null) {
            return;
        }

        try {
            txtTotalEquipos.setText(String.valueOf(db.contarRegistros("equipos")));
            txtMantenimientos.setText(String.valueOf(db.contarRegistros("mantenimientos")));
            txtIncidenciasAbiertas.setText(String.valueOf(contarIncidenciasAbiertas()));
        } catch (Exception e) {
            txtTotalEquipos.setText("0");
            txtIncidenciasAbiertas.setText("0");
            txtMantenimientos.setText("0");
        }
    }

    private int contarIncidenciasAbiertas() {
        android.database.Cursor cursor = null;
        try {
            cursor = db.getDb().rawQuery(
                    "SELECT COUNT(*) FROM incidencias " +
                            "WHERE estado_incidencia IN ('pendiente', 'abierta', 'en_proceso')",
                    null
            );

            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
            return 0;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void verificarEstadoDispositivoYMostrarAlertas() {
        boolean gpsActivo = estaGpsActivo();
        boolean internetActivo = hayAccesoInternet();

        if (gpsActivo != ultimoGpsActivo) {
            alertaGpsCerrada = false;
            ultimoGpsActivo = gpsActivo;
        }

        if (internetActivo != ultimoInternetActivo) {
            alertaInternetCerrada = false;
            ultimoInternetActivo = internetActivo;
        }

        limpiarAlertasDispositivo();

        if (!gpsActivo && !alertaGpsCerrada) {
            agregarAlertaDispositivo(
                    "GPS desactivado",
                    "Activa el GPS del dispositivo para usar mapas y ubicaciones correctamente.",
                    () -> alertaGpsCerrada = true
            );
        }

        if (!internetActivo && !alertaInternetCerrada) {
            agregarAlertaDispositivo(
                    "Sin acceso a internet",
                    "Revisa tu conexión para usar servicios en la nube, mapas o funciones en línea.",
                    () -> alertaInternetCerrada = true
            );
        }
    }

    private void limpiarAlertasDispositivo() {
        if (contenedorAlertasDispositivo != null) {
            contenedorAlertasDispositivo.removeAllViews();
        }
    }

    private void agregarAlertaDispositivo(String titulo, String mensaje, Runnable accionCerrar) {
        LinearLayout contenedor = obtenerContenedorAlertasDispositivo();
        if (contenedor == null) {
            return;
        }

        LinearLayout alerta = new LinearLayout(this);
        alerta.setOrientation(LinearLayout.HORIZONTAL);
        alerta.setGravity(Gravity.CENTER_VERTICAL);
        alerta.setPadding(dp(14), dp(10), dp(8), dp(10));

        GradientDrawable fondo = new GradientDrawable();
        fondo.setColor(Color.WHITE);
        fondo.setCornerRadius(dp(14));
        fondo.setStroke(dp(1), Color.rgb(207, 216, 220));
        alerta.setBackground(fondo);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            alerta.setElevation(dp(6));
        }

        LinearLayout textos = new LinearLayout(this);
        textos.setOrientation(LinearLayout.VERTICAL);

        TextView txtTitulo = new TextView(this);
        txtTitulo.setText(titulo);
        txtTitulo.setTextColor(Color.rgb(38, 50, 56));
        txtTitulo.setTextSize(15);
        txtTitulo.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        TextView txtMensaje = new TextView(this);
        txtMensaje.setText(mensaje);
        txtMensaje.setTextColor(Color.rgb(69, 90, 100));
        txtMensaje.setTextSize(13);
        txtMensaje.setPadding(0, dp(2), 0, 0);

        textos.addView(txtTitulo);
        textos.addView(txtMensaje);

        LinearLayout.LayoutParams paramsTextos = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );
        alerta.addView(textos, paramsTextos);

        TextView btnCerrar = new TextView(this);
        btnCerrar.setText("×");
        btnCerrar.setTextColor(Color.rgb(69, 90, 100));
        btnCerrar.setTextSize(26);
        btnCerrar.setGravity(Gravity.CENTER);
        btnCerrar.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        btnCerrar.setPadding(dp(10), 0, dp(10), dp(2));
        btnCerrar.setOnClickListener(v -> {
            contenedor.removeView(alerta);
            if (accionCerrar != null) {
                accionCerrar.run();
            }
        });

        alerta.addView(btnCerrar, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        LinearLayout.LayoutParams paramsAlerta = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        paramsAlerta.setMargins(0, 0, 0, dp(10));
        contenedor.addView(alerta, paramsAlerta);
    }

    private LinearLayout obtenerContenedorAlertasDispositivo() {
        if (contenedorAlertasDispositivo != null) {
            return contenedorAlertasDispositivo;
        }

        FrameLayout root = findViewById(android.R.id.content);
        if (root == null) {
            return null;
        }

        contenedorAlertasDispositivo = new LinearLayout(this);
        contenedorAlertasDispositivo.setOrientation(LinearLayout.VERTICAL);
        contenedorAlertasDispositivo.setPadding(dp(16), dp(12), dp(16), 0);
        contenedorAlertasDispositivo.setClickable(false);
        contenedorAlertasDispositivo.setFocusable(false);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.TOP;

        root.addView(contenedorAlertasDispositivo, params);
        return contenedorAlertasDispositivo;
    }

    private boolean estaGpsActivo() {
        try {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            return locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean hayAccesoInternet() {
        try {
            ConnectivityManager connectivityManager =
                    (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

            if (connectivityManager == null) {
                return false;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Network network = connectivityManager.getActiveNetwork();
                if (network == null) {
                    return false;
                }

                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
                return capabilities != null
                        && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
            } else {
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                return networkInfo != null && networkInfo.isConnected();
            }
        } catch (Exception e) {
            return false;
        }
    }

    private int dp(int valor) {
        return (int) (valor * getResources().getDisplayMetrics().density + 0.5f);
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
