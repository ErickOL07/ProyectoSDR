package com.app.proyectosdr;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.view.View;

public class ListaRegistro extends AppCompatActivity {

    private TextView registrosTextView;
    private Button exportarPdfButton;
    private ListaEnlazada<Registro> registros;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lista_registro);

        registrosTextView = findViewById(R.id.registrosTextView);
        exportarPdfButton = findViewById(R.id.exportarPdfButton);

        registros = RegistrosSingleton.getInstance().getRegistros();

        mostrarRegistros();

        exportarPdfButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exportarRegistrosAPdf();
            }
        });
    }

    private void mostrarRegistros() {
        StringBuilder registrosTexto = new StringBuilder();
        Nodo<Registro> actual = registros.getHead();

        while (actual != null) {
            Registro registro = actual.getData();
            registrosTexto.append(registro.toString()).append("\n\n");
            actual = actual.getNext();
        }

        registrosTextView.setText(registrosTexto.toString());
    }

    private void exportarRegistrosAPdf() {
        ExportarPDF exportarPdf = new ExportarPDF();
        exportarPdf.generarPdf(this, registros);
    }
}
