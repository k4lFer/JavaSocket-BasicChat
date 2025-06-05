package com.mycompany.jpsocket.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientHandler implements Runnable {

    private static final List<ClientHandler> clients = new ArrayList<>(); // Lista de clientes conectados

    private final Socket socket; // Socket del cliente
    private String clientName; // Nombre del cliente, puede ser utilizado para personalizar mensajes
    private DataInputStream input; // Stream de entrada para recibir mensajes
    private DataOutputStream output; // Stream de salida para enviar mensajes
    private final Map<String, List<String>> data = new HashMap<>(); // Mapa para almacenar datos
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

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
            this.clientName = input.readUTF(); // Lee el nombre del usuario desde el stream de entrada
            broadcastUserList();
            while (!socket.isClosed()) { // Mientras el socket no esté cerrado
                
                // Lee el mensaje enviado por el cliente
                String message = input.readUTF();
                if(message.equals("FILE"))
                {
                    //String fileName = message.substring(4).trim(); // Extrae el nombre del archivo del mensaje
                    //System.out.println("Archivo solicitado: " + fileName); // Imprime el nombre del archivo solicitado
                    receiveFile();
                    //sendFile(fileName); // Envía el archivo al cliente
                }
                System.out.println("Mensaje enviado de: " + socket.getRemoteSocketAddress() + " a " + socket.getLocalSocketAddress() +" Hora :" + formatter.format(LocalTime.now())); // Imprime el mensaje recibido junto con las direcciones del socket remoto y local
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
            broadcastUserList();
        }
    }

    // Método para enviar un mensaje a todos los clientes conectados
    private void broadcast(String message) { 
        synchronized (clients) { // Bloque sincronizado para evitar problemas de concurrencia
            for (ClientHandler client : clients) { // Itera sobre la lista de clientes conectados
                try { // Intenta enviar el mensaje al cliente
                    client.output.writeUTF("[" + formatter.format(java.time.LocalTime.now()) + "] "+ clientName+": " + message); // Envia el mensaje
                } catch (IOException ignored) {
                    // Si ocurre un error al enviar el mensaje, se ignora
                    System.out.println("Error al enviar mensaje a un cliente: " + ignored.getMessage());
                } 
            }
        }
    }

    private List<String> getHistory(String sender, String receiver) {
        // La clave debe ser ordenada alfabéticamente para que coincida con la usada en 'send'
        String key = sender.compareTo(receiver) < 0
            ? sender + "|" + receiver
            : receiver + "|" + sender;

        // Busca el historial en todos los clientes conectados
        for (ClientHandler client : clients) {
            if (client.data.containsKey(key)) {
                return client.data.get(key);
            }
        }
        return null;
    }
    

    // Metodo para enviar un mensaje a un cliente específico
    private void broadcastOnebyOne(String message, String sender, String receiver) {
        synchronized (clients) { // Bloque sincronizado para evitar problemas de concurrencia
            for (ClientHandler client : clients) { // Itera sobre la lista de clientes conectados
                if (client.clientName.equals(receiver)) { // Si el cliente es el destinatario del mensaje
                    try {
                        // Crear una clave única para el par de usuarios (ordenada alfabéticamente)
                        String key = sender.compareTo(receiver) < 0
                            ? sender + "|" + receiver
                            : receiver + "|" + sender;

                        // Guardar el mensaje en el historial
                        data.putIfAbsent(key, new ArrayList<>());
                        List<String> messages = data.get(key);
                        messages.add("[" + formatter.format(java.time.LocalTime.now()) + "] " + sender + ": " + message);

                        client.output.writeUTF("[" + formatter.format(java.time.LocalTime.now()) + "] " + sender + ": " + message); // Envia el mensaje al cliente
                    } catch (IOException e) {
                        System.out.println("Error al enviar mensaje a " + receiver + ": " + e.getMessage()); // Imprime el mensaje de error
                    }
                    break; // Sale del bucle una vez que se ha enviado el mensaje al destinatario
                }
            }
        }

    }

    private void broadcastUserList() {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                client.sendUserList();
            }
        }
    }

    private void sendUserList() {
        synchronized (clients) {
            try {
                StringBuilder userList = new StringBuilder();
                for (ClientHandler client : clients) {
                    if (client != this && client.clientName != null) {
                        userList.append(client.clientName).append(",");
                    }
                }
                // Quitar la última coma
                if (userList.length() > 0) {
                    userList.setLength(userList.length() - 1);
                }

                output.writeUTF("USERLIST:" + userList.toString());
            } catch (IOException e) {
                System.out.println("Error al enviar la lista de usuarios: " + e.getMessage());
            }
        }
    }

    private void receiveFile() throws IOException {
        String fileName = input.readUTF();
        long fileSize = input.readLong();

        File saveDir = new File("archivos_recibidos");
        if (!saveDir.exists()) saveDir.mkdirs();

        File outFile = new File(saveDir, fileName);

        try (FileOutputStream fos = new FileOutputStream(outFile)) {
            byte[] buffer = new byte[4096];
            long totalRead = 0;
            int read;

            while (totalRead < fileSize && (read = input.read(buffer, 0, (int)Math.min(buffer.length, fileSize - totalRead))) != -1) {
                fos.write(buffer, 0, read);
                totalRead += read;
            }

            System.out.println("Archivo recibido: " + fileName + (" (" + totalRead + " bytes)"));

            // Envía una confirmación al cliente
            String content = new String(java.nio.file.Files.readAllBytes(outFile.toPath()), java.nio.charset.StandardCharsets.UTF_8);
            System.out.println("contenido: " + content);
            String escapedContent = content
                                    .replace("&", "&amp;")
                                    .replace("<", "&lt;")
                                    .replace(">", "&gt;")
                                    .replace("\n", "<br/>");
             broadcast("<b>Contenido del archivo \"" + fileName + "\":</b><br/>" + escapedContent);
        }
    }

}
