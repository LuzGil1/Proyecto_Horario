package com.example.proyectohorario_1.Respositorio;

import com.example.proyectohorario_1.Entidad.Inscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InscripcionRepositorio extends JpaRepository<Inscripcion, Long> {

    // Buscar por estudiante y asignación específica
    Optional<Inscripcion> findByEstudiante_CarneEstudianteAndAsignacion_IdAsignacion(String carneEstudiante, Long idAsignacion);

    // Buscar por estudiante y curso (sin importar sección)
    List<Inscripcion> findByEstudiante_CarneEstudianteAndAsignacion_Curso_IdCurso(String carneEstudiante, Long idCurso);


}
