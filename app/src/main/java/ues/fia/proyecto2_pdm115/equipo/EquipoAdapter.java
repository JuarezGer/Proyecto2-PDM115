package ues.fia.proyecto2_pdm115.equipo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import ues.fia.proyecto2_pdm115.R;
import ues.fia.proyecto2_pdm115.controlDBLabCare;

public class EquipoAdapter extends ArrayAdapter<EquipoAdapter.Equipo> {

    private Context context;
    private ArrayList<Equipo> lista;

    public EquipoAdapter(Context context, ArrayList<Equipo> lista) {
        super(context, 0, lista);

        this.context = context;
        this.lista = lista;
    }

    @NonNull
    @Override
    public View getView(int position,
                        @Nullable View convertView,
                        @NonNull ViewGroup parent) {

        if (convertView == null) {

            convertView = LayoutInflater
                    .from(context)
                    .inflate(
                            R.layout.item_equipo,
                            parent,
                            false
                    );

        }

        Equipo equipo = lista.get(position);

        TextView txtNombre =
                convertView.findViewById(R.id.txtNombreEquipoItem);

        TextView txtCodigoInventario =
                convertView.findViewById(R.id.txtCodigoInventarioItem);

        TextView txtMarca =
                convertView.findViewById(R.id.txtMarcaItem);

        TextView txtModelo =
                convertView.findViewById(R.id.txtModeloItem);

        TextView txtEstado =
                convertView.findViewById(R.id.txtEstadoItem);

        TextView txtCategoria =
                convertView.findViewById(R.id.txtCategoriaItem);

        TextView txtLaboratorio =
                convertView.findViewById(R.id.txtLaboratorioItem);

        TextView txtEdificio =
                convertView.findViewById(R.id.txtEdificioItem);

        ImageButton btnEditar =
                convertView.findViewById(R.id.btnEditarEquipoItem);

        ImageButton btnEliminar =
                convertView.findViewById(R.id.btnEliminarEquipoItem);


        txtNombre.setText(equipo.nombre);

        txtCodigoInventario.setText(
                "Código: " + equipo.codigoInventario
        );

        txtMarca.setText(
                "Marca: " + equipo.marca
        );

        txtModelo.setText(
                "Modelo: " + equipo.modelo
        );

        txtEstado.setText(
                "Estado: " + equipo.estado
        );

        txtCategoria.setText(
                "Categoría: " + equipo.categoria
        );

        txtLaboratorio.setText(
                "Laboratorio: " + equipo.laboratorio
        );

        txtEdificio.setText(
                "Edificio: " + equipo.edificio
        );

        btnEditar.setOnClickListener(v -> {

            Intent intent = new Intent(
                    context,
                    ActualizarEquipoActivity.class
            );

            intent.putExtra("id_equipo", equipo.id);

            context.startActivity(intent);

        });

        btnEliminar.setOnClickListener(v -> {

            AlertDialog.Builder builder =
                    new AlertDialog.Builder(context);

            builder.setTitle("Eliminar equipo");

            builder.setMessage(
                    "¿Desea eliminar este equipo?"
            );

            builder.setPositiveButton(
                    "Sí",
                    (dialog, which) -> {

                        controlDBLabCare db =
                                new controlDBLabCare(context);

                        db.abrir();

                        String mensaje =
                                db.eliminarEquipo(equipo.id);

                        Toast.makeText(
                                context,
                                mensaje,
                                Toast.LENGTH_SHORT
                        ).show();

                        db.cerrar();

                        lista.remove(position);

                        notifyDataSetChanged();

                    }
            );

            builder.setNegativeButton(
                    "No",
                    null
            );

            builder.show();

        });

        return convertView;
    }

    public static class Equipo {

        int id;
        String codigoInventario;
        String codigoQr;
        String nombre;
        String marca;
        String modelo;
        String estado;
        String laboratorio;
        String categoria;
        String edificio;

        public Equipo(int id,
                      String codigoInventario,
                      String codigoQr,
                      String nombre,
                      String marca,
                      String modelo,
                      String estado,
                      String laboratorio,
                      String categoria,
                      String edificio) {

            this.id = id;
            this.codigoInventario = codigoInventario;
            this.codigoQr = codigoQr;
            this.nombre = nombre;
            this.marca = marca;
            this.modelo = modelo;
            this.estado = estado;
            this.laboratorio = laboratorio;
            this.categoria = categoria;
            this.edificio = edificio;
        }
    }

}