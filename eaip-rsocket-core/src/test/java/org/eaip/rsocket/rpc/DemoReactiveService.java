package org.eaip.rsocket.rpc;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Demo reactive service
 *
 * @author CuiCHangHe
 */
public interface DemoReactiveService {
    Mono<String> findNickById(int id);

    Flux<String> findNicks(Flux<Integer> ids);

    @Deprecated
    Mono<String> findNickByEmail(String email);
}
