package com.example.proyectohorario_1.DTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HorarioCatedraticoConsultaDTO {
    private String diaSemana;
    private String horaInicio;
    private String horaFin;
    private String curso;
    private String seccion;
    private String catedratico;
}
