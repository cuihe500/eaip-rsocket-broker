package org.eaip.rsocket.gateway.grpc;

import io.rsocket.RSocket;
import org.eaip.rsocket.upstream.UpstreamManager;
import org.eaip.spring.boot.rsocket.RSocketProperties;
import org.eaip.user.ReactorAccountServiceGrpc;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * gRPC Service Configuration
 *
 * @author CuiChangHe
 */
@Configuration
public class GrpcServiceConfiguration {

    @Bean
    public RSocket rsocketBroker(@NotNull UpstreamManager upstreamManager) {
        return upstreamManager.findBroker().getLoadBalancedRSocket();
    }

    @Bean
    public ReactorAccountServiceGrpc.AccountServiceImplBase grpcAccountService(@NotNull UpstreamManager upstreamManager,
                                                                               @NotNull RSocketProperties rsocketProperties) throws Exception {
        return GrpcServiceRSocketImplBuilder
                .stub(ReactorAccountServiceGrpc.AccountServiceImplBase.class)
                .upstreamManager(upstreamManager)
                .timeoutMillis(rsocketProperties.getTimeout())
                .build();
    }
}
