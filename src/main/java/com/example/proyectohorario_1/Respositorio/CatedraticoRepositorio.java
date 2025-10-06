package com.example.proyectohorario_1.Respositorio;

import com.example.proyectohorario_1.Entidad.Catedratico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CatedraticoRepositorio extends JpaRepository<Catedratico, Long> {

    @Query(value = "SELECT c.dpi_catedratico FROM catedraticos c WHERE c.id_usuario = :idUsuario", nativeQuery = true)
    Long findDpiByUsuarioId(@Param("idUsuario") Long idUsuario);

    @Query(value = """
            SELECT 
                cat.nombre_catedratico, 
                cat.profesion, 
                cat.especialidad_catedratico, 
                COUNT(DISTINCT CASE 
                    WHEN i.carne_estudiante IS NOT NULL 
                    THEN asig.id_asignacion 
                END) as cursos_asignados, 
                cat.carga_minima, 
                cat.carga_maxima, 
                CASE 
                    WHEN COUNT(DISTINCT CASE WHEN i.carne_estudiante IS NOT NULL THEN asig.id_asignacion END) < cat.carga_minima 
                    THEN 'SUBCARGADO' 
                    WHEN COUNT(DISTINCT CASE WHEN i.carne_estudiante IS NOT NULL THEN asig.id_asignacion END) > cat.carga_maxima 
                    THEN 'SOBRECARGADO' 
                    ELSE 'OPTIMO' 
                END as estado_carga, 
                CASE 
                    WHEN COUNT(DISTINCT CASE WHEN i.carne_estudiante IS NOT NULL THEN asig.id_asignacion END) < cat.carga_minima 
                    THEN cat.carga_minima - COUNT(DISTINCT CASE WHEN i.carne_estudiante IS NOT NULL THEN asig.id_asignacion END) 
                    WHEN COUNT(DISTINCT CASE WHEN i.carne_estudiante IS NOT NULL THEN asig.id_asignacion END) > cat.carga_maxima 
                    THEN COUNT(DISTINCT CASE WHEN i.carne_estudiante IS NOT NULL THEN asig.id_asignacion END) - cat.carga_maxima 
                    ELSE 0 
                END as diferencia 
            FROM catedraticos cat 
            LEFT JOIN asignaciones asig ON cat.dpi_catedratico = asig.dpi_catedratico 
                AND asig.estado = 'ACTIVO' 
            LEFT JOIN inscripciones i ON asig.id_asignacion = i.id_asignacion 
                AND i.estado = 'INSCRITO' 
            GROUP BY 
                cat.dpi_catedratico, 
                cat.nombre_catedratico, 
                cat.profesion, 
                cat.especialidad_catedratico, 
                cat.carga_minima, 
                cat.carga_maxima 
            ORDER BY 
                estado_carga DESC, 
                diferencia DESC
            """, nativeQuery = true)
    List<Object[]> obtenerEstadoCargaCatedraticos();
}