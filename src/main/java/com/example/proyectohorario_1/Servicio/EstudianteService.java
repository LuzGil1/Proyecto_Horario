package com.example.proyectohorario_1.Servicio;

import com.example.proyectohorario_1.DTO.CursoHorarioDTO;
import com.example.proyectohorario_1.Respositorio.UsuarioRespositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EstudianteService {

    private final UsuarioRespositorio usuarioRespositorio;

    public List<CursoHorarioDTO> obtenerHorario(Long idUsuario) {
        List<Object[]> resultados = usuarioRespositorio.obtenerHorarioPorUsuario(idUsuario);

        return resultados.stream()
                .map(r -> new CursoHorarioDTO(
                        (String) r[0], // nombreCurso
                        (String) r[1], // diaSemana
                        r[2] != null ? r[2].toString() : null, // horaInicio
                        r[3] != null ? r[3].toString() : null, // horaFin
                        (String) r[4]  // nombreCatedratico
                ))
                .collect(Collectors.toList());
    }
}