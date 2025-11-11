package com.monitoreo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Sistema de métricas avanzadas para el monitoreo
 */
public class MetricasAvanzadas {
    private List<MetricaAvanzada> metricas;
    private int ventanaMovil; // tamaño de la ventana móvil en minutos
    
    public MetricasAvanzadas(int ventanaMovil) {
        this.ventanaMovil = ventanaMovil;
        this.metricas = new ArrayList<>();
    }
    
    public void registrarMetrica(String dispositivo, double valor, TipoMetrica tipo) {
        metricas.add(new MetricaAvanzada(dispositivo, valor, tipo, LocalDateTime.now()));
        limpiarMetricasAntiguas();
    }
    
    private void limpiarMetricasAntiguas() {
        LocalDateTime limite = LocalDateTime.now().minusMinutes(ventanaMovil);
        metricas.removeIf(m -> m.timestamp.isBefore(limite));
    }
    
    public double getPromedioMovil(String dispositivo, TipoMetrica tipo) {
        return metricas.stream()
            .filter(m -> m.dispositivo.equals(dispositivo) && m.tipo == tipo)
            .mapToDouble(m -> m.valor)
            .average()
            .orElse(0.0);
    }
    
    public double getMaximo(String dispositivo, TipoMetrica tipo) {
        return metricas.stream()
            .filter(m -> m.dispositivo.equals(dispositivo) && m.tipo == tipo)
            .mapToDouble(m -> m.valor)
            .max()
            .orElse(0.0);
    }
    
    public double getMinimo(String dispositivo, TipoMetrica tipo) {
        return metricas.stream()
            .filter(m -> m.dispositivo.equals(dispositivo) && m.tipo == tipo)
            .mapToDouble(m -> m.valor)
            .min()
            .orElse(0.0);
    }
}

class MetricaAvanzada {
    String dispositivo;
    double valor;
    TipoMetrica tipo;
    LocalDateTime timestamp;
    
    public MetricaAvanzada(String dispositivo, double valor, TipoMetrica tipo, LocalDateTime timestamp) {
        this.dispositivo = dispositivo;
        this.valor = valor;
        this.tipo = tipo;
        this.timestamp = timestamp;
    }
}

enum TipoMetrica {
    TIEMPO_RESPUESTA,
    DISPONIBILIDAD,
    PAQUETES_PERDIDOS
}