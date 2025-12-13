package com.cesar.netedu.socket;

import com.cesar.netedu.model.Pregunta;
import com.cesar.netedu.model.Usuario;
import com.cesar.netedu.service.JuegoService;
import com.cesar.netedu.service.UsuarioService;
import com.cesar.netedu.service.UDPService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * MANEJADOR DE CLIENTE
 * * Esta clase implementa 'Runnable' para ejecutarse en paralelo
 * * su responsabilidad es atender a un solo alumno para mantener su sesión
 * * y servir de puente entre el alumno y la base de datos
 */
public class ClientHandler implements Runnable {

    private Socket clientSocket;
    private JuegoService juegoService;
    private UsuarioService usuarioService; // agrego el servicio de usuarios para poder validar contraseñas
    private UDPService udpService; // agrego el servicio UDP para enviar notificaciones rápidas
    
    private PrintWriter out;    // Canal de salida (Enviar datos al alumno)
    private BufferedReader in;  // Canal de entrada (Leer datos del alumno)

    // --- MEMORIA DEL HILO ---
    // agrego estas variables para saber en qué estado se encuentra el alumno
    private Usuario usuarioLogueado = null; // si esto es null, es un usuario "fantasma" que no ha iniciado sesión
    private Pregunta preguntaActual = null; // guarda la pregunta que se está haciendo
    private boolean esperandoRespuesta = false; // bandera para saber si espero A/B/C o un comando normal

    /**
     * CONSTRUCTOR
     * Recibo el socket de conexión y las instancias de los servicios
     * es necesario pasar 'juegoService', 'usuarioService' y 'udpService' por parámetro 
     * porque, al ser un hilo manual, springboot no puede inyectar dependencias automáticamente aquí
     */
    public ClientHandler(Socket socket, JuegoService juegoService, UsuarioService usuarioService, UDPService udpService) {
        this.clientSocket = socket;
        this.juegoService = juegoService;
        this.usuarioService = usuarioService;
        this.udpService = udpService;
    }

