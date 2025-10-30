import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Clase principal del sistema de monitoreo de hosts.
 * Gestiona la verificación periódica de disponibilidad de múltiples hosts,
 * genera alertas y mantiene estadísticas de rendimiento.
 */
public class Monitoreo {
    private final ArrayList<Dispositivos> listaDispositivos;  // Lista de dispositivos a monitorear
    private final int intervalo;                             // Intervalo entre chequeos en segundos
    private final ArrayList<Eventos> registroEventos;        // Registro de eventos del sistema
    private final Verificador verificador;                   // Verificador de dispositivos
    private final ManejoAlertas manejoAlertas;              // Sistema de manejo de alertas
    private final Map<String, HostEstadisticas> estadisticas; // Estadísticas por dispositivo
    private final GeneradorReportes generadorReportes;       // Generador de reportes
    private static final String LOG_FILE = "monitoreo.log";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Constructor de la clase Monitoreo
     * @param dispositivos Lista de dispositivos a monitorear
     * @param intervalo Intervalo entre verificaciones en segundos
     */
    public Monitoreo(ArrayList<String> hosts, int intervalo) {
        this.listaDispositivos = new ArrayList<>();
        this.registroEventos = new ArrayList<>();
        this.intervalo = intervalo;
        this.estadisticas = new HashMap<>();
        this.verificador = new Verificador();
        this.manejoAlertas = new ManejoAlertas(99.0, 2000);
        
        // Convertir los hosts a dispositivos
        for (String host : hosts) {
            Dispositivos dispositivo = new Dispositivos(host, host);
            listaDispositivos.add(dispositivo);
            estadisticas.put(host, new HostEstadisticas(host));
        }
        
        this.generadorReportes = new GeneradorReportes("./reportes", estadisticas);
        
        // Configurar notificaciones por consola
        manejoAlertas.agregarObservador(mensaje -> System.out.println("[Notificación] " + mensaje));
        
        registrarEvento("Sistema de monitoreo iniciado con " + listaDispositivos.size() + " dispositivos");
    }

