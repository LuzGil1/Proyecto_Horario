package com.example.proyectohorario_1.Servicio;

import com.example.proyectohorario_1.Entidad.*;
import com.example.proyectohorario_1.Respositorio.*;
import com.example.proyectohorario_1.DTO.AsignacionProblema;
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


//      Paso 1: Obtener estudiantes---------------------------------------------------------------------------------------------------
//        Este método busca TODOS los estudiantes inscritos en la BD, si no hay retorna una lista vacía
        List<Estudiante> estudiantes = estudianteRepositorio.findAll();
        if (estudiantes.isEmpty()) {
            return new ArrayList<>();
        }
        System.out.println("Total estudiantes: " + estudiantes.size());
//      --------------------------------------------------------------------------------------------------------------------------------

//      Paso 2: Organizar en secciones ----------------------------------------------------------------------------------------------------
//        Ok, estamos usando un Map que hace la función agruparlos  asociando el ID de la sección con la lista de estudiantes en esa sección
//        ejemplo
//        1 -> [est1, est2, est3] están en la sección A
//        2 -> [est4, est5, est6] están en la sección B
        Map<Integer, List<Estudiante>> estudiantesPorSeccion = organizarEstudiantesPorSecciones(estudiantes);
        int numSecciones = estudiantesPorSeccion.size();
//        --------------------------------------------------------------------------------------------------------------------------------

//      Paso 3: Obtener recursos ----------------------------------------------------------------------------------------------------
        List<Curso> cursos = cursoRepositorio.findAll(); //Encuentra todos los cursos registrados en la BD (Introducción, logica, contabilidad, desarrollo humano y metodologia)
        List<Catedratico> catedraticos = catedraticoRepositorio.findAll(); //Encuentra todos los catedráticos registrados en la BD
        Map<Integer, Aula> aulasMap = obtenerAulasRequeridas(numSecciones); //Obtiene o crea Salones necesarios (1 por sección)
        List<Aula> aulas = new ArrayList<>(aulasMap.values()); //Convierte el Map de aulas a una lista
        List<Horario> horarios = obtenerHorariosValidos(); //Obtiene todos los horarios válidos (excluyendo 1-2pm)

//     Imprime en consola un resumen de los recursos obtenidos
        System.out.println("Secciones: " + estudiantesPorSeccion.size());
        System.out.println("Cursos: " + cursos.size());
        System.out.println("Catedráticos: " + catedraticos.size());
        System.out.println("Aulas: " + aulas.size());
//      --------------------------------------------------------------------------------------------------------------------------------

//      Paso 4: El ciclo de intentos ----------------------------------------------------------------------------------------------------
        List<AsignacionProblema> mejorSolucion = null; // Aquí guardaremos la mejor solución encontrada
        int mejorScore = -1; // Puntuación de la mejor solución (número de asignaciones exitosas)

        for (int intento = 0; intento < MAX_INTENTOS; intento++) {
            System.out.println("\n--- Intento " + (intento + 1) + " ---");

//          Las 4 siguientes lineas de código toma dos listas (Secciones y cursos) y crea copias de ellas que luego mezcla completamente de forma aleatoria, para intentar diferentes combinaciones en cada intento
            List<Integer> seccionesOrdenadas = new ArrayList<>(estudiantesPorSeccion.keySet()); //Toma la lista de IDs de secciones y la guarda en una nueva lista seccionesOrdenadas
            Collections.shuffle(seccionesOrdenadas); // Mezcla el orden de las secciones, gracias al shuffle = aleatoriza, es decir si estaban como [1,2,3] ahora pueden quedar como [3,1,2]
            List<Curso> cursosShuffled = new ArrayList<>(cursos); // Toma la lista original de cursos y la guarda en una nueva lista cursosShuffled
            Collections.shuffle(cursosShuffled); // Mezcla el orden de los cursos en esta nueva lista

//          Paso 5: Preparar estructuras de datos  ----------------------------------------------------------------------------------------------------
            Map<Long, Integer> cargaCatedraticos = new HashMap<>(); //  Crear una lista  para llevar la cuenta de cuántos secciones tiene cada catedrático, para no exceder el máximo permitido (4)
            Set<String> horariosOcupados = new HashSet<>(); // Crear una lista para guardar y verificar rápidamente qué horarios ya han sido tomados, previniendo que se asignen dos veces
            Map<Integer, Aula> aulasPorSeccion = new HashMap<>(); // Crear una lista para asignar un aula específica a cada sección
            Map<Integer, Set<Long>> catedraticosPorSeccion = new HashMap<>(); // Crear una lista para rastrear qué catedráticos ya han sido asignados a cada sección, evitando repetir catedráticos en la misma sección

//          Paso 6: Intentar asignar todas las secciones ----------------------------------------------------------------------------------------------------
            List<AsignacionProblema> solucionActual = new ArrayList<>(); //Se crea una lista vacía que servirá para ir guardando las asignaciones que se van haciendo. Si el proceso tiene éxito, esta lista contendrá la solución final.

            //Ok, esta es una combinacion de una variable y una llamada a un método. de manera simple, lo que hace es llamar al método asignarTodasLasSecciones y le pasa todas las listas y mapas que hemos ido creando y llenando hasta ahora.
//            Como es un boolean, lo que hace es devolver true si logra asignar todas las secciones correctamente, o false si no lo logra.
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

//           Paso 7: Evaluar el resultado ----------------------------------------------------------------------------------------------------
            int score = solucionActual.size(); //Almacena en la variable score la cantidad de asignaciones que se lograron hacer en este intento
            System.out.println("Asignaciones logradas: " + score + "/" + (cursos.size() * estudiantesPorSeccion.size())); //Imprime en consola cuántas asignaciones se lograron hacer en este intento

            //Si el intento fue exitoso y la cantidad de asignaciones logradas es mayor que la mejor hasta ahora, entonces guarda esta solución como la mejor encontrada hasta el momento
            if (exito && score > mejorScore) {
//          exito: Verifica si el último intento de asignación (llamado a asignarTodasLasSecciones) logró completar todas las asignaciones posibles si es asi devuelve true.
//          Entonces digamos que funciona asi: Si exito es true y logra 50 asignaciones (score es 50) entonces la condicion que evalua es if (true && 50 > -1) → la condiciones es Verdadera.
//          La solución de 50 asignaciones se guarda inmediatamente como la nueva mejorSolucion, y mejorScore se actualiza a 50

                mejorSolucion = new ArrayList<>(solucionActual); // Guarda una copia de la solución actual como la mejor solución
                mejorScore = score; // Actualiza el mejor score
                System.out.println("✓ Nueva mejor solución encontrada");

                if (mejorScore == cursos.size() * estudiantesPorSeccion.size()) { // Si se logró el  100%  la solución perfecta, se sale del ciclo de intentos
                    System.out.println(" SOLUCIÓN COMPLETA EN INTENTO " + (intento + 1)); // Imprime un mensaje de celebración y la cantidad de intentos que tomó encontrarla
                    break;
                }
            }
        }

