
package com.ejemplo.mibiblioteca;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LibroAdapter extends RecyclerView.Adapter<LibroAdapter.LibroViewHolder> {

    private final Context context;
    private final List<Libro> listaLibros;
    private final LibroDao libroDao;
    private final Runnable actualizarResumen;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    public LibroAdapter(Context context, List<Libro> listaLibros,
                        LibroDao libroDao, Runnable actualizarResumen) {

        this.context = context;
        this.listaLibros = listaLibros;
        this.libroDao = libroDao;
        this.actualizarResumen = actualizarResumen;
    }

    @NonNull
    @Override
    public LibroViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                              int viewType) {

        View vista = LayoutInflater.from(context)
                .inflate(R.layout.item_libro, parent, false);

        return new LibroViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull LibroViewHolder holder,
                                 int position) {

        Libro libro = listaLibros.get(position);

        holder.tvTitulo.setText(libro.titulo);
        holder.tvAutor.setText(libro.autor);

        holder.tvPaginas.setText(
                context.getString(R.string.paginas_formato, libro.paginas)
        );

        // Configurar estado de lectura
        holder.cbLeido.setOnCheckedChangeListener(null);
        holder.cbLeido.setChecked(libro.leido);

        holder.cbLeido.setOnCheckedChangeListener((buttonView, isChecked) -> {

            libro.leido = isChecked;

            executor.execute(() -> {
                libroDao.actualizar(libro);
                actualizarResumen.run();
            });
        });

        // Configurar botón de eliminar
        holder.btnEliminar.setOnClickListener(v -> {

            new AlertDialog.Builder(context)
                    .setTitle("Eliminar libro")
                    .setMessage("¿Estás seguro de que quieres eliminar este libro?")
                    .setPositiveButton("Eliminar", (dialog, which) -> {

                        ViewGroup contenedor =
                                (ViewGroup) holder.itemView.getParent();

                        executor.execute(() -> {

                            libroDao.eliminar(libro);

                            contenedor.post(() -> {

                                int posicion = listaLibros.indexOf(libro);

                                if (posicion != -1) {
                                    listaLibros.remove(posicion);
                                    notifyItemRemoved(posicion);
                                }

                                actualizarResumen.run();

                                // Mostrar Snackbar con opción de deshacer
                                Snackbar.make(
                                        contenedor,
                                        "Libro eliminado",
                                        Snackbar.LENGTH_LONG
                                ).setAction("Deshacer", accion -> {

                                    executor.execute(() -> {

                                        libroDao.insertar(libro);

                                        // Actualizar la lista y el contador
                                        actualizarResumen.run();
                                    });

                                }).show();
                            });
                        });
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return listaLibros.size();
    }

    public void cerrar() {
        executor.shutdown();
    }

    public static class LibroViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitulo;
        TextView tvAutor;
        TextView tvPaginas;
        CheckBox cbLeido;
        ImageButton btnEliminar;

        public LibroViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTitulo = itemView.findViewById(R.id.tvItemTitulo);
            tvAutor = itemView.findViewById(R.id.tvItemAutor);
            tvPaginas = itemView.findViewById(R.id.tvItemPaginas);
            cbLeido = itemView.findViewById(R.id.cbItemLeido);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
        }
    }
}
