package com.cesar.netedu.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * MANEJADOR DE CLIENTE
 * * Esta clase implementa 'Runnable' para poder ser ejecutada en un hilo paralelo
 * Se encarga de atender a un solo alumno durante toda su sesión de juego
 */
public class ClientHandler implements Runnable {

    private Socket clientSocket;
    private PrintWriter out;    // Para enviar datos al alumno
    private BufferedReader in;  // Para leer datos del alumno

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try {
            // Configuro los canales de comunicación (Streams)
            // 'true' en PrintWriter activa el auto-flush para enviar mensajes al instante
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            // Protocolo de bienvenida
            out.println("BIENVENIDO AL SERVIDOR");
            out.println("Escribe 'HELLOUDA' para saludar o 'SALIR' para desconectar");

            String inputLine;
            
            // Bucle de comunicación continua
            //  Mantengo escuchando mientras la conexión esté activa y lleguen mensajes
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Mensaje recibido de " + clientSocket.getInetAddress() + ": " + inputLine);

                // Aquí irá la lógica futura del juego
                if ("HELLOUDA".equalsIgnoreCase(inputLine)) {
                    out.println("SERVIDOR: Hola! Listo para aprender?");
                } else if ("SALIR".equalsIgnoreCase(inputLine)) {
                    out.println("SERVIDOR: Chao tu progreso ha sido guardado");
                    break; // Rompemos el ciclo para cerrar la conexión
                } else {
                    out.println("SERVIDOR: Comando no reconocido por favor intenta con 'HELLOUDA'.");
                }
            }

        } catch (IOException e) {
            System.err.println("Error en la comunicación con el alumno: " + e.getMessage());
        } finally {
            // Limpieza de recursos (cierre de sockets)
            try {
                if (clientSocket != null) clientSocket.close();
                System.out.println(" >> Conexión cerrada con " + clientSocket.getInetAddress());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}