package com.monitoreo;

/**
 * Clase que implementa la lógica de verificación de dispositivos
 */
public class Verificador {
    /**
     * Ejecuta una prueba de verificación sobre un dispositivo
     * @param d Dispositivo a verificar
     * @return Resultado de la verificación (true si está activo, false si no)
     */
    public boolean ejecutarPrueba(Dispositivos d) {
        long inicio = System.currentTimeMillis();
        boolean resultado = d.verificarEstado();
        long tiempoRespuesta = System.currentTimeMillis() - inicio;

        // Crear una descripción y mostrar en consola (uso para evitar variables sin usar)
        String tipo = resultado ? "VERIFICACION_EXITOSA" : "VERIFICACION_FALLIDA";
        String descripcion = String.format("%s - Verificación del dispositivo %s (%s): %s",
            tipo, d.getId(), d.getDireccionIP(), d.getEstado());

        System.out.println(descripcion + " - Tiempo respuesta: " + tiempoRespuesta + "ms");

        // Retornar el resultado de la verificación
        return resultado;
    }
}