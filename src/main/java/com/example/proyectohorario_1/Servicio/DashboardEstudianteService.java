package com.example.proyectohorario_1.Servicio;


import com.example.proyectohorario_1.DTO.HorarioEstudianteDTO;
import com.example.proyectohorario_1.Respositorio.DashboardEstudianteRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Time;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardEstudianteService {

    private final DashboardEstudianteRepositorio dashboardEstudianteRepositorio;

    public List<HorarioEstudianteDTO> obtenerHorario(String carne) {
        List<Object[]> rows = dashboardEstudianteRepositorio.obtenerHorarioPorEstudiante(carne);

        return rows.stream().map(row -> new HorarioEstudianteDTO(
                (String) row[0],  // nombreCurso
                (String) row[1],  // codigoCurso
                (String) row[2],  // nombreSeccion
                (String) row[3],  // diaSemana
                ((Time) row[4]).toLocalTime(), // horaInicio
                ((Time) row[5]).toLocalTime(), // horaFin
                (String) row[6],  // nombreAula
                (String) row[7]   // nombreCatedratico
        )).toList();
    }
}
