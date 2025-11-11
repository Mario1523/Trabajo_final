package com.monitoreo;

import java.util.ArrayList;
import java.util.List;

/**
 * Sistema de notificaciones para diferentes canales (consola, email, etc)
 */
public class SistemaNotificaciones {
    private List<NotificacionHandlerAvanzado> handlers;
    
    public SistemaNotificaciones() {
        handlers = new ArrayList<>();
        // Por defecto agregamos notificación por consola
        agregarHandler(new ConsolaNotificacionHandlerAvanzado());
    }
    
    public void agregarHandler(NotificacionHandlerAvanzado handler) {
        handlers.add(handler);
    }
    
    public void enviarNotificacion(String mensaje, TipoNotificacion tipo) {
        for (NotificacionHandlerAvanzado handler : handlers) {
            handler.manejarNotificacion(mensaje, tipo);
        }
    }
}

interface NotificacionHandlerAvanzado {
    void manejarNotificacion(String mensaje, TipoNotificacion tipo);
}

enum TipoNotificacion {
    INFO,
    ADVERTENCIA,
    ERROR,
    CRITICO
}

class ConsolaNotificacionHandlerAvanzado implements NotificacionHandlerAvanzado {
    @Override
    public void manejarNotificacion(String mensaje, TipoNotificacion tipo) {
        String prefix;
        switch (tipo) {
            case INFO:
                prefix = "[INFO]";
                break;
            case ADVERTENCIA:
                prefix = "[ADVERTENCIA]";
                break;
            case ERROR:
                prefix = "[ERROR]";
                break;
            case CRITICO:
                prefix = "[CRITICO]";
                break;
            default:
                prefix = "[INFO]";
                break;
        }
        System.out.println(prefix + " " + mensaje);
    }
}