package com.example.proyectohorario_1.DTO;

import lombok.Data;

import java.sql.Time;
import java.util.List;

    @Data
    public class AsignacionProblema {
        private String cursoCodigo;
        private String cursoNombre; // Agrega o verifica este campo
        private Long catedraticoDpi;
        private String nombreCatedratico; // Agrega o verifica este campo
        private String aulaNombre;
        private String nombreSeccion;
        private String diaSemana;
        private Time horaInicio;
        private Time horaFin;
        private List<String> nombresEstudiantes;
    }