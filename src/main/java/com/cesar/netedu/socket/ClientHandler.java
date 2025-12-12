package com.cesar.netedu.socket;

import com.cesar.netedu.model.Pregunta;
import com.cesar.netedu.service.JuegoService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * MANEJADOR DE CLIENTE (es un hilo independiente)
 * * Esta clase implementa 'Runnable' para ejecutarse en paralelo
 * * su responsabilidad es atender a un solo alumno mantener su sesión
 *   y servir de puente entre el alumno y la base de datos
 */
public class ClientHandler implements Runnable {

    private Socket clientSocket;
    private JuegoService juegoService; 
    
    private PrintWriter out;    // Canal de salida (Enviar datos al alumno)
    private BufferedReader in;  // Canal de entrada (Leer datos del alumno)

    /**
     * CONSTRUCTOR
     * Recibo el socket de conexión y la instancia del servicio de juego
     * es necesario pasar el 'juegoService' por parámetro porque, al ser un hilo manual,
     * springboot no puede inyectar dependencias automáticamente aquí
     */
    public ClientHandler(Socket socket, JuegoService service) {
        this.clientSocket = socket;
        this.juegoService = service;
    }

    @Override
    public void run() {
        try {
            // Configuro los flujos de comunicación (Streams)
            // 'true' en PrintWriter activa el auto-flush para que los mensajes salgan al instante
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            // 1. Protocolo de Bienvenida
            out.println("BIENVENIDO AL SERVIDOR NETEDU");
            out.println("Escribe 'JUGAR' para recibir una pregunta de la BD o 'SALIR' para irte.");

            String inputLine;
            
            // 2. Bucle de comunicación continua
            // mantengo escuchando mientras la conexión esté activa y lleguen mensajes
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Mensaje recibido de " + clientSocket.getInetAddress() + ": " + inputLine);

                // Lógica de comandos
                if ("JUGAR".equalsIgnoreCase(inputLine)) {
                    // Si el alumno quiere jugar, le pido una pregunta al servicio
                    enviarPregunta();
                } else if ("SALIR".equalsIgnoreCase(inputLine)) {
                    out.println("SERVIDOR: ¡Gracias por participar! Tu progreso ha sido guardado.");
                    break; // Rompemos el ciclo para cerrar la conexión
                } else {
                    out.println("SERVIDOR: Comando no reconocido. Intenta escribir 'JUGAR'.");
                }
            }

        } catch (IOException e) {
            System.err.println("Error en la comunicación con el alumno: " + e.getMessage());
        } finally {
            // 3. Limpieza de recursos
            // es necesario cerrar el socket al terminar para liberar el puerto
            try {
                if (clientSocket != null) clientSocket.close();
                System.out.println(" >> Conexión cerrada con " + clientSocket.getInetAddress());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * aqui hago un método auxiliar para buscar una pregunta en la BD y enviarla formateada
     */
    private void enviarPregunta() {
        // uso el servicio inyectado para buscar una pregunta aleatoria en Postgres
        Pregunta p = juegoService.obtenerPreguntaAleatoria();

        if (p == null) {
            out.println("ERROR: La base de datos de preguntas está vacía. Avisa al profesor.");
        } else {
            // Envío la pregunta línea por línea para que el cliente la lea fácil
            out.println("--- PREGUNTA DE REDES ---");
            out.println("PROBLEMA: " + p.getEnunciado());
            out.println("A) " + p.getOpcionA());
            out.println("B) " + p.getOpcionB());
            out.println("C) " + p.getOpcionC());
            out.println("-------------------------");
            out.println("RESPONDE (A, B o C):");
        }
    }
}