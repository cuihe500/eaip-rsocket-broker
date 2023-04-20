package org.eaip.rsocket.reactive;

import org.eaip.rsocket.MutableContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Reactive Adapter to Reactor
 *
 * @author CuiCHangHe
 */
public interface ReactiveAdapter {

    @NotNull
    static ReactiveAdapter findAdapter(String returnTypeName) {
        if (returnTypeName.equals("java.util.concurrent.CompletableFuture")) {
            return ReactiveAdapterFuture.getInstance();
        } else if (returnTypeName.startsWith("io.reactivex.rxjava3.")) {
            return ReactiveAdapterRxJava3.getInstance();
        } else if (returnTypeName.startsWith("io.reactivex.")) {
            return ReactiveAdapterRxJava2.getInstance();
        } else if (returnTypeName.startsWith("kotlinx.coroutines")) {
            return ReactiveAdapterKotlin.getInstance();
        } else {
            return ReactiveAdapterDefault.getInstance();
        }
    }

    <T> Mono<T> toMono(@Nullable Object source);

    <T> Flux<T> toFlux(@Nullable Object source);

    Object fromPublisher(Mono<?> mono, Class<?> returnType, MutableContext mutableContext);

    Object fromPublisher(Flux<?> flux, Class<?> returnType, MutableContext mutableContext);

    Object fromPublisher(Flux<?> flux, Class<?> returnType);
}
