package org.eaip.rsocket.listen;

import io.rsocket.ConnectionSetupPayload;
import io.rsocket.RSocket;
import io.rsocket.SocketAcceptor;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

/**
 * rsocket responder handler factory
 *
 * @author CuiCHangHe
 */
@FunctionalInterface
public interface RSocketResponderHandlerFactory extends SocketAcceptor {

    @NotNull Mono<RSocket> accept(@NotNull ConnectionSetupPayload setup, @NotNull RSocket sendingSocket);
}
