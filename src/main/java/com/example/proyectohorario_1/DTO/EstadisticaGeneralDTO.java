package com.example.proyectohorario_1.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
class EstadisticaGeneralDTO {
    private Long totalEstudiantes;
    private Long totalHombres;
    private Long totalMujeres;
    private Long totalCatedraticos;
    private Long totalSecciones;
    private Long totalCursos;
    private Long totalAulas;
    private Double porcentajeHombres;
    private Double porcentajeMujeres;
}
