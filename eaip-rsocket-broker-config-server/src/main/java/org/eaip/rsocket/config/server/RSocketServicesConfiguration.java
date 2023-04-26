package org.eaip.rsocket.config.server;

import org.eaip.rsocket.cloudevents.CloudEventsNotifyService;
import org.eaip.rsocket.invocation.RSocketRemoteServiceBuilder;
import org.eaip.rsocket.upstream.UpstreamManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration(proxyBeanMethods = false)
public class RSocketServicesConfiguration {
    @Bean
    public CloudEventsNotifyService cloudEventsNotifyService(UpstreamManager upstreamManager) {
        return RSocketRemoteServiceBuilder
                .client(CloudEventsNotifyService.class)
                .upstreamManager(upstreamManager)
                .build();
    }
}
