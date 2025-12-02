package com.monitoreo;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.List;

/**
 * Clase que encapsula la lógica de identificación de dispositivos:
 * fabricante, tipo, interfaz WiFi y nombre de host.
 * Extraída de {@link EscaneadorRed} para hacerlo más corto y modular.
 */
public class IdentificadorDispositivos {

    public String identificarInterfazWiFi(String mac, String ip) {
        if (mac != null) {
            String macUpper = mac.toUpperCase();
            if (macUpper.startsWith("60:") || macUpper.startsWith("7C:")
                    || macUpper.startsWith("DC:") || macUpper.startsWith("2E:")) {
                return "5GHz";
            }
        }
        return "WiFi";
    }

    public void identificarPorNombre(String nombre, EscaneadorRed.DispositivoEncontrado dispositivo) {
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

    public String identificarFabricante(String mac) {
        if (mac == null || mac.equals("Desconocida")) {
            return "Desconocido";
        }
        String prefijo = mac.substring(0, 8).toUpperCase();

        if (prefijo.startsWith("00:50:56") || prefijo.startsWith("00:0C:29")) {
            return "VMware";
        } else if (prefijo.startsWith("00:1B:21") || prefijo.startsWith("00:1C:42")) {
            return "Xen";
        } else if (prefijo.startsWith("08:00:27")) {
            return "VirtualBox";
        }
        return "Desconocido";
    }

    public void identificarTipoDispositivo(EscaneadorRed.DispositivoEncontrado dispositivo) {
        List<Integer> puertos = dispositivo.getPuertosAbiertos();

        if (puertos.contains(9100) || puertos.contains(9101) || puertos.contains(9102) ||
            puertos.contains(631) || puertos.contains(515)) {
            dispositivo.setTipoDispositivo("Impresora");
            dispositivo.setSistemaOperativo("Impresora de Red");
            return;
        }

        if (puertos.contains(62078) || puertos.contains(62079) || puertos.contains(62080) ||
            puertos.contains(5000) || puertos.contains(5001)) {
            dispositivo.setTipoDispositivo("Teléfono Móvil");
            dispositivo.setSistemaOperativo("iOS/Android");
            return;
        }

        if (puertos.contains(80) || puertos.contains(443) || puertos.contains(8080)) {
            dispositivo.setTipoDispositivo("Servidor Web");
            dispositivo.setSistemaOperativo("Linux/Windows Server");
        } else if (puertos.contains(22)) {
            dispositivo.setTipoDispositivo("Servidor Linux/Unix");
            dispositivo.setSistemaOperativo("Linux/Unix");
        } else if (puertos.contains(3389)) {
            dispositivo.setTipoDispositivo("Servidor Windows");
            dispositivo.setSistemaOperativo("Windows Server");
        } else if (puertos.contains(445) || puertos.contains(139)) {
            dispositivo.setTipoDispositivo("Computadora Windows");
            dispositivo.setSistemaOperativo("Windows");
        } else if (puertos.contains(21)) {
            dispositivo.setTipoDispositivo("Servidor FTP");
        } else if (puertos.contains(5000) || puertos.contains(5001) || puertos.contains(5002)) {
            dispositivo.setTipoDispositivo("Dispositivo Multimedia");
            dispositivo.setSistemaOperativo("DLNA/AirPlay");
        } else if (puertos.isEmpty() && "Activo".equals(dispositivo.getEstado())) {
            dispositivo.setTipoDispositivo("Dispositivo de Red");
            dispositivo.setSistemaOperativo("Router/Switch/AP");
        } else {
            dispositivo.setTipoDispositivo("Dispositivo de Red");
        }
    }

    public String obtenerMACAddress(InetAddress address) {
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

    public String obtenerNombreHost(InetAddress address) {
        try {
            String hostName = address.getHostName();
            if (!hostName.equals(address.getHostAddress())) {
                return hostName;
            }
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
}


