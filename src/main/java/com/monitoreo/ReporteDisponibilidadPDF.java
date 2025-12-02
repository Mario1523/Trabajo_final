package com.monitoreo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

/**
 * Reporte específico de disponibilidad por host.
 */
public class ReporteDisponibilidadPDF extends ReportePDFBase {

    private final Map<String, HostEstadisticas> estadisticas;

    public ReporteDisponibilidadPDF(String directorio, Map<String, HostEstadisticas> estadisticas) {
        super(directorio);
        this.estadisticas = estadisticas;
    }

    public String generar() {
        String nombreArchivo = directorio + java.io.File.separator + "reporte_disponibilidad_"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                + ".pdf";
        System.out.println("Generando reporte de disponibilidad PDF en: " + nombreArchivo);
        return generarDocumento(nombreArchivo);
    }

    @Override
    protected void escribirContenido(Document document) throws Exception {
        document.add(crearTitulo("REPORTE DE DISPONIBILIDAD"));
        document.add(crearParrafoFecha());

        Paragraph disponibilidadTitulo = new Paragraph("DISPONIBILIDAD POR HOST")
                .setFontSize(16)
                .setBold()
                .setMarginTop(20)
                .setMarginBottom(10);
        document.add(disponibilidadTitulo);

        Table tablaDisponibilidad = new Table(UnitValue.createPercentArray(new float[]{2, 1}))
                .useAllAvailableWidth()
                .setMarginBottom(30);

        tablaDisponibilidad.addHeaderCell(new Cell().add(new Paragraph("Host").setBold()));
        tablaDisponibilidad.addHeaderCell(new Cell().add(new Paragraph("Disponibilidad").setBold()));

        double disponibilidadTotal = 0;
        for (Map.Entry<String, HostEstadisticas> entrada : estadisticas.entrySet()) {
            double disponibilidad = entrada.getValue().getDisponibilidad();
            disponibilidadTotal += disponibilidad;

            tablaDisponibilidad.addCell(entrada.getKey());

            Cell disponibilidadCell = new Cell()
                    .add(new Paragraph(String.format("%.2f%%", disponibilidad)));
            if (disponibilidad >= 95) {
                disponibilidadCell.setBackgroundColor(ColorConstants.GREEN);
            } else if (disponibilidad >= 80) {
                disponibilidadCell.setBackgroundColor(ColorConstants.YELLOW);
            } else {
                disponibilidadCell.setBackgroundColor(ColorConstants.RED);
            }
            tablaDisponibilidad.addCell(disponibilidadCell);
        }
        document.add(tablaDisponibilidad);

        double disponibilidadPromedio =
                estadisticas.size() > 0 ? disponibilidadTotal / estadisticas.size() : 0;

        Paragraph resumenTitulo = new Paragraph("RESUMEN")
                .setFontSize(16)
                .setBold()
                .setMarginTop(20)
                .setMarginBottom(10);
        document.add(resumenTitulo);

        Table tablaResumen = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .useAllAvailableWidth()
                .setMarginBottom(20);

        tablaResumen.addHeaderCell(new Cell().add(new Paragraph("Métrica").setBold()));
        tablaResumen.addHeaderCell(new Cell().add(new Paragraph("Valor").setBold()));

        tablaResumen.addCell("Total de hosts");
        tablaResumen.addCell(String.valueOf(estadisticas.size()));

        tablaResumen.addCell("Disponibilidad promedio del sistema");
        Cell promedioCell = new Cell()
                .add(new Paragraph(String.format("%.2f%%", disponibilidadPromedio)));
        if (disponibilidadPromedio >= 95) {
            promedioCell.setBackgroundColor(ColorConstants.GREEN);
        } else if (disponibilidadPromedio >= 80) {
            promedioCell.setBackgroundColor(ColorConstants.YELLOW);
        } else {
            promedioCell.setBackgroundColor(ColorConstants.RED);
        }
        tablaResumen.addCell(promedioCell);

        document.add(tablaResumen);

        Paragraph pie = new Paragraph("Reporte generado automáticamente por el Sistema de Monitoreo de Redes")
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(30)
                .setFontColor(ColorConstants.GRAY);
        document.add(pie);
    }
}


