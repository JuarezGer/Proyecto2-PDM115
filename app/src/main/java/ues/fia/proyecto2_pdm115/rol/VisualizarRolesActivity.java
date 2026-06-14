package ues.fia.proyecto2_pdm115.rol;

import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import ues.fia.proyecto2_pdm115.R;
import ues.fia.proyecto2_pdm115.controlDBLabCare;

public class VisualizarRolesActivity extends AppCompatActivity {

    private TextView txtRoles;
    private Button btnActualizarListaRoles;
    private Button btnVolverConsultarRoles;

    private controlDBLabCare db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consultar_roles);

        enlazarVistas();
        abrirBaseDatos();
        configurarEventos();
        cargarRoles();
    }

    private void enlazarVistas() {
        txtRoles = findViewById(R.id.txtRoles);
        btnActualizarListaRoles = findViewById(R.id.btnActualizarListaRoles);
        btnVolverConsultarRoles = findViewById(R.id.btnVolverConsultarRoles);
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
        btnActualizarListaRoles.setOnClickListener(v -> cargarRoles());
        btnVolverConsultarRoles.setOnClickListener(v -> finish());
    }

    private void cargarRoles() {
        Cursor cursor = null;
        StringBuilder sb = new StringBuilder();
        int total = 0;

        try {
            if (db == null || db.getDb() == null || !db.getDb().isOpen()) {
                abrirBaseDatos();
            }

            cursor = db.consultarRolesCursor();

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    total++;
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("id_rol"));
                    String nombre = obtenerTextoCursor(cursor, "nombre");
                    String descripcion = obtenerTextoCursor(cursor, "descripcion");

                    sb.append("Registro #").append(total).append("\n");
                    sb.append("ID: ").append(id).append("\n");
                    sb.append("Nombre: ").append(nombre).append("\n");
                    sb.append("Descripción: ")
                            .append(descripcion.isEmpty() ? "Sin descripción" : descripcion)
                            .append("\n");
                    sb.append("\n-----------------------------\n\n");
                } while (cursor.moveToNext());
            }

            if (total == 0) {
                txtRoles.setText("No hay roles registrados.");
            } else {
                txtRoles.setText("Total de roles: " + total + "\n\n" + sb.toString());
            }

        } catch (Exception e) {
            mostrarMensaje("Error al cargar roles: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private String obtenerTextoCursor(Cursor cursor, String columna) {
        int index = cursor.getColumnIndex(columna);
        if (index < 0 || cursor.isNull(index)) {
            return "";
        }
        return cursor.getString(index);
    }

    private void mostrarMensaje(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
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
