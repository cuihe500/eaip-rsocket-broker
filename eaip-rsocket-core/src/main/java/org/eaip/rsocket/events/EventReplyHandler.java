package org.eaip.rsocket.events;

/**
 * Event Reply Handler
 *
 * @author CuiCHangHe
 */
@FunctionalInterface
public interface EventReplyHandler {

    void accept(EventReply reply);
}
