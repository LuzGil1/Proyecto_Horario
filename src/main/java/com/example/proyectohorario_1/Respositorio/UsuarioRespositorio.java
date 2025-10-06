package com.example.proyectohorario_1.Respositorio;

import com.example.proyectohorario_1.Entidad.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRespositorio extends JpaRepository<Usuario, Long> {

//    aQUI PUEDE SER POR ESTO QUE NO FUNCION
//Usuario findByEmailAndPassword(String email, String password);


    Optional<Usuario> findByEmail(String email);


    Optional<Usuario> findByEmailAndPassword(String email, String password);

    @Query(value = """
            SELECT cu.nombre_curso,
                   h.dia_semana,
                   h.hora_inicio,
                   h.hora_fin,
                   ca.nombre_catedratico
            FROM usuarios u
            JOIN estudiantes e ON u.id_usuario = e.id_usuario
            JOIN inscripciones ic ON e.carne_estudiante = ic.carne_estudiante
            JOIN cursos cu ON ic.id_curso = cu.id_curso
            LEFT JOIN asignacion a ON cu.id_curso = a.id_curso
            LEFT JOIN catedraticos ca ON a.id_catedratico = ca.dpi_catedratico
            LEFT JOIN horario h ON a.id_horario = h.id_horario
            WHERE u.id_usuario = :idUsuario;
            """, nativeQuery = true)
    List<Object[]> obtenerHorarioPorUsuario(Long idUsuario);


}
