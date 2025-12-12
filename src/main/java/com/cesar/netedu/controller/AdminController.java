package com.cesar.netedu.controller;

import com.cesar.netedu.model.Pregunta;
import com.cesar.netedu.repository.PreguntaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * CONTROLADOR REST PARA ADMINISTRACIÓN
 * * Esta clase expone la "interfaz RMI (RESTful)" que necesito en el proyecto
 *   me permite gestionar el banco de preguntas desde fuera del servidor
 * * Rutas
 * - GET /api/admin/preguntas  -> Ver todas las preguntas
 * - POST /api/admin/preguntas -> Crear una nueva pregunta
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private PreguntaRepository preguntaRepository;

    // Requisito utilizar comandos HTTP GET
    @GetMapping("/preguntas")
    public List<Pregunta> obtenerTodasLasPreguntas() {
        // Retorna la lista en formato JSON automáticamente
        return preguntaRepository.findAll();
    }

    // Requisito utilizar comandos HTTP POST
    @PostMapping("/preguntas")
    public Pregunta crearPregunta(@RequestBody Pregunta nuevaPregunta) {
        // Recibe un JSON, lo convierte a objeto Java y lo guarda en Postgres
        return preguntaRepository.save(nuevaPregunta);
    }
}