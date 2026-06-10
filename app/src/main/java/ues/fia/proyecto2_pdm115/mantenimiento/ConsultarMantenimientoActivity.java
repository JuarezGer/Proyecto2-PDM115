package ues.fia.proyecto2_pdm115.mantenimiento;

import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.os.Handler;
import android.os.Looper;

import ues.fia.proyecto2_pdm115.R;
import ues.fia.proyecto2_pdm115.controlDBLabCare;

public class ConsultarMantenimientoActivity extends AppCompatActivity {

    private controlDBLabCare helper;
    private RecyclerView recyclerView;
    private MantenimientoAdapter adapter;
    private TextView txtNoData;
    private ArrayList<HashMap<String, String>> listaMantenimientos;

    // Gestión de hilos de fondo para la consulta detallada
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consultar_mantenimiento);

        // Reutilizamos el mismo layout de la lista si lo deseas, o uno clonado
        helper = new controlDBLabCare(this);
        recyclerView = findViewById(R.id.recyclerMantenimientos);
        txtNoData = findViewById(R.id.txtNoData);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        cargarDatos();
    }

    private void cargarDatos() {
        helper.abrir();
        Cursor cursor = helper.consultarMantenimientosCursor();
        listaMantenimientos = helper.cursorAListaMap(cursor);
        helper.cerrar();

        if (listaMantenimientos.isEmpty()) {
            txtNoData.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            txtNoData.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            if (adapter == null) {
                // Al hacer clic, cargamos los detalles completos usando el ID
                adapter = new MantenimientoAdapter(listaMantenimientos, (mantenimiento, position) -> {
                    String idStr = mantenimiento.get("id_mantenimiento");
                    if (idStr != null) {
                        obtenerYMostrarDetalles(Integer.parseInt(idStr));
                    }
                });
                recyclerView.setAdapter(adapter);
            } else {
                adapter.updateData(listaMantenimientos);
            }
        }
    }

    private void obtenerYMostrarDetalles(int idMantenimiento) {
        Toast.makeText(this, "Cargando detalles...", Toast.LENGTH_SHORT).show();

        // Consultamos la BD usando tu consulta optimizada en un hilo secundario
        executorService.execute(() -> {
            helper.abrir();
            Cursor cursorDetalle = helper.obtenerDatosCompletosMantenimiento(idMantenimiento);

            if (cursorDetalle != null && cursorDetalle.moveToFirst()) {
                // Mapeamos los datos de manera segura
                HashMap<String, String> detalles = extraerDatosCursor(cursorDetalle);
                cursorDetalle.close();
                helper.cerrar();

                // Regresamos al hilo principal para renderizar la interfaz
                mainHandler.post(() -> mostrarDialogoDetalle(detalles));
            } else {
                if (cursorDetalle != null) cursorDetalle.close();
                helper.cerrar();
                mainHandler.post(() -> Toast.makeText(ConsultarMantenimientoActivity.this, "No se encontraron detalles", Toast.LENGTH_SHORT).show());
            }
        });
    }

    // Helper para extraer campos evitando nulos
    private HashMap<String, String> extraerDatosCursor(Cursor c) {
        HashMap<String, String> datos = new HashMap<>();
        String[] columnas = {
                "id_mantenimiento", "tipo_mantenimiento", "estado_mantenimiento",
                "diagnostico", "solucion_aplicada", "fecha_inicio", "fecha_fin",
                "nombre_equipo", "codigo_inventario", "marca", "modelo", "estado_equipo",
                "nombre_laboratorio", "piso", "nombre_edificio",
                "titulo_incidencia", "descripcion_incidencia", "prioridad", "fecha_reporte",
                "nombre_creador", "nombre_tecnico"
        };

        for (String col : columnas) {
            int index = c.getColumnIndex(col);
            if (index != -1 && !c.isNull(index)) {
                datos.put(col, c.getString(index));
            } else {
                datos.put(col, "—");
            }
        }
        return datos;
    }

    private void mostrarDialogoDetalle(HashMap<String, String> d) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();

        // Inflamos un layout personalizado con scroll para ver todo con comodidad
        View dialogView = inflater.inflate(R.layout.dialog_detalle_mantenimiento, null);
        builder.setView(dialogView);

        // Vinculamos los TextViews del diseño personalizado
        TextView tvId = dialogView.findViewById(R.id.detIdMantenimiento);
        TextView tvTipo = dialogView.findViewById(R.id.detTipo);
        TextView tvEstado = dialogView.findViewById(R.id.detEstado);
        TextView tvFechas = dialogView.findViewById(R.id.detFechas);
        TextView tvDiag = dialogView.findViewById(R.id.detDiagnostico);
        TextView tvSol = dialogView.findViewById(R.id.detSolucion);
        TextView tvEquipo = dialogView.findViewById(R.id.detEquipo);
        TextView tvUbicacion = dialogView.findViewById(R.id.detUbicacion);
        TextView tvIncidencia = dialogView.findViewById(R.id.detIncidencia);
        TextView tvPersonal = dialogView.findViewById(R.id.detPersonal);

        // Asignamos la información
        tvId.setText("Mantenimiento #" + d.get("id_mantenimiento"));
        tvTipo.setText(d.get("tipo_mantenimiento").toUpperCase());
        tvEstado.setText(d.get("estado_mantenimiento").toUpperCase());
        tvFechas.setText("Inicio: " + d.get("fecha_inicio") + " | Fin: " + d.get("fecha_fin"));
        tvDiag.setText(d.get("diagnostico"));
        tvSol.setText(d.get("solucion_aplicada"));

        tvEquipo.setText(d.get("nombre_equipo") + " (" + d.get("codigo_inventario") + ")\n" +
                "Marca: " + d.get("marca") + " | Modelo: " + d.get("modelo") + "\nEstado: " + d.get("estado_equipo"));

        tvUbicacion.setText(d.get("nombre_laboratorio") + " (Piso " + d.get("piso") + ")\nEdificio: " + d.get("nombre_edificio"));

        if (d.get("titulo_incidencia").equals("—")) {
            tvIncidencia.setText("Sin incidencia asociada.");
        } else {
            tvIncidencia.setText("Título: " + d.get("titulo_incidencia") + "\nPrioridad: " + d.get("prioridad") +
                    "\nReportada: " + d.get("fecha_reporte") + "\nDesc: " + d.get("descripcion_incidencia"));
        }

        tvPersonal.setText("Creado por: " + d.get("nombre_creador") + "\nTécnico Asignado: " + d.get("nombre_tecnico"));

        builder.setPositiveButton("Cerrar", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}