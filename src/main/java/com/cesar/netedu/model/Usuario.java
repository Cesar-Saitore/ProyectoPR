package com.cesar.netedu.model;

import jakarta.persistence.*;

/**
 * ENTIDAD USUARIO
 * * Uso la anotación @Entity para que JPA convierta esta clase automáticamente
 *   en una tabla 'usuarios' dentro de Postgres
 *   esto me facilita la gestión de datos sin escribir SQL manual constantemente
 */
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Llave primaria autogenerada

    @Column(nullable = false, unique = true)
    private String username; // Identificador del alumno

    @Column(nullable = false)
    private String password; 

    // Rol para distinguir entre Profesor (Admin) y Alumno
    private String rol; 

    // Campo clave para la Gamificación: Acumula el progreso del alumno
    private int puntajeTotal; 

    // Constructor vacío requerido por JPA
    public Usuario() {}

    public Usuario(String username, String password, String rol) {
        this.username = username;
        this.password = password;
        this.rol = rol;
        this.puntajeTotal = 0; // Todos empiezan con 0 puntos (esto es importante por la forma en la que lo quiero desarrollar)
    }

    // --- Getters y Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public int getPuntajeTotal() { return puntajeTotal; }
    public void setPuntajeTotal(int puntajeTotal) { this.puntajeTotal = puntajeTotal; }
}