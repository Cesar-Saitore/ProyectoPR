package com.cesar.netedu.client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;

public class ClienteGrafico extends JFrame {

    // --- PALETA DE COLORES ---
    private static final Color COLOR_FONDO = Color.decode("#1e2128");
    private static final Color COLOR_CARD  = Color.decode("#282c34");
    private static final Color COLOR_VERDE = Color.decode("#58cc02");
    private static final Color COLOR_AZUL  = Color.decode("#1cb0f6"); // Para registro y opciones
    private static final Color COLOR_ROJO  = Color.decode("#ff4b4b");
    private static final Color COLOR_TEXTO = Color.WHITE;
    private static final Color COLOR_INPUT = Color.decode("#3c4048"); // Gris para cajas de texto

    // RED
    private static final String SERVER_IP = "localhost";
    private static final int TCP_PORT = 5000;
    private static final int UDP_PORT = 9876;

    // GUI
    private PrintWriter out;
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private String usuarioActual = "";
    
    // Componentes dinámicos
    private JLabel labelUsuario;
    private JLabel labelPuntos;
    private JTextArea displayPregunta;

    public ClienteGrafico() {
        super("NetEdu - Cliente");
        configurarVentana();
        conectarTCP();
        iniciarEscuchaUDP();
    }

    private void configurarVentana() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(550, 850); // MÁS GRANDE para que no se vea compacto
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_FONDO);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        // Agregamos las 3 pantallas
        mainPanel.add(crearPanelLogin(), "LOGIN");
        mainPanel.add(crearPanelRegistro(), "REGISTRO");
        mainPanel.add(crearPanelJuego(), "JUEGO");

        add(mainPanel);
    }

    // ==========================================
    // 1. PANTALLA DE LOGIN (Corregida)
    // ==========================================
    private JPanel crearPanelLogin() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(COLOR_FONDO);
        
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(COLOR_CARD);
        // Padding interno generoso
        card.setBorder(new EmptyBorder(40, 40, 40, 40)); 
        
        // Título
        JLabel titulo = new JLabel("NetEdu");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 36));
        titulo.setForeground(COLOR_VERDE);
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Campos
        JTextField userField = new JTextField();
        estilizarCampo(userField);
        
        JPasswordField passField = new JPasswordField();
        estilizarCampo(passField);

        // Botón Entrar
        JButton btnEntrar = new JButton("INICIAR SESIÓN");
        estilizarBoton(btnEntrar, COLOR_VERDE);
        
        // Botón ir a Registro
        JButton btnIrRegistro = new JButton("Crear Cuenta Nueva");
        estilizarBotonSecundario(btnIrRegistro);

        // Lógica Login
        btnEntrar.addActionListener(e -> {
            String u = userField.getText();
            String p = new String(passField.getPassword());
            if(!u.isEmpty() && !p.isEmpty()){
                usuarioActual = u;
                enviarComando("LOGIN " + u + " " + p);
            } else {
                JOptionPane.showMessageDialog(this, "Escribe usuario y contraseña");
            }
        });

        // Lógica ir a Registro
        btnIrRegistro.addActionListener(e -> cardLayout.show(mainPanel, "REGISTRO"));

        // Armar tarjeta con espaciado
        card.add(titulo);
        card.add(Box.createVerticalStrut(30)); // Espacio
        card.add(crearLabel("Usuario:"));
        card.add(userField);
        card.add(Box.createVerticalStrut(15));
        card.add(crearLabel("Contraseña:"));
        card.add(passField);
        card.add(Box.createVerticalStrut(30));
        card.add(btnEntrar);
        card.add(Box.createVerticalStrut(10));
        card.add(btnIrRegistro);

        panel.add(card);
        return panel;
    }

    // ==========================================
    // 2. PANTALLA DE REGISTRO (Nueva)
    // ==========================================
    private JPanel crearPanelRegistro() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(COLOR_FONDO);
        
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(COLOR_CARD);
        card.setBorder(new EmptyBorder(40, 40, 40, 40));
        
        JLabel titulo = new JLabel("Registro");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titulo.setForeground(COLOR_AZUL); // Azul para diferenciar
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JTextField userReg = new JTextField();
        estilizarCampo(userReg);
        
        JPasswordField passReg = new JPasswordField();
        estilizarCampo(passReg);

        JButton btnRegistrar = new JButton("REGISTRARME");
        estilizarBoton(btnRegistrar, COLOR_AZUL);
        
        JButton btnVolver = new JButton("Volver al Login");
        estilizarBotonSecundario(btnVolver);

        // Lógica Registro
        btnRegistrar.addActionListener(e -> {
            String u = userReg.getText();
            String p = new String(passReg.getPassword());
            if(!u.isEmpty() && !p.isEmpty()){
                enviarComando("REGISTRAR " + u + " " + p);
                JOptionPane.showMessageDialog(this, "Solicitud enviada. Si es correcto, inicia sesión.");
                cardLayout.show(mainPanel, "LOGIN");
            }
        });

        btnVolver.addActionListener(e -> cardLayout.show(mainPanel, "LOGIN"));

        card.add(titulo);
        card.add(Box.createVerticalStrut(30));
        card.add(crearLabel("Elige tu Usuario:"));
        card.add(userReg);
        card.add(Box.createVerticalStrut(15));
        card.add(crearLabel("Elige tu Contraseña:"));
        card.add(passReg);
        card.add(Box.createVerticalStrut(30));
        card.add(btnRegistrar);
        card.add(Box.createVerticalStrut(10));
        card.add(btnVolver);

        panel.add(card);
        return panel;
    }

    // ==========================================
    // 3. PANTALLA DE JUEGO
    // ==========================================
    private JPanel crearPanelJuego() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_FONDO);

        // BARRA SUPERIOR
        JPanel panelTop = new JPanel(new BorderLayout());
        panelTop.setBackground(COLOR_CARD);
        panelTop.setBorder(new EmptyBorder(15, 20, 15, 20));

        JPanel infoUser = new JPanel(new GridLayout(2, 1));
        infoUser.setOpaque(false);
        labelUsuario = new JLabel("Jugador");
        labelUsuario.setFont(new Font("Segoe UI", Font.BOLD, 18));
        labelUsuario.setForeground(Color.WHITE);
        
        labelPuntos = new JLabel("Puntos: 0");
        labelPuntos.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        labelPuntos.setForeground(Color.decode("#ffc107")); // Dorado

        infoUser.add(labelUsuario);
        infoUser.add(labelPuntos);

        JButton btnSalir = new JButton("Cerrar Sesión");
        estilizarBotonSecundario(btnSalir);
        btnSalir.setForeground(COLOR_ROJO);
        btnSalir.addActionListener(e -> logout());

        panelTop.add(infoUser, BorderLayout.WEST);
        panelTop.add(btnSalir, BorderLayout.EAST);

        // CENTRO (PREGUNTA)
        displayPregunta = new JTextArea("Esperando servidor...");
        displayPregunta.setFont(new Font("Segoe UI", Font.BOLD, 20)); // Letra más grande
        displayPregunta.setLineWrap(true);
        displayPregunta.setWrapStyleWord(true);
        displayPregunta.setEditable(false);
        displayPregunta.setBackground(COLOR_CARD);
        displayPregunta.setForeground(Color.WHITE);
        displayPregunta.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.DARK_GRAY, 1),
                new EmptyBorder(30, 30, 30, 30) // Más margen interno
        ));

        JPanel panelPreguntaContainer = new JPanel(new BorderLayout());
        panelPreguntaContainer.setBackground(COLOR_FONDO);
        panelPreguntaContainer.setBorder(new EmptyBorder(20, 20, 20, 20));
        panelPreguntaContainer.add(displayPregunta, BorderLayout.CENTER);

        // ABAJO (BOTONES)
        JPanel panelBotones = new JPanel(new GridLayout(4, 1, 15, 15)); // Más separación entre botones
        panelBotones.setBackground(COLOR_FONDO);
        panelBotones.setBorder(new EmptyBorder(0, 30, 30, 30));

        JButton btnA = new JButton("OPCIÓN A");
        JButton btnB = new JButton("OPCIÓN B");
        JButton btnC = new JButton("OPCIÓN C");
        JButton btnJugar = new JButton("SIGUIENTE PREGUNTA");

        estilizarBoton(btnA, COLOR_AZUL);
        estilizarBoton(btnB, COLOR_AZUL);
        estilizarBoton(btnC, COLOR_AZUL);
        estilizarBoton(btnJugar, COLOR_VERDE);

        btnA.addActionListener(e -> enviarComando("A"));
        btnB.addActionListener(e -> enviarComando("B"));
        btnC.addActionListener(e -> enviarComando("C"));
        btnJugar.addActionListener(e -> enviarComando("JUGAR"));

        panelBotones.add(btnA);
        panelBotones.add(btnB);
        panelBotones.add(btnC);
        panelBotones.add(btnJugar);

        panel.add(panelTop, BorderLayout.NORTH);
        panel.add(panelPreguntaContainer, BorderLayout.CENTER);
        panel.add(panelBotones, BorderLayout.SOUTH);

        return panel;
    }

    // ==========================================
    // MÉTODOS DE RED Y UTILIDADES
    // ==========================================
    private void conectarTCP() {
        new Thread(() -> {
            try {
                Socket socket = new Socket(SERVER_IP, TCP_PORT);
                out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String linea;
                StringBuilder buffer = new StringBuilder();
                while ((linea = in.readLine()) != null) procesarMensaje(linea, buffer);
            } catch (Exception e) {}
        }).start();
    }

    private void iniciarEscuchaUDP() {
        new Thread(() -> {
            try (DatagramSocket udpSocket = new DatagramSocket(UDP_PORT)) {
                byte[] buffer = new byte[1024];
                while (true) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    udpSocket.receive(packet);
                    String msg = new String(packet.getData(), 0, packet.getLength());
                    SwingUtilities.invokeLater(() -> {
                        Toolkit.getDefaultToolkit().beep();
                        JOptionPane.showMessageDialog(this, msg, "¡BONUS UDP!", JOptionPane.INFORMATION_MESSAGE);
                    });
                }
            } catch (Exception e) {}
        }).start();
    }

    private void procesarMensaje(String msg, StringBuilder buffer) {
        SwingUtilities.invokeLater(() -> {
            if (msg.contains("EXITO")) { // Login exitoso
                labelUsuario.setText(usuarioActual);
                cardLayout.show(mainPanel, "JUEGO");
                enviarComando("JUGAR");
            } 
            else if (msg.contains("Puntos:") || msg.contains("puntaje")) {
                 labelPuntos.setText(msg); // Actualizar puntos
            }
            else if (msg.startsWith("--- PREGUNTA") || msg.startsWith("PROBLEMA")) {
                buffer.setLength(0);
                buffer.append(msg).append("\n\n");
            } 
            else if (msg.startsWith("A)") || msg.startsWith("B)") || msg.startsWith("C)")) {
                buffer.append(msg).append("\n");
                displayPregunta.setText(buffer.toString());
            }
            else if (msg.contains("CORRECTO")) {
                displayPregunta.setText("¡CORRECTO! \n\nPresiona 'Siguiente'...");
                displayPregunta.setBackground(new Color(20, 60, 20));
            }
            else if (msg.contains("INCORRECTO")) {
                displayPregunta.setText("¡FALLASTE! \n\n" + msg);
                displayPregunta.setBackground(new Color(60, 20, 20));
            }
            else if (msg.contains("JUGAR")) {
                displayPregunta.setBackground(COLOR_CARD);
            }
        });
    }

    private void enviarComando(String cmd) {
        if (out != null) out.println(cmd);
    }
    
    private void logout() {
        usuarioActual = "";
        cardLayout.show(mainPanel, "LOGIN");
    }

    // --- ESTILIZACIÓN VISUAL CORREGIDA ---
    
    private JLabel crearLabel(String texto) {
        JLabel l = new JLabel(texto);
        l.setForeground(Color.LIGHT_GRAY);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    // CORRECCIÓN: Usamos BasicButtonUI para que Windows no bloquee el color
    private void estilizarBoton(JButton btn, Color colorFondo) {
        btn.setUI(new BasicButtonUI()); // TRUCO PARA QUE PINTE EL COLOR
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setBackground(colorFondo);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(12, 12, 12, 12)); // Botones más gorditos
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Efecto Hover simple
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(colorFondo.brighter()); }
            public void mouseExited(MouseEvent e) { btn.setBackground(colorFondo); }
        });
    }

    private void estilizarBotonSecundario(JButton btn) {
        btn.setUI(new BasicButtonUI());
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setBackground(COLOR_CARD); // Fondo transparente/oscuro
        btn.setForeground(Color.LIGHT_GRAY);
        btn.setFocusPainted(false);
        btn.setBorder(null);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setForeground(Color.WHITE); }
            public void mouseExited(MouseEvent e) { btn.setForeground(Color.LIGHT_GRAY); }
        });
    }

    // CORRECCIÓN: Fondo explícito gris para que se vea la letra blanca
    private void estilizarCampo(JTextField campo) {
        campo.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        campo.setBackground(COLOR_INPUT); 
        campo.setForeground(Color.WHITE);
        campo.setCaretColor(Color.WHITE);
        campo.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Color.GRAY, 1),
            new EmptyBorder(8, 10, 8, 10) // Padding interno del texto
        ));
        campo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClienteGrafico().setVisible(true));
    }
}