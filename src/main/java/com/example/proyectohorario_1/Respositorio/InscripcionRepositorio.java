package com.example.proyectohorario_1.Respositorio;

import com.example.proyectohorario_1.Entidad.Inscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InscripcionRepositorio extends JpaRepository<Inscripcion, Long> {

    // Buscar por estudiante y asignación específica
    Optional<Inscripcion> findByEstudiante_CarneEstudianteAndAsignacion_IdAsignacion(
            String carneEstudiante, Long idAsignacion);

    // Buscar por estudiante, curso y sección
    Optional<Inscripcion> findByEstudiante_CarneEstudianteAndAsignacion_Curso_IdCursoAndAsignacion_Seccion_IdSeccion(
            String carneEstudiante, Long idCurso, Integer idSeccion);

    // Buscar por estudiante y curso (sin importar sección)
    List<Inscripcion> findByEstudiante_CarneEstudianteAndAsignacion_Curso_IdCurso(
            String carneEstudiante, Long idCurso);

    /**
     * Buscar inscripciones por estudiante (carne) y código de curso
     * @param carneEstudiante Carne del estudiante
     * @param codigoCurso Código del curso
     * @return Lista de inscripciones que cumplan con ambos criterios
     */
    List<Inscripcion> findByEstudiante_CarneEstudianteAndAsignacion_Curso_CodigoCurso(
            String carneEstudiante,
            String codigoCurso
    );

    /**
     * Todas las inscripciones de un estudiante
     * @param carneEstudiante Carne del estudiante
     * @return Lista de inscripciones
     */
    List<Inscripcion> findByEstudiante_CarneEstudiante(String carneEstudiante);

    /**
     * Todas las inscripciones por asignación
     * @param idAsignacion ID de la asignación
     * @return Lista de inscripciones
     */
    List<Inscripcion> findByAsignacion_IdAsignacion(Integer idAsignacion);

    /**
     * Opcional: buscar inscripciones activas de un estudiante
     */
    List<Inscripcion> findByEstudiante_CarneEstudianteAndEstado(String carneEstudiante, String estado);

    /**
     * Opcional: buscar inscripciones por curso y sección
     */
    List<Inscripcion> findByAsignacion_Curso_CodigoCursoAndAsignacion_Seccion_IdSeccion(
            String codigoCurso,
            Integer idSeccion
    );

}
