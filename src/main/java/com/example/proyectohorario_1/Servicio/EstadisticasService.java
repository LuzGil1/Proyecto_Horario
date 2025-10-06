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
        Long totalEstudiantes = estudianteRepositorio.count();
        Long totalHombres = estudianteRepositorio.contarHombres();
        Long totalMujeres = estudianteRepositorio.contarMujeres();
        Long totalCatedraticos = catedraticoRepositorio.count();
        Long totalSecciones = seccionRepositorio.count();
        Long totalCursos = cursoRepositorio.count();
        Long totalAulas = aulaRepositorio.count();

        Double porcentajeH = totalEstudiantes > 0
                ? round((totalHombres * 100.0) / totalEstudiantes, 2)
                : 0.0;
        Double porcentajeM = totalEstudiantes > 0
                ? round((totalMujeres * 100.0) / totalEstudiantes, 2)
                : 0.0;

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
        List<Object[]> resultados = estudianteRepositorio.obtenerDistribucionPorAula();
        List<EstadisticaAulaDTO> distribucion = new ArrayList<>();

        for (Object[] row : resultados) {
            String nombreAula = (String) row[0];

            // Conversión segura manejando diferentes tipos numéricos
            Long total = convertirALong(row[1]);
            Long hombres = convertirALong(row[2]);
            Long mujeres = convertirALong(row[3]);

            Double porcH = total > 0 ? round((hombres * 100.0) / total, 2) : 0.0;
            Double porcM = total > 0 ? round((mujeres * 100.0) / total, 2) : 0.0;

            distribucion.add(new EstadisticaAulaDTO(
                    nombreAula, total, hombres, mujeres, porcH, porcM
            ));
        }

        return new EstadisticasDTO(general, distribucion);
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