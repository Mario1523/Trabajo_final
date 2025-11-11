package com.monitoreo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase que gestiona las alertas y notificaciones del sistema
 * Combina la funcionalidad de ManejoAlertas y SistemaNotificaciones
 */
public class GestorAlertas {
    private final List<NotificacionHandler> handlers;
    private final Configuracion configuracion;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public GestorAlertas(Configuracion configuracion) {
        this.handlers = new ArrayList<>();
        this.configuracion = configuracion;
        
        // Agregar handler por defecto para consola
        agregarHandler(new ConsolaNotificacionHandler());
        
        // Si está configurado el email, agregar handler de email
        if (configuracion.isNotificacionesEmail()) {
            agregarHandler(new EmailNotificacionHandler(configuracion.getEmailDestino()));
        }
    }

    /**
     * Evalúa si se debe generar una alerta basada en los umbrales configurados
     */
    public boolean evaluarAlerta(double disponibilidad, int tiempoRespuesta) {
        return disponibilidad < configuracion.getUmbralDisponibilidad() || 
               tiempoRespuesta > configuracion.getUmbralTiempoRespuesta();
    }

    /**
     * Notifica una alerta a través de todos los handlers configurados
     */
    public void notificarAlerta(String mensaje) {
        String timestampedMessage = LocalDateTime.now().format(formatter) + " - " + mensaje;
        for (NotificacionHandler handler : handlers) {
            handler.manejarNotificacion(timestampedMessage);
        }
    }

    /**
     * Agrega un nuevo handler de notificaciones
     */
    public void agregarHandler(NotificacionHandler handler) {
        if (!handlers.contains(handler)) {
            handlers.add(handler);
        }
    }

    /**
     * Remueve un handler de notificaciones
     */
    public void removerHandler(NotificacionHandler handler) {
        handlers.remove(handler);
    }
}

/**
 * Interfaz para los manejadores de notificaciones
 */
interface NotificacionHandler {
    void manejarNotificacion(String mensaje);
}

/**
 * Implementación de notificaciones por consola
 */
class ConsolaNotificacionHandler implements NotificacionHandler {
    @Override
    public void manejarNotificacion(String mensaje) {
        System.out.println("[ALERTA] " + mensaje);
    }
}

/**
 * Implementación de notificaciones por email
 */
class EmailNotificacionHandler implements NotificacionHandler {
    private final String emailDestino;

    public EmailNotificacionHandler(String emailDestino) {
        this.emailDestino = emailDestino;
    }

    @Override
    public void manejarNotificacion(String mensaje) {
        // Aquí iría la implementación del envío de email
        System.out.println("[EMAIL] Enviando a " + emailDestino + ": " + mensaje);
    }
}