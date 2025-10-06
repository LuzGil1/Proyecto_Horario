package com.example.proyectohorario_1.DTO;


import java.time.LocalTime;

public class HorarioCatedraticoDTO {
    private String nombreCurso;
    private String nombreSeccion;
    private String diaSemana;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String nombreAula;
    private Long estudiantesInscritos;

    public HorarioCatedraticoDTO(String nombreCurso, String nombreSeccion, String diaSemana,
                                 LocalTime horaInicio, LocalTime horaFin,
                                 String nombreAula, Long estudiantesInscritos) {
        this.nombreCurso = nombreCurso;
        this.nombreSeccion = nombreSeccion;
        this.diaSemana = diaSemana;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.nombreAula = nombreAula;
        this.estudiantesInscritos = estudiantesInscritos;
    }

    // getters
    public String getNombreCurso() { return nombreCurso; }
    public String getNombreSeccion() { return nombreSeccion; }
    public String getDiaSemana() { return diaSemana; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public LocalTime getHoraFin() { return horaFin; }
    public String getNombreAula() { return nombreAula; }
    public Long getEstudiantesInscritos() { return estudiantesInscritos; }
}