package com.example.proyectohorario_1.DTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CatedraticoDTO {
    private String nombreCatedratico;
    private String profesion;
    private String especialidadCatedratico;
    private Long cursosAsignados;
    private Integer cargaMinima;
    private Integer cargaMaxima;
    private String estadoCarga;
    private Integer diferencia;
}
