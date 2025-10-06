package com.example.proyectohorario_1.Respositorio;

import com.example.proyectohorario_1.DTO.HorarioCatedraticoDTO;
import com.example.proyectohorario_1.Entidad.Horario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Time;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HorarioRepositorio extends JpaRepository<Horario, Integer> {
    Optional<Horario> findByHoraInicio(LocalTime horaInicio);

}