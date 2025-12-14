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

    // Metodo para borrar una pregunta si me equivoque
    public boolean borrarPregunta(Long id) {
        if (preguntaRepository.existsById(id)) {
            preguntaRepository.deleteById(id);
            return true; // Se logro borrar
        }
        return false; // No existe el id de la pregunta que se quiere borrrar
    }

    // Método para editar una pregunta existente
    public Pregunta editarPregunta(Long id, Pregunta nuevosDatos) {
        // Busco si existe la pregunta con ese id
        return preguntaRepository.findById(id).map(preguntaExistente -> {
            // Si existe, actualizo sus campos con los nuevos datos
            preguntaExistente.setEnunciado(nuevosDatos.getEnunciado());
            preguntaExistente.setOpcionA(nuevosDatos.getOpcionA());
            preguntaExistente.setOpcionB(nuevosDatos.getOpcionB());
            preguntaExistente.setOpcionC(nuevosDatos.getOpcionC());
            preguntaExistente.setOpcionCorrecta(nuevosDatos.getOpcionCorrecta());
            preguntaExistente.setPuntos(nuevosDatos.getPuntos());
            
            // Guardo los cambios en la base de datos
            return preguntaRepository.save(preguntaExistente);
        }).orElse(null); // Si no existe el is retorno null
    }
}