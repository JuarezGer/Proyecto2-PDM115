package ues.fia.proyecto2_pdm115;

import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import ues.fia.proyecto2_pdm115.categoriaEquipo.CategoriaEquipoMenuActivity;
import ues.fia.proyecto2_pdm115.edificio.EdificioMenuActivity;
import ues.fia.proyecto2_pdm115.equipo.EquipoMenuActivity;
import ues.fia.proyecto2_pdm115.evidencia.EvidenciaMenuActivity;
import ues.fia.proyecto2_pdm115.indicencia.IncidenciaMenuActivity;
import ues.fia.proyecto2_pdm115.laboratorio.LaboratorioMenuActivity;
import ues.fia.proyecto2_pdm115.mantenimiento.MantenimientoMenuActivity;
import ues.fia.proyecto2_pdm115.rol.RolMenuActivity;
import ues.fia.proyecto2_pdm115.tipoIncidencia.TipoIncidenciaMenuActivity;
import ues.fia.proyecto2_pdm115.usuario.UsuarioMenuActivity;
import ues.fia.proyecto2_pdm115.utils.*;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private controlDBLabCare db;
    private TextView txtTotalEquipos;
    private TextView txtIncidenciasAbiertas;
    private TextView txtMantenimientos;
    private SessionManager sessionManager;

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

        txtTotalEquipos = findViewById(R.id.txtTotalEquipos);
        txtIncidenciasAbiertas = findViewById(R.id.txtIncidenciasAbiertas);
        txtMantenimientos = findViewById(R.id.txtMantenimientos);

        db = new controlDBLabCare(this);
        abrirBaseDatos();
        configurarMenuPrincipal();
        configurarBarraInferior();
        cargarResumenRapido();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (db == null) {
            db = new controlDBLabCare(this);
        }
        abrirBaseDatos();
        cargarResumenRapido();
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

    private void configurarMenuPrincipal() {
        findViewById(R.id.cardRoles).setOnClickListener(v -> abrirMenu(RolMenuActivity.class));
        findViewById(R.id.cardUsuarios).setOnClickListener(v -> abrirMenu(UsuarioMenuActivity.class));
        findViewById(R.id.cardEdificios).setOnClickListener(v -> abrirMenu(EdificioMenuActivity.class));
        findViewById(R.id.cardLaboratorios).setOnClickListener(v -> abrirMenu(LaboratorioMenuActivity.class));
        findViewById(R.id.cardCategoriasEquipo).setOnClickListener(v -> abrirMenu(CategoriaEquipoMenuActivity.class));
        findViewById(R.id.cardEquipos).setOnClickListener(v -> abrirMenu(EquipoMenuActivity.class));
        findViewById(R.id.cardTiposIncidencia).setOnClickListener(v -> abrirMenu(TipoIncidenciaMenuActivity.class));
        findViewById(R.id.cardIncidencias).setOnClickListener(v -> abrirMenu(IncidenciaMenuActivity.class));
        findViewById(R.id.cardMantenimientos).setOnClickListener(v -> abrirMenu(MantenimientoMenuActivity.class));
        findViewById(R.id.cardEvidencias).setOnClickListener(v -> abrirMenu(EvidenciaMenuActivity.class));

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

        findViewById(R.id.navEquipos).setOnClickListener(v -> abrirMenu(EquipoMenuActivity.class));
        findViewById(R.id.navReportes).setOnClickListener(v -> abrirMenu(IncidenciaMenuActivity.class));
        findViewById(R.id.navPerfil).setOnClickListener(v -> abrirMenu(UsuarioMenuActivity.class));
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null) {
            db.cerrar();
            db = null;
        }
    }
}
