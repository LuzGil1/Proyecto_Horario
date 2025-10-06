package com.example.proyectohorario_1.Controlador;

import com.example.proyectohorario_1.DTO.HorarioEstudianteDTO;
import com.example.proyectohorario_1.Entidad.Usuario;
import com.example.proyectohorario_1.Respositorio.EstudianteRepositorio;

import com.example.proyectohorario_1.Servicio.DashboardEstudianteService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class DashboardEstudianteController {

    private final EstudianteRepositorio estudianteRepositorio;
    private final DashboardEstudianteService dashboardEstudianteService;

    @GetMapping("/dashboard-estudiante")
    public String dashboardEstudiante(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            return "redirect:/login";
        }

        // Obtener carne del estudiante logueado
        String carneEstudiante = estudianteRepositorio.findCarneByUsuarioId(usuario.getId());
        System.out.println(">>> Carne estudiante logueado: " + carneEstudiante);

        // Lista de clases
        List<HorarioEstudianteDTO> horario = dashboardEstudianteService.obtenerHorario(carneEstudiante);

        // Convertir lista a mapa agrupado por día y hora
        Map<String, Map<String, HorarioEstudianteDTO>> horarioMap = horario.stream()
                .collect(Collectors.groupingBy(
                        HorarioEstudianteDTO::getDiaSemana,
                        Collectors.toMap(
                                h -> h.getHoraInicio().toString().substring(0, 5), // ej: "07:00"
                                h -> h,
                                (h1, h2) -> h1 // en caso de choque, se queda el primero
                        )
                ));

        model.addAttribute("usuarioNombre", usuario.getNombreUsuario());
        model.addAttribute("horarioMap", horarioMap);

        return "dashboard-estudiante";
    }

}
