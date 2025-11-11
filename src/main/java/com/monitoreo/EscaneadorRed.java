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
                        encontrado = verificarDispositivoPorPuertos(direccionIP);
                    }
                    
                    if (encontrado) {
                        long tiempoRespuesta = System.currentTimeMillis() - inicio;
                        
                        // Intentar obtener el nombre del host
                        String nombreHost = obtenerNombreHost(address);
                        String estado = "Activo";
                        
                        DispositivoEncontrado dispositivo = new DispositivoEncontrado(direccionIP, nombreHost, estado);
                        dispositivo.setTiempoRespuesta(tiempoRespuesta);
                        
                        // Intentar obtener MAC address
                        String mac = obtenerMACAddress(address);
                        if (mac != null) {
                            dispositivo.setMacAddress(mac);
                            dispositivo.setFabricante(identificarFabricante(mac));
                            // Intentar identificar interfaz WiFi basado en MAC
                            dispositivo.setInterfaz(identificarInterfazWiFi(mac, direccionIP));
                        } else {
                            // Si no se puede obtener MAC, intentar identificar interfaz por otros medios
                            dispositivo.setInterfaz(identificarInterfazWiFi(null, direccionIP));
                        }
                        
                        // Escanear puertos comunes (incluye impresoras, móviles, etc.)
                        escanearPuertosComunes(dispositivo);
                        
                        // Identificar tipo de dispositivo (incluye impresoras, móviles, computadoras)
                        identificarTipoDispositivo(dispositivo);
                        
                        // Si no se identificó por puertos, intentar por nombre
                        if (dispositivo.getTipoDispositivo().equals("Unknown")) {
                            identificarPorNombre(nombreHost, dispositivo);
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
    
    /**
     * Verifica si un dispositivo existe escaneando puertos comunes aunque no responda a ping
     */
    private static boolean verificarDispositivoPorPuertos(String ip) {
        // Puertos comunes que pueden indicar un dispositivo activo
        int[] puertosPrueba = {80, 443, 22, 445, 9100, 631, 515, 62078, 5000};
        
        for (int puerto : puertosPrueba) {
            try {
                java.net.Socket socket = new java.net.Socket();
                socket.connect(new java.net.InetSocketAddress(ip, puerto), 200);
                socket.close();
                return true; // Si al menos un puerto responde, el dispositivo existe
            } catch (Exception e) {
                // Continuar probando
            }
        }
        return false;
    }
    
    /**
     * Intenta identificar la interfaz WiFi (2.4GHz o 5GHz) basado en MAC y comportamiento
     */
    private static String identificarInterfazWiFi(String mac, String ip) {
        // Nota: En Java es difícil determinar exactamente 2.4GHz vs 5GHz sin acceso al router
        // Esto es una aproximación basada en patrones comunes
        
        // Dispositivos que típicamente usan 5GHz (smartphones modernos, laptops)
        if (mac != null) {
            String macUpper = mac.toUpperCase();
            // Prefijos MAC comunes de dispositivos modernos que suelen usar 5GHz
            if (macUpper.startsWith("60:") || macUpper.startsWith("7C:") || 
                macUpper.startsWith("DC:") || macUpper.startsWith("2E:")) {
                // Depende del dispositivo, pero muchos modernos usan 5GHz
                return "5GHz";
            }
        }
        
        // Por defecto, la mayoría de dispositivos modernos pueden usar ambos
        // Retornamos "WiFi" como genérico ya que no podemos determinarlo con certeza
        return "WiFi";
    }
    
    /**
     * Identifica el tipo de dispositivo por su nombre de host
     */
    private static void identificarPorNombre(String nombre, DispositivoEncontrado dispositivo) {
        if (nombre == null || nombre.equals("Desconocido")) {
            return;
        }
        
        String nombreLower = nombre.toLowerCase();
        
        // Impresoras
        if (nombreLower.contains("printer") || nombreLower.contains("print") || 
            nombreLower.contains("hp") || nombreLower.contains("canon") || 
            nombreLower.contains("epson") || nombreLower.contains("brother") ||
            nombreLower.contains("xerox") || nombreLower.contains("lexmark") ||
            nombreLower.contains("samsung") || nombreLower.contains("konica")) {
            dispositivo.setTipoDispositivo("Impresora");
            dispositivo.setSistemaOperativo("Impresora de Red");
        }
        // Teléfonos móviles
        else if (nombreLower.contains("iphone") || nombreLower.contains("android") ||
                 nombreLower.contains("phone") || nombreLower.contains("mobile") ||
                 nombreLower.contains("samsung") || nombreLower.contains("huawei") ||
                 nombreLower.contains("xiaomi") || nombreLower.contains("pixel")) {
            dispositivo.setTipoDispositivo("Teléfono Móvil");
            dispositivo.setSistemaOperativo("iOS/Android");
        }
        // Computadoras
        else if (nombreLower.contains("pc") || nombreLower.contains("laptop") ||
                 nombreLower.contains("desktop") || nombreLower.contains("computer") ||
                 nombreLower.contains("notebook") || nombreLower.contains("macbook")) {
            dispositivo.setTipoDispositivo("Computadora");
            dispositivo.setSistemaOperativo("Windows/Linux/Mac");
        }
        // Routers
        else if (nombreLower.contains("router") || nombreLower.contains("gateway") ||
                 nombreLower.contains("ap") || nombreLower.contains("access-point") ||
                 nombreLower.contains("tp-link") || nombreLower.contains("netgear") ||
                 nombreLower.contains("cisco") || nombreLower.contains("d-link")) {
            dispositivo.setTipoDispositivo("Router/Access Point");
            dispositivo.setSistemaOperativo("Router OS");
        }
    }
    
    /**
     * Escanea puertos comunes en un dispositivo (incluyendo puertos de impresoras, móviles, etc.)
     */
    private static void escanearPuertosComunes(DispositivoEncontrado dispositivo) {
        // Puertos comunes: servidores, impresoras, móviles, computadoras
        int[] puertosComunes = {
            // Servidores
            21, 22, 23, 25, 53, 80, 110, 143, 443, 445, 3389, 8080, 8443,
            // Impresoras
            515, 631, 9100, 9101, 9102, 9103, 9104, 9105, 9106, 9107, 9108, 9109,
            // Servicios de red
            135, 139, 445, 548, 993, 995,
            // Servicios móviles y dispositivos
            62078, 62079, 62080, 62081, 62082, 62083, 62084, 62085, 62086, 62087, 62088, 62089, 62090,
            // Servicios multimedia (DLNA, AirPlay)
            5000, 5001, 5002, 5003, 5004, 5005,
            // Otros servicios comunes
            1723, 3306, 5432, 5900, 5901, 5902, 5903, 5904, 5905
        };
        
        for (int puerto : puertosComunes) {
            try {
                java.net.Socket socket = new java.net.Socket();
                socket.connect(new java.net.InetSocketAddress(dispositivo.getIp(), puerto), 300);
                socket.close();
                dispositivo.agregarPuertoAbierto(puerto);
            } catch (Exception e) {
                // Puerto cerrado o inaccesible
            }
        }
    }
    
    /**
     * Intenta obtener la dirección MAC (limitado en Java)
     */
    private static String obtenerMACAddress(InetAddress address) {
        try {
            NetworkInterface networkInterface = NetworkInterface.getByInetAddress(address);
            if (networkInterface != null) {
                byte[] mac = networkInterface.getHardwareAddress();
                if (mac != null) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < mac.length; i++) {
                        sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? ":" : ""));
                    }
                    return sb.toString();
                }
            }
        } catch (Exception e) {
            // No se puede obtener MAC directamente
        }
        return null;
    }
    
    /**
     * Identifica el fabricante basado en los puertos abiertos
     */
    private static String identificarFabricante(String mac) {
        if (mac == null || mac.equals("Desconocida")) {
            return "Desconocido";
        }
        // Prefijos MAC comunes (primeros 3 octetos)
        String prefijo = mac.substring(0, 8).toUpperCase();
        
        // Algunos prefijos comunes
        if (prefijo.startsWith("00:50:56") || prefijo.startsWith("00:0C:29")) {
            return "VMware";
        } else if (prefijo.startsWith("00:1B:21") || prefijo.startsWith("00:1C:42")) {
            return "Xen";
        } else if (prefijo.startsWith("08:00:27")) {
            return "VirtualBox";
        }
        return "Desconocido";
    }
    
    /**
     * Identifica el tipo de dispositivo basado en puertos y servicios
     * Incluye detección de impresoras, teléfonos, computadoras, etc.
     */
    private static void identificarTipoDispositivo(DispositivoEncontrado dispositivo) {
        java.util.List<Integer> puertos = dispositivo.getPuertosAbiertos();
        
        // Impresoras (puertos comunes de impresoras)
        if (puertos.contains(9100) || puertos.contains(9101) || puertos.contains(9102) ||
            puertos.contains(631) || puertos.contains(515)) {
            dispositivo.setTipoDispositivo("Impresora");
            dispositivo.setSistemaOperativo("Impresora de Red");
            return;
        }
        
        // Teléfonos móviles (puertos comunes de iOS/Android)
        if (puertos.contains(62078) || puertos.contains(62079) || puertos.contains(62080) ||
            puertos.contains(5000) || puertos.contains(5001)) {
            dispositivo.setTipoDispositivo("Teléfono Móvil");
            dispositivo.setSistemaOperativo("iOS/Android");
            return;
        }
        
        // Servidores web
        if (puertos.contains(80) || puertos.contains(443) || puertos.contains(8080)) {
            dispositivo.setTipoDispositivo("Servidor Web");
            dispositivo.setSistemaOperativo("Linux/Windows Server");
        } 
        // Servidores Linux/Unix
        else if (puertos.contains(22)) {
            dispositivo.setTipoDispositivo("Servidor Linux/Unix");
            dispositivo.setSistemaOperativo("Linux/Unix");
        } 
        // Servidores Windows
        else if (puertos.contains(3389)) {
            dispositivo.setTipoDispositivo("Servidor Windows");
            dispositivo.setSistemaOperativo("Windows Server");
        } 
        // Servidores de archivos Windows
        else if (puertos.contains(445) || puertos.contains(139)) {
            dispositivo.setTipoDispositivo("Computadora Windows");
            dispositivo.setSistemaOperativo("Windows");
        } 
        // Servidores FTP/SSH
        else if (puertos.contains(21)) {
            dispositivo.setTipoDispositivo("Servidor FTP");
        }
        // Dispositivos multimedia (DLNA, AirPlay)
        else if (puertos.contains(5000) || puertos.contains(5001) || puertos.contains(5002)) {
            dispositivo.setTipoDispositivo("Dispositivo Multimedia");
            dispositivo.setSistemaOperativo("DLNA/AirPlay");
        }
        // Si no tiene puertos abiertos pero respondió a ping
        else if (puertos.isEmpty() && dispositivo.getEstado().equals("Activo")) {
            dispositivo.setTipoDispositivo("Dispositivo de Red");
            dispositivo.setSistemaOperativo("Router/Switch/AP");
        } 
        // Por defecto
        else {
            dispositivo.setTipoDispositivo("Dispositivo de Red");
        }
    }
    
    /**
     * Intenta obtener el nombre del host desde una dirección IP
     * @param address Dirección IP
     * @return Nombre del host o "Desconocido" si no se puede resolver
     */
    private static String obtenerNombreHost(InetAddress address) {
        try {
            String hostName = address.getHostName();
            // Si el hostname es diferente a la IP, significa que se resolvió
            if (!hostName.equals(address.getHostAddress())) {
                return hostName;
            }
            // Si es igual, intentar con reverse lookup
            InetAddress reverse = InetAddress.getByName(address.getHostAddress());
            String reverseName = reverse.getHostName();
            if (!reverseName.equals(address.getHostAddress())) {
                return reverseName;
            }
            return "Desconocido";
        } catch (Exception e) {
            return "Desconocido";
        }
    }
    
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

