package com.example.proyectohorario_1.Controlador;


import com.example.proyectohorario_1.Servicio.ImportacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/importacion")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ImportacionController {

    private final ImportacionService importacionService;

    /**
     * Endpoint para importar desde Excel con DOS HOJAS:
     * Hoja 1: "Estudiantes"
     * Hoja 2: "Inscripciones"
     */
    @PostMapping("/completo")
    public ResponseEntity<Map<String, Object>> importarCompleto(
            @RequestParam("archivo") MultipartFile archivo) {

        Map<String, Object> response = new HashMap<>();

        try {
            if (archivo.isEmpty()) {
                response.put("success", false);
                response.put("mensaje", "El archivo está vacío");
                return ResponseEntity.badRequest().body(response);
            }

            if (!archivo.getOriginalFilename().endsWith(".xlsx") &&
                    !archivo.getOriginalFilename().endsWith(".xls")) {
                response.put("success", false);
                response.put("mensaje", "Solo se permiten archivos Excel (.xlsx, .xls)");
                return ResponseEntity.badRequest().body(response);
            }

            Map<String, Object> resultado = importacionService.importarCompleto(archivo);

            response.put("success", true);
            response.put("mensaje", "Importación completada");
            response.putAll(resultado);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("mensaje", "Error al importar: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}