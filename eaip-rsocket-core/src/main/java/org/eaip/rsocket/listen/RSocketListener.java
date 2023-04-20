package org.eaip.rsocket.listen;

import io.rsocket.SocketAcceptor;
import io.rsocket.plugins.DuplexConnectionInterceptor;
import io.rsocket.plugins.RSocketInterceptor;
import io.rsocket.plugins.SocketAcceptorInterceptor;
import org.eaip.rsocket.listen.impl.RSocketListenerBuilderImpl;

import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.Collection;

/**
 * RSocket listener: support multi ports and protocols
 *
 * @author CuiCHangHe
 */
public interface RSocketListener {

    static Builder builder() {
        return new RSocketListenerBuilderImpl();
    }

    Collection<String> serverUris();

    void start() throws Exception;

    void stop() throws Exception;

    Integer getStatus();

    interface Builder {

        Builder host(String host);

        Builder listen(String schema, int port);

        Builder sslContext(Certificate certificate, PrivateKey privateKey);

        Builder addResponderInterceptor(RSocketInterceptor interceptor);

        Builder addSocketAcceptorInterceptor(SocketAcceptorInterceptor interceptor);

        Builder addConnectionInterceptor(DuplexConnectionInterceptor interceptor);

        Builder acceptor(SocketAcceptor acceptor);

        RSocketListener build();
    }
}
