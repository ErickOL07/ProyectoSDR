package com.app.proyectosdr;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Registro {
    private String pensamiento;
    private String estadoAnimo;
    private String reaccionesFisicas;
    private String actividad;
    private String fechaHora;

    public Registro(String pensamiento, String estadoAnimo, String reaccionesFisicas, String actividad) {
        this.pensamiento = pensamiento.isEmpty() ? "-" : pensamiento;
        this.estadoAnimo = estadoAnimo.isEmpty() ? "-" : estadoAnimo;
        this.reaccionesFisicas = reaccionesFisicas.isEmpty() ? "-" : reaccionesFisicas;
        this.actividad = actividad.isEmpty() ? "-" : actividad;
        this.fechaHora = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    @Override
    public String toString() {
        return "Fecha y Hora: " + fechaHora +
                "\nPensamiento: " + pensamiento +
                "\nEstado de Ánimo: " + estadoAnimo +
                "\nReacciones Físicas: " + reaccionesFisicas +
                "\nActividad: " + actividad;
    }
}
