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

public class GraficosIncidenciasActivity extends AppCompatActivity {

    private TextView txtTotalIncidenciasGraficos;
    private TextView txtMensajeGraficosIncidencias;
    private PieChart chartIncidenciasTipo;
    private PieChart chartIncidenciasEstado;
    private BarChart chartIncidenciasPrioridad;
    private BarChart chartIncidenciasOrigen;
    private Button btnActualizarGraficosIncidencias;
    private Button btnVolverGraficosIncidencias;

    private controlDBLabCare db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graficos_incidencias);

        enlazarVistas();
        abrirBaseDatos();
        configurarEventos();
        cargarGraficos();
    }

    private void enlazarVistas() {
        txtTotalIncidenciasGraficos = findViewById(R.id.txtTotalIncidenciasGraficos);
        txtMensajeGraficosIncidencias = findViewById(R.id.txtMensajeGraficosIncidencias);
        chartIncidenciasTipo = findViewById(R.id.chartIncidenciasTipo);
        chartIncidenciasEstado = findViewById(R.id.chartIncidenciasEstado);
        chartIncidenciasPrioridad = findViewById(R.id.chartIncidenciasPrioridad);
        chartIncidenciasOrigen = findViewById(R.id.chartIncidenciasOrigen);
        btnActualizarGraficosIncidencias = findViewById(R.id.btnActualizarGraficosIncidencias);
        btnVolverGraficosIncidencias = findViewById(R.id.btnVolverGraficosIncidencias);
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
        btnActualizarGraficosIncidencias.setOnClickListener(v -> cargarGraficos());
        btnVolverGraficosIncidencias.setOnClickListener(v -> finish());
    }

    private void cargarGraficos() {
        try {
            if (db == null || db.getDb() == null || !db.getDb().isOpen()) {
                abrirBaseDatos();
            }

            int totalIncidencias = contarIncidencias();
            txtTotalIncidenciasGraficos.setText("Total de incidencias: " + totalIncidencias);

            ArrayList<DatoGrafico> datosPorTipo = obtenerDatos(
                    "SELECT ti.nombre AS categoria, COUNT(*) AS total " +
                            "FROM incidencias i " +
                            "INNER JOIN tipos_incidencia ti ON ti.id_tipo_incidencia = i.id_tipo_incidencia " +
                            "GROUP BY ti.nombre " +
                            "ORDER BY total DESC"
            );

            ArrayList<DatoGrafico> datosPorEstado = obtenerDatos(
                    "SELECT estado_incidencia AS categoria, COUNT(*) AS total " +
                            "FROM incidencias " +
                            "GROUP BY estado_incidencia " +
                            "ORDER BY total DESC"
            );

            ArrayList<DatoGrafico> datosPorPrioridad = obtenerDatos(
                    "SELECT prioridad AS categoria, COUNT(*) AS total " +
                            "FROM incidencias " +
                            "GROUP BY prioridad " +
                            "ORDER BY CASE prioridad " +
                            "WHEN 'critica' THEN 1 " +
                            "WHEN 'alta' THEN 2 " +
                            "WHEN 'media' THEN 3 " +
                            "WHEN 'baja' THEN 4 " +
                            "ELSE 5 END"
            );

            ArrayList<DatoGrafico> datosPorOrigen = obtenerDatos(
                    "SELECT origen_registro AS categoria, COUNT(*) AS total " +
                            "FROM incidencias " +
                            "GROUP BY origen_registro " +
                            "ORDER BY total DESC"
            );

            configurarPie(chartIncidenciasTipo, datosPorTipo, "Incidencias por tipo");
            configurarPie(chartIncidenciasEstado, datosPorEstado, "Incidencias por estado");
            configurarBarra(chartIncidenciasPrioridad, datosPorPrioridad, "Incidencias por prioridad");
            configurarBarra(chartIncidenciasOrigen, datosPorOrigen, "Incidencias por origen");

            txtMensajeGraficosIncidencias.setVisibility(View.GONE);

        } catch (Exception e) {
            mostrarMensaje("Error al cargar gráficos: " + e.getMessage());
        }
    }

    private int contarIncidencias() {
        Cursor cursor = null;
        try {
            cursor = db.getDb().rawQuery("SELECT COUNT(*) FROM incidencias", null);
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
        txtMensajeGraficosIncidencias.setText(mensaje);
        txtMensajeGraficosIncidencias.setVisibility(View.VISIBLE);
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
