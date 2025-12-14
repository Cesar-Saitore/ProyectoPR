package com.cesar.netedu.controller;

import com.cesar.netedu.model.Pregunta;
import com.cesar.netedu.repository.PreguntaRepository;
import com.cesar.netedu.service.JuegoService;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * CONTROLADOR REST PARA ADMINISTRACIÓN
 * * Esta clase expone la "interfaz RMI (RESTful)" que necesito en el proyecto
 *   me permite gestionar el banco de preguntas desde fuera del servidor
 * * Las rutas que tengo son
 * - GET /api/admin/preguntas  -> Ver todas las preguntas
 * - POST /api/admin/preguntas -> Crear una nueva pregunta
 * - DELETE /api/admin/preguntas/{id} -> Borrar una pregunta por su ID
 * - PUT /api/admin/preguntas/{id} -> Editar una pregunta existente
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private PreguntaRepository preguntaRepository;

    @Autowired
    private JuegoService juegoService; // Inyecto el servicio para usar la lógica de editar/borrar

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

    // Requisito utilizar comandos HTTP DELETE
    @DeleteMapping("/preguntas/{id}")
    public ResponseEntity<String> eliminarPregunta(@PathVariable Long id) {
        // Uso el método booleano que creamos en el servicio
        boolean eliminado = juegoService.borrarPregunta(id);
        
        if (eliminado) {
            return ResponseEntity.ok("Pregunta con ID " + id + " eliminada correctamente.");
        } else {
            // Retorno 404 Not Found si el ID no existe
            return ResponseEntity.notFound().build();
        }
    }

    // Requisito utilizar comandos HTTP PUT
    @PutMapping("/preguntas/{id}")
    public ResponseEntity<Pregunta> actualizarPregunta(@PathVariable Long id, @RequestBody Pregunta preguntaActualizada) {
        // Llamo a la lógica de actualización del servicio
        Pregunta p = juegoService.editarPregunta(id, preguntaActualizada);
        
        if (p != null) {
            // Si la encontró y editó, devuelvo la pregunta nueva (Status 200 OK)
            return ResponseEntity.ok(p); 
        } else {
            // Si no existía devuelvo error (Status 404 Not Found)
            return ResponseEntity.notFound().build(); 
        }
    }
}