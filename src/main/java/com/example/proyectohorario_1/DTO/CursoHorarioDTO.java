package com.example.proyectohorario_1.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CursoHorarioDTO {
    private String nombreCurso;
    private String diaSemana;
    private String horaInicio;
    private String horaFin;
    private String nombreCatedratico;
}