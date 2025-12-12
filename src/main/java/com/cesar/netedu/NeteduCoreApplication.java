package com.cesar.netedu;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.cesar.netedu.model.ClientHandler; 

import java.net.ServerSocket;
import java.net.Socket;

/**
 * CLASE PRINCIPAL DEL SISTEMA
 * * Esta clase es el punto de entrada. Aquí implementé una arquitectura híbrida:
 * 		Spring Boot: Se encarga de la parte web y la conexión a base de datos
 * 		ServerSocket: Gestiona las conexiones TCP para el juego en tiempo real
 */
@SpringBootApplication
public class NeteduCoreApplication implements CommandLineRunner {

    // Defino el puerto 5000 para TCP separado del 8080 (Web) para evitar conflictos
    private static final int TCP_PORT = 5000;

    public static void main(String[] args) {
        // Arranca el contexto de Spring 
        SpringApplication.run(NeteduCoreApplication.class, args);
    }

    /**
     * Este método se ejecuta justo después de que Spring termina de cargar
     * Lo uso para iniciar mis servicios de red personalizados
     */
    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n--------------------------------------------------");
        System.out.println(" SISTEMA NETEDU - SERVIDOR HÍBRIDO INICIADO");
        System.out.println(" 1. Web API (REST): Activa en puerto 8080 (Gestión)");
        System.out.println(" 2. Base de Datos:  Conectada a PostgreSQL (Persistencia)");
        System.out.println("--------------------------------------------------\n");

        // IMPORTANTE: Inicio el servidor TCP en un hilo NUEVO
        // Si no hago esto, el bucle 'while(true)' bloquearía a Spring Boot y la web no funcionaría
        new Thread(this::iniciarServidorTCP).start();
    }

    /**
     * Esta es la lógica del servidor TCP (Cumpliendo el requisito de Sockets Orientados a Conexión)
     */
    private void iniciarServidorTCP() {
        try (ServerSocket serverSocket = new ServerSocket(TCP_PORT)) {
            System.out.println(" >> SERVIDOR TCP: Escuchando en puerto " + TCP_PORT);
            System.out.println(" >> Esperando jugadores para la sesión de aprendizaje...\n");

            while (true) {
                // El servidor se queda "dormido" aquí esperando a que un alumno se conecte
                Socket clientSocket = serverSocket.accept();
                System.out.println(" >> NUEVA CONEXIÓN!!! Cliente: " + clientSocket.getInetAddress());

                // REQUISITO DE MULTITHREADING:
                // Por cada alumno que llega, creo un objeto 'ClientHandler' y lo lanzo en su propio hilo
                // Así, si un alumno tarda en responder, no congela a los demás
                ClientHandler handler = new ClientHandler(clientSocket);
                new Thread(handler).start(); 
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}