package com.cesar.netedu.controller;

import com.cesar.netedu.model.Pregunta;
import com.cesar.netedu.service.JuegoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller // NOTA: Usamos @Controller, NO @RestController (porque devolvemos HTML visual)
public class WebController {

    @Autowired
    private JuegoService juegoService;

    // 1. Mostrar la pantalla de juego
    @GetMapping("/web/jugar")
    public String mostrarJuego(Model model) {
        // Buscamos una pregunta aleatoria usando tu servicio existente
        Pregunta p = juegoService.obtenerPreguntaAleatoria();
        
        // Se la pasamos al HTML
        model.addAttribute("pregunta", p);
        
        // Esto busca un archivo llamado "juego.html" en la carpeta templates
        return "juego"; 
    }

    // 2. Procesar la respuesta del alumno
    @PostMapping("/web/responder")
    public String procesarRespuesta(@RequestParam String respuestaUsuario, 
                                    @RequestParam String respuestaCorrecta, 
                                    Model model) {
        
        boolean acerto = respuestaUsuario.equalsIgnoreCase(respuestaCorrecta);
        
        model.addAttribute("resultado", acerto ? "CORRECTO!" : "INCORRECTO ");
        model.addAttribute("mensaje", acerto ? "Has ganado puntos." : "Sigue intentando.");
        
        return "resultado"; // Busca el archivo "resultado.html"
    }
}