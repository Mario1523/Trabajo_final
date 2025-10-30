import java.util.Date;

/**
 * Clase que representa los eventos generados durante el monitoreo
 */
public class Eventos {
    private String tipo;            // Tipo de evento
    private String descripcion;     // Descripción detallada del evento
    private double tiempoRespuesta; // Tiempo de respuesta en milisegundos
    private Date fechaHora;         // Fecha y hora del evento

    /**
     * Constructor de la clase Eventos
     * @param tipo Tipo de evento
     * @param descripcion Descripción del evento
     * @param tiempoRespuesta Tiempo de respuesta medido
     */
    public Eventos(String tipo, String descripcion, double tiempoRespuesta) {
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.tiempoRespuesta = tiempoRespuesta;
        this.fechaHora = new Date();
    }

    // Getters
    public String getTipo() {
        return tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public double getTiempoRespuesta() {
        return tiempoRespuesta;
    }

    public Date getFechaHora() {
        return new Date(fechaHora.getTime());
    }

    @Override
    public String toString() {
        return String.format("[%s] %s - %s (Tiempo de respuesta: %.2fms)",
            fechaHora, tipo, descripcion, tiempoRespuesta);
    }
}