    /**
     * Verifica la disponibilidad de un host intentando establecer una conexión TCP.
     * @param host Nombre o dirección IP del host a verificar
     * @return true si el host responde, false si no es alcanzable
     */
    private boolean pingHost(String host) {
        // Intenta conectar al puerto 80 del host
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, 80), 2000);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Inicia el proceso de monitoreo continuo de todos los hosts registrados.
     * Realiza verificaciones periódicas, actualiza estadísticas y genera reportes.
     */
    /**
     * Inicia el proceso de monitoreo continuo de dispositivos
     */
    public void iniciar() {
        System.out.println("Inicio de monitoreo de dispositivos...");
        int ciclos = 0;
        
        while (true) {
            for (Dispositivos dispositivo : listaDispositivos) {
                long inicio = System.currentTimeMillis();
                boolean disponible = verificador.ejecutarPrueba(dispositivo);
                long tiempoRespuesta = System.currentTimeMillis() - inicio;
                
                // Registrar evento
                Eventos evento = new Eventos(
                    disponible ? "VERIFICACION_EXITOSA" : "VERIFICACION_FALLIDA",
                    "Verificación del dispositivo " + dispositivo.getId(),
                    tiempoRespuesta
                );
                registroEventos.add(evento);
                
                // Actualizar estadísticas
                HostEstadisticas stats = estadisticas.get(dispositivo.getId());
                stats.registrarChequeo(disponible);
                
                // Evaluar alertas por rendimiento
                if (manejoAlertas.evaluarAlerta(stats.getDisponibilidad(), (int)tiempoRespuesta)) {
                    manejoAlertas.notificarAlerta(
                        String.format("Alerta de rendimiento para %s - Disponibilidad: %.2f%%, Tiempo de respuesta: %dms",
                            dispositivo.getId(), stats.getDisponibilidad(), tiempoRespuesta));
                }
            }
            
            ciclos++;
            if (ciclos % 10 == 0) { // Generar reportes cada 10 ciclos
                generadorReportes.generarReporteDiario();
                generadorReportes.generarReporteDisponibilidad();
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
        String mensaje = "[ALERTA] El host " + host + " está " + estado;
        System.out.println(mensaje);
        registrarEvento(mensaje);
    }

    private void registrarEvento(String mensaje) {
        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             BufferedWriter bw = new BufferedWriter(fw)) {
            String timestampedMessage = LocalDateTime.now().format(formatter) + " - " + mensaje;
            bw.write(timestampedMessage);
            bw.newLine();
        } catch (IOException e) {
            System.err.println("Error al escribir en el archivo de log: " + e.getMessage());
        }
    }

    /**
     * Agrega un nuevo dispositivo al monitoreo
     * @param id Identificador del dispositivo
     * @param direccionIP Dirección IP del dispositivo
     */
    public void agregarDispositivo(String id, String direccionIP) {
        Dispositivos dispositivo = new Dispositivos(id, direccionIP);
        if (!listaDispositivos.contains(dispositivo)) {
            listaDispositivos.add(dispositivo);
            estadisticas.put(id, new HostEstadisticas(id));
            registrarEvento("Nuevo dispositivo agregado: " + id);
        }
    }

    /**
     * Remueve un dispositivo del monitoreo
     * @param id Identificador del dispositivo a remover
     */
    public void removerDispositivo(String id) {
        listaDispositivos.removeIf(d -> d.getId().equals(id));
        estadisticas.remove(id);
        registrarEvento("Dispositivo removido: " + id);
    }

    /**
     * Obtiene la lista de IDs de dispositivos monitoreados
     * @return ArrayList con los IDs de los dispositivos
     */
    public ArrayList<String> getDispositivos() {
        ArrayList<String> ids = new ArrayList<>();
        for (Dispositivos d : listaDispositivos) {
            ids.add(d.getId());
        }
        return ids;
    }

    /**
     * Detiene el monitoreo de dispositivos
     */
    public void detenerMonitoreo() {
        registrarEvento("Deteniendo el monitoreo de dispositivos");
        Thread.currentThread().interrupt();
    }

    // Método para pruebas funcionales
    /**
     * Ejecuta una serie de pruebas funcionales para verificar el correcto
     * funcionamiento del sistema de monitoreo.
     * Prueba las funciones de agregar/remover hosts y verificación de conectividad.
     */
    /**
     * Ejecuta pruebas funcionales del sistema
     */
    public static void pruebasFuncionales() {
        System.out.println("Iniciando pruebas funcionales...");
        
        ArrayList<String> hosts = new ArrayList<>();
        hosts.add("google.com");
        hosts.add("github.com");
        
        Monitoreo monitor = new Monitoreo(hosts, 5);
        
        // Prueba 1: Agregar dispositivo
        System.out.println("\nPrueba 1: Agregar dispositivo");
        monitor.agregarDispositivo("microsoft.com", "40.76.4.15");
        System.out.println("Dispositivos actuales: " + monitor.getDispositivos());
        
        // Prueba 2: Remover dispositivo
        System.out.println("\nPrueba 2: Remover dispositivo");
        monitor.removerDispositivo("github.com");
        System.out.println("Dispositivos actuales: " + monitor.getDispositivos());
        
        // Prueba 3: Verificar conectividad
        System.out.println("\nPrueba 3: Verificar conectividad");
        Verificador verificador = new Verificador();
        for (Dispositivos d : monitor.listaDispositivos) {
            boolean disponible = verificador.ejecutarPrueba(d);
            System.out.println("Dispositivo: " + d.getId() + " - Disponible: " + disponible);
        }
        
        System.out.println("\nPruebas funcionales completadas.");
    }

    /**
     * Método principal que inicia el sistema de monitoreo
     */
    public static void main(String[] args) {
        // Crear directorio para reportes
        new File("./reportes").mkdirs();
        
        // Ejecutar pruebas funcionales
        pruebasFuncionales();
        
        // Iniciar monitoreo normal
        System.out.println("\nIniciando monitoreo normal...");
        ArrayList<String> hosts = new ArrayList<>();
        hosts.add("google.com");
        hosts.add("github.com");
        hosts.add("microsoft.com");
        
        Monitoreo monitor = new Monitoreo(hosts, 10);
        
        // Iniciar el monitoreo
        monitor.iniciar();
    }
    
    public ConfiguracionAlertas getConfiguracionAlertas() {
        return configuracionAlertas;
    }
}