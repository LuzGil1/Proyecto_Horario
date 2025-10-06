package com.example.proyectohorario_1.Servicio;

import com.example.proyectohorario_1.Entidad.*;
import com.example.proyectohorario_1.Respositorio.*;
import com.example.proyectohorario_1.model.AsignacionProblema;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Time;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HorarioService {

    private final AsignacionRepositorio asignacionRepositorio;
    private final CatedraticoRepositorio catedraticoRepositorio;
    private final CursoRepositorio cursoRepositorio;
    private final AulaRepositorio aulaRepositorio;
    private final HorarioRepositorio horarioRepositorio;
    private final EstudianteRepositorio estudianteRepositorio;
    private final SeccionRepositorio seccionRepositorio;
    private final InscripcionRepositorio inscripcionRepositorio;

    private static final int MAX_INTENTOS = 5;
    private static final Long DPI_PLACEHOLDER = 0L;

    @Transactional
    public List<AsignacionProblema> optimizarHorarios() {
        System.out.println("=== OPTIMIZACIÓN: CATEDRÁTICO ÚNICO POR SECCIÓN (NO POR CURSO) ===\n");

        List<Estudiante> estudiantes = estudianteRepositorio.findAll();
        if (estudiantes.isEmpty()) {
            return new ArrayList<>();
        }

        System.out.println("Total estudiantes: " + estudiantes.size());

        Map<Integer, List<Estudiante>> estudiantesPorSeccion = organizarEstudiantesPorSecciones(estudiantes);
        int numSecciones = estudiantesPorSeccion.size();

        List<Curso> cursos = cursoRepositorio.findAll();
        List<Catedratico> catedraticos = catedraticoRepositorio.findAll();

        Map<Integer, Aula> aulasMap = obtenerAulasRequeridas(numSecciones);
        List<Aula> aulas = new ArrayList<>(aulasMap.values());

        List<Horario> horarios = obtenerHorariosValidos();

        System.out.println("Secciones: " + estudiantesPorSeccion.size());
        System.out.println("Cursos: " + cursos.size());
        System.out.println("Catedráticos: " + catedraticos.size());
        System.out.println("Aulas: " + aulas.size());

        List<AsignacionProblema> mejorSolucion = null;
        int mejorScore = -1;

        for (int intento = 0; intento < MAX_INTENTOS; intento++) {
            System.out.println("\n--- Intento " + (intento + 1) + " ---");

            List<Integer> seccionesOrdenadas = new ArrayList<>(estudiantesPorSeccion.keySet());
            Collections.shuffle(seccionesOrdenadas);

            List<Curso> cursosShuffled = new ArrayList<>(cursos);
            Collections.shuffle(cursosShuffled);

            Map<Long, Integer> cargaCatedraticos = new HashMap<>();
            Set<String> horariosOcupados = new HashSet<>();
            Map<Integer, Aula> aulasPorSeccion = new HashMap<>();
            // YA NO usamos cursosAsignados
            Map<Integer, Set<Long>> catedraticosPorSeccion = new HashMap<>();

            List<AsignacionProblema> solucionActual = new ArrayList<>();
            boolean exito = asignarTodasLasSecciones(
                    seccionesOrdenadas,
                    estudiantesPorSeccion,
                    cursosShuffled,
                    catedraticos,
                    aulas,
                    horarios,
                    aulasPorSeccion,
                    cargaCatedraticos,
                    horariosOcupados,
                    catedraticosPorSeccion,
                    solucionActual
            );

            int score = solucionActual.size();
            System.out.println("Asignaciones logradas: " + score + "/" + (cursos.size() * estudiantesPorSeccion.size()));

            if (exito && score > mejorScore) {
                mejorSolucion = new ArrayList<>(solucionActual);
                mejorScore = score;
                System.out.println("✓ Nueva mejor solución encontrada");

                if (mejorScore == cursos.size() * estudiantesPorSeccion.size()) {
                    System.out.println("\n🎉 SOLUCIÓN COMPLETA EN INTENTO " + (intento + 1));
                    break;
                }
            }
        }

        if (mejorSolucion == null || mejorSolucion.isEmpty()) {
            System.out.println("\n⚠️ NO SE ENCONTRÓ SOLUCIÓN VÁLIDA");
            return new ArrayList<>();
        }

        System.out.println("\n=== APLICANDO MEJOR SOLUCIÓN ===");
        aplicarSolucion(mejorSolucion, estudiantesPorSeccion);

        mostrarResumen(mejorSolucion);
        return mejorSolucion;
    }

    private boolean asignarTodasLasSecciones(
            List<Integer> seccionIds,
            Map<Integer, List<Estudiante>> estudiantesPorSeccion,
            List<Curso> cursos,
            List<Catedratico> catedraticos,
            List<Aula> aulas,
            List<Horario> horarios,
            Map<Integer, Aula> aulasPorSeccion,
            Map<Long, Integer> cargaCatedraticos,
            Set<String> horariosOcupados,
            Map<Integer, Set<Long>> catedraticosPorSeccion,
            List<AsignacionProblema> solucion) {

        return backtrackSecciones(
                0, seccionIds, estudiantesPorSeccion, cursos, catedraticos,
                aulas, horarios, aulasPorSeccion, cargaCatedraticos,
                horariosOcupados, catedraticosPorSeccion, solucion
        );
    }

    private boolean backtrackSecciones(
            int indiceSeccion,
            List<Integer> seccionIds,
            Map<Integer, List<Estudiante>> estudiantesPorSeccion,
            List<Curso> cursos,
            List<Catedratico> catedraticos,
            List<Aula> aulas,
            List<Horario> horarios,
            Map<Integer, Aula> aulasPorSeccion,
            Map<Long, Integer> cargaCatedraticos,
            Set<String> horariosOcupados,
            Map<Integer, Set<Long>> catedraticosPorSeccion,
            List<AsignacionProblema> solucion) {

        if (indiceSeccion >= seccionIds.size()) {
            System.out.println("\n✓✓✓ TODAS LAS SECCIONES ASIGNADAS ✓✓✓");
            return true;
        }

        Integer seccionId = seccionIds.get(indiceSeccion);
        List<Estudiante> estudiantes = estudiantesPorSeccion.get(seccionId);

        System.out.println("\n  → Procesando Sección " + seccionId + " (" + estudiantes.size() + " est.)");

        List<Aula> aulasDisponibles = aulas.stream()
                .filter(a -> !aulasPorSeccion.containsValue(a))
                .filter(a -> a.getCapacidad() >= estudiantes.size())
                .collect(Collectors.toList());

        if (aulasDisponibles.isEmpty()) {
            System.out.println("    ✗ No hay aulas disponibles");
            return false;
        }

        for (Aula aula : aulasDisponibles) {
            System.out.println("    → Probando aula: " + aula.getNombreAula());

            aulasPorSeccion.put(seccionId, aula);
            catedraticosPorSeccion.putIfAbsent(seccionId, new HashSet<>());

            List<AsignacionProblema> asignacionesSeccion = new ArrayList<>();
            boolean exito = asignarCursosDeSeccion(
                    seccionId, estudiantes, aula, cursos, catedraticos, horarios,
                    cargaCatedraticos, horariosOcupados, catedraticosPorSeccion,
                    asignacionesSeccion
            );

            if (exito) {
                solucion.addAll(asignacionesSeccion);
                System.out.println("    ✓ Sección asignada en " + aula.getNombreAula());

                if (backtrackSecciones(
                        indiceSeccion + 1, seccionIds, estudiantesPorSeccion, cursos,
                        catedraticos, aulas, horarios, aulasPorSeccion,
                        cargaCatedraticos, horariosOcupados, catedraticosPorSeccion, solucion)) {
                    return true;
                }

                System.out.println("    ↩ Backtrack en Sección " + seccionId);
                solucion.removeAll(asignacionesSeccion);

                for (AsignacionProblema ap : asignacionesSeccion) {
                    deshacerAsignacion(ap, aula, cargaCatedraticos, horariosOcupados,
                            catedraticosPorSeccion, seccionId);
                }
            } else {
                catedraticosPorSeccion.get(seccionId).clear();
            }

            aulasPorSeccion.remove(seccionId);
        }

        return false;
    }

    private boolean asignarCursosDeSeccion(
            Integer seccionId,
            List<Estudiante> estudiantes,
            Aula aula,
            List<Curso> cursos,
            List<Catedratico> catedraticos,
            List<Horario> horarios,
            Map<Long, Integer> cargaCatedraticos,
            Set<String> horariosOcupados,
            Map<Integer, Set<Long>> catedraticosPorSeccion,
            List<AsignacionProblema> asignacionesSeccion) {

        Seccion seccion = seccionRepositorio.findById(seccionId).orElse(null);
        if (seccion == null) return false;

        System.out.println("      Asignando " + cursos.size() + " cursos");

        for (Curso curso : cursos) {
            System.out.println("      → Curso: " + curso.getNombreCurso());

            AsignacionProblema asignacion = intentarAsignarCursoLibre(
                    curso, seccion, estudiantes, aula, catedraticos, horarios,
                    cargaCatedraticos, horariosOcupados, catedraticosPorSeccion
            );

            if (asignacion == null) {
                System.out.println("        ✗ No se pudo asignar");
                return false;
            }

            asignacionesSeccion.add(asignacion);
            catedraticosPorSeccion.get(seccionId).add(asignacion.getCatedraticoDpi());
            System.out.println("        ✓ Asignado a " + asignacion.getNombreCatedratico());
        }

        return true;
    }

    // NUEVO MÉTODO: Sin restricción de "mismo catedrático por curso"
    private AsignacionProblema intentarAsignarCursoLibre(
            Curso curso,
            Seccion seccion,
            List<Estudiante> estudiantes,
            Aula aula,
            List<Catedratico> catedraticos,
            List<Horario> horarios,
            Map<Long, Integer> cargaCatedraticos,
            Set<String> horariosOcupados,
            Map<Integer, Set<Long>> catedraticosPorSeccion) {

        Set<Long> catedraticosEnSeccion = catedraticosPorSeccion.get(seccion.getIdSeccion());

        // Buscar catedrático del tipo correcto que NO esté en esta sección
        List<Catedratico> elegibles = filtrarCatedraticosPorCurso(curso, catedraticos).stream()
                .filter(c -> !catedraticosEnSeccion.contains(c.getDpiCatedratico()))
                .filter(c -> {
                    if (c.getDpiCatedratico().equals(DPI_PLACEHOLDER)) {
                        return true; // Placeholder siempre disponible
                    }
                    return cargaCatedraticos.getOrDefault(c.getDpiCatedratico(), 0) < 4;
                })
                .collect(Collectors.toList());

        if (elegibles.isEmpty()) {
            return null;
        }

        // Priorizar catedráticos reales sobre placeholder
        elegibles.sort((c1, c2) -> {
            if (c1.getDpiCatedratico().equals(DPI_PLACEHOLDER)) return 1;
            if (c2.getDpiCatedratico().equals(DPI_PLACEHOLDER)) return -1;
            return 0;
        });

        // Mezclar solo los primeros 3 catedráticos reales
        int cantidadReales = (int) elegibles.stream()
                .filter(c -> !c.getDpiCatedratico().equals(DPI_PLACEHOLDER))
                .count();
        if (cantidadReales > 0) {
            Collections.shuffle(elegibles.subList(0, Math.min(3, cantidadReales)));
        }

        for (Catedratico catedratico : elegibles) {
            List<Horario> horariosShuffled = new ArrayList<>(horarios);
            Collections.shuffle(horariosShuffled);

            for (Horario horario : horariosShuffled) {
                String claveCat = catedratico.getDpiCatedratico() + "-" + horario.getIdHorario();
                String claveAula = aula.getIdAula() + "-" + horario.getIdHorario();

                if (!horariosOcupados.contains(claveCat) && !horariosOcupados.contains(claveAula)) {
                    horariosOcupados.add(claveCat);
                    horariosOcupados.add(claveAula);
                    cargaCatedraticos.put(catedratico.getDpiCatedratico(),
                            cargaCatedraticos.getOrDefault(catedratico.getDpiCatedratico(), 0) + 1);

                    return crearAsignacionProblema(
                            curso, seccion, catedratico, aula, horario, estudiantes
                    );
                }
            }
        }

        return null;
    }

    // NUEVO: Método auxiliar para deshacer asignaciones (reduce duplicación)
    private void deshacerAsignacion(
            AsignacionProblema ap,
            Aula aula,
            Map<Long, Integer> cargaCatedraticos,
            Set<String> horariosOcupados,
            Map<Integer, Set<Long>> catedraticosPorSeccion,
            Integer seccionId) {

        String claveCat = ap.getCatedraticoDpi() + "-" + ap.getHoraInicio();
        String claveAula = aula.getIdAula() + "-" + ap.getHoraInicio();
        horariosOcupados.remove(claveCat);
        horariosOcupados.remove(claveAula);

        Long dpi = ap.getCatedraticoDpi();
        Integer cargaActual = cargaCatedraticos.get(dpi);
        if (cargaActual != null && cargaActual > 0) {
            cargaCatedraticos.put(dpi, cargaActual - 1);
        }

        catedraticosPorSeccion.get(seccionId).remove(dpi);
    }

    // ========== MÉTODOS AUXILIARES (sin cambios) ==========

    private void actualizarInscripcionesParaAsignacion(
            Asignacion asignacion, List<Estudiante> estudiantes) {

        Long idAsignacion = asignacion.getIdAsignacion();
        Long cursoId = Long.valueOf(asignacion.getCurso().getIdCurso());

        for (Estudiante estudiante : estudiantes) {
            String carne = estudiante.getCarneEstudiante();

            try {
                Optional<Inscripcion> yaExiste = inscripcionRepositorio
                        .findByEstudiante_CarneEstudianteAndAsignacion_IdAsignacion(carne, idAsignacion);

                if (yaExiste.isPresent()) {
                    continue;
                }

                List<Inscripcion> inscripcionesCurso = inscripcionRepositorio
                        .findByEstudiante_CarneEstudianteAndAsignacion_Curso_IdCurso(carne, cursoId);

                if (!inscripcionesCurso.isEmpty()) {
                    Inscripcion insc = inscripcionesCurso.get(0);
                    insc.setAsignacion(asignacion);
                    inscripcionRepositorio.saveAndFlush(insc);
                } else {
                    Inscripcion nueva = new Inscripcion();
                    nueva.setEstudiante(estudiante);
                    nueva.setAsignacion(asignacion);
                    nueva.setEstado("INSCRITO");
                    inscripcionRepositorio.saveAndFlush(nueva);
                }

            } catch (DataIntegrityViolationException e) {
                System.out.println("      (Inscripción duplicada ignorada para " +
                        estudiante.getNombreEstudiante() + ")");
            }
        }
    }

    private Map<String, Seccion> obtenerSeccionesRequeridas(int numSecciones) {
        Map<String, Seccion> secciones = new HashMap<>();

        for (int i = 0; i < numSecciones; i++) {
            String nombre = String.valueOf((char)('A' + i));

            Seccion seccion = seccionRepositorio.findByNombreSeccion(nombre)
                    .orElseGet(() -> {
                        System.out.println("  Creando sección " + nombre);
                        Seccion nueva = new Seccion();
                        nueva.setNombreSeccion(nombre);
                        nueva.setSemestre(1);
                        nueva.setCupoMaximo(25);
                        nueva.setCupoActual(0);
                        nueva.setActiva(true);
                        return seccionRepositorio.save(nueva);
                    });

            secciones.put(nombre, seccion);
        }

        return secciones;
    }

    private void aplicarSolucion(
            List<AsignacionProblema> solucion,
            Map<Integer, List<Estudiante>> estudiantesPorSeccion) {

        System.out.println("\n=== APLICANDO SOLUCIÓN A BASE DE DATOS ===");
        System.out.println("Total asignaciones a guardar: " + solucion.size());

        Map<String, List<AsignacionProblema>> porSeccion = solucion.stream()
                .collect(Collectors.groupingBy(AsignacionProblema::getNombreSeccion));

        int asignacionesGuardadas = 0;

        for (Map.Entry<String, List<AsignacionProblema>> entry : porSeccion.entrySet()) {
            String nombreSeccion = entry.getKey();
            List<AsignacionProblema> asignacionesSeccion = entry.getValue();

            System.out.println("\n--- Procesando Sección " + nombreSeccion + " ---");

            Seccion seccion = seccionRepositorio.findByNombreSeccion(nombreSeccion)
                    .orElseThrow(() -> new RuntimeException("Sección no encontrada: " + nombreSeccion));

            String nombreAula = asignacionesSeccion.get(0).getAulaNombre();
            Aula aula = aulaRepositorio.findByNombreAula(nombreAula)
                    .orElseThrow(() -> new RuntimeException("Aula no encontrada: " + nombreAula));

            System.out.println("Sección ID: " + seccion.getIdSeccion() + " | Aula: " + aula.getNombreAula());

            for (AsignacionProblema ap : asignacionesSeccion) {
                Curso curso = cursoRepositorio.findByCodigoCurso(ap.getCursoCodigo()).orElse(null);
                if (curso == null) continue;

                Catedratico catedratico = catedraticoRepositorio.findById(ap.getCatedraticoDpi()).orElse(null);
                if (catedratico == null) continue;

                Horario horario = horarioRepositorio.findByHoraInicio(ap.getHoraInicio().toLocalTime()).orElse(null);
                if (horario == null) continue;

                Optional<Asignacion> asignacionExistente = asignacionRepositorio.findAll().stream()
                        .filter(a -> a.getCurso().getIdCurso().equals(curso.getIdCurso()) &&
                                a.getSeccion().getIdSeccion().equals(seccion.getIdSeccion()) &&
                                a.getSemestre() == 1 && a.getAnio() == 2025)
                        .findFirst();

                Asignacion asignacion;
                if (asignacionExistente.isPresent()) {
                    asignacion = asignacionExistente.get();
                    asignacion.setCatedratico(catedratico);
                    asignacion.setAula(aula);
                    asignacion.setHorario(horario);
                    System.out.println("  ↻ Actualizando: " + curso.getNombreCurso());
                } else {
                    asignacion = new Asignacion();
                    asignacion.setCurso(curso);
                    asignacion.setSeccion(seccion);
                    asignacion.setCatedratico(catedratico);
                    asignacion.setAula(aula);
                    asignacion.setHorario(horario);
                    asignacion.setSemestre(1);
                    asignacion.setAnio(2025);
                    asignacion.setEstado("ACTIVO");
                    asignacion.setFechaAsignacion(new java.sql.Date(System.currentTimeMillis()));
                    System.out.println("  + Creando: " + curso.getNombreCurso());
                }

                asignacion = asignacionRepositorio.save(asignacion);
                asignacionesGuardadas++;

                List<Estudiante> estudiantes = estudiantesPorSeccion.get(seccion.getIdSeccion());
                if (estudiantes != null) {
                    actualizarInscripcionesParaAsignacion(asignacion, estudiantes);
                }
            }
        }

        System.out.println("\n✓ Total asignaciones guardadas: " + asignacionesGuardadas);
    }

    private AsignacionProblema crearAsignacionProblema(
            Curso curso, Seccion seccion, Catedratico catedratico,
            Aula aula, Horario horario, List<Estudiante> estudiantes) {

        AsignacionProblema dto = new AsignacionProblema();
        dto.setCursoCodigo(curso.getCodigoCurso());
        dto.setCursoNombre(curso.getNombreCurso());
        dto.setCatedraticoDpi(catedratico.getDpiCatedratico());
        dto.setNombreCatedratico(catedratico.getNombreCatedratico());
        dto.setAulaNombre(aula.getNombreAula());
        dto.setNombreSeccion(seccion.getNombreSeccion());
        dto.setDiaSemana(horario.getDiaSemana());
        dto.setHoraInicio(Time.valueOf(horario.getHoraInicio()));
        dto.setHoraFin(Time.valueOf(horario.getHoraFin()));
        dto.setNombresEstudiantes(
                estudiantes.stream()
                        .map(Estudiante::getNombreEstudiante)
                        .collect(Collectors.toList())
        );
        return dto;
    }

    private Map<Integer, List<Estudiante>> organizarEstudiantesPorSecciones(
            List<Estudiante> estudiantes) {

        System.out.println("\n=== ORGANIZANDO ESTUDIANTES EN SECCIONES ===");
        System.out.println("Total estudiantes a distribuir: " + estudiantes.size());

        int seccionesNecesarias = (int) Math.ceil((double) estudiantes.size() / 25.0);
        System.out.println("Secciones necesarias: " + seccionesNecesarias);

        Map<String, Seccion> seccionesMap = obtenerSeccionesRequeridas(seccionesNecesarias);
        List<Seccion> secciones = new ArrayList<>(seccionesMap.values());

        System.out.println("Secciones disponibles en BD: " + secciones.size());
        secciones.forEach(s -> System.out.println("  - Sección " + s.getNombreSeccion() + " (ID: " + s.getIdSeccion() + ")"));

        Map<Integer, List<Estudiante>> distribucion = crearNuevasAgrupaciones(estudiantes, secciones);

        distribucion.forEach((id, estuds) -> {
            String nombreSeccion = secciones.stream()
                    .filter(s -> s.getIdSeccion().equals(id))
                    .map(Seccion::getNombreSeccion)
                    .findFirst()
                    .orElse("?");
            System.out.println("  Sección " + nombreSeccion + ": " + estuds.size() + " estudiantes");
        });

        return distribucion;
    }

    private Map<Integer, List<Estudiante>> crearNuevasAgrupaciones(
            List<Estudiante> estudiantes, List<Seccion> secciones) {

        Map<Integer, List<Estudiante>> grupos = new HashMap<>();

        List<Estudiante> hombres = estudiantes.stream()
                .filter(e -> "M".equals(e.getSexo()))
                .collect(Collectors.toList());

        List<Estudiante> mujeres = estudiantes.stream()
                .filter(e -> "F".equals(e.getSexo()))
                .collect(Collectors.toList());

        Collections.shuffle(hombres);
        Collections.shuffle(mujeres);

        int seccionesNecesarias = (int) Math.ceil((double) estudiantes.size() / 25.0);

        if (secciones.size() < seccionesNecesarias) {
            throw new IllegalStateException(
                    "Insuficientes secciones. Necesarias: " + seccionesNecesarias +
                            ", Disponibles: " + secciones.size());
        }

        for (int i = 0; i < seccionesNecesarias; i++) {
            grupos.put(secciones.get(i).getIdSeccion(), new ArrayList<>());
        }

        Integer[] seccionIds = grupos.keySet().toArray(new Integer[0]);

        int idxHombre = 0, idxMujer = 0;

        for (int i = 0; i < estudiantes.size(); i++) {
            Integer seccionId = seccionIds[i % seccionIds.length];

            if (idxMujer < mujeres.size() && grupos.get(seccionId).size() < 25) {
                grupos.get(seccionId).add(mujeres.get(idxMujer++));
            } else if (idxHombre < hombres.size() && grupos.get(seccionId).size() < 25) {
                grupos.get(seccionId).add(hombres.get(idxHombre++));
            }
        }

        return grupos;
    }

    private Map<Integer, Aula> obtenerAulasRequeridas(int numAulas) {
        Map<Integer, Aula> aulas = new HashMap<>();

        for (int i = 0; i < numAulas; i++) {
            String nombreAula = "Salon " + (i + 1);

            Aula aula = aulaRepositorio.findByNombreAula(nombreAula)
                    .orElseGet(() -> {
                        System.out.println("  Creando aula: " + nombreAula);
                        Aula nueva = new Aula();
                        nueva.setNombreAula(nombreAula);
                        nueva.setCapacidad(25);
                        return aulaRepositorio.save(nueva);
                    });

            aulas.put(aula.getIdAula().intValue(), aula);
        }

        return aulas;
    }

    private List<Catedratico> filtrarCatedraticosPorCurso(
            Curso curso, List<Catedratico> catedraticos) {

        String tipoCurso = curso.getTipo() != null ? curso.getTipo().toLowerCase() : "";

        return catedraticos.stream()
                .filter(cat -> {
                    if (cat.getDpiCatedratico().equals(DPI_PLACEHOLDER)) {
                        return true;
                    }

                    String profesion = cat.getProfesion().toLowerCase();
                    if ("ing".equals(tipoCurso)) {
                        return profesion.contains("ingenier");
                    } else if ("lic".equals(tipoCurso)) {
                        return profesion.contains("licencia");
                    }
                    return false;
                })
                .collect(Collectors.toList());
    }

    private List<Horario> obtenerHorariosValidos() {
        return horarioRepositorio.findAll().stream()
                .filter(h -> !h.getHoraInicio().equals(LocalTime.of(13, 0)))
                .sorted(Comparator.comparing(Horario::getHoraInicio))
                .collect(Collectors.toList());
    }

    private void mostrarResumen(List<AsignacionProblema> resultados) {
        System.out.println("\n=== RESUMEN ===");
        System.out.println("Total asignaciones: " + resultados.size());

        // Contar asignaciones con placeholder
        long asignacionesPendientes = resultados.stream()
                .filter(ap -> ap.getCatedraticoDpi().equals(DPI_PLACEHOLDER))
                .count();

        if (asignacionesPendientes > 0) {
            System.out.println("\n⚠️⚠️⚠️ ADVERTENCIA ⚠️⚠️⚠️");
            System.out.println("╔════════════════════════════════════════════════╗");
            System.out.println("║  " + asignacionesPendientes + " ASIGNACIONES SIN CATEDRÁTICO        ║");
            System.out.println("║                                                ║");
            System.out.println("║  ACCIÓN REQUERIDA:                             ║");
            System.out.println("║  Contratar " + asignacionesPendientes + " catedrático(s) adicional(es)  ║");
            System.out.println("║  y reorganizar horarios nuevamente.            ║");
            System.out.println("╚════════════════════════════════════════════════╝");
        }

    }
}