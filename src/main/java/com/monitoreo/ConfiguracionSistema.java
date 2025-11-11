package com.monitoreo;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Clase para manejar la configuración del sistema de monitoreo
 */
public class ConfiguracionSistema {
    private Properties configuracion;
    private static final String ARCHIVO_CONFIG = "config.properties";
    
    public ConfiguracionSistema() {
        configuracion = new Properties();
        cargarConfiguracionPorDefecto();
        cargarConfiguracion();
    }
    
    private void cargarConfiguracionPorDefecto() {
        configuracion.setProperty("intervalo_monitoreo", "10");
        configuracion.setProperty("umbral_disponibilidad", "99.0");
        configuracion.setProperty("umbral_tiempo_respuesta", "2000");
        configuracion.setProperty("directorio_reportes", "./reportes");
        configuracion.setProperty("archivo_log", "monitoreo.log");
    }
    
    private void cargarConfiguracion() {
        try (FileInputStream fis = new FileInputStream(ARCHIVO_CONFIG)) {
            configuracion.load(fis);
        } catch (IOException e) {
            System.out.println("No se encontró archivo de configuración, usando valores por defecto");
            guardarConfiguracion();
        }
    }
    
    public void guardarConfiguracion() {
        try (FileOutputStream fos = new FileOutputStream(ARCHIVO_CONFIG)) {
            configuracion.store(fos, "Configuración del sistema de monitoreo");
        } catch (IOException e) {
            System.err.println("Error al guardar la configuración: " + e.getMessage());
        }
    }
    
    public int getIntervaloMonitoreo() {
        return Integer.parseInt(configuracion.getProperty("intervalo_monitoreo"));
    }
    
    public double getUmbralDisponibilidad() {
        return Double.parseDouble(configuracion.getProperty("umbral_disponibilidad"));
    }
    
    public int getUmbralTiempoRespuesta() {
        return Integer.parseInt(configuracion.getProperty("umbral_tiempo_respuesta"));
    }
    
    public String getDirectorioReportes() {
        return configuracion.getProperty("directorio_reportes");
    }
    
    public String getArchivoLog() {
        return configuracion.getProperty("archivo_log");
    }
}