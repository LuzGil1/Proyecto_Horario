package com.example.proyectohorario_1.Entidad;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "cursos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_curso")
    private Integer idCurso;

    @Column(name = "nombre_curso", nullable = false, length = 100)
    private String nombreCurso;

    @Column(name = "codigo_curso", nullable = false, unique = true, length = 10)
    private String codigoCurso;

    @Column(name = "tipo", length = 50)
    private String tipo;
}