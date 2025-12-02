package com.monitoreo;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Clase responsable de generar reportes sobre el estado y rendimiento del sistema de monitoreo.
 * Todos los reportes se generan exclusivamente en formato PDF.
 */
public class GeneradorReportes {
    private final String directorio;              // Directorio donde se guardan los reportes
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
    }

    /**
     * Genera un reporte diario detallado en formato PDF con información de todos los hosts monitoreados.
     * El reporte incluye estadísticas individuales y un resumen general del sistema.
     * 
     * @return Ruta del archivo PDF generado, o null si hubo error
     */
    public String generarReporteDiario() {
        return new ReporteDiarioPDF(directorio, estadisticas).generar();
    }

    /**
     * Genera un reporte específico de disponibilidad en formato PDF para todos los hosts.
     * Incluye porcentajes individuales y el promedio general del sistema.
     * 
     * @return Ruta del archivo PDF generado, o null si hubo error
     */
    public String generarReporteDisponibilidad() {
        return new ReporteDisponibilidadPDF(directorio, estadisticas).generar();
    }


    /**
     * Genera un reporte completo en formato PDF sobre las redes monitoreadas.
     * Incluye información detallada de todos los dispositivos, estadísticas y resumen general.
     * @param dispositivos Lista de dispositivos monitoreados
     * @return Ruta del archivo PDF generado, o null si hubo error o las dependencias no están disponibles
     */
    public String generarPDF(List<Dispositivos> dispositivos) {
        return new InformeRedesPDF(directorio, estadisticas, dispositivos).generar();
    }
}