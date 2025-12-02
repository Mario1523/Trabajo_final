package com.monitoreo;

/**
 * Clase responsable de realizar operaciones de escaneo de puertos.
 * Extraída desde {@link EscaneadorRed} para reducir su tamaño y
 * separar responsabilidades.
 */
public class EscaneoPuertos {

    /**
     * Verifica si un dispositivo existe escaneando puertos comunes
     * aunque no responda a ping.
     */
    public boolean verificarDispositivoPorPuertos(String ip) {
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
     * Escanea puertos comunes en un dispositivo (incluyendo puertos de impresoras, móviles, etc.)
     */
    public void escanearPuertosComunes(EscaneadorRed.DispositivoEncontrado dispositivo) {
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
}


