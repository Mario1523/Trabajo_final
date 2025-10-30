package com.monitoreo;

import java.io.IOException;
import java.net.UnknownHostException;

/**
 * Clase que representa un dispositivo a monitorear en el sistema
 */
public class Dispositivos {
    private final String id;              // Identificador único del dispositivo
    private final String direccionIP;     // Dirección IP del dispositivo
    private String estado;          // Estado actual del dispositivo

    /**
     * Constructor de la clase Dispositivos
     * @param id Identificador único del dispositivo
     * @param direccionIP Dirección IP del dispositivo
     */
    public Dispositivos(String id, String direccionIP) {
        this.id = id;
        this.direccionIP = direccionIP;
        this.estado = "DESCONOCIDO";
    }

    /**
     * Verifica el estado actual del dispositivo
     * @return true si el dispositivo está activo, false en caso contrario
     */
    public boolean verificarEstado() {
        try {
            java.net.InetAddress address = java.net.InetAddress.getByName(direccionIP);
            boolean alcanzable = address.isReachable(2000);
            this.estado = alcanzable ? "ACTIVO" : "INACTIVO";
            return alcanzable;
        } catch (UnknownHostException e) {
            this.estado = "ERROR: Host desconocido";
            return false;
        } catch (IOException e) {
            this.estado = "ERROR: Problema de conexión";
            return false;
        }
    }

    // Getters y setters
    public String getId() {
        return id;
    }

    public String getDireccionIP() {
        return direccionIP;
    }

    public String getEstado() {
        return estado;
    }
}