package com.example.proyectohorario_1.Entidad;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "asignaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Asignacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_asignacion")
    private Long idAsignacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_curso")
    private Curso curso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_seccion")
    private Seccion seccion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_aula")
    private Aula aula;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dpi_catedratico")
    private Catedratico catedratico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_horario")
    private Horario horario;

    @Column(name = "semestre")
    private Integer semestre;

    @Column(name = "anio")
    private Integer anio;

    @Column(name = "estado")
    private String estado;

    @Column(name = "fecha_asignacion")
    private Date fechaAsignacion;

    @OneToMany(mappedBy = "asignacion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Inscripcion> inscripciones;
}
