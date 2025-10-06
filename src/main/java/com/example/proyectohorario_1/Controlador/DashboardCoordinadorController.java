package com.example.proyectohorario_1.Controlador;

import com.example.proyectohorario_1.DTO.EstadisticasDTO;
import com.example.proyectohorario_1.Entidad.Usuario;
import com.example.proyectohorario_1.Servicio.EstadisticasService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
public class DashboardCoordinadorController {

    private final EstadisticasService estadisticasService;

    @GetMapping("/dashboard-coordinador")
    public String dashboardCoordinador(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            return "redirect:/login";
        }
        model.addAttribute("usuarioNombre", usuario.getNombreUsuario());

        return "dashboard-coordinador";
    }

    @GetMapping("/api/estadisticas")
    @ResponseBody
    public EstadisticasDTO obtenerEstadisticas() {
        return estadisticasService.obtenerEstadisticas();
    }
}
