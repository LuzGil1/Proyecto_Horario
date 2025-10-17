package com.example.proyectohorario_1.Respositorio;

import com.example.proyectohorario_1.Entidad.Seccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SeccionRepositorio extends JpaRepository<Seccion, Integer> {
    Optional<Seccion> findByNombreSeccion(String nombreSeccion); // Buscar sección por nombre

}