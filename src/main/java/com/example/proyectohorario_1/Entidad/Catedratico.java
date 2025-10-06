package com.example.proyectohorario_1.Entidad;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "catedraticos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Catedratico {

    @Id
    @Column(name = "dpi_catedratico", nullable = false, length = 15)
    private Long dpiCatedratico;

    @Column(name = "nombre_catedratico", nullable = false, length = 75)
    private String nombreCatedratico;

    @Column(name = "especialidad_catedratico", length = 50)
    private String especialidadCatedratico;

    @Column(name = "email_catedratico", unique = true, length = 50)
    private String emailCatedratico;

    @Column(name = "fecha_contratacion")
    private java.sql.Date fechaContratacion;

    @Column(name = "numero_telefono", length = 15)
    private String numeroTelefono;

    @Column(name = "profesion", length = 75)
    private String profesion;

    @Column(name = "carga_minima")
    private Integer cargaMinima;

    @Column(name = "carga_maxima")
    private Integer cargaMaxima;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    // Dentro de la clase Catedratico
    public String getTipo() {
        if (this.profesion != null) {
            if (this.profesion.toLowerCase().contains("ingenier")) {
                return "ING";
            }
            if (this.profesion.toLowerCase().contains("licenciad")) {
                return "LIC";
            }
        }
        return "OTRO";
    }
}