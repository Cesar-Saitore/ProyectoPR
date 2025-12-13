package com.cesar.netedu.service;

import org.springframework.stereotype.Service;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * SERVICIO DE NOTIFICACIONES UDP
 * * Me ayuda a cumplir con el requisito de usar sockets UDP para notificaciones rápidas
 * * a diferencia de TCP aquí no establecemos conexión solo mandamos el paquete
 * * a la dirección IP del usuario y esperamos que lo atrape
 */
@Service
public class UDPService {

    private DatagramSocket socket;

    public UDPService() {
        try {
            // Iniciamos el socket UDP en un puerto aleatorio disponible para enviar
            this.socket = new DatagramSocket();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Método para enviar una notificación rápida a un usuario específico
     * @param direccionIP La IP del alumno (se saca del socket TCP)
     * @param mensaje El texto a enviar 
     */
    public void enviarMensaje(InetAddress direccionIP, String mensaje) {
        try {
            byte[] buffer = mensaje.getBytes();
            
            // Puerto destino: Usaremos el 9876
            // el cliente (celular/PC) debe estar escuchando en este puerto UDP
            int puertoDestino = 9876; 

            // Empaquetamos los datos (Carta, Longitud, Destinatario, Puerto)
            DatagramPacket paquete = new DatagramPacket(
                buffer, 
                buffer.length, 
                direccionIP, 
                puertoDestino
            );

            // Lo mandamos
            socket.send(paquete);
            System.out.println(" >> UDP enviado a " + direccionIP + ": " + mensaje);

        } catch (Exception e) {
            System.err.println("Error enviando UDP: " + e.getMessage());
        }
    }
}