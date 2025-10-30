package com.monitoreo;

import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Clase que mantiene las estadísticas de disponibilidad y rendimiento de un host.
 * Registra información sobre chequeos, fallos y calcula la disponibilidad.
 */
public class HostEstadisticas {
    private final String host;                     // Nombre o dirección IP del host
    private int totalChequeos;                     // Contador total de verificaciones realizadas
    private int fallos;                           // Contador de fallos detectados
    private LocalDateTime ultimoChequeo;          // Fecha y hora del último chequeo
    private LocalDateTime ultimoFallo;            // Fecha y hora del último fallo
    private final ArrayList<LocalDateTime> historicoFallos; // Registro histórico de fallos
    private double disponibilidad;                // Porcentaje de disponibilidad calculado

    /**
     * Constructor que inicializa las estadísticas para un host específico.
     * @param host Nombre o dirección IP del host a monitorear
     */
    public HostEstadisticas(String host) {
        this.host = host;
        this.totalChequeos = 0;
        this.fallos = 0;
        this.historicoFallos = new ArrayList<>();
        this.disponibilidad = 100.0;
    }

    /**
     * Registra el resultado de un chequeo de disponibilidad.
     * Actualiza los contadores y el histórico de fallos si es necesario.
     * @param disponible true si el host está disponible, false si no responde
     */
    public void registrarChequeo(boolean disponible) {
        totalChequeos++;
        ultimoChequeo = LocalDateTime.now();
        
        if (!disponible) {
            fallos++;
            ultimoFallo = LocalDateTime.now();
            historicoFallos.add(ultimoFallo);
        }
        
        calcularDisponibilidad();
    }

    /**
     * Calcula el porcentaje de disponibilidad basado en el total de chequeos y fallos.
     * La disponibilidad se calcula como (total_chequeos - fallos) / total_chequeos * 100
     */
    private void calcularDisponibilidad() {
        if (totalChequeos > 0) {
            disponibilidad = ((double)(totalChequeos - fallos) / totalChequeos) * 100;
        }
    }

    /**
     * Genera un resumen completo del estado del host.
     * @return String con información detallada sobre disponibilidad, chequeos y fallos
     */
    public String getResumen() {
        return String.format("Host: %s\nDisponibilidad: %.2f%%\nTotal chequeos: %d\nFallos: %d\nÚltimo chequeo: %s\nÚltimo fallo: %s",
            host, disponibilidad, totalChequeos, fallos, 
            ultimoChequeo != null ? ultimoChequeo.toString() : "N/A",
            ultimoFallo != null ? ultimoFallo.toString() : "N/A");
    }

    public ArrayList<LocalDateTime> getHistoricoFallos() {
        return new ArrayList<>(historicoFallos);
    }

    public double getDisponibilidad() {
        return disponibilidad;
    }
}