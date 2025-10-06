package com.example.proyectohorario_1.Controlador;


import com.example.proyectohorario_1.Entidad.Usuario;
import com.example.proyectohorario_1.Respositorio.UsuarioRespositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;

import java.util.Optional;

@Controller
public class LoginController {

    @Autowired
    private UsuarioRespositorio usuarioRepository;

    @GetMapping("/login")
    public String mostrarLogin() {
        return "login";
    }

    @PostMapping("/login")
    public String procesarLogin(@RequestParam String email,
                                @RequestParam String password,
                                HttpSession session,
                                Model model) {

        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmailAndPassword(email, password);

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();

            session.setAttribute("usuario", usuario);

            // Obtener el nombre del rol desde el objeto Rol
            String rol = usuario.getRol().getNombreRol().trim().toLowerCase();

            switch (rol) {
                case "administrador":
                    return "redirect:/dashboard-admin";
                case "coordinador":
                    return "redirect:/dashboard-coordinador";
                case "catedratico":
                    return "redirect:/dashboard-profesor";
                case "estudiante":
                    return "redirect:/dashboard-estudiante";
                default:
                    model.addAttribute("error", "Rol desconocido: " + rol);
                    return "login";
            }
        } else {
            model.addAttribute("error", "Usuario o contraseña incorrectos.");
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}