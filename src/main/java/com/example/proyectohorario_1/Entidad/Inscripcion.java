package com.example.proyectohorario_1.Entidad;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;


@Entity
@Table(name = "inscripciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_inscripcion")
    private Long idInscripcion;


    @ManyToOne
    @JoinColumn(name = "carne_estudiante", referencedColumnName = "carne_estudiante")
    private Estudiante estudiante;

    @Column(name = "fecha_inscripcion")
    private Date fechaInscripcion;

    @ManyToOne
    @JoinColumn(name = "id_asignacion", referencedColumnName = "id_asignacion")
    private Asignacion asignacion;

    @Column(name = "estado", length = 20)
    private String estado;

    @Column(name = "nota_final")
    private Integer notaFinal;

}