//      Paso 8:Aplicar la mejor solución encontrada ----------------------------------------------------------------------------------------------------
//      Esta condicion Pregunta si la lista que debería contener la mejor solución (mejorSolucion) es nula (nunca se inicializó correctamente) O si está vacía (el proceso no pudo hacer ni una sola asignación exitosa).
        if (mejorSolucion == null || mejorSolucion.isEmpty()) {
            System.out.println("NO SE ENCONTRÓ SOLUCIÓN VÁLIDA");
            return new ArrayList<>();
        }

        System.out.println("\n=== APLICANDO MEJOR SOLUCIÓN ===");
        //Aqui se esta llamando al metodo aplicarSolucion y le pasa la mejor solución encontrada y la lista de estudiantes por sección y lo guarda en la base de datos
        aplicarSolucion(mejorSolucion, estudiantesPorSeccion);

//        Se llama al metodo mostrarResumen() y le pasa la mejor solución para que imprima un resumen en consola
        mostrarResumen(mejorSolucion);

        return mejorSolucion; //Finalmente, el método devuelve la lista de asignaciones (mejorSolucion) para que el resto del programa pueda usar estos resultados.
    }


//  ----------------------------------------------- Métodos auxiliares -----------------------------------------------------


//    Metodo Auxiliar: asignarTodasLasSecciones  -----------------------------------------------------------------------------------------------------------------------------
//    Proposito: Iniciar el proceso de asignación de todas las secciones utilizando backtracking osea el algoritmo que estamos usando
    //Este metodo es un metodo privado que solo hace una cosa llama y devuelve el resultado de la funcion recursiva central que resuelve problema backtrackingSecciones
    private boolean asignarTodasLasSecciones(
            //Todos estos son parametros que recibe todos los datos necesarios para la asignacion
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
//      Esta es la acción clave. Transfiere todos los datos recibidos a al metodo backtrackSecciones
        return backtrackSecciones(
//                porque 0? Porque es el índice inicial, es decir, empezará a intentar asignar desde la primera sección en la lista seccionIds,
//                ya que las listas en Java son indexadas desde 0.
                0, seccionIds, estudiantesPorSeccion, cursos, catedraticos, aulas, horarios, aulasPorSeccion, cargaCatedraticos, horariosOcupados, catedraticosPorSeccion, solucion);
    }
//      --------------------------------------------------------------------------------------------------------------------------------


//   Metodo Auxiliar: backtrackSecciones
//   Propósito: Implementar el algoritmo de backtracking para asignar aulas, catedráticos y horarios a cada sección
//   Como Funciona: Funciona de forma recursiva, es decir se llama a sí misma,
//   tratando de asignar recursos a una sección a la vez. Si en algún punto no puede continuar(no hay recursos disponibles), retrocede (backtrack) y prueba una asignación diferente,
//   en palabras simples es como probar diferentes combinaciones hasta encontrar una que funcione para la sección actual y luego pasar a la siguiente sección.
//  !!!!!!!!!!!!!!!!!!!!!!!!!!!!!AQUI ESTA EL ALGORITMO DE BACKTRACKING!!!!!!!!!!!!!!!!!!!!!!!

    private boolean backtrackSecciones(
            int indiceSeccion, // En qué sección estamos trabajando 1, 2, 3
//          Todos estos so parametos que recibe todos los datos necesarios para la asignacion
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
            System.out.println("\n TODAS LAS SECCIONES ASIGNADAS ");
            return true;
        }

//       Obtiene el ID de la sección ACTUAL que debe procesar en este nivel de la recursión y el número de estudiantes para calcular la capacidad necesaria
        Integer seccionId = seccionIds.get(indiceSeccion); // ejemplo: obtiene el ID 1 → sección A
        List<Estudiante> estudiantes = estudiantesPorSeccion.get(seccionId); // ejemplo: obtiene la lista de estudiantes en la sección A
        System.out.println("\nProcesando Sección " + seccionId + " (" + estudiantes.size() + " est.)");

        // Filtrar aulas que no estén ya asignadas y que tengan capacidad suficiente, en palabras simples busca aulas disponibles a traves de una lista
        List<Aula> aulasDisponibles = aulas.stream()
//                El aula es candidata si cumple con
                .filter(a -> !aulasPorSeccion.containsValue(a)) //No ha sido asignada previamente a otra sección en este intento
                .filter(a -> a.getCapacidad() >= estudiantes.size()) //Su capacidad es suficiente para el número de estudiantes, es decir 25 (getCapacidad) >= a la cantidad de estudiantes en esta sección
                .collect(Collectors.toList()); //Finalmente, recopila todas las aulas que cumplen estos criterios en una nueva lista llamada aulasDisponibles

