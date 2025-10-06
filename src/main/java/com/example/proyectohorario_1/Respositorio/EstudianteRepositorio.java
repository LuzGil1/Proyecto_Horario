package com.example.proyectohorario_1.Respositorio;

import com.example.proyectohorario_1.Entidad.Estudiante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EstudianteRepositorio extends JpaRepository<Estudiante, String> {
    @Query("SELECT e.carneEstudiante FROM Estudiante e WHERE e.usuario.id = :usuarioId")
    String findCarneByUsuarioId(@Param("usuarioId") Long usuarioId);

//------------

//    para las estadisticas
@Query("SELECT COUNT(e) FROM Estudiante e WHERE e.sexo = 'M'")
Long contarHombres();

    @Query("SELECT COUNT(e) FROM Estudiante e WHERE e.sexo = 'F'")
    Long contarMujeres();

    // Distribución por género en cada aula
    @Query(value = """
        SELECT 
            a.nombre_aula,
            COUNT(DISTINCT e.carne_estudiante) as total_estudiantes,
            COUNT(DISTINCT CASE WHEN e.sexo = 'M' THEN e.carne_estudiante END) as total_hombres,
            COUNT(DISTINCT CASE WHEN e.sexo = 'F' THEN e.carne_estudiante END) as total_mujeres
        FROM aulas a
        LEFT JOIN asignaciones asig ON asig.id_aula = a.id_aula
        LEFT JOIN inscripciones i ON i.id_asignacion = asig.id_asignacion
        LEFT JOIN estudiantes e ON e.carne_estudiante = i.carne_estudiante
        WHERE e.carne_estudiante IS NOT NULL
        GROUP BY a.id_aula, a.nombre_aula
        ORDER BY a.nombre_aula
        """, nativeQuery = true)
    java.util.List<Object[]> obtenerDistribucionPorAula();


}