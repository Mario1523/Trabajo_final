package com.monitoreo;

import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * Clase que maneja el sistema de alertas del monitoreo
 */
public class ManejoAlertas {
    private final ArrayList<Consumer<String>> observadores;
    private final double umbralDisponibilidad;
    private final int tiempoRespuestaMaximo;

    /**
     * Constructor de ManejoAlertas
     * @param umbralDisponibilidad Porcentaje mínimo de disponibilidad aceptable
     * @param tiempoRespuestaMaximo Tiempo máximo de respuesta aceptable en ms
     */
    public ManejoAlertas(double umbralDisponibilidad, int tiempoRespuestaMaximo) {
        this.observadores = new ArrayList<>();
        this.umbralDisponibilidad = umbralDisponibilidad;
        this.tiempoRespuestaMaximo = tiempoRespuestaMaximo;
    }

    /**
     * Agrega un nuevo observador para recibir alertas
     * @param observador Función que procesará la alerta
     */
    public void agregarObservador(Consumer<String> observador) {
        observadores.add(observador);
    }

    /**
     * Notifica una alerta a todos los observadores registrados
     * @param mensaje Mensaje de alerta a enviar
     */
    public void notificarAlerta(String mensaje) {
        for (Consumer<String> observador : observadores) {
            observador.accept(mensaje);
        }
    }

    /**
     * Evalúa si se debe generar una alerta basada en los parámetros dados
     * @param disponibilidad Porcentaje actual de disponibilidad
     * @param tiempoRespuesta Tiempo de respuesta actual en ms
     * @return true si se debe generar una alerta, false en caso contrario
     */
    public boolean evaluarAlerta(double disponibilidad, int tiempoRespuesta) {
        return disponibilidad < umbralDisponibilidad || 
               tiempoRespuesta > tiempoRespuestaMaximo;
    }
}