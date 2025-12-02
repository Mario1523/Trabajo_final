package com.monitoreo;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Clase para escanear la red local y descubrir dispositivos activos
 */
public class EscaneadorRed {
    // Composición: reutilizamos clases auxiliares para reducir tamaño de esta clase
    private static final EscaneoPuertos escaneoPuertos = new EscaneoPuertos();
    private static final IdentificadorDispositivos identificador = new IdentificadorDispositivos();

    /**
     * Clase para almacenar información completa de un dispositivo encontrado (estilo nmap)
     */
    public static class DispositivoEncontrado {
        private String ip;
        private String nombre;
        private String estado;
        private String macAddress;
        private String sistemaOperativo;
        private java.util.List<Integer> puertosAbiertos;
        private String fabricante;
        private long tiempoRespuesta;
        private String tipoDispositivo;
        private String interfaz; // 2.4GHz, 5GHz, Ethernet
        private String tipoConexion; // DHCP-IP, Static, etc.
        
        public DispositivoEncontrado(String ip, String nombre, String estado) {
            this.ip = ip;
            this.nombre = nombre;
            this.estado = estado;
            this.puertosAbiertos = new java.util.ArrayList<>();
            this.macAddress = "Unknown";
            this.sistemaOperativo = "Unknown";
            this.fabricante = "Unknown";
            this.tiempoRespuesta = 0;
            this.tipoDispositivo = "Unknown";
            this.interfaz = "Unknown";
            this.tipoConexion = "DHCP-IP";
        }
        
        public String getIp() {
            return ip;
        }
        
        public String getNombre() {
            return nombre;
        }
        
        public String getEstado() {
            return estado;
        }
        
        public String getMacAddress() {
            return macAddress;
        }
        
        public void setMacAddress(String macAddress) {
            this.macAddress = macAddress;
        }
        
        public String getSistemaOperativo() {
            return sistemaOperativo;
        }
        
        public void setSistemaOperativo(String sistemaOperativo) {
            this.sistemaOperativo = sistemaOperativo;
        }
        
        public java.util.List<Integer> getPuertosAbiertos() {
            return puertosAbiertos;
        }
        
        public void agregarPuertoAbierto(int puerto) {
            if (!puertosAbiertos.contains(puerto)) {
                puertosAbiertos.add(puerto);
            }
        }
        
        public String getFabricante() {
            return fabricante;
        }
        
        public void setFabricante(String fabricante) {
            this.fabricante = fabricante;
        }
        
        public long getTiempoRespuesta() {
            return tiempoRespuesta;
        }
        
        public void setTiempoRespuesta(long tiempoRespuesta) {
            this.tiempoRespuesta = tiempoRespuesta;
        }
        
        public String getTipoDispositivo() {
            return tipoDispositivo;
        }
        
        public void setTipoDispositivo(String tipoDispositivo) {
            this.tipoDispositivo = tipoDispositivo;
        }
        
        public String getPuertosComoString() {
            if (puertosAbiertos.isEmpty()) {
                return "Ninguno";
            }
            return puertosAbiertos.toString().replace("[", "").replace("]", "");
        }
        
        public String getInterfaz() {
            return interfaz;
        }
        
        public void setInterfaz(String interfaz) {
            this.interfaz = interfaz;
        }
        
        public String getTipoConexion() {
            return tipoConexion;
        }
        
        public void setTipoConexion(String tipoConexion) {
            this.tipoConexion = tipoConexion;
        }
    }
    
