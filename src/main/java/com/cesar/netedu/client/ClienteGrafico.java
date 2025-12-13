package com.cesar.netedu.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClienteGrafico extends JFrame {

    // Configuración de red
    private static final String SERVER_IP = "192.168.1.67";
    private static final int SERVER_PORT = 5000;

    // Componentes visuales
    private JTextArea areaChat;
    private JTextField campoTexto;
    private PrintWriter out;

    public ClienteGrafico() {
        super("NetEdu - Estudiante");
        configurarVentana();
        conectarAlServidor();
    }

    private void configurarVentana() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 600);
        setLayout(new BorderLayout());

        // Área de chat (por el momento solo lectura)
        areaChat = new JTextArea();
        areaChat.setEditable(false);
        areaChat.setFont(new Font("Monospaced", Font.PLAIN, 14));
        areaChat.setBackground(new Color(30, 30, 30));
        areaChat.setForeground(Color.GREEN);
        add(new JScrollPane(areaChat), BorderLayout.CENTER);

        // Panel inferior para escribir
        JPanel panelInferior = new JPanel(new BorderLayout());
        campoTexto = new JTextField();
        campoTexto.setFont(new Font("SansSerif", Font.PLAIN, 16));
        
        JButton botonEnviar = new JButton("ENVIAR");
        
        panelInferior.add(campoTexto, BorderLayout.CENTER);
        panelInferior.add(botonEnviar, BorderLayout.EAST);
        add(panelInferior, BorderLayout.SOUTH);

        // Acción al presionar Enter o click en Enviar
        ActionListener enviarListener = e -> enviarMensaje();
        campoTexto.addActionListener(enviarListener);
        botonEnviar.addActionListener(enviarListener);
    }

    private void conectarAlServidor() {
        try {
            // Conexión TCP
            Socket socket = new Socket(SERVER_IP, SERVER_PORT);
            mensajeSistema(">> Conectado al servidor " + SERVER_IP);

            // Flujo de salida (Envió datos)
            out = new PrintWriter(socket.getOutputStream(), true);

            // Flujo de entrada (Escuchar datos)
            // Necesito un hilo para que la ventana no se congele esperando mensajes
            new Thread(() -> escucharServidor(socket)).start();

        } catch (IOException e) {
            mensajeSistema(">> ERROR: No se pudo conectar al servidor.");
            mensajeSistema(">> Asegurate que la IP sea correcta y el servidor esté corriendo.");
        }
    }

    private void escucharServidor(Socket socket) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String mensajeEntrante;
            
            // Bucle infinito leyendo lo que dice el servidor
            while ((mensajeEntrante = in.readLine()) != null) {
                mensajeServidor(mensajeEntrante);
            }
        } catch (IOException e) {
            mensajeSistema(">> Desconectado del servidor.");
        }
    }

    private void enviarMensaje() {
        String texto = campoTexto.getText().trim();
        if (!texto.isEmpty() && out != null) {
            out.println(texto); 
            mensajeUsuario(texto); 
            campoTexto.setText("");
        }
    }

    // --- Métodos para imprimir en la pantalla ---
    
    private void mensajeServidor(String msg) {
        SwingUtilities.invokeLater(() -> areaChat.append(msg + "\n"));
    }

    private void mensajeUsuario(String msg) {
        SwingUtilities.invokeLater(() -> areaChat.append("YO: " + msg + "\n"));
    }

    private void mensajeSistema(String msg) {
        SwingUtilities.invokeLater(() -> areaChat.append("SYSTEM: " + msg + "\n"));
    }

    public static void main(String[] args) {
        // Iniciar la ventana
        SwingUtilities.invokeLater(() -> new ClienteGrafico().setVisible(true));
    }
}