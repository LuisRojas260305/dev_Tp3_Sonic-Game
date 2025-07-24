package com.miestudio.jsonic.Server.domain;

import java.io.Serializable;

/**
 * Paquete de señalización utilizado para indicar el cierre de la conexión o del servidor.
 * Implementa {@link Serializable} para poder ser enviado a través de la red.
 */
public class ShutdownPacket implements Serializable {
    private static final long serialVersionUID = 1L;
}
