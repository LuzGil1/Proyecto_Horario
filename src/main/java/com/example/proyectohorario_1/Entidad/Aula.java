package com.example.proyectohorario_1.Entidad;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "aulas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Aula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_aula")
    private Integer idAula;

    @Column(name = "nombre_aula", nullable = false, unique = true, length = 50)
    private String nombreAula;

    @Column(name = "capacidad")
    private Integer capacidad;
}
