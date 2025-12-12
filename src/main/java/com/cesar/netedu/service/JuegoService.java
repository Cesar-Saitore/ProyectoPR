package com.cesar.netedu.service;

import com.cesar.netedu.model.Pregunta;
import com.cesar.netedu.repository.PreguntaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Random;

/**
 * SERVICIO DE LÓGICA DE JUEGO
 * * Esta clase actúa como intermediario para el Servidor TCP no toca la bd directamente,
 *   se lo pide a este servicio (es para mantener todo mas ordenado)
 */
@Service
public class JuegoService {

    @Autowired
    private PreguntaRepository preguntaRepository;

    private Random random = new Random();

    /**
     *  Obtiene una pregunta aleatoria de la base de datos
     *  si no hay preguntas retorna null
     */
    public Pregunta obtenerPreguntaAleatoria() {
        List<Pregunta> todas = preguntaRepository.findAll();
        
        if (todas.isEmpty()) {
            return null;
        }
        
        // Elige una al azar de la lista
        int indice = random.nextInt(todas.size());
        return todas.get(indice);
    }

    /**
     *   Aquí a futuro implementaré lógica en funciones como estas:
     * - validarRespuesta(idPregunta, respuestaUsuario)
     * - calcularPuntos()
     */
}