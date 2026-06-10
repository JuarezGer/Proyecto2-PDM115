package ues.fia.proyecto2_pdm115.mantenimiento;

import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import ues.fia.proyecto2_pdm115.R;

public class MantenimientoEditAdapter extends RecyclerView.Adapter<MantenimientoEditAdapter.ViewHolder> {

    private Cursor cursor;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int idMantenimiento);
    }

    public MantenimientoEditAdapter(Cursor cursor, OnItemClickListener listener) {
        this.cursor = cursor;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mantenimiento_edit, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (cursor != null && cursor.moveToPosition(position)) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("id_mantenimiento"));
            String tipo = cursor.getString(cursor.getColumnIndexOrThrow("tipo_mantenimiento"));
            String estado = cursor.getString(cursor.getColumnIndexOrThrow("estado_mantenimiento"));
            String fecha = cursor.getString(cursor.getColumnIndexOrThrow("fecha_inicio"));

            holder.tvId.setText("ID: #" + id);
            holder.tvTipo.setText("Tipo: " + tipo);
            holder.tvEstado.setText(estado.toUpperCase());
            holder.tvFecha.setText("Inicio: " + (fecha != null ? fecha : "N/A"));

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(id);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return (cursor != null) ? cursor.getCount() : 0;
    }

    public void setCursor(Cursor newCursor) {
        if (cursor != null) {
            cursor.close();
        }
        cursor = newCursor;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvId, tvTipo, tvEstado, tvFecha;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tvIdMantenimiento);
            tvTipo = itemView.findViewById(R.id.tvTipoMantenimiento);
            tvEstado = itemView.findViewById(R.id.tvEstadoMantenimiento);
            tvFecha = itemView.findViewById(R.id.tvFechaInicio);
        }
    }
}