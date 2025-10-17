package com.example.proyectohorario_1.Servicio;


import com.example.proyectohorario_1.DTO.CatedraticoDTO;
import com.example.proyectohorario_1.DTO.catedraticoInfoDTO;
import com.example.proyectohorario_1.Respositorio.CatedraticoRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CatedraticoService {

    private final CatedraticoRepositorio catedraticoRepositorio;



    public List<CatedraticoDTO> obtenerEstadoCarga() {
        List<Object[]> resultados = catedraticoRepositorio.obtenerEstadoCargaCatedraticos();

        return resultados.stream().map(resultado -> {
            Short cargaMinimaShort = (Short) resultado[4];
            Short cargaMaximaShort = (Short) resultado[5];

            return new CatedraticoDTO(
                    (String) resultado[0],
                    (String) resultado[1],
                    (String) resultado[2],
                    ((Number) resultado[3]).longValue(), // Usa Number para una conversión segura
                    cargaMinimaShort != null ? cargaMinimaShort.intValue() : null,
                    cargaMaximaShort != null ? cargaMaximaShort.intValue() : null,
                    (String) resultado[6],
                    ((Number) resultado[7]).intValue()
            );
        }).collect(Collectors.toList());
    }
}