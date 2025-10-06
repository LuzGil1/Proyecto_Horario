package com.example.proyectohorario_1.Servicio;


import com.example.proyectohorario_1.DTO.HorarioCatedraticoConsultaDTO;
import com.example.proyectohorario_1.DTO.HorarioCatedraticoDTO;
import com.example.proyectohorario_1.DTO.HorarioSeccionDTO;
import com.example.proyectohorario_1.Respositorio.AsignacionRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HorarioConsultaService {

    private final AsignacionRepositorio asignacionRepositorio;

    @Transactional(readOnly = true)
    public List<HorarioCatedraticoConsultaDTO> obtenerHorariosCatedraticos() {
        List<Object[]> resultados = asignacionRepositorio.obtenerHorariosCatedraticosSql();

        return resultados.stream()
                .map(row -> new HorarioCatedraticoConsultaDTO(
                        (String) row[0],  // diaSemana
                        (String) row[1],  // horaInicio
                        (String) row[2],  // horaFin
                        (String) row[3],  // curso
                        (String) row[4],  // seccion
                        (String) row[5]   // catedratico
                ))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<HorarioSeccionDTO> obtenerHorariosSecciones() {
        List<Object[]> resultados = asignacionRepositorio.obtenerHorariosSeccionesSql();

        return resultados.stream()
                .map(row -> new HorarioSeccionDTO(
                        (String) row[0],  // seccion
                        (String) row[1],  // dia
                        (String) row[2],  // inicio
                        (String) row[3],  // fin
                        (String) row[4],  // cursoAsignado
                        (String) row[5],  // catedratico
                        (String) row[6]   // aula
                ))
                .collect(Collectors.toList());
    }
}