package com.mycompany.jpsocket.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler implements Runnable {

    private static final List<ClientHandler> clients = new ArrayList<>(); // Lista de clientes conectados

    private final Socket socket; // Socket del cliente
    private DataInputStream input; // Stream de entrada para recibir mensajes
    private DataOutputStream output; // Stream de salida para enviar mensajes


    /**
     * Constructor que inicializa el socket y los streams de entrada y salida.
     *
     * @param socket El socket del cliente.
     */
    public ClientHandler(Socket socket) {
        this.socket = socket; // Inicializa el socket del cliente
        // Inicializa los streams de entrada y salida
        try {
            input = new DataInputStream(socket.getInputStream()); // Stream de entrada para recibir mensajes
            output = new DataOutputStream(socket.getOutputStream()); // Stream de salida para enviar mensajes
            synchronized (clients) { // Bloque sincronizado para evitar problemas de concurrencia
                clients.add(this); // Agrega este cliente a la lista de clientes conectados
            }
        } catch (IOException e) { // Maneja excepciones al crear los streams
            System.out.println("Error al crear streams: " + e.getMessage()); // Imprime el mensaje de error
        }
    }

    @Override
    public void run() {
        try {
            while (!socket.isClosed()) { // Mientras el socket no esté cerrado
                // Lee el mensaje enviado por el cliente
                String message = input.readUTF();
                broadcast(message); // Envía el mensaje a todos los clientes conectados
            }
        } catch (IOException e) { // Maneja excepciones al leer mensajes
            System.out.println("Cliente desconectado.");
        } finally { // Bloque finally para cerrar recursos
            // Cierra los streams de entrada y salida
            try {
                socket.close();
            } catch (IOException ignored) {}

            synchronized (clients) { // Bloque sincronizado para evitar problemas de concurrencia
                // Elimina este cliente de la lista de clientes conectados
                clients.remove(this);
            }
        }
    }

    // Método para enviar un mensaje a todos los clientes conectados
    private void broadcast(String message) { 
        synchronized (clients) { // Bloque sincronizado para evitar problemas de concurrencia
            for (ClientHandler client : clients) { // Itera sobre la lista de clientes conectados
                try { // Intenta enviar el mensaje al cliente
                    client.output.writeUTF(message); // Envia el mensaje
                } catch (IOException ignored) {
                    // Si ocurre un error al enviar el mensaje, se ignora
                    System.out.println("Error al enviar mensaje a un cliente: " + ignored.getMessage());
                } 
            }
        }
    }
}
