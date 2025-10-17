package com.example.proyectohorario_1.Servicio;

import com.example.proyectohorario_1.Entidad.*;
import com.example.proyectohorario_1.Respositorio.*;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ImportacionService {

    private final EstudianteRepositorio estudianteRepositorio;
    private final InscripcionRepositorio inscripcionRepositorio;
    private final AsignacionRepositorio asignacionRepositorio;
    private final UsuarioRespositorio usuarioRepositorio;

    /**
     * Importa desde Excel con DOS HOJAS:
     * Hoja 1: "Estudiantes" o "estudiantes"
     * Hoja 2: "Inscripciones" o "inscripciones"
     */
    @Transactional
    public Map<String, Object> importarCompleto(MultipartFile archivo) throws IOException {
        Map<String, Object> resultado = new HashMap<>();

        try (Workbook workbook = new XSSFWorkbook(archivo.getInputStream())) {

            // Verificar que tenga al menos 2 hojas
            if (workbook.getNumberOfSheets() < 2) {
                throw new RuntimeException("El archivo debe contener 2 hojas: 'Estudiantes' e 'Inscripciones'");
            }

            // Importar HOJA 1: Estudiantes
            Sheet sheetEstudiantes = workbook.getSheetAt(0);
            Map<String, Integer> resEstudiantes = procesarHojaEstudiantes(sheetEstudiantes);
            resultado.put("estudiantesImportados", resEstudiantes.get("importados"));
            resultado.put("estudiantesActualizados", resEstudiantes.get("actualizados"));
            resultado.put("estudiantesErrores", resEstudiantes.get("errores"));

            // Importar HOJA 2: Inscripciones
            Sheet sheetInscripciones = workbook.getSheetAt(1);
            Map<String, Integer> resInscripciones = procesarHojaInscripciones(sheetInscripciones);
            resultado.put("inscripcionesImportadas", resInscripciones.get("importados"));
            resultado.put("inscripcionesActualizadas", resInscripciones.get("actualizados"));
            resultado.put("inscripcionesErrores", resInscripciones.get("errores"));
        }

        return resultado;
    }

    /**
     * Procesa la hoja de ESTUDIANTES
     * Columnas: carne_estudiante, nombre_estudiante, edad_estudiante, fecha_inscripcion,
     *           sexo, email, carrera, es_primer_ingreso, id_usuario
     */
    private Map<String, Integer> procesarHojaEstudiantes(Sheet sheet) {
        Map<String, Integer> resultado = new HashMap<>();
        int importados = 0;
        int actualizados = 0;
        int errores = 0;

        System.out.println("\n=== PROCESANDO HOJA: ESTUDIANTES ===");

        // Saltar la fila de encabezados (fila 0)
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            try {
                String carne = getCellValueAsString(row.getCell(0));
                if (carne == null || carne.trim().isEmpty()) {
                    System.out.println("Fila " + (i+1) + ": Carné vacío, saltando...");
                    errores++;
                    continue;
                }

                //Leer datos de la fila
                String nombre = getCellValueAsString(row.getCell(1));
                Integer edad = getCellValueAsInteger(row.getCell(2));
                Date fechaInscripcion = getCellValueAsDate(row.getCell(3));
                String sexo = getCellValueAsString(row.getCell(4));
                String email = getCellValueAsString(row.getCell(5));
                String carrera = getCellValueAsString(row.getCell(6));
                Boolean esPrimerIngreso = getCellValueAsBoolean(row.getCell(7));
                Integer idUsuario = getCellValueAsInteger(row.getCell(8));

                // Validaciones básicas
                if (nombre == null || email == null || carrera == null) {
                    System.out.println("Fila " + (i+1) + ": Datos obligatorios faltantes");
                    errores++;
                    continue;
                }

                if (sexo == null || (!sexo.equals("M") && !sexo.equals("F"))) {
                    System.out.println("Fila " + (i+1) + ": Sexo inválido (debe ser M o F)");
                    errores++;
                    continue;
                }

                // Buscar o crear estudiante
                Optional<Estudiante> estudianteOpt = estudianteRepositorio.findById(carne);
                Estudiante estudiante;

                if (estudianteOpt.isPresent()) {
                    // Actualizar estudiante existente
                    estudiante = estudianteOpt.get();
                    estudiante.setNombreEstudiante(nombre);
                    estudiante.setEdadEstudiante(edad);
                    estudiante.setSexo(sexo);
                    estudiante.setEmail(email);
                    estudiante.setCarrera(carrera);
                    estudiante.setEsPrimerIngreso(esPrimerIngreso != null ? esPrimerIngreso : true);

                    if (fechaInscripcion != null) {
                        estudiante.setFechaInscripcion(
                                java.sql.Date.valueOf(fechaInscripcion.toInstant()
                                        .atZone(ZoneId.systemDefault())
                                        .toLocalDate())
                        );
                    }

                    actualizados++;
                    System.out.println("Estudiante actualizado: " + carne);
                } else {
                    // Crear nuevo estudiante
                    estudiante = new Estudiante();
                    estudiante.setCarneEstudiante(carne);
                    estudiante.setNombreEstudiante(nombre);
                    estudiante.setEdadEstudiante(edad);
                    estudiante.setSexo(sexo);
                    estudiante.setEmail(email);
                    estudiante.setCarrera(carrera);
                    estudiante.setEsPrimerIngreso(esPrimerIngreso != null ? esPrimerIngreso : true);

                    if (fechaInscripcion != null) {
                        estudiante.setFechaInscripcion(
                                java.sql.Date.valueOf(fechaInscripcion.toInstant()
                                        .atZone(ZoneId.systemDefault())
                                        .toLocalDate())
                        );
                    } else {
                        estudiante.setFechaInscripcion(java.sql.Date.valueOf(LocalDate.now()));
                    }

                    importados++;
                    System.out.println("✓ Nuevo estudiante creado: " + carne);
                }

                // Asignar usuario si existe
                if (idUsuario != null) {
                    Optional<Usuario> usuario = usuarioRepositorio.findById(Long.valueOf(idUsuario));
                    usuario.ifPresent(estudiante::setUsuario);
                }

                estudianteRepositorio.save(estudiante);

            } catch (Exception e) {
                System.out.println("✗ Error en fila " + (i+1) + ": " + e.getMessage());
                errores++;
            }
        }

        resultado.put("importados", importados);
        resultado.put("actualizados", actualizados);
        resultado.put("errores", errores);

        System.out.println("\n--- RESUMEN ESTUDIANTES ---");
        System.out.println("Importados: " + importados);
        System.out.println("Actualizados: " + actualizados);
        System.out.println("Errores: " + errores);

        return resultado;
    }

    /**
     * Procesa la hoja de INSCRIPCIONES
     * Columnas: carne_estudiante, id_asignacion, fecha_inscripcion, estado, nota_final
     */
    private Map<String, Integer> procesarHojaInscripciones(Sheet sheet) {
        Map<String, Integer> resultado = new HashMap<>();
        int importados = 0;
        int actualizados = 0;
        int errores = 0;

        System.out.println("\n=== PROCESANDO HOJA: INSCRIPCIONES ===");

        // Saltar la fila de encabezados (fila 0)
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            try {
                String carne = getCellValueAsString(row.getCell(0));
                Integer idAsignacion = getCellValueAsInteger(row.getCell(1));
                Date fechaInscripcion = getCellValueAsDate(row.getCell(2));
                String estado = getCellValueAsString(row.getCell(3));
                Double notaFinal = getCellValueAsDouble(row.getCell(4));

                // Validaciones
                if (carne == null || carne.trim().isEmpty()) {
                    System.out.println("Fila " + (i+1) + ": Carné vacío, saltando...");
                    errores++;
                    continue;
                }

                if (idAsignacion == null) {
                    System.out.println("Fila " + (i+1) + ": ID asignación faltante");
                    errores++;
                    continue;
                }

                // Buscar estudiante
                Optional<Estudiante> estudianteOpt = estudianteRepositorio.findById(carne);
                if (!estudianteOpt.isPresent()) {
                    System.out.println("Fila " + (i+1) + ": Estudiante no encontrado: " + carne);
                    errores++;
                    continue;
                }

                // Buscar asignación
                Optional<Asignacion> asignacionOpt = asignacionRepositorio.findById(
                        Long.valueOf(idAsignacion));
                if (!asignacionOpt.isPresent()) {
                    System.out.println("Fila " + (i+1) + ": Asignación no encontrada: " + idAsignacion);
                    errores++;
                    continue;
                }

                Estudiante estudiante = estudianteOpt.get();
                Asignacion asignacion = asignacionOpt.get();

                // Buscar inscripción existente
                Optional<Inscripcion> inscripcionOpt = inscripcionRepositorio
                        .findByEstudiante_CarneEstudianteAndAsignacion_IdAsignacion(
                                carne, Long.valueOf(idAsignacion));

                Inscripcion inscripcion;
                if (inscripcionOpt.isPresent()) {
                    // Actualizar inscripción existente
                    inscripcion = inscripcionOpt.get();
                    inscripcion.setEstado(estado != null ? estado : "INSCRITO");

                    actualizados++;
                    System.out.println("✓ Inscripción actualizada: " + carne + " - Asignación " + idAsignacion);
                } else {
                    // Crear nueva inscripción
                    inscripcion = new Inscripcion();
                    inscripcion.setEstudiante(estudiante);
                    inscripcion.setAsignacion(asignacion);
                    inscripcion.setEstado(estado != null ? estado : "INSCRITO");

                    if (fechaInscripcion != null) {
                        inscripcion.setFechaInscripcion(
                                new java.sql.Timestamp(fechaInscripcion.getTime()));
                    } else {
                        inscripcion.setFechaInscripcion(
                                new java.sql.Timestamp(System.currentTimeMillis()));
                    }



                    importados++;
                    System.out.println("✓ Nueva inscripción creada: " + carne + " - Asignación " + idAsignacion);
                }

                inscripcionRepositorio.save(inscripcion);

            } catch (Exception e) {
                System.out.println("✗ Error en fila " + (i+1) + ": " + e.getMessage());
                e.printStackTrace();
                errores++;
            }
        }

        resultado.put("importados", importados);
        resultado.put("actualizados", actualizados);
        resultado.put("errores", errores);

        System.out.println("\n--- RESUMEN INSCRIPCIONES ---");
        System.out.println("Importadas: " + importados);
        System.out.println("Actualizadas: " + actualizados);
        System.out.println("Errores: " + errores);

        return resultado;
    }

    // ========== MÉTODOS AUXILIARES ==========

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    return sdf.format(cell.getDateCellValue());
                }
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }

    private Integer getCellValueAsInteger(Cell cell) {
        if (cell == null) return null;

        try {
            switch (cell.getCellType()) {
                case NUMERIC:
                    return (int) cell.getNumericCellValue();
                case STRING:
                    String value = cell.getStringCellValue().trim();
                    return value.isEmpty() ? null : Integer.parseInt(value);
                default:
                    return null;
            }
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Double getCellValueAsDouble(Cell cell) {
        if (cell == null) return null;

        try {
            switch (cell.getCellType()) {
                case NUMERIC:
                    return cell.getNumericCellValue();
                case STRING:
                    String value = cell.getStringCellValue().trim();
                    return value.isEmpty() ? null : Double.parseDouble(value);
                default:
                    return null;
            }
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Date getCellValueAsDate(Cell cell) {
        if (cell == null) return null;

        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return cell.getDateCellValue();
            } else if (cell.getCellType() == CellType.STRING) {
                String dateStr = cell.getStringCellValue().trim();
                SimpleDateFormat[] formats = {
                        new SimpleDateFormat("dd/MM/yyyy"),
                        new SimpleDateFormat("d/M/yyyy"),
                        new SimpleDateFormat("yyyy-MM-dd"),
                        new SimpleDateFormat("dd-MM-yyyy")
                };

                for (SimpleDateFormat format : formats) {
                    try {
                        return format.parse(dateStr);
                    } catch (Exception e) {
                        continue;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error parseando fecha: " + e.getMessage());
        }
        return null;
    }

    private Boolean getCellValueAsBoolean(Cell cell) {
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case STRING:
                String value = cell.getStringCellValue().trim().toUpperCase();
                return value.equals("TRUE") || value.equals("1") || value.equals("SI") || value.equals("SÍ");
            case NUMERIC:
                return cell.getNumericCellValue() == 1.0;
            default:
                return null;
        }
    }
}