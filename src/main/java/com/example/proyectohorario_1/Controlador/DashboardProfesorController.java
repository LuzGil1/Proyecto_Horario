package com.example.proyectohorario_1.Controlador;

import com.example.proyectohorario_1.DTO.HorarioCatedraticoDTO;
import com.example.proyectohorario_1.Entidad.Usuario;
import com.example.proyectohorario_1.Respositorio.CatedraticoRepositorio;
import com.example.proyectohorario_1.Servicio.DashboardCatedraticoService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class DashboardProfesorController {

    private final DashboardCatedraticoService dashboardCatedraticoService;
    private final CatedraticoRepositorio catedraticoRepositorio;

    @GetMapping("/dashboard-profesor")
    public String dashboardProfesor(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            return "redirect:/login";
        }

        Long dpiCatedratico = catedraticoRepositorio.findDpiByUsuarioId(usuario.getId());
        System.out.println(">>> DPI del catedrático logueado: " + dpiCatedratico);

        if (dpiCatedratico == null) {
            model.addAttribute("usuarioNombre", usuario.getNombreUsuario());
            model.addAttribute("horarioMap", Map.of());
            model.addAttribute("mensaje", "No se encontró horario asignado para este usuario.");
            return "dashboard-profesor";
        }

        // 1. Traer horario desde el servicio
        List<HorarioCatedraticoDTO> horario = dashboardCatedraticoService.obtenerHorario(dpiCatedratico);

        // 2. Normalizar formato de horas y agrupar
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        Map<String, Map<String, HorarioCatedraticoDTO>> horarioMap = horario.stream()
                .collect(Collectors.groupingBy(
                        h -> h.getDiaSemana().toUpperCase(), // LUNES, MARTES...
                        Collectors.toMap(
                                h -> h.getHoraInicio().format(formatter), // "07:00"
                                h -> h,
                                (h1, h2) -> h1 // evitar duplicados
                        )
                ));

        // 3. Pasar al modelo
        model.addAttribute("usuarioNombre", usuario.getNombreUsuario());
        model.addAttribute("horarioMap", horarioMap);

        // 4. Lista fija de bloques de horas (en formato HH:mm)
        List<String> bloquesHoras = Arrays.asList("07:00","09:00","11:00","14:00","16:00");
        model.addAttribute("bloquesHoras", bloquesHoras);

        return "dashboard-profesor";
    }
}