//        Y pues aqui literalmennte dice si el aula esta vacia retorna false y sale del método
        if (aulasDisponibles.isEmpty()) {
            System.out.println("No hay aulas disponibles");
            return false;
        }

        // Asignamos temporalmente esta aula a la sección y tratamos de asignar todos los cursos en esta aula
        for (Aula aula : aulasDisponibles) { // Itera sobre cada aula disponible para intentar asignarla a la sección actual
            System.out.println("Probando aula: " + aula.getNombreAula());

            aulasPorSeccion.put(seccionId, aula); // Asigna temporalmente esta aula a la sección actual en el mapa aulasPorSeccion y lo hace por medio del put
            catedraticosPorSeccion.putIfAbsent(seccionId, new HashSet<>()); // Inicializa el conjunto de catedráticos para esta sección si no existe
            // que rayos es un putIfAbsent? → Si no existe una entrada para esta sección, crea una nueva entrada con un conjunto vacío, y es una funcion de Map

            List<AsignacionProblema> asignacionesSeccion = new ArrayList<>(); // Lista para guardar las asignaciones hechas para esta sección
//          guarda el resultado en la variable exito, pero primero Llama al metodo asignarCursosDeSeccion y le pasa todos los datos necesarios para intentar asignar todos los cursos a esta sección en el aula seleccionada
            boolean exito = asignarCursosDeSeccion(seccionId, estudiantes, aula, cursos, catedraticos, horarios, cargaCatedraticos, horariosOcupados, catedraticosPorSeccion, asignacionesSeccion);

            if (exito) { // Si se lograron asignar todos los cursos en esta sección
                solucion.addAll(asignacionesSeccion); // Agrega las asignaciones de esta sección a la solución global
                System.out.println("Sección asignada en " + aula.getNombreAula());

                // Recurre para asignar la siguiente sección, por eso es recursivo porque aqui se llama a si mismo porque ya se logro una seccion y ahora intenta con la siguiente, ealeeee!
                if (backtrackSecciones(indiceSeccion + 1, seccionIds, estudiantesPorSeccion, cursos, catedraticos, aulas, horarios, aulasPorSeccion, cargaCatedraticos, horariosOcupados, catedraticosPorSeccion, solucion)) {
//                   indiceSeccion  + 1 →  Le dice a la función que ahora debe trabajar en la siguiente sección osea esta aumentando el índice para que en la próxima llamada recursiva procese la siguiente sección en la lista seccionIds
                    return true;
                }

//              ¿Qué pasa si no se puede asignar osea que el if de arriba retornara false?  si No se pudo asignar la siguiente sección, entonces hay que deshacer lo hecho en esta sección actual y probar con otra aula
                System.out.println("Backtrack en Sección " + seccionId);
                solucion.removeAll(asignacionesSeccion); // Elimina las asignaciones hechas para esta sección de la solución global

                for (AsignacionProblema ap : asignacionesSeccion) {
                    //Este bucle recorre cada asignación de curso que se hizo para la sección y llama a la función para revertir esos cambios,
                    // reducer la carga del catedrático, liberar los horarios ocupados y eliminar al catedrático de la sección
                    deshacerAsignacion(ap, aula, cargaCatedraticos, horariosOcupados, catedraticosPorSeccion, seccionId);
                }
            } else {
                // Si no se pudieron asignar todos los cursos en esta sección, deshacer lo hecho y probar con otra aula
                catedraticosPorSeccion.get(seccionId).clear();
            }
            //Esta línea está fuera del if  pero dentro del gran for que prueba aulas. Se asegura de liberar el aula que se estaba probando
            aulasPorSeccion.remove(seccionId);
        }

//      Si ninguna aula funcionó para esta sección, retorna false para indicar que no se pudo asignar esta sección con los recursos disponibles
        return false;
    }
    //--------------------------------------------------------------------------------------------------------------------------------


//   Metodo Auxiliar: asignarCursosDeSeccion
//   Propósito: Intentar asignar todos los cursos a una sección específica
    private boolean asignarCursosDeSeccion(
            //Todos estos son parametos que recibe todos los datos necesarios para la asignacion
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


        //Oki, lo primero que hace es buscar la sección en la base de datos usando el ID proporcionado del metodo backtrackSecciones
        Seccion seccion = seccionRepositorio.findById(seccionId).orElse(null);
        if (seccion == null) return false;

        System.out.println("Asignando " + cursos.size() + " cursos"); //Imprime en consola la cantidad de cursos que intentará asignar a esta sección

        for (Curso curso : cursos) { // Itera sobre uno a uno todos los cursos disponibles en la lista cursos para intentar asignarlos a esta sección
            AsignacionProblema asignacion = intentarAsignarCursoLibre(curso, seccion,  //aqui llama al metodo intentarAsignarCursoLibre quien verdaderamente buscar una combinación válida de catedratico este disponible y cumpla que sea ING O LIC y que tenga un horario
                    estudiantes, aula, catedraticos, horarios,
                    cargaCatedraticos, horariosOcupados, catedraticosPorSeccion);
            System.out.println("Curso: " + curso.getNombreCurso()); //Imprime en consola el nombre del curso que está intentando asignar

            if (asignacion == null) {
//                Si intentarAsignarCursoLibre devuelve null, significa que no se pudo encontrar un catedrático y horario para ese curso específico
                System.out.println("No se pudo asignar");
                return false;
            }


            asignacionesSeccion.add(asignacion); //Si se encontró una asignación válida, la agrega a la lista de asignaciones para esta sección
            // El placeholder (DPI=0) puede repetirse en la misma sección, por lo tanto NO se agrega al conjunto de catedráticos ya usados
            if (!asignacion.getCatedraticoDpi().equals(DPI_PLACEHOLDER)) { //Si el catedrático asignado NO es el placeholder, entonces sí se agrega al conjunto
                catedraticosPorSeccion.get(seccionId).add(asignacion.getCatedraticoDpi()); //Agrega el DPI del catedrático real asignado al conjunto de catedráticos ya asignados para esta sección, para evitar repetirlo en la misma sección
            }

            System.out.println("Asignado a " + asignacion.getNombreCatedratico()); //Imprime en consola el nombre del catedrático al que se asignó el curso
        }

        // Si logra asignar todos los cursos, retorna true para indicar éxito
        return true;
    }
    //--------------------------------------------------------------------------------------------------------------------------------


