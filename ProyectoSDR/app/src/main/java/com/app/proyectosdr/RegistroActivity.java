package com.app.proyectosdr;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class RegistroActivity extends AppCompatActivity {
    private int ritmoActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registro_act);

        ritmoActual = getIntent().getIntExtra("ritmoActual", 0);

        TextView tvRitmoActual = findViewById(R.id.tvRitmoActual);
        tvRitmoActual.setText("Ritmo cardíaco actual: " + ritmoActual);

        Button btnRegistrar = findViewById(R.id.btnRegistrar);
        btnRegistrar.setOnClickListener(v -> registrarDatos());
    }

    private void registrarDatos() {
        EditText etPensamiento = findViewById(R.id.etPensamiento);
        EditText etEstadoAnimo = findViewById(R.id.etEstadoAnimo);
        EditText etReaccionesFisicas = findViewById(R.id.etReaccionesFisicas);
        EditText etActividad = findViewById(R.id.etActividad);

        ((Main) getApplicationContext()).registrar(
                etPensamiento.getText().toString(),
                etEstadoAnimo.getText().toString(),
                etReaccionesFisicas.getText().toString(),
                etActividad.getText().toString()
        );

        finish();
    }
}
