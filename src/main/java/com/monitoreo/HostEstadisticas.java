package com.monitoreo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Clase que maneja las estadísticas y métricas de un host
 * Incorpora tanto estadísticas básicas como métricas avanzadas
 */
public class HostEstadisticas {
    private final String host;                     // Nombre o dirección IP del host
    private int totalChequeos;                     // Contador total de verificaciones realizadas
    private int fallos;                           // Contador de fallos detectados
    private LocalDateTime ultimoChequeo;          // Fecha y hora del último chequeo
    private LocalDateTime ultimoFallo;            // Fecha y hora del último fallo
    private final ArrayList<LocalDateTime> historicoFallos; // Registro histórico de fallos
    private double disponibilidad;                // Porcentaje de disponibilidad calculado
    private final Queue<Long> tiemposRespuesta;   // Historial de tiempos de respuesta
    private final Queue<Boolean> estadosRecientes; // Historial de estados recientes
    private final List<Metrica> metricas;         // Lista de métricas configuradas
    private static final int MAX_HISTORIAL = 100;  // Máximo número de registros en el historial

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
        this.tiemposRespuesta = new LinkedList<>();
        this.estadosRecientes = new LinkedList<>();
        this.metricas = new ArrayList<>();
        
        // Inicializar métricas predefinidas
        inicializarMetricasBasicas();
    }

    /**
     * Inicializa las métricas básicas del sistema
     */
    private void inicializarMetricasBasicas() {
        // Métrica de disponibilidad
        metricas.add(new Metrica("Disponibilidad", "Porcentaje de tiempo que el host está disponible") {
            @Override
            public double calcular() {
                return getDisponibilidad();
            }
        });
        
        // Métrica de tiempo de respuesta promedio
        metricas.add(new Metrica("TiempoRespuestaPromedio", "Tiempo de respuesta promedio en ms") {
            @Override
            public double calcular() {
                return getTiempoRespuestaPromedio();
            }
        });
        
        // Métrica de estabilidad
        metricas.add(new Metrica("Estabilidad", "Porcentaje de estabilidad basado en cambios de estado") {
            @Override
            public double calcular() {
                return getEstabilidad();
            }
        });
    }

    /**
     * Registra el resultado de un chequeo de disponibilidad.
     * @param disponible true si el host está disponible, false si no responde
     * @param tiempoRespuesta tiempo de respuesta en milisegundos
     */
    public void registrarChequeo(boolean disponible, long tiempoRespuesta) {
        totalChequeos++;
        ultimoChequeo = LocalDateTime.now();
        
        if (!disponible) {
            fallos++;
            ultimoFallo = LocalDateTime.now();
            historicoFallos.add(ultimoFallo);
        }
        
        // Mantener historial de tiempos de respuesta
        this.tiemposRespuesta.offer(tiempoRespuesta);
        if (this.tiemposRespuesta.size() > MAX_HISTORIAL) {
            this.tiemposRespuesta.poll();
        }

        // Mantener historial de estados
        this.estadosRecientes.offer(disponible);
        if (this.estadosRecientes.size() > MAX_HISTORIAL) {
            this.estadosRecientes.poll();
        }
        
        calcularDisponibilidad();
    }

    /**
     * Calcula el porcentaje de disponibilidad basado en el total de chequeos y fallos.
     */
    private void calcularDisponibilidad() {
        if (totalChequeos > 0) {
            disponibilidad = ((double)(totalChequeos - fallos) / totalChequeos) * 100;
        }
    }

    /**
     * Calcula el tiempo de respuesta promedio
     */
    public double getTiempoRespuestaPromedio() {
        if (tiemposRespuesta.isEmpty()) return 0.0;
        return tiemposRespuesta.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);
    }

    /**
     * Calcula la estabilidad basada en cambios de estado
     */
    public double getEstabilidad() {
        if (estadosRecientes.size() < 2) return 100.0;
        
        int cambios = 0;
        boolean estadoAnterior = estadosRecientes.peek();
        
        for (boolean estado : estadosRecientes) {
            if (estado != estadoAnterior) {
                cambios++;
            }
            estadoAnterior = estado;
        }
        
        return 100.0 * (1.0 - ((double)cambios / estadosRecientes.size()));
    }

    /**
     * Genera un resumen completo del estado del host.
     */
    public String getResumen() {
        return String.format("Host: %s\nDisponibilidad: %.2f%%\nTiempo de respuesta promedio: %.2fms\n" +
            "Estabilidad: %.2f%%\nTotal chequeos: %d\nFallos: %d\nÚltimo chequeo: %s\nÚltimo fallo: %s",
            host, getDisponibilidad(), getTiempoRespuestaPromedio(), getEstabilidad(),
            totalChequeos, fallos, 
            ultimoChequeo != null ? ultimoChequeo.toString() : "N/A",
            ultimoFallo != null ? ultimoFallo.toString() : "N/A");
    }

    /**
     * Agrega una nueva métrica personalizada
     */
    public void agregarMetrica(String nombre, String descripcion, MetricaCalculable calculador) {
        metricas.add(new Metrica(nombre, descripcion) {
            @Override
            public double calcular() {
                return calculador.calcular(HostEstadisticas.this);
            }
        });
    }

    /**
     * Obtiene todas las métricas calculadas
     */
    public List<ResultadoMetrica> obtenerMetricas() {
        List<ResultadoMetrica> resultados = new ArrayList<>();
        LocalDateTime timestamp = LocalDateTime.now();
        
        for (Metrica metrica : metricas) {
            resultados.add(new ResultadoMetrica(
                metrica.getNombre(),
                metrica.getDescripcion(),
                metrica.calcular(),
                timestamp
            ));
        }
        
        return resultados;
    }

    public ArrayList<LocalDateTime> getHistoricoFallos() {
        return new ArrayList<>(historicoFallos);
    }

    public double getDisponibilidad() {
        return disponibilidad;
    }
    
    public int getTotalChequeos() {
        return totalChequeos;
    }
    
    public int getFallos() {
        return fallos;
    }
}

/**
 * Clase abstracta base para métricas
 */
abstract class Metrica {
    private final String nombre;
    private final String descripcion;

    public Metrica(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public abstract double calcular();

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }
}

/**
 * Interfaz para métricas calculables personalizadas
 */
interface MetricaCalculable {
    double calcular(HostEstadisticas estadisticas);
}

/**
 * Clase que representa el resultado de una métrica
 */
class ResultadoMetrica {
    private final String nombre;
    private final String descripcion;
    private final double valor;
    private final LocalDateTime timestamp;

    public ResultadoMetrica(String nombre, String descripcion, double valor, LocalDateTime timestamp) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.valor = valor;
        this.timestamp = timestamp;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public double getValor() {
        return valor;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}