    /**
     * Escanea la red local y devuelve las IPs de dispositivos activos
     * @param rangoInicio Primera IP del rango (ej: 1)
     * @param rangoFin Última IP del rango (ej: 254)
     * @param timeout Timeout en milisegundos para cada ping
     * @return Lista de IPs que responden
     */
    public static List<String> escanearRango(String redBase, int rangoInicio, int rangoFin, int timeout) {
        List<String> dispositivosEncontrados = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(50);
        
        for (int i = rangoInicio; i <= rangoFin; i++) {
            final int ip = i;
            executor.submit(() -> {
                try {
                    String direccionIP = redBase + "." + ip;
                    InetAddress address = InetAddress.getByName(direccionIP);
                    if (address.isReachable(timeout)) {
                        synchronized (dispositivosEncontrados) {
                            dispositivosEncontrados.add(direccionIP);
                        }
                    }
                } catch (Exception e) {
                    // Ignorar errores de ping
                }
            });
        }
        
        executor.shutdown();
        try {
            executor.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
        
        return dispositivosEncontrados;
    }
    
    /**
     * Escanea la red local estilo nmap con información completa de dispositivos
     * Incluye detección de teléfonos, computadoras, impresoras, etc.
     * @param redBase Base de la red (ej: "192.168.1")
     * @param rangoInicio Primera IP del rango (ej: 1)
     * @param rangoFin Última IP del rango (ej: 254)
     * @param timeout Timeout en milisegundos para cada ping
     * @return Lista de dispositivos encontrados con información completa
     */
    public static List<DispositivoEncontrado> escanearRangoCompleto(String redBase, int rangoInicio, int rangoFin, int timeout) {
        List<DispositivoEncontrado> dispositivosEncontrados = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(100); // Más threads para escanear más rápido
        
        // Primera fase: descubrimiento de hosts (ping scan + port scan)
        for (int i = rangoInicio; i <= rangoFin; i++) {
            final int ip = i;
            executor.submit(() -> {
                try {
                    String direccionIP = redBase + "." + ip;
                    long inicio = System.currentTimeMillis();
                    InetAddress address = InetAddress.getByName(direccionIP);
                    
                    boolean encontrado = false;
                    
                    // Método 1: Ping tradicional
                    if (address.isReachable(timeout)) {
                        encontrado = true;
                    } else {
                        // Método 2: Si no responde a ping, intentar escanear puertos directamente
                        // Algunos dispositivos (impresoras, móviles) no responden a ping pero sí en puertos
                        encontrado = escaneoPuertos.verificarDispositivoPorPuertos(direccionIP);
                    }
                    
                    if (encontrado) {
                        long tiempoRespuesta = System.currentTimeMillis() - inicio;
                        
                        // Intentar obtener el nombre del host
                        String nombreHost = identificador.obtenerNombreHost(address);
                        String estado = "Activo";
                        
                        DispositivoEncontrado dispositivo = new DispositivoEncontrado(direccionIP, nombreHost, estado);
                        dispositivo.setTiempoRespuesta(tiempoRespuesta);
                        
                        // Intentar obtener MAC address
                        String mac = identificador.obtenerMACAddress(address);
                        if (mac != null) {
                            dispositivo.setMacAddress(mac);
                            dispositivo.setFabricante(identificador.identificarFabricante(mac));
                            // Intentar identificar interfaz WiFi basado en MAC
                            dispositivo.setInterfaz(identificador.identificarInterfazWiFi(mac, direccionIP));
                        } else {
                            // Si no se puede obtener MAC, intentar identificar interfaz por otros medios
                            dispositivo.setInterfaz(identificador.identificarInterfazWiFi(null, direccionIP));
                        }
                        
                        // Escanear puertos comunes (incluye impresoras, móviles, etc.)
                        escaneoPuertos.escanearPuertosComunes(dispositivo);
                        
                        // Identificar tipo de dispositivo (incluye impresoras, móviles, computadoras)
                        identificador.identificarTipoDispositivo(dispositivo);
                        
                        // Si no se identificó por puertos, intentar por nombre
                        if (dispositivo.getTipoDispositivo().equals("Unknown")) {
                            identificador.identificarPorNombre(nombreHost, dispositivo);
                        }
                        
                        synchronized (dispositivosEncontrados) {
                            dispositivosEncontrados.add(dispositivo);
                        }
                    }
                } catch (Exception e) {
                    // Ignorar errores
                }
            });
        }
        
        executor.shutdown();
        try {
            executor.awaitTermination(90, TimeUnit.SECONDS); // Más tiempo para escaneo completo
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
        
        return dispositivosEncontrados;
    }
    
    // Métodos auxiliares ahora se delegan a EscaneoPuertos e IdentificadorDispositivos
    
    /**
     * Obtiene la dirección IP de la red local
     * @return String con la base de la red (ej: "192.168.1")
     */
    public static String obtenerRedLocal() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                
                // Ignorar interfaces loopback y no activas
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }
                
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    String ip = address.getHostAddress();
                    
                    // Buscar una dirección IPv4 privada
                    if (ip.startsWith("192.168.") || 
                        ip.startsWith("10.") || 
                        ip.startsWith("172.16.") || 
                        ip.startsWith("172.17.") || 
                        ip.startsWith("172.18.") || 
                        ip.startsWith("172.19.") || 
                        ip.startsWith("172.20.") || 
                        ip.startsWith("172.21.") || 
                        ip.startsWith("172.22.") || 
                        ip.startsWith("172.23.") || 
                        ip.startsWith("172.24.") || 
                        ip.startsWith("172.25.") || 
                        ip.startsWith("172.26.") || 
                        ip.startsWith("172.27.") || 
                        ip.startsWith("172.28.") || 
                        ip.startsWith("172.29.") || 
                        ip.startsWith("172.30.") || 
                        ip.startsWith("172.31.")) {
                        
                        // Extraer la base de la red (primeros 3 octetos)
                        String[] partes = ip.split("\\.");
                        if (partes.length == 4) {
                            return partes[0] + "." + partes[1] + "." + partes[2];
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Si no se encuentra, usar un valor por defecto común
        return "192.168.1";
    }
    
    /**
     * Escanea la red local automáticamente detectando el rango
     * @param timeout Timeout en milisegundos
     * @return Lista de IPs activas
     */
    public static List<String> escanearRedLocal(int timeout) {
        String redBase = obtenerRedLocal();
        return escanearRango(redBase, 1, 254, timeout);
    }
}