//El placeholder (DPI=0) puede repetirse en la misma sección porque representa múltiples contrataciones futuras
    private AsignacionProblema intentarAsignarCursoLibre(
//           Todos estos son parametos que recibe todos los datos necesarios para la asignacion
            Curso curso,
            Seccion seccion,
            List<Estudiante> estudiantes,
            Aula aula,
            List<Catedratico> catedraticos,
            List<Horario> horarios,
            Map<Long, Integer> cargaCatedraticos,
            Set<String> horariosOcupados,
            Map<Integer, Set<Long>> catedraticosPorSeccion) {



        Set<Long> catedraticosEnSeccion = catedraticosPorSeccion.get(seccion.getIdSeccion()); // Obtiene los catedráticos ya asignados a esta sección

        // Buscar catedrático del tipo correcto que NO esté en esta sección
        // El placeholder puede repetirse porque representa diferentes contrataciones
        List<Catedratico> elegibles = filtrarCatedraticosPorCurso(curso, catedraticos).stream()  //Llama al meteodo filtrarCatedraticosPorCurso para obtener solo los catedráticos que pueden enseñar este curso (ING o LIC)
                .filter(c -> {
                    // El placeholder siempre es elegible (puede repetirse en la misma sección)
                    if (c.getDpiCatedratico().equals(DPI_PLACEHOLDER)) { //Si es un placeholder dpi=0, siempre está disponible ya que representa una contratación futura y puede usarse múltiples veces
                        return true;
                    }
                    // Los catedráticos reales no pueden repetirse en la misma sección
                    return !catedraticosEnSeccion.contains(c.getDpiCatedratico()); //Verifica que este catedrático real no esté ya asignado a esta sección
                })
                .filter(c -> {
                    if (c.getDpiCatedratico().equals(DPI_PLACEHOLDER)) { //Si es un placeholder dpi=0, siempre cumple con la restricción de carga
                        return true;
                    }
                    return cargaCatedraticos.getOrDefault(c.getDpiCatedratico(), 0) < 4; //Si no es placeholder, verifica que su carga actual (número de secciones asignadas) sea menor a 4
                }).collect(Collectors.toList()); // Finalmente, recopila todos los catedráticos que cumplen estos criterios en una nueva lista llamada elegibles

        if (elegibles.isEmpty()) {
            // Si no hay catedráticos elegibles, retorna null para indicar que no se pudo asignar este curso
            return null;
        }

        // Priorizar catedráticos reales sobre placeholder. Esto asegura que la solución intente asignar personas reales antes de usar la opción de "relleno".
        elegibles.sort((c1, c2) -> { // Si c1 es placeholder y c2 no, c1 va después (retorna 1),
            // ejemplo c1=0 y c2=1234567890123 entonces quien va primero es c2 pero retorna 1
//                                                            o si c2=0 y c1=1534567890123 entonces quien va primero es c1 pero retorna -1
            if (c1.getDpiCatedratico().equals(DPI_PLACEHOLDER)) return 1;
            if (c2.getDpiCatedratico().equals(DPI_PLACEHOLDER)) return -1;
            return 0; // Si ambos son reales no cambia su orden relativo retorna 0
        });

        // Mezclar solo los primeros 3 catedráticos reales para evitar siempre elegir a los mismos, así se da oportunidad a otros y potenciar en el siguiente intento
        int cantidadReales = (int) elegibles.stream().filter(c -> !c.getDpiCatedratico().equals(DPI_PLACEHOLDER)).count(); //Cuenta cuántos catedráticos reales hay en la lista de elegibles
        if (cantidadReales > 0) { // Si hay al menos un catedrático real, mezcla los primeros 3 en la lista
            Collections.shuffle(elegibles.subList(0, Math.min(3, cantidadReales))); // Mezcla solo los primeros 3 catedráticos reales
        }

        // Intentar asignar un horario disponible para cada catedrático elegible
        for (Catedratico catedratico : elegibles) { // Itera sobre cada catedrático elegible para intentar asignarle un horario
            List<Horario> horariosShuffled = new ArrayList<>(horarios); //Crea una copia de la lista original de horarios
            Collections.shuffle(horariosShuffled); // Mezcla el orden de los horarios en esta nueva lista

            for (Horario horario : horariosShuffled) { // Itera sobre cada horario disponible para intentar asignarlo al catedrático
                String claveCat = catedratico.getDpiCatedratico() + "-" + horario.getIdHorario(); // Crea una clave única combinando el DPI del catedrático y el ID del horario, lo hace para verificar rápidamente si este catedrático ya tiene este horario ocupado
                String claveAula = aula.getIdAula() + "-" + horario.getIdHorario(); // Crea una clave única combinando el ID del aula y el ID del horario, para verificar si este aula ya tiene este horario ocupado

                //  El placeholder NO debe bloquear horarios porque puede tener múltiples asignaciones en diferentes horarios de la misma sección
                boolean horarioCatDisponible = !horariosOcupados.contains(claveCat); //Verifica si el catedrático tiene disponible este horario
                boolean horarioAulaDisponible = !horariosOcupados.contains(claveAula); //Verifica si el aula tiene disponible este horario

                // Si es placeholder, solo verificar que el aula esté libre, porque el placeholder nunca tiene conflicto de horario consigo mismo
                if (catedratico.getDpiCatedratico().equals(DPI_PLACEHOLDER)) {
                    horarioCatDisponible = true; // El placeholder nunca tiene conflicto de horario, siempre está "disponible"
                }

                if (horarioCatDisponible && horarioAulaDisponible) {  //Verifica que tanto el catedrático como el aula tengan este horario disponible
                    horariosOcupados.add(claveCat); // Marca este horario como ocupado para el catedrático (o placeholder en este horario específico)
                    horariosOcupados.add(claveAula); // Marca este horario como ocupado para el aula
                    cargaCatedraticos.put(catedratico.getDpiCatedratico(), cargaCatedraticos.getOrDefault(catedratico.getDpiCatedratico(), 0) + 1); // Incrementa la carga del catedrático (número de secciones asignadas), esto incluye al placeholder para llevar estadísticas

                    return crearAsignacionProblema(curso, seccion, catedratico, aula, horario, estudiantes); //Llama al metodo crearAsignacionProblema para crear y retornar la asignación encontrada temporalmente
                }
            }
        }
        return null; // Si no se pudo asignar este curso a ningún catedrático y horario, retorna null
    }
    //--------------------------------------------------------------------------------------------------------------------------------


