package org.eaip.spring.boot.rsocket.responder.invocation;

import org.eaip.rsocket.RSocketService;
import org.junit.jupiter.api.Assertions;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * local reactive test service implementation
 *
 * @author CuiChangHe
 */
@RSocketService(serviceInterface = ReactiveTestService.class)
@Primary
public class ReactiveTestServiceImpl implements ReactiveTestService {
    @Override
    public Mono<String> findNickById(Integer id) {
        return Mono.just("Leijuan");
    }

    @Override
    public Flux<String> findNames() {
        return Flux.just("first", "second");
    }

    @Override
    public Flux<String> requestGlobals(Flux<String> actions) {
        return actions.map(text -> "received: " + text);
    }

    @Override
    public Flux<String> requestRecommendations(String level, Flux<String> actions) {
        System.out.println("level: " + level);
        Assertions.assertNotNull(level);
        return actions.map(action -> "received:" + action);
    }

}
