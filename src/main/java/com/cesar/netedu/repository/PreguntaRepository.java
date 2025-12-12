package com.cesar.netedu.repository;

import com.cesar.netedu.model.Pregunta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * REPOSITORIO DE PREGUNTAS
 * * Esta interfaz es el puente directo con la tabla 'preguntas' en Postgres
 *   al extender de 'JpaRepository', tengo métodos como:
 * - save() -> Guardar una pregunta
 * - findAll() -> Obtener todas las preguntas
 * - findById() -> Buscar una específica
 * * no necesito escribir SQL manual, Spring lo genera por mi
 */
@Repository
public interface PreguntaRepository extends JpaRepository<Pregunta, Long> {
    // Aquí puedo definir a futuro búsquedas personalizadas
    // como por ejemplo: List<Pregunta> findByPuntosGreaterThan(int puntos);
}