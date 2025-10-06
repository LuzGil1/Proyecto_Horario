package com.example.proyectohorario_1.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HorarioSeccionDTO {
    private String seccion;
    private String dia;
    private String inicio;
    private String fin;
    private String cursoAsignado;
    private String catedratico;
    private String aula;
}
