package com.example.proyectohorario_1.Respositorio;

import com.example.proyectohorario_1.DTO.HorarioCatedraticoDTO;
import com.example.proyectohorario_1.DTO.HorarioSeccionDTO;
import com.example.proyectohorario_1.Entidad.*;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface AsignacionRepositorio extends JpaRepository<Asignacion, Long> {



    @Query(value = """
    SELECT DISTINCT
        H.dia_semana,
        TO_CHAR(H.hora_inicio, 'HH24:MI'),
        TO_CHAR(H.hora_fin, 'HH24:MI'),
        C.nombre_curso,
        S.nombre_seccion,
        T.nombre_catedratico
    FROM asignaciones A
    JOIN horarios H ON A.id_horario = H.id_horario
    JOIN cursos C ON A.id_curso = C.id_curso
    JOIN secciones S ON A.id_seccion = S.id_seccion
    JOIN catedraticos T ON A.dpi_catedratico = T.dpi_catedratico
    WHERE EXISTS (
        SELECT 1
        FROM inscripciones I
        WHERE I.id_asignacion = A.id_asignacion
    )
    ORDER BY H.dia_semana, TO_CHAR(H.hora_inicio, 'HH24:MI'), T.nombre_catedratico
    """, nativeQuery = true)
    List<Object[]> obtenerHorariosCatedraticosSql();

    @Query(value = """
    SELECT
        S.nombre_seccion,
        H.dia_semana,
        TO_CHAR(H.hora_inicio, 'HH24:MI'),
        TO_CHAR(H.hora_fin, 'HH24:MI'),
        C.nombre_curso,
        T.nombre_catedratico,
        AUL.nombre_aula
    FROM asignaciones ASIG
    JOIN secciones S ON ASIG.id_seccion = S.id_seccion
    JOIN horarios H ON ASIG.id_horario = H.id_horario
    JOIN cursos C ON ASIG.id_curso = C.id_curso
    JOIN catedraticos T ON ASIG.dpi_catedratico = T.dpi_catedratico
    JOIN aulas AUL ON ASIG.id_aula = AUL.id_aula
    WHERE EXISTS (
        SELECT 1
        FROM inscripciones I
        WHERE I.id_asignacion = ASIG.id_asignacion
    )
    ORDER BY S.nombre_seccion, H.dia_semana, H.hora_inicio
    """, nativeQuery = true)
    List<Object[]> obtenerHorariosSeccionesSql();


}