package com.cesar.netedu.model;

import jakarta.persistence.*;

/**
 * ENTIDAD PREGUNTA
 * * Aquí defino el modelo de datos para el contenido educativo
 *   decidí estructurarlo de esta manera para cumplir con dos objetivos
 *   almacenar el conocimiento sobre "Redes de Computadora" de forma persistente y
 *   asignar un valor de "puntos" a cada reactivo para la mecánica de gamificación
 */
@Entity
@Table(name = "preguntas")
public class Pregunta {

    // Identificador único autogenerado por la base de datos
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // El texto de la pregunta (Ejejmplo: "¿Cuál es la PDU de la capa 4?")
    @Column(nullable = false)
    private String enunciado;

    // Opciones de respuesta para el formato de opción múltiple
    @Column(nullable = false)
    private String opcionA;

    @Column(nullable = false)
    private String opcionB;

    @Column(nullable = false)
    private String opcionC;

    // Almacena la letra correcta ("A", "B" o "C") para validar la respuesta del alumno automáticamente
    @Column(nullable = false)
    private String opcionCorrecta;

    // Valor numérico para el sistema de puntos (y me gustaría que conforme haya retos más difíciles dan más puntos)
    private int puntos;

    // --- CONSTRUCTORES ---

    // Constructor vacío (Requerido obligatoriamente por JPA)
    public Pregunta() {
    }

    public Pregunta(String enunciado, String opcionA, String opcionB, String opcionC, String opcionCorrecta, int puntos) {
        this.enunciado = enunciado;
        this.opcionA = opcionA;
        this.opcionB = opcionB;
        this.opcionC = opcionC;
        this.opcionCorrecta = opcionCorrecta;
        this.puntos = puntos;
    }

    // --- GETTERS Y SETTERS ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEnunciado() {
        return enunciado;
    }

    public void setEnunciado(String enunciado) {
        this.enunciado = enunciado;
    }

    public String getOpcionA() {
        return opcionA;
    }

    public void setOpcionA(String opcionA) {
        this.opcionA = opcionA;
    }

    public String getOpcionB() {
        return opcionB;
    }

    public void setOpcionB(String opcionB) {
        this.opcionB = opcionB;
    }

    public String getOpcionC() {
        return opcionC;
    }

    public void setOpcionC(String opcionC) {
        this.opcionC = opcionC;
    }

    public String getOpcionCorrecta() {
        return opcionCorrecta;
    }

    public void setOpcionCorrecta(String opcionCorrecta) {
        this.opcionCorrecta = opcionCorrecta;
    }

    public int getPuntos() {
        return puntos;
    }

    public void setPuntos(int puntos) {
        this.puntos = puntos;
    }
}