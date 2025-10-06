package com.example.proyectohorario_1.model;

import lombok.Data;
import java.time.LocalTime;

@Data
public class HorarioProblema {
    private String dia;
    private LocalTime horaInicio;
    private LocalTime horaFin;
}