package org.eaip.rsocket.loadbalance;

/**
 * No available connection exception
 *
 * @author CuiCHangHe
 */
public class NoAvailableConnectionException extends Exception {
    public NoAvailableConnectionException(String message) {
        super(message);
    }
}