//    Metodo auxiliar: deshacerAsignacion
//    Propósito: Revertir los cambios hechos al asignar un curso, liberando recursos para intentar otras combinaciones
    private void deshacerAsignacion(
//          Todo estos son los parametro que recibe para revertir los cambios hechos al asignar un curso
            AsignacionProblema ap,
            Aula aula,
            Map<Long, Integer> cargaCatedraticos,
            Set<String> horariosOcupados,
            Map<Integer, Set<Long>> catedraticosPorSeccion,
            Integer seccionId) {

        String claveCat = ap.getCatedraticoDpi() + "-" + ap.getHoraInicio(); // Crea la clave única para el catedrático y horario
        String claveAula = aula.getIdAula() + "-" + ap.getHoraInicio(); // Crea la clave única para el aula y horario
        horariosOcupados.remove(claveCat); // Libera el horario para el catedrático
        horariosOcupados.remove(claveAula); // Libera el horario para el aula
//        Ahora El horario en esa aula y para ese catedrático específico ahora se considera disponible para ser utilizado en una nueva asignación.


        Long dpi = ap.getCatedraticoDpi();  // Obtiene el DPI del catedrático de la asignación
        Integer cargaActual = cargaCatedraticos.get(dpi); // Obtiene la carga actual del catedrático
        if (cargaActual != null && cargaActual > 0) {
            cargaCatedraticos.put(dpi, cargaActual - 1); // Actualiza la carga del catedrático, reduciéndola en 1
        }
//        Esto permite que ese catedrático pueda ser considerado para impartir otro curso en un intento posterior.

        // El placeholder nunca se agregó al conjunto, así que no hay que removerlo, sino existe no se debe remover
        if (!dpi.equals(DPI_PLACEHOLDER)) { //Si el catedrático NO es el placeholder, entonces sí se remueve del conjunto
            catedraticosPorSeccion.get(seccionId).remove(dpi);  //Se elimina el catedrático real de la lista de catedráticos que están enseñando en la seccionId actual.
        }}
    //--------------------------------------------------------------------------------------------------------------------------------


    //Metodo auxiliar: actualizarInscripcionesParaAsignacion
// Propósito: Actualizar las inscripciones de los estudiantes para una asignación específica
    private void actualizarInscripcionesParaAsignacion(Asignacion asignacion, List<Estudiante> estudiantes) { //Recibe una asignación específica y una lista de estudiantes

        //Aqui esta preparando los datos necesarios para actualizar las inscripciones
        Long idAsignacion = asignacion.getIdAsignacion(); //Obtiene el ID de la asignación
        Long cursoId = Long.valueOf(asignacion.getCurso().getIdCurso()); //Obtiene el ID del curso asociado a la asignación

        for (Estudiante estudiante : estudiantes) { //Itera sobre cada estudiante en la lista proporcionada
            String carne = estudiante.getCarneEstudiante(); //usando su carne para realizar las búsquedas.

            try { //Intenta hacer lo siguiente, y si hay un error de integridad de datos como una inscripción duplicada, lo captura y lo maneja
                Optional<Inscripcion> yaExiste = inscripcionRepositorio.findByEstudiante_CarneEstudianteAndAsignacion_IdAsignacion(carne, idAsignacion);
                //Busca si ya existe una inscripción para este estudiante y esta asignación específica

                if (yaExiste.isPresent()) { //Si ya existe una inscripción, simplemente la ignora y pasa al siguiente estudiante, para evitar duplicacion
                    continue;
                }

                // Busca si el estudiante ya tiene una inscripción registrada para el mismo curso,  pero que aún no está vinculada a esta nueva asignacion específica
                List<Inscripcion> inscripcionesCurso = inscripcionRepositorio.findByEstudiante_CarneEstudianteAndAsignacion_Curso_IdCurso(carne, cursoId);

                if (!inscripcionesCurso.isEmpty()) { //Si inscripcionesCurso no está vacía, significa que el estudiante ya tiene una inscripción para este curso
                    Inscripcion insc = inscripcionesCurso.get(0); //Toma la primera inscripción encontrada
                    insc.setAsignacion(asignacion);  //Actualiza la inscripción existente para vincularla a la nueva asignación específica
                    inscripcionRepositorio.saveAndFlush(insc); //Guarda los cambios en la base de datos, por medio del saveAndFlush que lo que hace es guardar y actualizar inmediatamente
                } else { //Sino, si no tiene ninguna inscripción para este curso, crea una nueva inscripción
                    Inscripcion nueva = new Inscripcion(); //Crea una nueva instancia de Inscripcion
                    nueva.setEstudiante(estudiante); //Vincula el estudiante a la nueva inscripción
                    nueva.setAsignacion(asignacion); //Vincula la nueva inscripción a la asignación específica
                    nueva.setEstado("INSCRITO");//Establece el estado de la inscripción como "INSCRITO"
                    inscripcionRepositorio.saveAndFlush(nueva); //Guarda la nueva inscripción en la base de datos
                }

            } catch (DataIntegrityViolationException e) { //captura la excepción si ocurre un error de integridad de datos
                System.out.println("(Inscripción duplicada ignorada para " + estudiante.getNombreEstudiante() + ")");
            }
        }
    }
    //--------------------------------------------------------------------------------------------------------------------------------


    //metodo auxiliar: obtenerSeccionesRequeridas
