package com.example.proyectohorario_1.Controlador;
import com.example.proyectohorario_1.DTO.CatedraticoDTO;
import com.example.proyectohorario_1.DTO.HorarioCatedraticoConsultaDTO;
import com.example.proyectohorario_1.DTO.HorarioSeccionDTO;
import com.example.proyectohorario_1.DTO.catedraticoInfoDTO;
import com.example.proyectohorario_1.Entidad.*;
import com.example.proyectohorario_1.Respositorio.*;
import com.example.proyectohorario_1.Servicio.CatedraticoService;
import com.example.proyectohorario_1.Servicio.HorarioConsultaService;
import com.example.proyectohorario_1.Servicio.HorarioPDFService;
import com.example.proyectohorario_1.Servicio.HorarioService;
import com.example.proyectohorario_1.model.AsignacionProblema;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class DashboardAdminController {

    private final CatedraticoService catedraticoService;
    private final HorarioService horarioService;
    private final HorarioConsultaService horarioConsultaService;
    private final CursoRepositorio cursoRepositorio;
    private final CatedraticoRepositorio catedraticoRepositorio;
    private final EstudianteRepositorio estudianteRepositorio;
    private final HorarioPDFService horarioPDFService;

    @GetMapping("/dashboard-admin")
    public String dashboardAdmin(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) return "redirect:/login";

        model.addAttribute("usuarioNombre", usuario.getNombreUsuario());

        List<CatedraticoDTO> estadoCargaCatedraticos = catedraticoService.obtenerEstadoCarga();
        model.addAttribute("estadoCargaCatedraticos", estadoCargaCatedraticos);

        List<AsignacionProblema> nuevasAsignaciones =
                (List<AsignacionProblema>) session.getAttribute("nuevasAsignaciones");

        model.addAttribute("nuevasAsignaciones", nuevasAsignaciones);

        return "dashboard-admin";
    }

    @PostMapping("/reorganizar-horarios")
    public String reorganizarHorarios(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) return "redirect:/login";

        model.addAttribute("usuarioNombre", usuario.getNombreUsuario());

        try {
            List<AsignacionProblema> nuevasAsignaciones = horarioService.optimizarHorarios();
            session.setAttribute("nuevasAsignaciones", nuevasAsignaciones);
            model.addAttribute("nuevasAsignaciones", nuevasAsignaciones);
            model.addAttribute("mensaje", "✅ Horarios reorganizados exitosamente.");
        } catch (Exception e) {
            System.err.println("❌ ERROR en reorganización: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("mensaje", "❌ Error al reorganizar horarios: " + e.getMessage());
        }

        List<CatedraticoDTO> estadoCargaCatedraticos = catedraticoService.obtenerEstadoCarga();
        model.addAttribute("estadoCargaCatedraticos", estadoCargaCatedraticos);

        return "dashboard-admin";
    }

    @GetMapping("/cursos")
    @ResponseBody
    public List<Curso> obtenerCursos() {
        return cursoRepositorio.findAll();
    }

    @GetMapping("/catedraticos")
    @ResponseBody
    public List<catedraticoInfoDTO> listarCatedraticos() {
        return catedraticoRepositorio.findAll().stream()
                .map(c -> new catedraticoInfoDTO(
                        c.getDpiCatedratico(),
                        c.getNombreCatedratico(),
                        c.getEspecialidadCatedratico(),
                        c.getNumeroTelefono(),
                        c.getEmailCatedratico()
                ))
                .toList();
    }

    @GetMapping("/estudiantes")
    @ResponseBody
    public List<Estudiante> listarEstudiantes() {
        return estudianteRepositorio.findAll();
    }

    // ENDPOINTS PARA HORARIOS
    @GetMapping("/horarios/catedraticos")
    @ResponseBody
    public List<HorarioCatedraticoConsultaDTO> obtenerHorariosCatedraticos() {
        return horarioConsultaService.obtenerHorariosCatedraticos();
    }

    @GetMapping("/horarios/secciones")
    @ResponseBody
    public List<HorarioSeccionDTO> obtenerHorariosSecciones() {
        return horarioConsultaService.obtenerHorariosSecciones();
    }

    @GetMapping("/horarios/pdf/general")
    public ResponseEntity<byte[]> exportarPDFGeneral() throws Exception {
        byte[] pdf = horarioPDFService.generarPDFHorarioGeneral();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "Horario_General_Catedraticos.pdf");

        return ResponseEntity.ok().headers(headers).body(pdf);
    }

    @GetMapping("/horarios/pdf/catedratico/{nombre}")
    public ResponseEntity<byte[]> exportarPDFCatedratico(@PathVariable String nombre) throws Exception {
        byte[] pdf = horarioPDFService.generarPDFHorarioPorCatedratico(nombre);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "Horario_" + nombre.replace(" ", "_") + ".pdf");

        return ResponseEntity.ok().headers(headers).body(pdf);
    }

    @GetMapping("/horarios/pdf/seccion/{nombre}")
    public ResponseEntity<byte[]> exportarPDFSeccion(@PathVariable String nombre) throws Exception {
        byte[] pdf = horarioPDFService.generarPDFHorarioPorSeccion(nombre);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "Horario_Seccion_" + nombre + ".pdf");

        return ResponseEntity.ok().headers(headers).body(pdf);
    }

    @GetMapping("/horarios/pdf/general-secciones")
    public ResponseEntity<byte[]> exportarPDFGeneralSecciones() throws Exception {
        byte[] pdf = horarioPDFService.generarPDFHorarioGeneralSecciones();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "Horario_General_Secciones.pdf");

        return ResponseEntity.ok().headers(headers).body(pdf);
    }
}