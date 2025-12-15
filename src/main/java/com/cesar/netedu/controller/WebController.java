package com.cesar.netedu.controller;

import com.cesar.netedu.model.Pregunta;
import com.cesar.netedu.model.Usuario;
import com.cesar.netedu.service.JuegoService;
import com.cesar.netedu.service.UsuarioService;
import jakarta.servlet.http.HttpSession; // Importante para manejar sesiones
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class WebController {

    @Autowired
    private JuegoService juegoService;

    @Autowired
    private UsuarioService usuarioService;

    // PANTALLA DE LOGIN
    @GetMapping("/web/login")
    public String mostrarLogin() {
        return "login"; // Busca login.html
    }

    // PROCESAR EL LOGIN
    @PostMapping("/web/login")
    public String procesarLogin(@RequestParam String username, 
                                @RequestParam String password, 
                                HttpSession session, 
                                Model model) {
        
        // Uso el servicio ya existente para validar
        Usuario u = usuarioService.login(username, password);

        if (u != null) {
            // Guardamos al usuario de la sesión
            session.setAttribute("usuarioLogueado", u);
            return "redirect:/web/jugar"; // Lo mandamos al juego
        } else {
            // ERROR
            model.addAttribute("error", "Usuario o contraseña incorrectos");
            return "login"; // Lo devolvemos a intentar
        }
    }

    // Mostrar el formulario de registro
    @GetMapping("/web/registro")
    public String mostrarRegistro() {
        return "registro"; // Busca registro.html
    }

    // Procesar la creación del usuario
    @PostMapping("/web/registro")
    public String procesarRegistro(@RequestParam String username, 
                                   @RequestParam String password, 
                                   Model model) {
        
        // Llamamos al servicio 
        Usuario nuevo = usuarioService.registrar(username, password);

        if (nuevo != null) {
            // si no existe lo mandamos al login con un mensaje de felicidad
            return "redirect:/web/login?registrado=true";
        } else {
            // Si el usuario ya existe
            model.addAttribute("error", "Ese nombre de usuario ya está en uso. Intenta otro.");
            return "registro"; // Se queda en la misma página para corregir
        }
    }

    // PANTALLA DE JUEGO
    @GetMapping("/web/jugar")
    public String mostrarJuego(Model model, HttpSession session) {
        // Verifico si tiene la seison iniciada
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        
        if (usuario == null) {
            return "redirect:/web/login"; // Si no hay usuario, regreso al login
        }

        // Si pasa, cargamos el juego
        Pregunta p = juegoService.obtenerPreguntaAleatoria();
        if (p == null) {
            p = new Pregunta();
            p.setEnunciado("No hay preguntas disponibles.");
        }
        
        model.addAttribute("usuario", usuario); // Pasamos datos del usuario para mostrar su nombre/puntos
        model.addAttribute("pregunta", p);
        return "juego"; 
    }

    // PROCESAR RESPUESTA 
    @PostMapping("/web/responder")
    public String procesarRespuesta(@RequestParam String respuestaUsuario, 
                                    @RequestParam String respuestaCorrecta, 
                                    Model model,
                                    HttpSession session) {
        
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) return "redirect:/web/login";

        boolean acerto = respuestaUsuario.equalsIgnoreCase(respuestaCorrecta);
        String mensaje;
        String resultado;

        if (acerto) {
            resultado = "¡CORRECTO!";
            mensaje = "Has sumado puntos a tu perfil.";
            // Sumamos puntos reales en la BD
            usuarioService.sumarPuntos(usuario, 10); // Sumamos 10 puntos (o lo que valga la pregunta)
        } else {
            resultado = "INCORRECTO";
            mensaje = "Sigue intenado";
        }
        
        model.addAttribute("resultado", resultado);
        model.addAttribute("mensaje", mensaje);
        return "resultado";
    }
    
    // CERRAR SESIÓN
    @GetMapping("/web/logout")
    public String cerrarSesion(HttpSession session) {
        session.invalidate(); // Destruimos la "pulsera"
        return "redirect:/web/login";
    }
}