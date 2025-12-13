package com.cesar.netedu.service;

import com.cesar.netedu.model.Usuario;
import com.cesar.netedu.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.MessageDigest; // Import necesario para el cifrado
import java.security.NoSuchAlgorithmException; // Import para manejar errores de cifrado
import java.util.Optional;

/**
 * SERVICIO DE GESTIÓN DE USUARIOS
 * * Aquí manejo toda la lógica de seguridad y acceso
 * * mis funciones principales son:
 * - autenticar: verificar que usuario y contraseña coincidan (usando cifrado SHA-256)
 * - registrar: crear nuevos alumnos en la base de datos protegiendo sus claves
 * - guardar progreso: actualizar puntajes al terminar la partida
 */
@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // --- MÉTODO PRIVADO AUXILIAR (esto me funciona como una "trituradora") ---
    // este método convierte texto plano "123" en una huella digital única (gracias al hash)
    private String encriptar(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            
            // Convierto los bytes raros a texto hexadecimal legible
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error fatal: No se encontró el algoritmo de seguridad", e);
        }
    }

    // método para validar el login
    public Usuario login(String username, String password) {
        // busco al usuario en la bd por su nombre
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);

        if (usuarioOpt.isPresent()) {
            Usuario u = usuarioOpt.get();
            
            // Primero encripto la contraseña que se ingresa
            String passIngresadaCifrada = encriptar(password);

            // Verifico si la contraseña cifrada coincide con la que tengo guardada
            if (u.getPassword().equals(passIngresadaCifrada)) {
                return u; // Si es así entonces retorno el usuario completo
            }
        }
        return null; // Si no coincide o no existe, retorno null
    }

    // Método para crear nuevos alumnos
    public Usuario registrar(String username, String password) {
        // Primero verifico que no exista ya
        if (usuarioRepository.findByUsername(username).isPresent()) {
            return null; // si ya existe no puedo duplicarlo
        }

        // Antes de guardar, cifro la contraseña para siempre
        String passwordSegura = encriptar(password);

        // Creo la entidad y la guardo (con la clave ya protegida por el cifrado)
        Usuario nuevo = new Usuario(username, passwordSegura, "ESTUDIANTE");
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