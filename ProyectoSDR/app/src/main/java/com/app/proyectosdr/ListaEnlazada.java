package com.app.proyectosdr;

public class ListaEnlazada<T> {
    private Nodo<T> head;

    public ListaEnlazada() {
        this.head = null;
    }

    public boolean estaVacia() {
        return head == null;
    }

    public void insertar(T data) {
        Nodo<T> nuevo = new Nodo<T>(data);
        if (estaVacia()) {
            head = nuevo;
        } else {
            Nodo<T> current = head;
            while (current.getNext() != null) {
                current = current.getNext();
            }
            current.setNext(nuevo);
        }
    }

    public Nodo<T> buscar(T data) {
        Nodo<T> current = head;
        while (current != null) {
            if (current.getData().equals(data)) {
                return current;
            }
            current = current.getNext();
        }
        return null;
    }

    public void eliminar(T data) {
        if (estaVacia()) {
            return;
        }

        if (head.getData().equals(data)) {
            head = head.getNext();
            return;
        }

        Nodo<T> current = head;
        Nodo<T> previous = null;

        while (current != null && !current.getData().equals(data)) {
            previous = current;
            current = current.getNext();
        }

        if (current != null) {
            previous.setNext(current.getNext());
        }
    }

    public Nodo<T> getHead() {
        return head;
    }

    public int contar() {
        int c = 0;
        Nodo<T> current = head;
        while (current != null) {
            c++;
            current = current.getNext();
        }
        return c;
    }

}

