package com.example.proyectohorario_1.Servicio;

import com.example.proyectohorario_1.DTO.HorarioCatedraticoDTO;
import com.example.proyectohorario_1.Respositorio.DashboardCatedraticoRepositorio;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Time;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardCatedraticoService {

    private final DashboardCatedraticoRepositorio dashboardCatedraticoRepositorio;

    public List<HorarioCatedraticoDTO> obtenerHorario(Long dpiCatedratico) {
        List<Object[]> rows = dashboardCatedraticoRepositorio.obtenerHorarioPorCatedratico(dpiCatedratico);

        return rows.stream().map(row -> new HorarioCatedraticoDTO(
                (String) row[0],              // nombreCurso
                (String) row[1],              // nombreSeccion
                (String) row[2],              // diaSemana
                ((Time) row[3]).toLocalTime(),// horaInicio
                ((Time) row[4]).toLocalTime(),// horaFin
                (String) row[5],              // nombreAula
                ((Number) row[6]).longValue() // estudiantesInscritos
        )).collect(Collectors.toList());
    }
}