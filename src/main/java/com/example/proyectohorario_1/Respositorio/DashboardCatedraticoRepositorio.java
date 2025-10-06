package com.example.proyectohorario_1.Respositorio;


import com.example.proyectohorario_1.Entidad.Asignacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DashboardCatedraticoRepositorio extends JpaRepository<Asignacion, Long> {

    @Query(value = """
        select 
            c.nombre_curso as nombreCurso,
            s.nombre_seccion as nombreSeccion,
            h.dia_semana as diaSemana,
            h.hora_inicio as horaInicio,
            h.hora_fin as horaFin,
            a.nombre_aula as nombreAula,
            count(i.carne_estudiante) as estudiantesInscritos
        from asignaciones asig
        join cursos c on asig.id_curso = c.id_curso
        join secciones s on asig.id_seccion = s.id_seccion
        join horarios h on asig.id_horario = h.id_horario
        join aulas a on asig.id_aula = a.id_aula
        left join inscripciones i on asig.id_asignacion = i.id_asignacion
            and i.estado = 'INSCRITO'
        where asig.dpi_catedratico = :dpi
          and asig.estado = 'ACTIVO'
        group by c.nombre_curso, s.nombre_seccion, h.dia_semana, h.hora_inicio, h.hora_fin, a.nombre_aula
        having count(i.carne_estudiante) > 0
        order by h.dia_semana, h.hora_inicio
        """, nativeQuery = true)
    List<Object[]> obtenerHorarioPorCatedratico(@Param("dpi") Long dpi);
}