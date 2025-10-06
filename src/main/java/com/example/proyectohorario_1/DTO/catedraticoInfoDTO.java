package com.example.proyectohorario_1.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class catedraticoInfoDTO {
    private Long dpiCatedratico;
    private String nombreCatedratico;
    private String especialidadCatedratico;
    private String numeroTelefono;
    private String emailCatedratico;
}
