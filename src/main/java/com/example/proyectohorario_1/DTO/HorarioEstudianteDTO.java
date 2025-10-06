package com.example.proyectohorario_1.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalTime;

@Data
@AllArgsConstructor
public class HorarioEstudianteDTO {
    private String nombreCurso;
    private String codigoCurso;
    private String nombreSeccion;
    private String diaSemana;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String nombreAula;
    private String nombreCatedratico;
}