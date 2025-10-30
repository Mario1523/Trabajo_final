import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Clase responsable de generar reportes sobre el estado y rendimiento del sistema de monitoreo.
 * Produce informes diarios y reportes de disponibilidad en formato texto.
 */
public class GeneradorReportes {
    private final String directorio;              // Directorio donde se guardan los reportes
    private final DateTimeFormatter formatter;     // Formateador de fecha y hora
    private final Map<String, HostEstadisticas> estadisticas; // Estadísticas de todos los hosts

    public GeneradorReportes(String directorio, Map<String, HostEstadisticas> estadisticas) {
        this.directorio = directorio;
        this.estadisticas = estadisticas;
        this.formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    }

    /**
     * Genera un reporte diario detallado con información de todos los hosts monitoreados.
     * El reporte incluye estadísticas individuales y un resumen general del sistema.
     */
    public void generarReporteDiario() {
        String nombreArchivo = directorio + "/reporte_" + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".txt";
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(nombreArchivo))) {
            writer.write("=== Reporte de Monitoreo - " + 
                LocalDateTime.now().format(formatter) + " ===\n\n");

            for (Map.Entry<String, HostEstadisticas> entrada : estadisticas.entrySet()) {
                writer.write(entrada.getValue().getResumen());
                writer.write("\n\n");
            }

            writer.write("=== Resumen General ===\n");
            writer.write(generarResumenGeneral());
            
        } catch (IOException e) {
            System.err.println("Error al generar el reporte: " + e.getMessage());
        }
    }

    /**
     * Genera un reporte específico de disponibilidad para todos los hosts.
     * Incluye porcentajes individuales y el promedio general del sistema.
     */
    public void generarReporteDisponibilidad() {
        String nombreArchivo = directorio + "/disponibilidad_" + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".txt";
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(nombreArchivo))) {
            writer.write("=== Reporte de Disponibilidad - " + 
                LocalDateTime.now().format(formatter) + " ===\n\n");

            double disponibilidadTotal = 0;
            for (Map.Entry<String, HostEstadisticas> entrada : estadisticas.entrySet()) {
                writer.write(String.format("Host: %s - Disponibilidad: %.2f%%\n", 
                    entrada.getKey(), entrada.getValue().getDisponibilidad()));
                disponibilidadTotal += entrada.getValue().getDisponibilidad();
            }

            double disponibilidadPromedio = disponibilidadTotal / estadisticas.size();
            writer.write(String.format("\nDisponibilidad promedio del sistema: %.2f%%", 
                disponibilidadPromedio));
            
        } catch (IOException e) {
            System.err.println("Error al generar el reporte de disponibilidad: " + e.getMessage());
        }
    }

    /**
     * Genera un resumen general del estado del sistema.
     * @return String con estadísticas globales del sistema de monitoreo
     */
    private String generarResumenGeneral() {
        StringBuilder resumen = new StringBuilder();
        int totalHosts = estadisticas.size();
        int hostsActivos = 0;
        double disponibilidadTotal = 0;

        for (HostEstadisticas stats : estadisticas.values()) {
            if (stats.getDisponibilidad() > 0) hostsActivos++;
            disponibilidadTotal += stats.getDisponibilidad();
        }

        resumen.append(String.format("Total de hosts monitoreados: %d\n", totalHosts));
        resumen.append(String.format("Hosts activos: %d\n", hostsActivos));
        resumen.append(String.format("Disponibilidad promedio: %.2f%%\n", 
            disponibilidadTotal / totalHosts));

        return resumen.toString();
    }
}