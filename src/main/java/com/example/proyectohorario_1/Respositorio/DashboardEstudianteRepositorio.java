package com.example.proyectohorario_1.Respositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.proyectohorario_1.Entidad.Inscripcion;

import java.util.List;
public interface DashboardEstudianteRepositorio extends JpaRepository<Inscripcion, Long> {

    @Query(value = """
        select
            c.nombre_curso,
            c.codigo_curso,
            s.nombre_seccion,
            h.dia_semana,
            h.hora_inicio,
            h.hora_fin,
            a.nombre_aula,
            cat.nombre_catedratico
        from inscripciones i
        join asignaciones asig on i.id_asignacion = asig.id_asignacion
        join cursos c on asig.id_curso = c.id_curso
        join secciones s on asig.id_seccion = s.id_seccion
        join horarios h on asig.id_horario = h.id_horario
        join aulas a on asig.id_aula = a.id_aula
        join catedraticos cat on asig.dpi_catedratico = cat.dpi_catedratico
        where i.carne_estudiante = :carne
          and i.estado = 'INSCRITO'
        order by h.dia_semana, h.hora_inicio
        """, nativeQuery = true)
    List<Object[]> obtenerHorarioPorEstudiante(@Param("carne") String carne);
}