// Propósito: Obtener el numeo de seccion requeridas, y si no existen, crearlas
    private Map<String, Seccion> obtenerSeccionesRequeridas(int numSecciones) {
        Map<String, Seccion> secciones = new HashMap<>(); //Devuelve en un map para facilitar el acceso ejemplo: secciones.get("A") devuelve la sección A, etc.

        for (int i = 0; i < numSecciones; i++) { // Itera desde 0 hasta numSecciones - 1
            String nombre = String.valueOf((char) ('A' + i)); //Convierte el índice i en una letra (A, B, C, etc.) para nombrar las secciones

            Seccion seccion = seccionRepositorio.findByNombreSeccion(nombre).orElseGet(() -> { //Busca la sección en la base de datos por su nombre, y si no existe, la crea
                System.out.println("  Creando sección " + nombre);
                Seccion nueva = new Seccion(); //Crea una nueva instancia de Seccion
                nueva.setNombreSeccion(nombre);
                nueva.setSemestre(1);
                nueva.setCupoMaximo(25);
                nueva.setCupoActual(0);
                nueva.setActiva(true);
                return seccionRepositorio.save(nueva);
            });

            secciones.put(nombre, seccion); //Agrega la sección existente o nueva al mapa con su nombre como clave
        }
        return secciones;  //Devuelve el mapa de secciones
    }
