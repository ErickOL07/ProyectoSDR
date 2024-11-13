package com.app.proyectosdr;

public class RegistrosSingleton {
    private static RegistrosSingleton instance;
    private ListaEnlazada<Registro> registros;

    private RegistrosSingleton() {
        registros = new ListaEnlazada<>();
    }

    public static synchronized RegistrosSingleton getInstance() {
        if (instance == null) {
            instance = new RegistrosSingleton();
        }
        return instance;
    }

    public ListaEnlazada<Registro> getRegistros() {
        return registros;
    }

    public void agregarRegistro(Registro registro) {
        registros.insertar(registro);
    }
}


