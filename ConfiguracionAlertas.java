import java.util.ArrayList;
import java.util.function.Consumer;

public class ConfiguracionAlertas {
    private final double umbralDisponibilidad;
    private final int tiempoMaximoRespuesta;
    private final ArrayList<Consumer<String>> observadores;
    private boolean alertasEmail;
    private boolean alertasSMS;
    private String emailDestino;
    private String numeroSMS;

    public ConfiguracionAlertas(double umbralDisponibilidad, int tiempoMaximoRespuesta) {
        this.umbralDisponibilidad = umbralDisponibilidad;
        this.tiempoMaximoRespuesta = tiempoMaximoRespuesta;
        this.observadores = new ArrayList<>();
        this.alertasEmail = false;
        this.alertasSMS = false;
    }

    public void agregarObservador(Consumer<String> observador) {
        observadores.add(observador);
    }

    public void notificarAlerta(String mensaje) {
        for (Consumer<String> observador : observadores) {
            observador.accept(mensaje);
        }

        if (alertasEmail && emailDestino != null) {
            enviarEmail(mensaje);
        }

        if (alertasSMS && numeroSMS != null) {
            enviarSMS(mensaje);
        }
    }

    private void enviarEmail(String mensaje) {
        // Simulación de envío de email
        System.out.println("Enviando email a " + emailDestino + ": " + mensaje);
    }

    private void enviarSMS(String mensaje) {
        // Simulación de envío de SMS
        System.out.println("Enviando SMS a " + numeroSMS + ": " + mensaje);
    }

    public void configurarEmail(String email) {
        this.emailDestino = email;
        this.alertasEmail = true;
    }

    public void configurarSMS(String numero) {
        this.numeroSMS = numero;
        this.alertasSMS = true;
    }

    public boolean evaluarAlerta(double disponibilidad, int tiempoRespuesta) {
        return disponibilidad < umbralDisponibilidad || tiempoRespuesta > tiempoMaximoRespuesta;
    }

    public double getUmbralDisponibilidad() {
        return umbralDisponibilidad;
    }

    public int getTiempoMaximoRespuesta() {
        return tiempoMaximoRespuesta;
    }
}