package ues.fia.proyecto2_pdm115.categoriaEquipo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import ues.fia.proyecto2_pdm115.R;
import ues.fia.proyecto2_pdm115.controlDBLabCare;

public class CategoriaEquipoAdapter extends ArrayAdapter<CategoriaEquipoAdapter.Categoria> {

    private Context context;
    private ArrayList<Categoria> lista;
    private controlDBLabCare db;

    public CategoriaEquipoAdapter(Context context, ArrayList<Categoria> lista) {
        super(context, 0, lista);
        this.context = context;
        this.lista = lista;

        db = new controlDBLabCare(context);
        db.abrir();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_categoria_equipo, parent, false);
        }

        Categoria categoria = lista.get(position);

        TextView txtNombre = convertView.findViewById(R.id.txtNombreCategoriaItem);
        ImageButton btnEditar = convertView.findViewById(R.id.btnEditarCategoriaItem);
        ImageButton btnEliminar = convertView.findViewById(R.id.btnEliminarCategoriaItem);

        txtNombre.setText(categoria.nombre);

        btnEditar.setOnClickListener(v -> {

            Intent intent = new Intent(context, ActualizarCategoriaEquipoActivity.class);

            intent.putExtra("id_categoria", categoria.id);
            intent.putExtra("nombre", categoria.nombre);

            context.startActivity(intent);

        });

        btnEliminar.setOnClickListener(v -> {

            AlertDialog.Builder builder = new AlertDialog.Builder(context);

            builder.setTitle("Eliminar categoría");
            builder.setMessage("¿Desea eliminar esta categoría?");

            builder.setPositiveButton("Sí", (dialog, which) -> {

                controlDBLabCare db = new controlDBLabCare(context);
                db.abrir();

                String mensaje = db.eliminarCategoriaEquipo(categoria.id);

                Toast.makeText(
                        context,
                        mensaje,
                        Toast.LENGTH_SHORT
                ).show();

                db.cerrar();

                lista.remove(position);
                notifyDataSetChanged();

            });

            builder.setNegativeButton("No", null);

            builder.show();
        });

        return convertView;
    }

    public static class Categoria {

        int id;
        String nombre;

        public Categoria(int id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }
    }
}