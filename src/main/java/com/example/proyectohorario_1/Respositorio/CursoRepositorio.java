package com.example.proyectohorario_1.Respositorio;

import com.example.proyectohorario_1.Entidad.Curso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CursoRepositorio extends JpaRepository<Curso, Integer> {

    Optional<Curso> findByCodigoCurso(String codigoCurso); // Buscar curso por código
}