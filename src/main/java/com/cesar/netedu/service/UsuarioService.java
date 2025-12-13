package com.cesar.netedu.service;

import com.cesar.netedu.model.Usuario;
import com.cesar.netedu.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

/**
 * SERVICIO DE GESTIÓN DE USUARIOS
 * * Aquí manejo toda la lógica de seguridad y acceso
 * * mis funciones principales son:
 * - autenticar: verificar que usuario y contraseña coincidan
 * - registrar: crear nuevos alumnos en la base de datos
 * - guardar progreso: actualizar puntajes al terminar la partida
 */
@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // método para validar el login
    public Usuario login(String username, String password) {
        // busco al usuario en la bd por su nombre
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);

        if (usuarioOpt.isPresent()) {
            Usuario u = usuarioOpt.get();
            // Verifico si la contraseña coincide 
            if (u.getPassword().equals(password)) {
                return u; // Si es así entonces retorno el usuario completo
            }
        }
        return null; // fallo al autenticar
    }

    // Método para crear nuevos alumnos
    public Usuario registrar(String username, String password) {
        // Primero verifico que no exista ya
        if (usuarioRepository.findByUsername(username).isPresent()) {
            return null; // si ya existe no puedo duplicarlo
        }
        // Creo la entidad y la guardo
        Usuario nuevo = new Usuario(username, password, "ESTUDIANTE");
        return usuarioRepository.save(nuevo);
    }
    
    // Método para sumar puntos (esto es necesario para la gamificacion)
    public void sumarPuntos(Usuario usuario, int puntos) {
        // Actualizo el objeto en memoria
        usuario.setPuntajeTotal(usuario.getPuntajeTotal() + puntos);
        // Lo guardo en postgres para que no se pierda
        usuarioRepository.save(usuario);
    }
}