package com.miestudio.jsonic.Server;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * Clase de utilidad para operaciones relacionadas con la red, como la obtencion de la direccion IP local.
 * Proporciona metodos estaticos para facilitar la configuracion de la red en el juego.
 */
public class NetworkHelper {
    /**
     * Obtiene la direccion IP local no-loopback de la maquina.
     * Busca entre todas las interfaces de red activas y devuelve la primera direccion IP de sitio local encontrada.
     * @return La direccion IP local como una cadena (String), o "127.0.0.1" si no se puede determinar una direccion de sitio local.
     */
    public static String getIpLocal(){
        try{
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

            while (interfaces.hasMoreElements()){
                NetworkInterface iface = interfaces.nextElement();

                // Saltar las interfaces loopback y no activas
                if (iface.isLoopback() || !iface.isUp()) continue;

                Enumeration<InetAddress> addresses = iface.getInetAddresses();

                while (addresses.hasMoreElements()){
                    InetAddress addr = addresses.nextElement();

                    // Buscar direcciones de sitio local (no loopback, no link-local, etc.)
                    if (addr.isSiteLocalAddress()){
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            // En caso de cualquier error al obtener las interfaces de red, imprimir el stack trace.
            e.printStackTrace();
        }

        return "127.0.0.1"; // Fallback a localhost si no se encuentra una dirección de sitio local.
    }
}