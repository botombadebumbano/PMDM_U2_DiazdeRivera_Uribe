
package com.ejemplo.enrutamientoavanzado;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;

import com.ejemplo.enrutamientoavanzado.databinding.FragmentFirstBinding;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonFirst.setOnClickListener(v -> {

            String textoNota = binding.etInputNota.getText().toString().trim();

            // Validar que el campo no esté vacío
            if (textoNota.isEmpty()) {
                Toast.makeText(getContext(),
                        R.string.error_titulo_vacio,
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Enviar el título mediante SafeArgs
            NavDirections action = FirstFragmentDirections
                    .actionFirstFragmentToSecondFragment(textoNota);

            // Navegar a la segunda pantalla
            NavHostFragment.findNavController(FirstFragment.this)
                    .navigate(action);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
