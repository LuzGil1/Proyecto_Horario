package com.example.proyectohorario_1.Servicio;

import com.example.proyectohorario_1.DTO.EstadisticasDTO;
import com.example.proyectohorario_1.Respositorio.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EstadisticasService {

    private final EstudianteRepositorio estudianteRepositorio;
    private final CatedraticoRepositorio catedraticoRepositorio;
    private final SeccionRepositorio seccionRepositorio;
    private final CursoRepositorio cursoRepositorio;
    private final AulaRepositorio aulaRepositorio;

    @Transactional(readOnly = true)
    public EstadisticasDTO obtenerEstadisticas() {

        // Totales generales - convertir a Long de forma segura

        Long totalEstudiantes = estudianteRepositorio.count(); // Cada .count() hace una consulta a la BD tipo: SELECT COUNT(*) FROM tabla
        // Ejemplo: 75 estudiantes en total

        Long totalHombres = estudianteRepositorio.contarHombres();
        Long totalMujeres = estudianteRepositorio.contarMujeres();
        Long totalCatedraticos = catedraticoRepositorio.count();
        Long totalSecciones = seccionRepositorio.count();
        Long totalCursos = cursoRepositorio.count();
        Long totalAulas = aulaRepositorio.count();

        // Calcula el porcentaje de hombres
        Double porcentajeH = totalEstudiantes > 0 // Evita división por cero
                ? round((totalHombres * 100.0) / totalEstudiantes, 2) // Redondea a 2 decimales
                : 0.0;// Si no hay estudiantes, el porcentaje es 0
        Double porcentajeM = totalEstudiantes > 0
                ? round((totalMujeres * 100.0) / totalEstudiantes, 2)
                : 0.0;

        // Empaca todos los datos generales en un objeto para transportarlos
        EstadisticaGeneralDTO general = new EstadisticaGeneralDTO(
                totalEstudiantes,
                totalHombres,
                totalMujeres,
                totalCatedraticos,
                totalSecciones,
                totalCursos,
                totalAulas,
                porcentajeH,
                porcentajeM
        );


        // Distribución por aula
//        Como rayos funiona esto
//        Esta consulta devuelve una lista de arrays de objetos (Object[])
//        Cada array representa una fila con la siguiente estructura:
//        [0] = nombreAula (String)
//        [1] = totalEstudiantes (Number - puede ser Long, Integer, BigInteger, etc.)
//        [2] = totalHombres (Number)
//        [3] = totalMujeres (Number)
//        Ejemplo de resultado:
//          [
//        ["Salon 1", 25, 13, 12],
//         ["Salon 2", 25, 14, 11],
//         ["Salon 3", 25, 13, 12]

        List<Object[]> resultados = estudianteRepositorio.obtenerDistribucionPorAula(); //
        // Lista para almacenar los DTOs procesados de cada aula
        List<EstadisticaAulaDTO> distribucion = new ArrayList<>();

//        pROCESAR CADA AULA
        // Recorre cada fila del resultado de la consulta
        for (Object[] row : resultados) {
            String nombreAula = (String) row[0]; // Nombre del aula ejemplo: "Salon 1"

            // Conversión segura manejando diferentes tipos numéricos para evitar errores,
//            esto lograria esto "Salon 1": total=25, hombres=13, mujeres=12
            Long total = convertirALong(row[1]);
            Long hombres = convertirALong(row[2]);
            Long mujeres = convertirALong(row[3]);
            // CALCULAR PORCENTAJES PARA ESTA AULA
            Double porcH = total > 0 ? round((hombres * 100.0) / total, 2) : 0.0; // Ejemplo: (13 * 100.0) / 25 = 52.00%
            Double porcM = total > 0 ? round((mujeres * 100.0) / total, 2) : 0.0;

            // CREAR DTO Y AGREGAR A LA LISTA
            distribucion.add(new EstadisticaAulaDTO(
                    nombreAula, total, hombres, mujeres, porcH, porcM
            ));
        }

        return new EstadisticasDTO(general, distribucion); // Empaquetar todo en el DTO final
    }

    // Método auxiliar para convertir diferentes tipos numéricos a Long
    private Long convertirALong(Object value) {
        if (value == null) return 0L;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Integer) return ((Integer) value).longValue();
        if (value instanceof BigInteger) return ((BigInteger) value).longValue();
        if (value instanceof BigDecimal) return ((BigDecimal) value).longValue();
        if (value instanceof Number) return ((Number) value).longValue();
        return 0L;
    }

    private Double round(Double value, int places) {
        if (value == null) return 0.0;
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    // Clases internas para DTOs
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class EstadisticaGeneralDTO {
        private Long totalEstudiantes;
        private Long totalHombres;
        private Long totalMujeres;
        private Long totalCatedraticos;
        private Long totalSecciones;
        private Long totalCursos;
        private Long totalAulas;
        private Double porcentajeHombres;
        private Double porcentajeMujeres;
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class EstadisticaAulaDTO {
        private String nombreAula;
        private Long totalEstudiantes;
        private Long totalHombres;
        private Long totalMujeres;
        private Double porcentajeHombres;
        private Double porcentajeMujeres;
    }
}