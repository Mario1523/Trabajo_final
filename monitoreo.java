import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Monitoreo {
    private List<String> hosts;
    private int intervalo; // segundos
    private Map<String, Boolean> estadoActual;

    public Monitoreo(List<String> hosts, int intervalo) {
        this.hosts = hosts;
        this.intervalo = intervalo;
        this.estadoActual = new HashMap<>();
        for (String host : hosts) {
            estadoActual.put(host, null);
        }
    }

    private boolean pingHost(String host) {
        // Intenta conectar al puerto 80 del host
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, 80), 2000);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void iniciar() {
        System.out.println("Inicio de monitoreo de hosts...");
        while (true) {
            for (String host : hosts) {
                boolean disponible = pingHost(host);
                Boolean previo = estadoActual.get(host);
                if (previo == null || disponible != previo) {
                    estadoActual.put(host, disponible);
                    alerta(host, disponible);
                }
            }
            try {
                Thread.sleep(intervalo * 1000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void alerta(String host, boolean disponible) {
        String estado = disponible ? "ACTIVO" : "CAÍDO";
        System.out.println("[ALERTA] El host " + host + " está " + estado);
    }

    // Avance: ejemplo de uso
    public static void main(String[] args) {
        List<String> hosts = List.of("192.168.1.1", "google.com");
        Monitoreo monitor = new Monitoreo(hosts, 10);
        monitor.iniciar();
    }
}