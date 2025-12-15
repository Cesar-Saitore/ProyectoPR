package com.cesar.netedu.controller;

import com.cesar.netedu.model.Pregunta;
import com.cesar.netedu.model.Usuario;
import com.cesar.netedu.service.JuegoService;
import com.cesar.netedu.repository.PreguntaRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/web/admin")
public class AdminWebController {

    @Autowired
    private PreguntaRepository preguntaRepository; // Para listar y guardar

    @Autowired
    private JuegoService juegoService; // Para la lógica de editar/borrar

    // MÉTODO AUXILIAR: Verificar si es admin
    private boolean esAdmin(HttpSession session) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        return u != null && "ADMIN".equals(u.getRol());
    }

    // 1. VER LISTA DE PREGUNTAS (El Panel Principal)
    @GetMapping("/panel")
    public String verPanel(HttpSession session, Model model) {
        if (!esAdmin(session)) return "redirect:/web/login"; // Seguridad

        model.addAttribute("preguntas", preguntaRepository.findAll());
        return "admin_panel"; // Busca admin_panel.html
    }

    // 2. FORMULARIO PARA CREAR NUEVA
    @GetMapping("/nueva")
    public String mostrarFormularioNueva(HttpSession session, Model model) {
        if (!esAdmin(session)) return "redirect:/web/login";

        model.addAttribute("pregunta", new Pregunta()); // Objeto vacío
        model.addAttribute("titulo", "Nueva Pregunta");
        return "admin_form"; // Busca admin_form.html
    }

    // 3. FORMULARIO PARA EDITAR EXISTENTE
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, HttpSession session, Model model) {
        if (!esAdmin(session)) return "redirect:/web/login";

        Pregunta p = preguntaRepository.findById(id).orElse(null);
        if (p == null) return "redirect:/web/admin/panel";

        model.addAttribute("pregunta", p);
        model.addAttribute("titulo", "Editar Pregunta #" + id);
        return "admin_form"; // Reutilizamos el mismo formulario
    }

    // 4. GUARDAR CAMBIOS (Sirve para CREAR y EDITAR)
    @PostMapping("/guardar")
    public String guardarPregunta(@ModelAttribute Pregunta pregunta, HttpSession session) {
        if (!esAdmin(session)) return "redirect:/web/login";

        // save() en JPA es inteligente: si tiene ID actualiza, si no tiene ID crea.
        preguntaRepository.save(pregunta); 
        return "redirect:/web/admin/panel";
    }

    // 5. BORRAR PREGUNTA
    @GetMapping("/borrar/{id}")
    public String borrarPregunta(@PathVariable Long id, HttpSession session) {
        if (!esAdmin(session)) return "redirect:/web/login";

        juegoService.borrarPregunta(id);
        return "redirect:/web/admin/panel";
    }
}