    @Override
    public void run() {
        try {
            // Configuro los flujos de comunicación (Streams)
            // pongo 'true' en PrintWriter lo que activa el auto-flush para que los mensajes salgan al instante
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            // 1. Protocolo de bienvenida y explico que deben logearse para poder usarlo
            out.println("BIENVENIDO AL SERVIDOR NETEDU ");
            out.println("Debes identificarte. Usa 'LOGIN <user> <pass>' o 'REGISTRAR <user> <pass>'");

            String inputLine;
            
            // 2. Bucle de comunicación continua
            // mantengo escuchando mientras la conexión esté activa y lleguen mensajes
            while ((inputLine = in.readLine()) != null) {
                // limpio la entrada para evitar errores con espacios
                String lineaLimpia = inputLine.trim();
                System.out.println("Mensaje recibido de " + clientSocket.getInetAddress() + ": " + lineaLimpia);

                // MAQUINA DE ESTADOS: decido si estoy esperando un comando o una respuesta de trivia
                if (esperandoRespuesta) {
                    // si la bandera es true, significa que el alumno está respondiendo la pregunta
                    procesarRespuesta(lineaLimpia.toUpperCase());
                } else {
                    // si es false, espero un comando normal (LOGIN, REGISTRAR, JUGAR)
                    procesarComando(lineaLimpia);
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
     * también maneja el login y registro separando las palabras del comando
     */
    private void procesarComando(String linea) {
        // separo la línea en partes por espacios (ej: "LOGIN cesar 123" -> ["LOGIN", "cesar", "123"])
        String[] partes = linea.split("\\s+");
        String comando = partes[0].toUpperCase();

        if ("LOGIN".equals(comando)) {
            intentarLogin(partes);
        } 
        else if ("REGISTRAR".equals(comando)) {
            intentarRegistro(partes);
        } 
        else if ("SALIR".equals(comando)) {
            out.println("SERVIDOR: Gracias por participar! Tu progreso ha sido guardado");
            cerrarConexion();
        } 
        // Esto es como una zona protegida ya que solo dejo pasar si ya iniciaron sesión
        else if ("JUGAR".equals(comando)) {
            if (usuarioLogueado == null) {
                out.println("ERROR: Debes iniciar sesion primero con LOGIN o REGISTRAR.");
            } else {
                // Si el alumno quiere jugar y ya está logueado, le pido una pregunta al servicio
                enviarPregunta();
            }
        } 
        else {
            out.println("SERVIDOR: Comando no reconocido. Intenta 'LOGIN', 'REGISTRAR' o 'JUGAR'");
        }
    }

    /**
     * metodo auxiliar para verificar si el usuario existe y la contraseña es correcta
     */
    private void intentarLogin(String[] partes) {
        if (partes.length < 3) {
            out.println("Faltan datos. Escribe: LOGIN <usuario> <password>");
            return;
        }
        String u = partes[1];
        String p = partes[2];
        
        // busco el usuario en la BD
        Usuario usuarioEncontrado = usuarioService.login(u, p);
        
        if (usuarioEncontrado != null) {
            usuarioLogueado = usuarioEncontrado; // guardo la sesión en la memoria del hilo
            out.println("EXITO: Hola de nuevo " + usuarioEncontrado.getUsername() + ". Tienes " + usuarioEncontrado.getPuntajeTotal() + " puntos.");
            
            // --- Aqui uso UDP ---
            // mando un saludo rápido por el canal UDP para verificar que funciona
            udpService.enviarMensaje(clientSocket.getInetAddress(), "UDP_CHECK: Sesion iniciada para " + usuarioEncontrado.getUsername());
            
            out.println("Escribe 'JUGAR' para comenzar.");
        } else {
            out.println("ERROR: Usuario o password incorrectos.");
        }
    }

    /**
     * metodo auxiliar para crear un usuario nuevo en la base de datos
     */
    private void intentarRegistro(String[] partes) {
        if (partes.length < 3) {
            out.println("Faltan datos, por favor escribe: REGISTRAR <usuario> <password>");
            return;
        }
        String u = partes[1];
        String p = partes[2];

        // intento guardarlo en la BD
        Usuario nuevoUsuario = usuarioService.registrar(u, p);

        if (nuevoUsuario != null) {
            usuarioLogueado = nuevoUsuario; // inicio sesión automáticamente al registrarse
            out.println("Registro exitoso: Bienvenido " + nuevoUsuario.getUsername() + ". Cuenta creada");
            out.println("Escribe 'JUGAR' para comenzar");
        } else {
            out.println("ERROR 404: Ese usuario ya existe, por favor prueba otro nombre");
        }
    }

    /**
     * aqui hago un método auxiliar para buscar una pregunta en la BD y enviarla formateada
     */
    private void enviarPregunta() {
        // uso el servicio inyectado para buscar una pregunta aleatoria en Postgres
        preguntaActual = juegoService.obtenerPreguntaAleatoria();

        if (preguntaActual == null) {
            out.println("ERROR: La base de datos de preguntas esta vacia. Avisa al profesor");
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
     * metodo para validar si la respuesta A, B o C es correcta
     */
    private void procesarRespuesta(String respuesta) {
        // primero valido que solo escriban A, B o C
        if (!respuesta.matches("[ABC]")) {
            out.println("Opción invalida. Por favor escribe solo A, B o C");
            return; // retorno para seguir esperando una respuesta válida
        }

        // comparo lo que escribió el alumno con la respuesta correcta de la BD
        if (respuesta.equals(preguntaActual.getOpcionCorrecta())) {
            int puntosGanados = preguntaActual.getPuntos();
            out.println("CORRECTO! Has ganado " + puntosGanados + " puntos.");
            
            // IMPORTANTE: guardo los puntos en la BD usando el servicio
            usuarioService.sumarPuntos(usuarioLogueado, puntosGanados);
            out.println("Tu puntaje total ahora es: " + usuarioLogueado.getPuntajeTotal());

            // --- esta parte tambien es parte del UDP ---
            // Envío una notificación "flash" al usuario usando sockets sin conexión
            udpService.enviarMensaje(clientSocket.getInetAddress(), "ALERT: Has sumado +" + puntosGanados + " puntos exp!");
            
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