//--------------------------------------------------------------------------------------------------------------------------------

    
//    Metodo auxiliar: aplicarSolucion
//    Propósito: Aplicar la solución encontrada, guardando las asignaciones en la base de datos y actualizando las inscripciones de los estudiantes
    private void aplicarSolucion(

            List<AsignacionProblema> solucion, //lista de asignaciones encontradas como solución
            Map<Integer, List<Estudiante>> estudiantesPorSeccion) { //mapa que organiza a los estudiantes por sección

        System.out.println("\n=== APLICANDO SOLUCIÓN A BASE DE DATOS ===");
        System.out.println("Total asignaciones a guardar: " + solucion.size());

        // Agrupa todas las asignaciones de la solución por el nombre de la sección (ej. "A", "B").
        Map<String, List<AsignacionProblema>> porSeccion = solucion.stream().collect(Collectors.groupingBy(AsignacionProblema::getNombreSeccion));


        int asignacionesGuardadas = 0; //Contador para llevar el registro de cuántas asignaciones se han guardado en la base de datos

        for (Map.Entry<String, List<AsignacionProblema>> entry : porSeccion.entrySet()) { // Itera sobre cada sección que tiene asignaciones.
            String nombreSeccion = entry.getKey(); //
            List<AsignacionProblema> asignacionesSeccion = entry.getValue();

            System.out.println("\n--- Procesando Sección " + nombreSeccion + " ---");

            // 1. Obtener los objetos de la base de datos (Sección y Aula)
            // Se busca la Sección y si no existe, lanza una excepción
            Seccion seccion = seccionRepositorio.findByNombreSeccion(nombreSeccion)
                    .orElseThrow(() -> new RuntimeException("Sección no encontrada: " + nombreSeccion));

            //usan la misma aula para todas las asignaciones en esta sección
            String nombreAula = asignacionesSeccion.get(0).getAulaNombre();
            Aula aula = aulaRepositorio.findByNombreAula(nombreAula)
                    .orElseThrow(() -> new RuntimeException("Aula no encontrada: " + nombreAula));
            System.out.println("Sección ID: " + seccion.getIdSeccion() + " | Aula: " + aula.getNombreAula());

            // Itera sobre CADA CURSO de esta sección para crear/actualizar la asignación.
            for (AsignacionProblema ap : asignacionesSeccion) {
                Curso curso = cursoRepositorio.findByCodigoCurso(ap.getCursoCodigo()).orElse(null);
                if (curso == null) continue; // Si el curso no existe, salta al siguiente.

                // 2. Obtener los objetos de la base de datos (Curso, Catedrático y Horario)
                Catedratico catedratico = catedraticoRepositorio.findById(ap.getCatedraticoDpi()).orElse(null);
                if (catedratico == null) continue; // Si el catedrático no existe, salta al siguiente.

                Horario horario = horarioRepositorio.findByHoraInicio(ap.getHoraInicio().toLocalTime()).orElse(null);
                if (horario == null) continue; // Si el horario no existe, salta al siguiente.

                // 3. Verificar si la asignación ya existe (para evitar duplicados o para actualizar)
                Optional<Asignacion> asignacionExistente = asignacionRepositorio.findAll().stream()
                        .filter(a -> a.getCurso().getIdCurso().equals(curso.getIdCurso()) &&
                                a.getSeccion().getIdSeccion().equals(seccion.getIdSeccion()) &&
                                a.getSemestre() == 1 && a.getAnio() == 2025) //Filtra por semestre = 1 y año = 2025
                        .findFirst();

                Asignacion asignacion;
                if (asignacionExistente.isPresent()) {
                    // Si ya existe, SE ACTULIZA LA ASIGNACION
                    asignacion = asignacionExistente.get();
                    asignacion.setCatedratico(catedratico);
                    asignacion.setAula(aula);
                    asignacion.setHorario(horario);
                    System.out.println("Actualizando: " + curso.getNombreCurso());
                } else {
                    // Si no existe, SE CREA UNA NUEVA ASIGNACION
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
                //guarda la asignación en la base de datos
                asignacion = asignacionRepositorio.save(asignacion);
                asignacionesGuardadas++; //Incrementa el contador de asignaciones guardadas

                // Actualizar las inscripciones de los estudiantes para esta asignación
                List<Estudiante> estudiantes = estudiantesPorSeccion.get(seccion.getIdSeccion());
                if (estudiantes != null) {
                    //Si hay estudiantes asignados a esta sección, llama al método para actualizar sus inscripciones
                    actualizarInscripcionesParaAsignacion(asignacion, estudiantes);
                }
            }
        }
        System.out.println("\nTotal asignaciones guardadas: " + asignacionesGuardadas);
    }
    //--------------------------------------------------------------------------------------------------------------------------------

    // Metodo auxiliar: crearAsignacionProblema
// Propósito: Crear un objeto AsignacionProblema con los detalles de la asignación temporalmente encontrada
    private AsignacionProblema crearAsignacionProblema(
            //Todo estos son los parametros que recibe para crear la asignacion
            Curso curso, Seccion seccion, Catedratico catedratico,
            Aula aula, Horario horario, List<Estudiante> estudiantes) {

        //Instancia el objeto AsignacionProblema y le asigna todos los datos necesarios
        AsignacionProblema dto = new AsignacionProblema();
        //aQUI COLOCA TODOS LOS DATOS EN EL DTO (AsignacionProblema)
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

        return dto; //Retorna el objeto AsignacionProblema creado temporalmente
    }
    //--------------------------------------------------------------------------------------------------------------------------------

    // Metodo auxiliar: organizarEstudiantesPorSecciones
    // Propósito: Organizar a los estudiantes en secciones de hasta 25 estudiantes cada una, buscando un balance
    private Map<Integer, List<Estudiante>> organizarEstudiantesPorSecciones(
            List<Estudiante> estudiantes) {

        System.out.println("\n=== ORGANIZANDO ESTUDIANTES EN SECCIONES ===");
        System.out.println("Total estudiantes a distribuir: " + estudiantes.size());

        // Calcula el número de secciones necesarias,  un máximo de 25 estudiantes por sección
//        el math.ceil funcion redondea hacia arriba para asegurar que haya suficientes secciones,
//        ejemplo si hay 51 estudiantes, se necesitan 3 secciones 51/25 = 2.04, redondeado hacia arriba es 3 entonces se crean 3 secciones
        int seccionesNecesarias = (int) Math.ceil((double) estudiantes.size() / 25.0);
        System.out.println("Secciones necesarias: " + seccionesNecesarias);

//        Se obtiene o se crean las secciones necesarias, por el metodo obtenerSeccionesRequeridas
        Map<String, Seccion> seccionesMap = obtenerSeccionesRequeridas(seccionesNecesarias);
        List<Seccion> secciones = new ArrayList<>(seccionesMap.values());

        System.out.println("Secciones disponibles en BD: " + secciones.size());
        secciones.forEach(s -> System.out.println("  - Sección " + s.getNombreSeccion() + " (ID: " + s.getIdSeccion() + ")")); // Muestra las secciones que se usarán en la distribución.

        // Crea las nuevas agrupaciones de estudiantes en secciones llamando al metodo crearNuevasAgrupaciones
        Map<Integer, List<Estudiante>> distribucion = crearNuevasAgrupaciones(estudiantes, secciones);

        //Este es un reporte en contsola de cómo quedaron distribuidos los estudiantes en las secciones
        distribucion.forEach((id, estuds) -> {
            String nombreSeccion = secciones.stream()
                    .filter(s -> s.getIdSeccion().equals(id))
                    .map(Seccion::getNombreSeccion)
                    .findFirst()
                    .orElse("?");
            System.out.println("  Sección " + nombreSeccion + ": " + estuds.size() + " estudiantes");
        });

        //Retorna la distribución final de estudiantes por sección
        return distribucion;
    }
    //--------------------------------------------------------------------------------------------------------------------------------


    // Metodo auxiliar: crearNuevasAgrupaciones
// Propósito: Crear nuevas agrupaciones de estudiantes en secciones, buscando balancear género y respetar el límite de 25 estudiantes por sección
    private Map<Integer, List<Estudiante>> crearNuevasAgrupaciones(
            List<Estudiante> estudiantes, List<Seccion> secciones) {

        //Mapa para almacenar las agrupaciones de estudiantes por sección
        Map<Integer, List<Estudiante>> grupos = new HashMap<>();

        // Separa los estudiantes por género
        List<Estudiante> hombres = estudiantes.stream()
//        Filtra la lista original para obtener solo estudiantes masculino = M
                .filter(e -> "M".equals(e.getSexo()))
                .collect(Collectors.toList());

        List<Estudiante> mujeres = estudiantes.stream()
//        Filtra la lista original para obtener solo estudiantes femenino = F
                .filter(e -> "F".equals(e.getSexo()))
                .collect(Collectors.toList());

        // Mezcla aleatoriamente las listas de hombres y mujeres
        Collections.shuffle(hombres);
        Collections.shuffle(mujeres);

        // Calcula el número de secciones necesarias, un máximo de 25 estudiantes por sección
//        el math.ceil funcion redondea hacia arriba para asegurar que haya suficientes secciones,
//        ejemplo si hay 51 estudiantes, se necesitan 3 secciones 51/25 = 2.04, redondeado hacia arriba es 3 entonces se crean 3 secciones
        int seccionesNecesarias = (int) Math.ceil((double) estudiantes.size() / 25.0);

        // sI el número de secciones disponibles es menor que las necesarias, lanza una excepción
        if (secciones.size() < seccionesNecesarias) {
            throw new IllegalStateException(
                    "Insuficientes secciones. Necesarias: " + seccionesNecesarias +
                            ", Disponibles: " + secciones.size());
        }

        // Inicializa las listas vacías para cada sección necesaria
//        Itera solo hasta el número de secciones que realmente se necesitan usar
        for (int i = 0; i < seccionesNecesarias; i++) {
            grupos.put(secciones.get(i).getIdSeccion(), new ArrayList<>());
        }


        Integer[] seccionIds = grupos.keySet().toArray(new Integer[0]); //Obtiene los IDs de las secciones que se van a usar

        int idxHombre = 0, idxMujer = 0; //INIcializa los índices para rastrear la posición actual en las listas de hombres y mujeres

        // eSTE FOR Distribuye los estudiantes alternando entre hombres y mujeres para balancear el género en cada sección
        for (int i = 0; i < estudiantes.size(); i++) { // Itera sobre el total de estudiantes a distribuir
            Integer seccionId = seccionIds[i % seccionIds.length]; //Selecciona la sección actual de forma cíclica, es decir va rotando entre las secciones disponibles,
            // ejemplo si hay 3 secciones A,B,C y 10 estudiantes, el primer estudiante va a A, el segundo a B, el tercero a C, el cuarto vuelve a A, el quinto a B, etc.

            // Intenta agregar una mujer primero si hay disponibles y la sección no está llena
            if (idxMujer < mujeres.size() && grupos.get(seccionId).size() < 25) {
                grupos.get(seccionId).add(mujeres.get(idxMujer++));

            } else if (idxHombre < hombres.size() && grupos.get(seccionId).size() < 25) { //Si no hay mujeres disponibles o la sección está llena, intenta agregar un hombre
                grupos.get(seccionId).add(hombres.get(idxHombre++));
            }
        }

        return grupos; //Retorna el mapa final de agrupaciones de estudiantes por sección
    }
    //-------------------------------------------------------------------------------------------------------------------------------


    // Metodo auxiliar: obtenerAulasRequeridas
// Propósito: Obtener el número de aulas requeridas, y si no existen, crearlas
    private Map<Integer, Aula> obtenerAulasRequeridas(int numAulas) {

        Map<Integer, Aula> aulas = new HashMap<>(); //Instancia un mapa para almacenar las aulas

        // Itera desde 0 hasta el número de aulas requeridas ejemplo., 0, 1, 2 si numAulas=3
        for (int i = 0; i < numAulas; i++) {
            String nombreAula = "Salon " + (i + 1); //Genera el nombre del aula, ejemplo "Salon 1", "Salon 2", etc.

            Aula aula = aulaRepositorio.findByNombreAula(nombreAula) //Busca el aula en la base de datos por su nombre, osea "Salon 1", "Salon 2", etc.
                    .orElseGet(() -> { //Si no existe, la crea
                        System.out.println("  Creando aula: " + nombreAula);
                        Aula nueva = new Aula();
                        nueva.setNombreAula(nombreAula);
                        nueva.setCapacidad(25);
                        return aulaRepositorio.save(nueva);
                    });

            aulas.put(aula.getIdAula().intValue(), aula); //Agrega el aula existente o nueva al mapa con su ID como clave
        }

        return aulas; //Retorna el mapa de aulas
    }
    //-------------------------------------------------------------------------------------------------------------------------------

    // Metodo auxiliar: filtrarCatedraticosPorCurso
// Propósito: Filtrar la lista de catedráticos para obtener solo aquellos que pueden enseñar el tipo de curso requerido (ING o LIC)

    private List<Catedratico> filtrarCatedraticosPorCurso(
            Curso curso, List<Catedratico> catedraticos) {

        //Extrae el tipo de curso si Necesita ING o LIC y lo convierte a minúsculas para facilitar la comparación
        String tipoCurso = curso.getTipo() != null ? curso.getTipo().toLowerCase() : "";

        //Filtra la lista de catedráticos según el tipo de curso requerido
        return catedraticos.stream()
                .filter(cat -> {
                    if (cat.getDpiCatedratico().equals(DPI_PLACEHOLDER)) { //Si es un placeholder dpi=0, siempre es válido
                        return true;
                    }
                    //Verifica si el catedrático tiene la profesión adecuada para el tipo de curso
                    String profesion = cat.getProfesion().toLowerCase();
                    if ("ing".equals(tipoCurso)) { //Si el curso es de tipo ING, busca "ingenier"  en la profesión del catedrático
                        return profesion.contains("ingenier");
                    } else if ("lic".equals(tipoCurso)) { //Si el curso es de tipo LIC, busca "licencia" en la profesión del catedrático
                        return profesion.contains("licencia");
                    }
                    return false;
                })
                .collect(Collectors.toList()); //Recopila y retorna la lista final de catedráticos que pueden enseñar este curso
    }
    //-------------------------------------------------------------------------------------------------------------------------------

    // Metodo auxiliar: obtenerHorariosValidos
// Propósito: Obtener la lista de horarios válidos, excluyendo aquellos que no se deben usar (ej. 1:00 PM)
    private List<Horario> obtenerHorariosValidos() {
        return horarioRepositorio.findAll().stream() //Obtiene todos los horarios de la base de datos
                .filter(h -> !h.getHoraInicio().equals(LocalTime.of(13, 0))) //Filtra para excluir los horarios que comienzan a la 1:00 PM por la hora de almuerzo
                .sorted(Comparator.comparing(Horario::getHoraInicio)) //Ordena los horarios restantes por hora de inicio
                .collect(Collectors.toList()); //Recopila y retorna la lista final de horarios válidos
    }
    //-------------------------------------------------------------------------------------------------------------------------------

    // Metodo auxiliar: mostrarResumen
// Propósito: Mostrar un resumen de la solución encontrad en la consola, incluyendo advertencias si hay asignaciones sin catedrático real
    private void mostrarResumen(List<AsignacionProblema> resultados) {
        System.out.println("\n=== RESUMEN ===");
        System.out.println("Total asignaciones: " + resultados.size());

        // Contar asignaciones con placeholder
        long asignacionesPendientes = resultados.stream()
                .filter(ap -> ap.getCatedraticoDpi().equals(DPI_PLACEHOLDER))
                .count();

        if (asignacionesPendientes > 0) {
            System.out.println(+ asignacionesPendientes + " ASIGNACIONES SIN CATEDRÁTICO");
            System.out.println("Contratar " + asignacionesPendientes + " catedrático(s) adicional(es)");
        }

    }
}