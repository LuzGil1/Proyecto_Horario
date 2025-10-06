package com.example.proyectohorario_1.Servicio;
import com.example.proyectohorario_1.DTO.HorarioCatedraticoConsultaDTO;
import com.example.proyectohorario_1.DTO.HorarioSeccionDTO;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class HorarioPDFService {

    private final HorarioConsultaService horarioConsultaService;

    // Color morado del tema
    private static final DeviceRgb COLOR_MORADO = new DeviceRgb(107, 79, 183);
    private static final DeviceRgb COLOR_MORADO_CLARO = new DeviceRgb(129, 104, 207);

    public byte[] generarPDFHorarioGeneral() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        // Título
        Paragraph titulo = new Paragraph("HORARIO GENERAL - TODOS LOS CATEDRÁTICOS")
                .setFontSize(18)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(COLOR_MORADO);
        document.add(titulo);

        // Fecha de generación
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        Paragraph fechaGen = new Paragraph("Generado el: " + fecha)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginBottom(10);
        document.add(fechaGen);

        // Obtener datos
        List<HorarioCatedraticoConsultaDTO> horarios = horarioConsultaService.obtenerHorariosCatedraticos();

        // Agrupar por catedrático
        Map<String, List<HorarioCatedraticoConsultaDTO>> porCatedratico = horarios.stream()
                .collect(Collectors.groupingBy(HorarioCatedraticoConsultaDTO::getCatedratico));

        // Generar tabla por cada catedrático
        for (Map.Entry<String, List<HorarioCatedraticoConsultaDTO>> entry : porCatedratico.entrySet()) {
            String catedratico = entry.getKey();
            List<HorarioCatedraticoConsultaDTO> clases = entry.getValue();

            // Subtítulo con nombre del catedrático
            Paragraph subtitulo = new Paragraph("Catedrático: " + catedratico)
                    .setFontSize(12)
                    .setBold()
                    .setFontColor(COLOR_MORADO_CLARO)
                    .setMarginTop(15);
            document.add(subtitulo);

            // Crear tabla
            Table table = new Table(UnitValue.createPercentArray(new float[]{2, 2, 2, 3, 2}))
                    .useAllAvailableWidth();

            // Encabezados
            agregarEncabezado(table, "Día");
            agregarEncabezado(table, "Inicio");
            agregarEncabezado(table, "Fin");
            agregarEncabezado(table, "Curso");
            agregarEncabezado(table, "Sección");

            // Datos
            for (HorarioCatedraticoConsultaDTO h : clases) {
                table.addCell(new Cell().add(new Paragraph(h.getDiaSemana()).setFontSize(9)));
                table.addCell(new Cell().add(new Paragraph(h.getHoraInicio()).setFontSize(9)));
                table.addCell(new Cell().add(new Paragraph(h.getHoraFin()).setFontSize(9)));
                table.addCell(new Cell().add(new Paragraph(h.getCurso()).setFontSize(9)));
                table.addCell(new Cell().add(new Paragraph(h.getSeccion()).setFontSize(9)));
            }

            document.add(table);
        }

        document.close();
        return baos.toByteArray();
    }

    public byte[] generarPDFHorarioPorCatedratico(String nombreCatedratico) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        // Título
        Paragraph titulo = new Paragraph("HORARIO DEL CATEDRÁTICO")
                .setFontSize(18)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(COLOR_MORADO);
        document.add(titulo);

        Paragraph nombre = new Paragraph(nombreCatedratico)
                .setFontSize(14)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(COLOR_MORADO_CLARO);
        document.add(nombre);

        // Fecha
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        Paragraph fechaGen = new Paragraph("Generado el: " + fecha)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginBottom(15);
        document.add(fechaGen);

        // Obtener datos filtrados
        List<HorarioCatedraticoConsultaDTO> horarios = horarioConsultaService.obtenerHorariosCatedraticos()
                .stream()
                .filter(h -> h.getCatedratico().equals(nombreCatedratico))
                .collect(Collectors.toList());

        if (horarios.isEmpty()) {
            document.add(new Paragraph("No se encontraron horarios para este catedrático."));
            document.close();
            return baos.toByteArray();
        }

        // Crear tabla
        Table table = new Table(UnitValue.createPercentArray(new float[]{2, 2, 2, 4, 2}))
                .useAllAvailableWidth();

        // Encabezados
        agregarEncabezado(table, "Día");
        agregarEncabezado(table, "Inicio");
        agregarEncabezado(table, "Fin");
        agregarEncabezado(table, "Curso");
        agregarEncabezado(table, "Sección");

        // Datos
        for (HorarioCatedraticoConsultaDTO h : horarios) {
            table.addCell(new Cell().add(new Paragraph(h.getDiaSemana()).setFontSize(10)));
            table.addCell(new Cell().add(new Paragraph(h.getHoraInicio()).setFontSize(10)));
            table.addCell(new Cell().add(new Paragraph(h.getHoraFin()).setFontSize(10)));
            table.addCell(new Cell().add(new Paragraph(h.getCurso()).setFontSize(10)));
            table.addCell(new Cell().add(new Paragraph(h.getSeccion()).setFontSize(10)));
        }

        document.add(table);
        document.close();
        return baos.toByteArray();
    }

    public byte[] generarPDFHorarioPorSeccion(String nombreSeccion) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        // Título
        Paragraph titulo = new Paragraph("HORARIO DE LA SECCIÓN")
                .setFontSize(18)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(COLOR_MORADO);
        document.add(titulo);

        Paragraph seccion = new Paragraph(nombreSeccion)
                .setFontSize(14)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(COLOR_MORADO_CLARO);
        document.add(seccion);

        // Fecha
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        Paragraph fechaGen = new Paragraph("Generado el: " + fecha)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginBottom(15);
        document.add(fechaGen);

        // Obtener datos filtrados
        List<HorarioSeccionDTO> horarios = horarioConsultaService.obtenerHorariosSecciones()
                .stream()
                .filter(h -> h.getSeccion().equals(nombreSeccion))
                .collect(Collectors.toList());

        if (horarios.isEmpty()) {
            document.add(new Paragraph("No se encontraron horarios para esta sección."));
            document.close();
            return baos.toByteArray();
        }

        // Crear tabla
        Table table = new Table(UnitValue.createPercentArray(new float[]{2, 2, 2, 3, 3, 2}))
                .useAllAvailableWidth();

        // Encabezados
        agregarEncabezado(table, "Día");
        agregarEncabezado(table, "Inicio");
        agregarEncabezado(table, "Fin");
        agregarEncabezado(table, "Curso");
        agregarEncabezado(table, "Catedrático");
        agregarEncabezado(table, "Aula");

        // Datos
        for (HorarioSeccionDTO h : horarios) {
            table.addCell(new Cell().add(new Paragraph(h.getDia()).setFontSize(10)));
            table.addCell(new Cell().add(new Paragraph(h.getInicio()).setFontSize(10)));
            table.addCell(new Cell().add(new Paragraph(h.getFin()).setFontSize(10)));
            table.addCell(new Cell().add(new Paragraph(h.getCursoAsignado()).setFontSize(10)));
            table.addCell(new Cell().add(new Paragraph(h.getCatedratico()).setFontSize(10)));
            table.addCell(new Cell().add(new Paragraph(h.getAula()).setFontSize(10)));
        }

        document.add(table);
        document.close();
        return baos.toByteArray();
    }

    public byte[] generarPDFHorarioGeneralSecciones() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        // Título
        Paragraph titulo = new Paragraph("HORARIO GENERAL - TODAS LAS SECCIONES")
                .setFontSize(18)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(COLOR_MORADO);
        document.add(titulo);

        // Fecha
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        Paragraph fechaGen = new Paragraph("Generado el: " + fecha)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginBottom(10);
        document.add(fechaGen);

        // Obtener datos
        List<HorarioSeccionDTO> horarios = horarioConsultaService.obtenerHorariosSecciones();

        // Agrupar por sección
        Map<String, List<HorarioSeccionDTO>> porSeccion = horarios.stream()
                .collect(Collectors.groupingBy(HorarioSeccionDTO::getSeccion));

        // Generar tabla por cada sección
        for (Map.Entry<String, List<HorarioSeccionDTO>> entry : porSeccion.entrySet()) {
            String seccion = entry.getKey();
            List<HorarioSeccionDTO> clases = entry.getValue();

            // Subtítulo
            Paragraph subtitulo = new Paragraph("Sección: " + seccion)
                    .setFontSize(12)
                    .setBold()
                    .setFontColor(COLOR_MORADO_CLARO)
                    .setMarginTop(15);
            document.add(subtitulo);

            // Crear tabla
            Table table = new Table(UnitValue.createPercentArray(new float[]{2, 2, 2, 3, 3, 2}))
                    .useAllAvailableWidth();

            // Encabezados
            agregarEncabezado(table, "Día");
            agregarEncabezado(table, "Inicio");
            agregarEncabezado(table, "Fin");
            agregarEncabezado(table, "Curso");
            agregarEncabezado(table, "Catedrático");
            agregarEncabezado(table, "Aula");

            // Datos
            for (HorarioSeccionDTO h : clases) {
                table.addCell(new Cell().add(new Paragraph(h.getDia()).setFontSize(9)));
                table.addCell(new Cell().add(new Paragraph(h.getInicio()).setFontSize(9)));
                table.addCell(new Cell().add(new Paragraph(h.getFin()).setFontSize(9)));
                table.addCell(new Cell().add(new Paragraph(h.getCursoAsignado()).setFontSize(9)));
                table.addCell(new Cell().add(new Paragraph(h.getCatedratico()).setFontSize(9)));
                table.addCell(new Cell().add(new Paragraph(h.getAula()).setFontSize(9)));
            }

            document.add(table);
        }

        document.close();
        return baos.toByteArray();
    }

    private void agregarEncabezado(Table table, String texto) {
        Cell cell = new Cell()
                .add(new Paragraph(texto).setBold().setFontSize(10))
                .setBackgroundColor(COLOR_MORADO)
                .setFontColor(ColorConstants.WHITE)
                .setTextAlignment(TextAlignment.CENTER);
        table.addHeaderCell(cell);
    }
}