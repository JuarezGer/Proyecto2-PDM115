package ues.fia.proyecto2_pdm115.reporte;

import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

import ues.fia.proyecto2_pdm115.R;
import ues.fia.proyecto2_pdm115.controlDBLabCare;

public class GraficosMantenimientosActivity extends AppCompatActivity {

    private TextView txtTotalMantenimientosGraficos;
    private TextView txtMensajeGraficosMantenimientos;
    private PieChart chartMantenimientosEstado;
    private PieChart chartMantenimientosTipo;
    private BarChart chartMantenimientosTecnico;
    private BarChart chartMantenimientosEquipo;
    private Button btnActualizarGraficosMantenimientos;
    private Button btnVolverGraficosMantenimientos;

    private controlDBLabCare db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graficos_mantenimientos);

        enlazarVistas();
        abrirBaseDatos();
        configurarEventos();
        cargarGraficos();
    }

    private void enlazarVistas() {
        txtTotalMantenimientosGraficos = findViewById(R.id.txtTotalMantenimientosGraficos);
        txtMensajeGraficosMantenimientos = findViewById(R.id.txtMensajeGraficosMantenimientos);
        chartMantenimientosEstado = findViewById(R.id.chartMantenimientosEstado);
        chartMantenimientosTipo = findViewById(R.id.chartMantenimientosTipo);
        chartMantenimientosTecnico = findViewById(R.id.chartMantenimientosTecnico);
        chartMantenimientosEquipo = findViewById(R.id.chartMantenimientosEquipo);
        btnActualizarGraficosMantenimientos = findViewById(R.id.btnActualizarGraficosMantenimientos);
        btnVolverGraficosMantenimientos = findViewById(R.id.btnVolverGraficosMantenimientos);
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
        btnActualizarGraficosMantenimientos.setOnClickListener(v -> cargarGraficos());
        btnVolverGraficosMantenimientos.setOnClickListener(v -> finish());
    }

    private void cargarGraficos() {
        try {
            if (db == null || db.getDb() == null || !db.getDb().isOpen()) {
                abrirBaseDatos();
            }

            int totalMantenimientos = contarMantenimientos();
            txtTotalMantenimientosGraficos.setText("Total de mantenimientos: " + totalMantenimientos);

            ArrayList<DatoGrafico> datosPorEstado = obtenerDatos(
                    "SELECT estado_mantenimiento AS categoria, COUNT(*) AS total " +
                            "FROM mantenimientos " +
                            "GROUP BY estado_mantenimiento " +
                            "ORDER BY total DESC"
            );

            ArrayList<DatoGrafico> datosPorTipo = obtenerDatos(
                    "SELECT tipo_mantenimiento AS categoria, COUNT(*) AS total " +
                            "FROM mantenimientos " +
                            "GROUP BY tipo_mantenimiento " +
                            "ORDER BY total DESC"
            );

            ArrayList<DatoGrafico> datosPorTecnico = obtenerDatos(
                    "SELECT ut.nombres || ' ' || ut.apellidos AS categoria, COUNT(*) AS total " +
                            "FROM mantenimientos m " +
                            "INNER JOIN usuarios ut ON ut.id_usuario = m.id_usuario_tecnico " +
                            "GROUP BY ut.id_usuario, ut.nombres, ut.apellidos " +
                            "ORDER BY total DESC"
            );

            ArrayList<DatoGrafico> datosPorEquipo = obtenerDatos(
                    "SELECT eq.nombre || ' (' || eq.codigo_inventario || ')' AS categoria, COUNT(*) AS total " +
                            "FROM mantenimientos m " +
                            "INNER JOIN equipos eq ON eq.id_equipo = m.id_equipo " +
                            "GROUP BY eq.id_equipo, eq.nombre, eq.codigo_inventario " +
                            "ORDER BY total DESC"
            );

            configurarPie(chartMantenimientosEstado, datosPorEstado, "Mantenimientos por estado");
            configurarPie(chartMantenimientosTipo, datosPorTipo, "Mantenimientos por tipo");
            configurarBarra(chartMantenimientosTecnico, datosPorTecnico, "Mantenimientos por técnico");
            configurarBarra(chartMantenimientosEquipo, datosPorEquipo, "Mantenimientos por equipo");

            txtMensajeGraficosMantenimientos.setVisibility(View.GONE);

        } catch (Exception e) {
            mostrarMensaje("Error al cargar gráficos: " + e.getMessage());
        }
    }

    private int contarMantenimientos() {
        Cursor cursor = null;
        try {
            cursor = db.getDb().rawQuery("SELECT COUNT(*) FROM mantenimientos", null);
            if (cursor.moveToFirst()) return cursor.getInt(0);
            return 0;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private ArrayList<DatoGrafico> obtenerDatos(String consulta) {
        ArrayList<DatoGrafico> lista = new ArrayList<>();
        Cursor cursor = null;

        try {
            cursor = db.getDb().rawQuery(consulta, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String categoria = cursor.getString(cursor.getColumnIndexOrThrow("categoria"));
                    int total = cursor.getInt(cursor.getColumnIndexOrThrow("total"));
                    lista.add(new DatoGrafico(formatearEtiqueta(categoria), total));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }

        return lista;
    }

    private void configurarPie(PieChart chart, ArrayList<DatoGrafico> datos, String titulo) {
        if (datos.isEmpty()) {
            chart.clear();
            chart.setNoDataText("No hay datos para mostrar.");
            chart.invalidate();
            return;
        }

        ArrayList<PieEntry> entradas = new ArrayList<>();
        for (DatoGrafico dato : datos) {
            entradas.add(new PieEntry(dato.total, dato.categoria));
        }

        PieDataSet dataSet = new PieDataSet(entradas, titulo);
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);

        PieData data = new PieData(dataSet);
        chart.setData(data);
        chart.setCenterText(titulo);
        chart.setCenterTextSize(13f);
        chart.setUsePercentValues(false);
        chart.getDescription().setEnabled(false);
        chart.setEntryLabelTextSize(11f);
        chart.animateY(900);
        chart.invalidate();
    }

    private void configurarBarra(BarChart chart, ArrayList<DatoGrafico> datos, String titulo) {
        if (datos.isEmpty()) {
            chart.clear();
            chart.setNoDataText("No hay datos para mostrar.");
            chart.invalidate();
            return;
        }

        ArrayList<BarEntry> entradas = new ArrayList<>();
        ArrayList<String> etiquetas = new ArrayList<>();

        for (int i = 0; i < datos.size(); i++) {
            entradas.add(new BarEntry(i, datos.get(i).total));
            etiquetas.add(datos.get(i).categoria);
        }

        BarDataSet dataSet = new BarDataSet(entradas, titulo);
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.65f);

        chart.setData(data);
        chart.getDescription().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setGranularity(1f);
        chart.getAxisLeft().setAxisMinimum(0f);

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(etiquetas));
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelRotationAngle(-25f);

        chart.animateY(900);
        chart.invalidate();
    }

    private String formatearEtiqueta(String texto) {
        if (texto == null || texto.trim().isEmpty()) return "Sin dato";
        return texto.replace("_", " ");
    }

    private void mostrarMensaje(String mensaje) {
        txtMensajeGraficosMantenimientos.setText(mensaje);
        txtMensajeGraficosMantenimientos.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null) {
            db.cerrar();
            db = null;
        }
    }

    private static class DatoGrafico {
        String categoria;
        int total;

        DatoGrafico(String categoria, int total) {
            this.categoria = categoria;
            this.total = total;
        }
    }
}
