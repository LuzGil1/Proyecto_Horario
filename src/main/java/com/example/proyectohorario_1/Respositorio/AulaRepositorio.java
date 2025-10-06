package com.example.proyectohorario_1.Respositorio;

import com.example.proyectohorario_1.Entidad.Aula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AulaRepositorio extends JpaRepository<Aula, Integer> {
    // Método para buscar un aula por su nombre

    Optional<Aula> findByNombreAula(String nombreAula);
}