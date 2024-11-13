package com.app.proyectosdr;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.kernel.pdf.PdfDocument;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ExportarPDF {

    public void generarPdf(Context context, ListaEnlazada<Registro> registros) {
        File archivoPDF = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Registros_Ritmo_Cardiaco.pdf");

        try {
            PdfWriter writer = new PdfWriter(new FileOutputStream(archivoPDF));
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);

            Nodo<Registro> actual = registros.getHead();
            while (actual != null) {
                Registro registro = actual.getData();
                document.add(new Paragraph(registro.toString()));
                document.add(new Paragraph("\n"));
                actual = actual.getNext();
            }

            document.close();
            Log.d("ExportarPdf", "Archivo PDF generado en " + archivoPDF.getAbsolutePath());
            Toast.makeText(context, "PDF descargado con éxito en la carpeta de descargas ✅", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Log.e("ExportarPdf", "Error al crear PDF: " + e.getMessage());
            Toast.makeText(context, "Error al crear PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
