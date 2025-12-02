package com.monitoreo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

/**
 * Informe completo de redes monitoreadas.
 */
public class InformeRedesPDF extends ReportePDFBase {

    private final Map<String, HostEstadisticas> estadisticas;
    private final List<Dispositivos> dispositivos;

    public InformeRedesPDF(String directorio,
                           Map<String, HostEstadisticas> estadisticas,
                           List<Dispositivos> dispositivos) {
        super(directorio);
        this.estadisticas = estadisticas;
        this.dispositivos = dispositivos;
    }

    public String generar() {
        String nombreArchivo = directorio + java.io.File.separator + "informe_redes_"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                + ".pdf";
        System.out.println("Generando informe PDF en: " + nombreArchivo);
        return generarDocumento(nombreArchivo);
    }

    @Override
    protected void escribirContenido(Document document) throws Exception {
        document.add(crearTitulo("INFORME DE REDES MONITOREADAS"));
        document.add(crearParrafoFecha());

        // Resumen general
        Paragraph resumenTitulo = new Paragraph("RESUMEN GENERAL")
                .setFontSize(16)
                .setBold()
                .setMarginTop(20)
                .setMarginBottom(10);
        document.add(resumenTitulo);

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

        Table tablaResumen = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .useAllAvailableWidth()
                .setMarginBottom(30);

        tablaResumen.addHeaderCell(new Cell().add(new Paragraph("Métrica").setBold()));
        tablaResumen.addHeaderCell(new Cell().add(new Paragraph("Valor").setBold()));

        tablaResumen.addCell("Total de dispositivos monitoreados");
        tablaResumen.addCell(String.valueOf(totalHosts));

        tablaResumen.addCell("Dispositivos activos");
        tablaResumen.addCell(String.valueOf(hostsActivos));

        tablaResumen.addCell("Disponibilidad promedio");
        tablaResumen.addCell(String.format("%.2f%%", disponibilidadPromedio));

        tablaResumen.addCell("Tiempo de respuesta promedio");
        tablaResumen.addCell(String.format("%.2f ms", tiempoRespuestaPromedio));

        document.add(tablaResumen);

        // Detalle de dispositivos
        Paragraph detalleTitulo = new Paragraph("DETALLE DE DISPOSITIVOS")
                .setFontSize(16)
                .setBold()
                .setMarginTop(20)
                .setMarginBottom(10);
        document.add(detalleTitulo);

        Table tablaDispositivos = new Table(
                UnitValue.createPercentArray(new float[]{2, 2, 1.5f, 1.5f, 1.5f, 1.5f, 1.5f}))
                .useAllAvailableWidth()
                .setMarginBottom(20);

        tablaDispositivos.addHeaderCell(new Cell().add(new Paragraph("Dispositivo").setBold()));
        tablaDispositivos.addHeaderCell(new Cell().add(new Paragraph("IP").setBold()));
        tablaDispositivos.addHeaderCell(new Cell().add(new Paragraph("Estado").setBold()));
        tablaDispositivos.addHeaderCell(new Cell().add(new Paragraph("Disponibilidad").setBold()));
        tablaDispositivos.addHeaderCell(new Cell().add(new Paragraph("Tiempo Resp.").setBold()));
        tablaDispositivos.addHeaderCell(new Cell().add(new Paragraph("Chequeos").setBold()));
        tablaDispositivos.addHeaderCell(new Cell().add(new Paragraph("Fallos").setBold()));

        for (Dispositivos dispositivo : dispositivos) {
            HostEstadisticas stats = estadisticas.get(dispositivo.getId());
            if (stats == null) {
                continue;
            }

            tablaDispositivos.addCell(dispositivo.getId());
            tablaDispositivos.addCell(dispositivo.getDireccionIP());

            Cell estadoCell = new Cell().add(new Paragraph(dispositivo.getEstado()));
            if ("ACTIVO".equals(dispositivo.getEstado())) {
                estadoCell.setBackgroundColor(ColorConstants.GREEN);
            } else {
                estadoCell.setBackgroundColor(ColorConstants.RED);
            }
            tablaDispositivos.addCell(estadoCell);

            tablaDispositivos.addCell(String.format("%.2f%%", stats.getDisponibilidad()));
            tablaDispositivos.addCell(String.format("%.2f ms", stats.getTiempoRespuestaPromedio()));
            tablaDispositivos.addCell(String.valueOf(stats.getTotalChequeos()));
            tablaDispositivos.addCell(String.valueOf(stats.getFallos()));
        }

        document.add(tablaDispositivos);

        // Estadísticas detalladas
        Paragraph estadisticasTitulo = new Paragraph("ESTADÍSTICAS DETALLADAS")
                .setFontSize(16)
                .setBold()
                .setMarginTop(20)
                .setMarginBottom(10);
        document.add(estadisticasTitulo);

        for (Map.Entry<String, HostEstadisticas> entrada : estadisticas.entrySet()) {
            HostEstadisticas stats = entrada.getValue();

            Paragraph dispositivoTitulo = new Paragraph("Dispositivo: " + entrada.getKey())
                    .setFontSize(14)
                    .setBold()
                    .setMarginTop(15)
                    .setMarginBottom(5);
            document.add(dispositivoTitulo);

            Paragraph info = new Paragraph(String.format(
                    "Disponibilidad: %.2f%% | Tiempo de respuesta promedio: %.2f ms | " +
                    "Total de chequeos: %d | Fallos: %d | Estabilidad: %.2f%%",
                    stats.getDisponibilidad(),
                    stats.getTiempoRespuestaPromedio(),
                    stats.getTotalChequeos(),
                    stats.getFallos(),
                    stats.getEstabilidad()))
                    .setFontSize(10)
                    .setMarginBottom(10);
            document.add(info);
        }

        Paragraph pie = new Paragraph("Reporte generado automáticamente por el Sistema de Monitoreo de Redes")
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(30)
                .setFontColor(ColorConstants.GRAY);
        document.add(pie);
    }
}


