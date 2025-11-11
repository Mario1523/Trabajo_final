package com.monitoreo;

import java.io.*;
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
    private final ArrayList<Eventos> registroEventos;        // Registro de eventos del sistema
    private final Verificador verificador;                   // Verificador de dispositivos
    private final ManejoAlertas manejoAlertas;              // Sistema de manejo de alertas
    private final Map<String, HostEstadisticas> estadisticas; // Estadísticas por dispositivo
    private final GeneradorReportes generadorReportes;       // Generador de reportes
    private final int intervalo;                             // Intervalo entre verificaciones en segundos
    private static final String LOG_FILE = "monitoreo.log";  // Archivo de log
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private Thread threadMonitoreo;                           // Thread para ejecutar el monitoreo
    private volatile boolean monitoreoActivo;                // Flag para controlar el monitoreo

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
        
        this.generadorReportes = new GeneradorReportes("reportes", estadisticas);
        
        // Configurar notificaciones por consola
        manejoAlertas.agregarObservador(mensaje -> System.out.println("[Notificación] " + mensaje));
        
        registrarEvento("Sistema de monitoreo iniciado con " + listaDispositivos.size() + " dispositivos");
    }

    /**
     * Inicia el proceso de monitoreo continuo de todos los hosts registrados.
     * Realiza verificaciones periódicas, actualiza estadísticas y genera reportes.
     */
    /**
     * Inicia el proceso de monitoreo continuo de dispositivos en un thread separado
     */
    public void iniciar() {
        if (monitoreoActivo) {
            return; // Ya está ejecutándose
        }
        
        monitoreoActivo = true;
        threadMonitoreo = new Thread(() -> {
            System.out.println("Inicio de monitoreo de dispositivos...");
            int ciclos = 0;
            
            while (monitoreoActivo && !Thread.currentThread().isInterrupted()) {
                try {
                    for (Dispositivos dispositivo : listaDispositivos) {
                        if (!monitoreoActivo || Thread.currentThread().isInterrupted()) {
                            break;
                        }
                        
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
                        if (stats != null) {
                            stats.registrarChequeo(disponible, tiempoRespuesta);
                            
                            // Evaluar alertas por rendimiento
                            if (manejoAlertas.evaluarAlerta(stats.getDisponibilidad(), (int)tiempoRespuesta)) {
                                manejoAlertas.notificarAlerta(
                                    String.format("Alerta de rendimiento para %s - Disponibilidad: %.2f%%, Tiempo de respuesta: %dms",
                                        dispositivo.getId(), stats.getDisponibilidad(), tiempoRespuesta));
                            }
                        }
                    }
                    
                    ciclos++;
                    if (ciclos % 10 == 0) { // Generar reportes cada 10 ciclos (solo PDF)
                        String rutaDiario = generadorReportes.generarReporteDiario();
                        String rutaDisponibilidad = generadorReportes.generarReporteDisponibilidad();
                        if (rutaDiario != null) {
                            registrarEvento("Reporte diario PDF generado: " + rutaDiario);
                        }
                        if (rutaDisponibilidad != null) {
                            registrarEvento("Reporte de disponibilidad PDF generado: " + rutaDisponibilidad);
                        }
                    }
                    
                    Thread.sleep(intervalo * 1000);
                } catch (InterruptedException ie) {
                    monitoreoActivo = false;
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            
            System.out.println("Monitoreo detenido.");
        });
        threadMonitoreo.setDaemon(true);
        threadMonitoreo.start();
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
        monitoreoActivo = false;
        if (threadMonitoreo != null) {
            threadMonitoreo.interrupt();
        }
    }
    
    /**
     * Verifica si el monitoreo está activo
     * @return true si el monitoreo está ejecutándose
     */
    public boolean estaActivo() {
        return monitoreoActivo;
    }
    
    /**
     * Obtiene las estadísticas de un dispositivo específico
     * @param dispositivoId ID del dispositivo
     * @return HostEstadisticas o null si no existe
     */
    public HostEstadisticas getEstadisticas(String dispositivoId) {
        return estadisticas.get(dispositivoId);
    }
    
    /**
     * Obtiene todas las estadísticas
     * @return Map con todas las estadísticas
     */
    public Map<String, HostEstadisticas> getAllEstadisticas() {
        return new HashMap<>(estadisticas);
    }
    
    /**
     * Obtiene el dispositivo por su ID
     * @param dispositivoId ID del dispositivo
     * @return Dispositivos o null si no existe
     */
    public Dispositivos getDispositivo(String dispositivoId) {
        for (Dispositivos d : listaDispositivos) {
            if (d.getId().equals(dispositivoId)) {
                return d;
            }
        }
        return null;
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
        for (String dispositivoId : monitor.getDispositivos()) {
            Dispositivos d = new Dispositivos(dispositivoId, dispositivoId);
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
    
    /**
     * Obtiene el manejador de alertas activo
     * @return instancia de ManejoAlertas
     */
    public ManejoAlertas getManejoAlertas() {
        return manejoAlertas;
    }
    
    /**
     * Obtiene la lista completa de objetos Dispositivos
     * @return ArrayList con todos los dispositivos monitoreados
     */
    public ArrayList<Dispositivos> getListaDispositivos() {
        return new ArrayList<>(listaDispositivos);
    }
    
    /**
     * Obtiene el generador de reportes
     * @return instancia de GeneradorReportes
     */
    public GeneradorReportes getGeneradorReportes() {
        return generadorReportes;
    }
}