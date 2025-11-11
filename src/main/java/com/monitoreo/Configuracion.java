package com.monitoreo;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 * Clase que maneja toda la configuración del sistema de monitoreo
 * Combina la configuración del sistema y de las alertas
 */
public class Configuracion {
    private Properties propiedades;
    private static final String CONFIG_FILE = "config.properties";
    
    // Valores por defecto
    private double umbralDisponibilidad = 99.0;
    private int umbralTiempoRespuesta = 2000;
    private int intervaloMonitoreo = 10;
    private String directorioReportes = "./reportes";
    private boolean notificacionesEmail = false;
    private String emailDestino = "";

    public Configuracion() {
        propiedades = new Properties();
        cargarConfiguracion();
    }

    /**
     * Carga la configuración desde el archivo
     */
    private void cargarConfiguracion() {
        try (InputStream input = new FileInputStream(CONFIG_FILE)) {
            propiedades.load(input);
            
            // Cargar valores de configuración
            umbralDisponibilidad = Double.parseDouble(
                propiedades.getProperty("umbral.disponibilidad", "99.0"));
            umbralTiempoRespuesta = Integer.parseInt(
                propiedades.getProperty("umbral.tiempoRespuesta", "2000"));
            intervaloMonitoreo = Integer.parseInt(
                propiedades.getProperty("intervalo.monitoreo", "10"));
            directorioReportes = propiedades.getProperty("directorio.reportes", "./reportes");
            notificacionesEmail = Boolean.parseBoolean(
                propiedades.getProperty("notificaciones.email", "false"));
            emailDestino = propiedades.getProperty("email.destino", "");
            
        } catch (IOException ex) {
            System.out.println("No se encontró archivo de configuración. Usando valores por defecto.");
        }
    }

    /**
     * Guarda la configuración actual en el archivo
     */
    public void guardarConfiguracion() {
        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            propiedades.setProperty("umbral.disponibilidad", String.valueOf(umbralDisponibilidad));
            propiedades.setProperty("umbral.tiempoRespuesta", String.valueOf(umbralTiempoRespuesta));
            propiedades.setProperty("intervalo.monitoreo", String.valueOf(intervaloMonitoreo));
            propiedades.setProperty("directorio.reportes", directorioReportes);
            propiedades.setProperty("notificaciones.email", String.valueOf(notificacionesEmail));
            propiedades.setProperty("email.destino", emailDestino);
            
            propiedades.store(output, "Configuración del Sistema de Monitoreo");
        } catch (IOException ex) {
            System.err.println("Error al guardar la configuración: " + ex.getMessage());
        }
    }

    // Getters y Setters
    public double getUmbralDisponibilidad() {
        return umbralDisponibilidad;
    }

    public void setUmbralDisponibilidad(double umbralDisponibilidad) {
        this.umbralDisponibilidad = umbralDisponibilidad;
    }

    public int getUmbralTiempoRespuesta() {
        return umbralTiempoRespuesta;
    }

    public void setUmbralTiempoRespuesta(int umbralTiempoRespuesta) {
        this.umbralTiempoRespuesta = umbralTiempoRespuesta;
    }

    public int getIntervaloMonitoreo() {
        return intervaloMonitoreo;
    }

    public void setIntervaloMonitoreo(int intervaloMonitoreo) {
        this.intervaloMonitoreo = intervaloMonitoreo;
    }

    public String getDirectorioReportes() {
        return directorioReportes;
    }

    public void setDirectorioReportes(String directorioReportes) {
        this.directorioReportes = directorioReportes;
    }

    public boolean isNotificacionesEmail() {
        return notificacionesEmail;
    }

    public void setNotificacionesEmail(boolean notificacionesEmail) {
        this.notificacionesEmail = notificacionesEmail;
    }

    public String getEmailDestino() {
        return emailDestino;
    }

    public void setEmailDestino(String emailDestino) {
        this.emailDestino = emailDestino;
    }
}