package com.mycompany.jpsocket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.mycompany.jpsocket.client.ClientHandler;

/**
 *
 * @author k4lfer
 */
public class JPSocket {

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(9999)) { // Crea un servidor en el puerto 9999
            System.out.println("Servidor iniciado en el puerto 9999...");
            while (true) { // Bucle infinito para aceptar conexiones de clientes
                Socket clientSocket = serverSocket.accept(); // Acepta una conexión de cliente
                System.out.println("Nuevo cliente conectado" + clientSocket); // Imprime un mensaje
                new Thread(new ClientHandler(clientSocket)).start(); // Crea un nuevo hilo para manejar al cliente conectado
            }
        } catch (IOException e) {
            System.out.println("Error en el servidor: " + e.getMessage());
        }
    }
}
