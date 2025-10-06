package com.example.proyectohorario_1.Entidad;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.sql.Date;

@Entity
@Table(name = "estudiantes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Estudiante {

    @Id
    @Column(name = "carne_estudiante", nullable = false, length = 15)
    private String carneEstudiante;

    @Column(name = "nombre_estudiante", nullable = false, length = 75)
    private String nombreEstudiante;

    @Column(name = "edad_estudiante")
    private Integer edadEstudiante;

    @Column(name = "carrera", length = 50)
    private String carrera;

    @Column(name = "sexo", length = 1)
    private String sexo;

    @Column(name = "fecha_inscripcion")
    private Date fechaInscripcion;


    @Column(name = "email", nullable = false, length = 50, unique = true)
    private String email;


    @Column(name = "es_primer_ingreso")
    private Boolean esPrimerIngreso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    @JsonIgnore
    private Usuario usuario;
}