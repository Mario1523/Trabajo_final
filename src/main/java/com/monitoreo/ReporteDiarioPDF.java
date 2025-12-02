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
 * Reporte diario detallado de monitoreo.
 */
public class ReporteDiarioPDF extends ReportePDFBase {

    private final Map<String, HostEstadisticas> estadisticas;

    public ReporteDiarioPDF(String directorio, Map<String, HostEstadisticas> estadisticas) {
        super(directorio);
        this.estadisticas = estadisticas;
    }

    public String generar() {
        String nombreArchivo = directorio + java.io.File.separator + "reporte_diario_"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                + ".pdf";
        System.out.println("Generando reporte diario PDF en: " + nombreArchivo);
        return generarDocumento(nombreArchivo);
    }

    @Override
    protected void escribirContenido(Document document) throws Exception {
        // Título y fecha
        document.add(crearTitulo("REPORTE DIARIO DE MONITOREO"));
        document.add(crearParrafoFecha());

        // Estadísticas por dispositivo
        Paragraph dispositivosTitulo = new Paragraph("ESTADÍSTICAS POR DISPOSITIVO")
                .setFontSize(16)
                .setBold()
                .setMarginTop(20)
                .setMarginBottom(10);
        document.add(dispositivosTitulo);

        for (Map.Entry<String, HostEstadisticas> entrada : estadisticas.entrySet()) {
            HostEstadisticas stats = entrada.getValue();

            Paragraph dispositivoTitulo = new Paragraph("Dispositivo: " + entrada.getKey())
                    .setFontSize(14)
                    .setBold()
                    .setMarginTop(15)
                    .setMarginBottom(5);
            document.add(dispositivoTitulo);

            Table tablaStats = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                    .useAllAvailableWidth()
                    .setMarginBottom(15);

            tablaStats.addHeaderCell(new Cell().add(new Paragraph("Métrica").setBold()));
            tablaStats.addHeaderCell(new Cell().add(new Paragraph("Valor").setBold()));

            tablaStats.addCell("Disponibilidad");
            tablaStats.addCell(String.format("%.2f%%", stats.getDisponibilidad()));

            tablaStats.addCell("Tiempo de respuesta promedio");
            tablaStats.addCell(String.format("%.2f ms", stats.getTiempoRespuestaPromedio()));

            tablaStats.addCell("Total de chequeos");
            tablaStats.addCell(String.valueOf(stats.getTotalChequeos()));

            tablaStats.addCell("Fallos");
            tablaStats.addCell(String.valueOf(stats.getFallos()));

            tablaStats.addCell("Estabilidad");
            tablaStats.addCell(String.format("%.2f%%", stats.getEstabilidad()));

            document.add(tablaStats);
        }

        // Resumen general
        Paragraph resumenTitulo = new Paragraph("RESUMEN GENERAL")
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

        int totalHosts = estadisticas.size();
        int hostsActivos = 0;
        double disponibilidadTotal = 0;
        double tiempoRespuestaTotal = 0;

        for (HostEstadisticas stats : estadisticas.values()) {
            if (stats.getDisponibilidad() > 0) hostsActivos++;
            disponibilidadTotal += stats.getDisponibilidad();
            tiempoRespuestaTotal += stats.getTiempoRespuestaPromedio();
        }

        double disponibilidadPromedio = totalHosts > 0 ? disponibilidadTotal / totalHosts : 0;
        double tiempoRespuestaPromedio = totalHosts > 0 ? tiempoRespuestaTotal / totalHosts : 0;

        tablaResumen.addCell("Total de hosts monitoreados");
        tablaResumen.addCell(String.valueOf(totalHosts));

        tablaResumen.addCell("Hosts activos");
        tablaResumen.addCell(String.valueOf(hostsActivos));

        tablaResumen.addCell("Disponibilidad promedio");
        tablaResumen.addCell(String.format("%.2f%%", disponibilidadPromedio));

        tablaResumen.addCell("Tiempo de respuesta promedio");
        tablaResumen.addCell(String.format("%.2f ms", tiempoRespuestaPromedio));

        document.add(tablaResumen);

        Paragraph pie = new Paragraph("Reporte generado automáticamente por el Sistema de Monitoreo de Redes")
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(30)
                .setFontColor(ColorConstants.GRAY);
        document.add(pie);
    }
}


