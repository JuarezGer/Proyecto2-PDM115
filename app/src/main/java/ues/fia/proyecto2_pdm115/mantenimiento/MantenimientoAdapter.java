package ues.fia.proyecto2_pdm115.mantenimiento;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.HashMap;
import ues.fia.proyecto2_pdm115.R;

public class MantenimientoAdapter extends RecyclerView.Adapter<MantenimientoAdapter.MantenimientoViewHolder> {

    private ArrayList<HashMap<String, String>> listaMantenimientos;
    private OnItemClickListener listener;
    private int selectedPosition = -1;

    public interface OnItemClickListener {
        void onItemClick(HashMap<String, String> mantenimiento, int position);
    }

    public MantenimientoAdapter(ArrayList<HashMap<String, String>> listaMantenimientos, OnItemClickListener listener) {
        this.listaMantenimientos = listaMantenimientos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MantenimientoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mantenimiento, parent, false);
        return new MantenimientoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MantenimientoViewHolder holder, int position) {
        HashMap<String, String> item = listaMantenimientos.get(position);
        holder.bind(item, position);
    }

    @Override
    public int getItemCount() {
        return listaMantenimientos.size();
    }

    public void updateData(ArrayList<HashMap<String, String>> newData) {
        this.listaMantenimientos = newData;
        this.selectedPosition = -1;
        notifyDataSetChanged();
    }

    class MantenimientoViewHolder extends RecyclerView.ViewHolder {
        TextView txtId, txtFecha, txtEquipo, txtTipo, txtEstado;
        CardView card;

        public MantenimientoViewHolder(@NonNull View itemView) {
            super(itemView);
            txtId = itemView.findViewById(R.id.txtIdMantenimiento);
            txtFecha = itemView.findViewById(R.id.txtFechaInicio);
            txtEquipo = itemView.findViewById(R.id.txtEquipo);
            txtTipo = itemView.findViewById(R.id.txtTipoMantenimiento);
            txtEstado = itemView.findViewById(R.id.txtEstadoMantenimiento);
            card = itemView.findViewById(R.id.cardMantenimiento);
        }

        public void bind(final HashMap<String, String> item, final int position) {
            txtId.setText("ID: " + item.get("id_mantenimiento"));
            txtFecha.setText(item.get("fecha_inicio"));
            txtEquipo.setText(item.get("equipo"));
            txtTipo.setText(item.get("tipo_mantenimiento").toUpperCase());
            txtEstado.setText(item.get("estado_mantenimiento").toUpperCase());

            // Destacar si está seleccionado
            if (selectedPosition == position) {
                card.setCardBackgroundColor(card.getContext().getResources().getColor(R.color.blue_gray_100));
            } else {
                card.setCardBackgroundColor(card.getContext().getResources().getColor(R.color.white));
            }

            itemView.setOnClickListener(v -> {
                int previousSelected = selectedPosition;
                selectedPosition = getAdapterPosition();
                notifyItemChanged(previousSelected);
                notifyItemChanged(selectedPosition);
                listener.onItemClick(item, selectedPosition);
            });
        }
    }
}
