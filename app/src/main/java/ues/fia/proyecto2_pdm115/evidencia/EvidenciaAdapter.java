package ues.fia.proyecto2_pdm115.evidencia;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

import ues.fia.proyecto2_pdm115.R;

public class EvidenciaAdapter extends BaseAdapter {

    private final Context context;
    private final List<Map<String, String>> datos;

    public EvidenciaAdapter(Context context, List<Map<String, String>> datos) {
        this.context = context;
        this.datos = datos;
    }

    @Override public int getCount() { return datos.size(); }
    @Override public Object getItem(int pos) { return datos.get(pos); }
    @Override public long getItemId(int pos) { return pos; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_evidencia, parent, false);
        }

        Map<String, String> item = datos.get(position);

        TextView tvTipo = convertView.findViewById(R.id.tvTipoEvidencia);
        TextView tvDesc = convertView.findViewById(R.id.tvDescripcion);
        TextView tvFecha = convertView.findViewById(R.id.tvFecha);

        tvTipo.setText("Tipo: " + item.get("tipo_evidencia"));
        tvDesc.setText("Descripción: " + item.get("descripcion"));
        tvFecha.setText("Fecha: " + item.get("fecha_registro"));

        return convertView;
    }
}
