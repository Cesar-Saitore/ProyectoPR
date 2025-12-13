package com.cesar.netedu.socket;

import com.cesar.netedu.model.Pregunta;
import com.cesar.netedu.service.JuegoService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * MANEJADOR DE CLIENTE
 * * Esta clase implementa 'Runnable' para ejecutarse en paralelo
 * * su responsabilidad es atender a un solo alumno mantener su sesión
 * * y servir de puente entre el alumno y la base de datos
 */
public class ClientHandler implements Runnable {

    private Socket clientSocket;
    private JuegoService juegoService; 
    
    private PrintWriter out;    // Canal de salida (Enviar datos al alumno)
    private BufferedReader in;  // Canal de entrada (Leer datos del alumno)

    // --- MEMORIA DEL HILO ---
    // agrego estas variables para saber en qué estado se encuentra el alumno
    private Pregunta preguntaActual = null; // guarda la pregunta que se está haciendo
    private boolean esperandoRespuesta = false; // y hay una bandera para saber si espero A/B/C o un comando normal

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
            // pongo 'true' en PrintWriter lo que activa el auto-flush para que los mensajes salgan al instante
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            // 1. Protocolo de Bienvenida
            out.println("BIENVENIDO AL SERVIDOR NETEDU");
            out.println("Escribe 'JUGAR' para recibir una pregunta de la BD o 'SALIR' para irte");

            String inputLine;
            
            // 2. Bucle de comunicación continua
            // mantengo escuchando mientras la conexión esté activa y lleguen mensajes
            while ((inputLine = in.readLine()) != null) {
                // limpio la entrada para evitar errores con espacios y mayúsculas
                String respuesta = inputLine.trim().toUpperCase();
                System.out.println("Mensaje recibido de " + clientSocket.getInetAddress() + ": " + respuesta);

                // MAQUINA DE ESTADOS: decido si estoy esperando un comando o una respuesta de trivia
                if (esperandoRespuesta) {
                    // si la bandera es true, significa que el alumno está respondiendo la pregunta
                    procesarRespuesta(respuesta);
                } else {
                    // si es false, espero un comando normal como JUGAR
                    procesarComando(respuesta);
                }
            }

        } catch (IOException e) {
            System.err.println("Error en la comunicacion con el alumno: " + e.getMessage());
        } finally {
            // 3. Limpieza de recursos
            // es necesario cerrar el socket al terminar para liberar el puerto
            cerrarConexion();
        }
    }

    /**
     * aqui tengo el metodo para manejar los comandos básicos cuando NO se está jugando
     */
    private void procesarComando(String comando) {
        if ("JUGAR".equals(comando)) {
            // Si el alumno quiere jugar, le pido una pregunta al servicio
            enviarPregunta();
        } else if ("SALIR".equals(comando)) {
            out.println("SERVIDOR: Gracias por participar! Tu progreso ha sido guardado");
            cerrarConexion();
        } else {
            out.println("SERVIDOR: Comando no reconocido. Por favor intenta escribir 'JUGAR'");
        }
    }

    /**
     * aqui hago un método auxiliar para buscar una pregunta en la BD y enviarla formateada
     */
    private void enviarPregunta() {
        // uso el servicio inyectado para buscar una pregunta aleatoria en Postgres
        preguntaActual = juegoService.obtenerPreguntaAleatoria();

        if (preguntaActual == null) {
            out.println("ERROR: La base de datos de preguntas está vacía. Avisa al profesor");
            esperandoRespuesta = false; // me aseguro de no quedarme esperando
        } else {
            // Envío la pregunta línea por línea para que el cliente la lea fácil
            out.println("--- PREGUNTA DE REDES ---");
            out.println("PROBLEMA: " + preguntaActual.getEnunciado());
            out.println("A) " + preguntaActual.getOpcionA());
            out.println("B) " + preguntaActual.getOpcionB());
            out.println("C) " + preguntaActual.getOpcionC());
            out.println("-------------------------");
            out.println("RESPONDE (A, B o C):");
            
            // activo la bandera para que el proximo mensaje se trate como respuesta
            esperandoRespuesta = true; 
        }
    }

    /**
     * metodo nuevo para validar si la respuesta A, B o C es correcta
     */
    private void procesarRespuesta(String respuesta) {
        // primero valido que solo escriban A, B o C
        if (!respuesta.matches("[ABC]")) {
            out.println("Opción inválida. Por favor escribe solo A, B o C");
            return; // retorno para seguir esperando una respuesta válida
        }

        // comparo lo que escribió el alumno con la respuesta correcta de la BD
        if (respuesta.equals(preguntaActual.getOpcionCorrecta())) {
            out.println("CORRECTO! Has ganado " + preguntaActual.getPuntos() + " puntos.");
            // aqui sumaremos los puntos al usuario mas adelante
        } else {
            out.println("INCORRECTO. La respuesta era " + preguntaActual.getOpcionCorrecta());
        }

        out.println("Escribe 'JUGAR' para otra o 'SALIR'.");
        
        // reinicio el estado para volver a recibir comandos normales
        preguntaActual = null;
        esperandoRespuesta = false;
    }

    // metodo auxiliar para no repetir código de cierre
    private void cerrarConexion() {
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
            System.out.println(" >> Conexión cerrada con " + clientSocket.getInetAddress());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}