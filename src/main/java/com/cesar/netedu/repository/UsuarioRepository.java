package com.cesar.netedu.repository;

import com.cesar.netedu.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * REPOSITORIO DE USUARIOS
 * * Gestiona las operaciones de base de datos para los alumnos
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    // Metodo personalizado clave para el Login:
    // "Busca un usuario donde el campo 'username' coincida con el parámetro"
    Optional<Usuario> findByUsername(String username);
}