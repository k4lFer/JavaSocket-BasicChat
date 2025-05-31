# JavaSocket-BasicChat

Un proyecto de chat simple implementado en Java utilizando sockets (`Socket` y `ServerSocket`). Permite la comunicación entre un cliente y un servidor dentro de una red local. Ideal para comprender los fundamentos de la programación en Java.

## 📦 Características

- Comunicación en tiempo real entre cliente y servidor
- Interfaz gráfica sencilla usando `Swing`
- Registro basico de su nombre
- Envío de mensajes presionando haciendo clic en un botón
- Soporte para múltiples clientes (todos comparten la misma sala)

## 📁 Estructura del proyecto

```bash
jPSocket/
├── src/main/java/com/mycompany/jpsocket
│ 
│ └── client/
│ └── JFClient.java
│ └── JPSocket.java #Clase main 
│
├── README.md
└── ...
```

## 🧰 Requisitos

- JDK 17
- IDE como NetBeans o VsCode (opcional)

## 🚀 Instrucciones para ejecutar

### 1. Compilar

```bash
javac src/main/java/com/mycompany/jpsocket/JPSocket.java
javac src/main/java/com/mycompany/jpsocket/client/JFClient.java
```

### 2. Ejecutar el servidor
Se ejecutara de forma local en el puerto 9999
```bash
java -cp src/main/java/com.mycompany.jpsocket.JPSocket
```

### 3. Ejecutar el cliente
Puedes abrir varias instancias del cliente si quieres probar múltiples conexiones.
```bash
java -cp src/main/java/com.mycompany.jpsocket.client.JFClient
```

### 4. Enviar mensajes
1. Registrar primero tu nombre.
2. Escribe un mensaje en el campo de texto y darle al boton enviar (Send).
3. El mensaje se enviará y aparecerá en el área de chat.
    ```
    🔔 Nota: Todos los clientes se conectan a la misma sala. No hay soporte para chats privados ni por salas individuales.
    ```

## 🧪 Posibles mejoras
- Soporte para múltiples salas (chat grupal por temas)
- Chat 1 a 1 entre usuarios
- Historial de mensajes
- Notificaciones de conexión/desconexión
- Mejora en el manejo de errores de red




