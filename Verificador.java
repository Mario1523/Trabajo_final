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

        // Crear y retornar un evento con el resultado
        String tipo = resultado ? "VERIFICACION_EXITOSA" : "VERIFICACION_FALLIDA";
        String descripcion = String.format("Verificación del dispositivo %s (%s): %s",
            d.getId(), d.getDireccionIP(), d.getEstado());
        
        // Retornar el resultado de la verificación
        return resultado;
    }
}