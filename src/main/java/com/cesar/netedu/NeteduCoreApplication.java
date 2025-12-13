package com.cesar.netedu;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


import com.cesar.netedu.socket.ClientHandler; 
import com.cesar.netedu.service.JuegoService; // Importo la lógica del juego
import com.cesar.netedu.service.UsuarioService;
import com.cesar.netedu.service.UDPService;

import java.net.ServerSocket;
import java.net.Socket;

/**
 * CLASE PRINCIPAL DEL SISTEMA NETEDU
 * * Esta clase es el punto de entrada, aquí implementé una arquitectura híbrida donde uso
 *   springboot que se encarga de la parte web y la conexión a la base de datos
 *   serversocket se encarga de gestiona las conexiones TCP para el juego en tiempo real
 */
@SpringBootApplication
public class NeteduCoreApplication implements CommandLineRunner {

    // Defino el puerto 5000 para TCP separado del 8080 para evitar conflictos
    private static final int TCP_PORT = 5000;

    /**
     * INYECCIÓN DE DEPENDENCIA (JuegoService):
     * Aquí le pido a Spring que me "preste" el servicio que conecta con la bd
     * como los Hilos no son manejados por Spring, necesito obtenerlo aquí
     * para pasarselo manualmente a cada cliente que se conecte
     */
    @Autowired
    private JuegoService juegoService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UDPService udpService;

    public static void main(String[] args) {
        // Arranco el uso de Spring 
        SpringApplication.run(NeteduCoreApplication.class, args);
    }

    /**
     * Este método se ejecuta justo después de que Spring termina de cargar
     * lo uso para iniciar mis servicios de red personalizados
     */
    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n--------------------------------------------------");
        System.out.println(" SISTEMA NETEDU - SERVIDOR INICIADO");
        System.out.println(" Web API (REST): Activa en puerto 8080");
        System.out.println(" Base de datos:  Conectada correctamente");
        System.out.println("--------------------------------------------------\n");

        // IMPORTANTE: Inicio el servidor TCP en un hilo nuevo
        // Si no hago esto, el bucle 'while(true)' bloquearía a Spring Boot y la web no funcionaría
        new Thread(this::iniciarServidorTCP).start();
    }

    /**
     * Esta es la lógica del servidor TCP (que cumple con el requisito de los sockets orientados a conexión)
     */
    private void iniciarServidorTCP() {
        try (ServerSocket serverSocket = new ServerSocket(TCP_PORT)) {
            System.out.println(" >> SERVIDOR TCP: Escuchando en puerto " + TCP_PORT);
            System.out.println(" >> Esperando jugadores para la sesión de aprendizaje...\n");

            while (true) {
                // El servidor se queda "dormido" aquí esperando a que un alumno se conecte
                Socket clientSocket = serverSocket.accept();
                System.out.println(" >> NUEVA CONEXION!!! Cliente: " + clientSocket.getInetAddress());

                // aqui manejo el uso de multithreading
                // por cada alumno que llega creo un objeto 'ClientHandler'
                // luego le paso 'clientSocket' y tambien 'juegoService'
                // esto permite que el hilo tenga acceso a las preguntas de la base de datos
                ClientHandler handler = new ClientHandler(clientSocket, juegoService, usuarioService, udpService);
                
                // Lanzo el hilo independiente
                new Thread(handler).start(); 
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
