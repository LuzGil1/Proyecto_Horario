package com.example.proyectohorario_1.Entidad;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "secciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Seccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_seccion")
    private Integer idSeccion;

    @Column(name = "nombre_seccion", nullable = false, unique = true, length = 10)
    private String nombreSeccion;

    @Column(name = "semestre", nullable = false)
    private Integer semestre;

    @Column(name = "cupo_maximo")
    private Integer cupoMaximo;

    @Column(name = "cupo_actual")
    private Integer cupoActual;

    @Column(name = "activa")
    private Boolean activa;
}