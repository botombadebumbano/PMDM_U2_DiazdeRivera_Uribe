
package com.ejemplo.mibiblioteca;

import android.view.View;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private EditText etTitulo, etAutor, etPaginas;
    private Button btnAnadir;
    private TextView tvResumen, tvVacio;
    private RecyclerView rvLibros;

    private LibroAdapter adapter;
    private LibroDao libroDao;

    private final List<Libro> listaLibros = new ArrayList<>();

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar elementos de la interfaz
        etTitulo = findViewById(R.id.etTitulo);
        etAutor = findViewById(R.id.etAutor);
        etPaginas = findViewById(R.id.etPaginas);

        btnAnadir = findViewById(R.id.btnAnadir);

        tvResumen = findViewById(R.id.tvResumen);
        tvVacio = findViewById(R.id.tvVacio);

        rvLibros = findViewById(R.id.rvLibros);

        // Obtener la base de datos
        AppDatabase db = AppDatabase.getInstance(
                getApplicationContext()
        );

        libroDao = db.libroDao();

        // Configurar RecyclerView
        rvLibros.setLayoutManager(
                new LinearLayoutManager(this)
        );

        adapter = new LibroAdapter(
                this,
                listaLibros,
                libroDao,
                this::cargarLibros
        );

        rvLibros.setAdapter(adapter);

        // Cargar libros almacenados
        cargarLibros();

        // Configurar botón de añadir
        btnAnadir.setOnClickListener(v -> anadirLibro());
    }

    private void cargarLibros() {

        executor.execute(() -> {

            List<Libro> libros = libroDao.obtenerTodos();
            int totalLeidos = libroDao.contarLeidos();

            runOnUiThread(() -> {

                listaLibros.clear();
                listaLibros.addAll(libros);

                adapter.notifyDataSetChanged();

                actualizarResumen(totalLeidos);
                actualizarEstadoVacio();
            });
        });
    }

    private void anadirLibro() {

        String titulo = etTitulo.getText().toString().trim();
        String autor = etAutor.getText().toString().trim();
        String paginasTexto = etPaginas.getText().toString().trim();

        // Comprobar campos obligatorios
        if (titulo.isEmpty() || autor.isEmpty()
                || paginasTexto.isEmpty()) {

            Toast.makeText(
                    this,
                    getString(R.string.error_obligatorio),
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        int paginas;

        try {
            paginas = Integer.parseInt(paginasTexto);

            if (paginas <= 0) {
                throw new NumberFormatException();
            }

        } catch (NumberFormatException e) {

            Toast.makeText(
                    this,
                    getString(R.string.error_paginas),
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // Crear objeto Libro
        Libro libro = new Libro(titulo, autor, paginas);

        // Guardar en la base de datos
        executor.execute(() -> {

            libroDao.insertar(libro);

            runOnUiThread(() -> {

                etTitulo.setText("");
                etAutor.setText("");
                etPaginas.setText("");

                Toast.makeText(
                        this,
                        getString(R.string.libro_guardado),
                        Toast.LENGTH_SHORT
                ).show();

                cargarLibros();
            });
        });
    }

    private void actualizarResumen(int totalLeidos) {

        int totalLibros = listaLibros.size();

        tvResumen.setText(
                getString(
                        R.string.resumen_libros,
                        totalLibros,
                        totalLeidos
                )
        );
    }

    private void actualizarEstadoVacio() {

        if (listaLibros.isEmpty()) {

            tvVacio.setVisibility(TextView.VISIBLE);
            rvLibros.setVisibility(View.GONE);

        } else {

            tvVacio.setVisibility(TextView.GONE);
            rvLibros.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (adapter != null) {
            adapter.cerrar();
        }

        executor.shutdown();
    }
}
