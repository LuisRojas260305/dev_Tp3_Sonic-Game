package com.miestudio.jsonic.Server.domain;

import java.io.Serializable;

/**
 * Paquete de señalizacion utilizado para indicar el cierre de la conexion o del servidor.
 * Implementa {@link Serializable} para poder ser enviado a traves de la red.
 */
public class ShutdownPacket implements Serializable {
    private static final long serialVersionUID = 1L;
}
