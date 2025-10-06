package com.example.proyectohorario_1.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO para distribución por género en aulas
@Data
@NoArgsConstructor
@AllArgsConstructor
class EstadisticaAulaDTO {
    private String nombreAula;
    private Long totalEstudiantes;
    private Long totalHombres;
    private Long totalMujeres;
    private Double porcentajeHombres;
    private Double porcentajeMujeres;
}