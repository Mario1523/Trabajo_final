package com.monitoreo;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

// Imports de iText para generación de PDFs
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

/**
 * Clase responsable de generar reportes sobre el estado y rendimiento del sistema de monitoreo.
 * Todos los reportes se generan exclusivamente en formato PDF.
 */
public class GeneradorReportes {
    private final String directorio;              // Directorio donde se guardan los reportes
    private final DateTimeFormatter formatter;     // Formateador de fecha y hora
    private final Map<String, HostEstadisticas> estadisticas; // Estadísticas de todos los hosts

    public GeneradorReportes(String directorio, Map<String, HostEstadisticas> estadisticas) {
        // Asegurar que el directorio existe
        File dir = new File(directorio);
        if (!dir.exists()) {
            dir.mkdirs();
            System.out.println("Directorio de reportes creado: " + dir.getAbsolutePath());
        }
        this.directorio = dir.getAbsolutePath();
        this.estadisticas = estadisticas;
        this.formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    }

    /**
     * Genera un reporte diario detallado en formato PDF con información de todos los hosts monitoreados.
     * El reporte incluye estadísticas individuales y un resumen general del sistema.
     * 
     * @return Ruta del archivo PDF generado, o null si hubo error
     */
    public String generarReporteDiario() {
        // Asegurar que el directorio existe antes de generar el PDF
        File dir = new File(directorio);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        String nombreArchivo = directorio + File.separator + "reporte_diario_" + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";
        
        try {
            System.out.println("Generando reporte diario PDF en: " + nombreArchivo);
            PdfWriter writer = new PdfWriter(nombreArchivo);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            
            // Título del documento
            Paragraph titulo = new Paragraph("REPORTE DIARIO DE MONITOREO")
                .setFontSize(20)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
            document.add(titulo);
            
            // Fecha y hora del reporte
            Paragraph fecha = new Paragraph("Fecha: " + LocalDateTime.now().format(formatter))
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(30);
            document.add(fecha);
            
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
                
                // Crear tabla de estadísticas para cada dispositivo
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
            
            // Tabla de resumen
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
            
            // Pie de página
            Paragraph pie = new Paragraph("Reporte generado automáticamente por el Sistema de Monitoreo de Redes")
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(30)
                .setFontColor(ColorConstants.GRAY);
            document.add(pie);
            
            document.close();
            
            System.out.println("Reporte diario PDF generado exitosamente: " + nombreArchivo);
            return nombreArchivo;
            
        } catch (Exception e) {
            System.err.println("Error al generar el reporte diario PDF: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Genera un reporte específico de disponibilidad en formato PDF para todos los hosts.
     * Incluye porcentajes individuales y el promedio general del sistema.
     * 
     * @return Ruta del archivo PDF generado, o null si hubo error
     */
    public String generarReporteDisponibilidad() {
        // Asegurar que el directorio existe antes de generar el PDF
        File dir = new File(directorio);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        String nombreArchivo = directorio + File.separator + "reporte_disponibilidad_" + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";
        
        try {
            System.out.println("Generando reporte de disponibilidad PDF en: " + nombreArchivo);
            PdfWriter writer = new PdfWriter(nombreArchivo);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            
            // Título del documento
            Paragraph titulo = new Paragraph("REPORTE DE DISPONIBILIDAD")
                .setFontSize(20)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
            document.add(titulo);
            
            // Fecha y hora del reporte
            Paragraph fecha = new Paragraph("Fecha: " + LocalDateTime.now().format(formatter))
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(30);
            document.add(fecha);
            
            // Tabla de disponibilidad por host
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
                
                // Celda con color según disponibilidad
                Cell disponibilidadCell = new Cell().add(new Paragraph(String.format("%.2f%%", disponibilidad)));
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
            
            // Resumen con disponibilidad promedio
            double disponibilidadPromedio = estadisticas.size() > 0 ? disponibilidadTotal / estadisticas.size() : 0;
            
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
            Cell promedioCell = new Cell().add(new Paragraph(String.format("%.2f%%", disponibilidadPromedio)));
            if (disponibilidadPromedio >= 95) {
                promedioCell.setBackgroundColor(ColorConstants.GREEN);
            } else if (disponibilidadPromedio >= 80) {
                promedioCell.setBackgroundColor(ColorConstants.YELLOW);
            } else {
                promedioCell.setBackgroundColor(ColorConstants.RED);
            }
            tablaResumen.addCell(promedioCell);
            
            document.add(tablaResumen);
            
            // Pie de página
            Paragraph pie = new Paragraph("Reporte generado automáticamente por el Sistema de Monitoreo de Redes")
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(30)
                .setFontColor(ColorConstants.GRAY);
            document.add(pie);
            
            document.close();
            
            System.out.println("Reporte de disponibilidad PDF generado exitosamente: " + nombreArchivo);
            return nombreArchivo;
            
        } catch (Exception e) {
            System.err.println("Error al generar el reporte de disponibilidad PDF: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Genera un reporte completo en formato PDF sobre las redes monitoreadas.
     * Incluye información detallada de todos los dispositivos, estadísticas y resumen general.
     * @param dispositivos Lista de dispositivos monitoreados
     * @return Ruta del archivo PDF generado, o null si hubo error o las dependencias no están disponibles
     */
    public String generarPDF(List<Dispositivos> dispositivos) {
        // Asegurar que el directorio existe antes de generar el PDF
        File dir = new File(directorio);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        String nombreArchivo = directorio + File.separator + "informe_redes_" + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";
        
        try {
            System.out.println("Generando informe PDF en: " + nombreArchivo);
            PdfWriter writer = new PdfWriter(nombreArchivo);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            
            // Título del documento
            Paragraph titulo = new Paragraph("INFORME DE REDES MONITOREADAS")
                .setFontSize(20)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
            document.add(titulo);
            
            // Fecha y hora del reporte
            Paragraph fecha = new Paragraph("Fecha: " + LocalDateTime.now().format(formatter))
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(30);
            document.add(fecha);
            
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
            
            // Tabla de resumen
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
            
            // Tabla de dispositivos
            Table tablaDispositivos = new Table(UnitValue.createPercentArray(new float[]{2, 2, 1.5f, 1.5f, 1.5f, 1.5f, 1.5f}))
                .useAllAvailableWidth()
                .setMarginBottom(20);
            
            // Encabezados
            tablaDispositivos.addHeaderCell(new Cell().add(new Paragraph("Dispositivo").setBold()));
            tablaDispositivos.addHeaderCell(new Cell().add(new Paragraph("IP").setBold()));
            tablaDispositivos.addHeaderCell(new Cell().add(new Paragraph("Estado").setBold()));
            tablaDispositivos.addHeaderCell(new Cell().add(new Paragraph("Disponibilidad").setBold()));
            tablaDispositivos.addHeaderCell(new Cell().add(new Paragraph("Tiempo Resp.").setBold()));
            tablaDispositivos.addHeaderCell(new Cell().add(new Paragraph("Chequeos").setBold()));
            tablaDispositivos.addHeaderCell(new Cell().add(new Paragraph("Fallos").setBold()));
            
            // Datos de cada dispositivo
            for (Dispositivos dispositivo : dispositivos) {
                HostEstadisticas stats = estadisticas.get(dispositivo.getId());
                
                if (stats != null) {
                    tablaDispositivos.addCell(dispositivo.getId());
                    tablaDispositivos.addCell(dispositivo.getDireccionIP());
                    
                    // Estado con color
                    Cell estadoCell = new Cell().add(new Paragraph(dispositivo.getEstado()));
                    if (dispositivo.getEstado().equals("ACTIVO")) {
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
            }
            
            document.add(tablaDispositivos);
            
            // Estadísticas detalladas por dispositivo
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
                
                Paragraph info = new Paragraph(
                    String.format("Disponibilidad: %.2f%% | Tiempo de respuesta promedio: %.2f ms | " +
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
            
            // Pie de página
            Paragraph pie = new Paragraph("Reporte generado automáticamente por el Sistema de Monitoreo de Redes")
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(30)
                .setFontColor(ColorConstants.GRAY);
            document.add(pie);
            
            document.close();
            
            System.out.println("Informe PDF generado exitosamente: " + nombreArchivo);
            return nombreArchivo;
            
        } catch (Exception e) {
            System.err.println("Error al generar el reporte PDF: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}