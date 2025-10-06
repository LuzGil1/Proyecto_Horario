package com.example.proyectohorario_1.DTO;
import com.example.proyectohorario_1.Servicio.EstadisticasService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstadisticasDTO {
    private EstadisticasService.EstadisticaGeneralDTO general;
    private List<EstadisticasService.EstadisticaAulaDTO> distribucionPorAula;
}