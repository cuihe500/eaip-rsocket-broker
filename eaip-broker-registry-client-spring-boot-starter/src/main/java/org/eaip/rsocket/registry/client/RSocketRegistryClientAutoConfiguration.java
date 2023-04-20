package org.eaip.rsocket.registry.client;

import org.eaip.rsocket.discovery.DiscoveryService;
import org.eaip.rsocket.invocation.RSocketRemoteServiceBuilder;
import org.eaip.rsocket.upstream.UpstreamManager;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RSocket registry client auto configuration
 *
 * @author CuiChangHe
 */
@Configuration
@EnableConfigurationProperties(RSocketRegistryClientProperties.class)
public class RSocketRegistryClientAutoConfiguration {

    @Bean
    public DiscoveryService rsocketDiscoveryService(UpstreamManager upstreamManager) {
        return RSocketRemoteServiceBuilder
                .client(DiscoveryService.class)
                .upstreamManager(upstreamManager)
                .build();
    }

    @Bean
    public ReactiveDiscoveryClient rsocketDiscoveryClient(DiscoveryService discoveryService) {
        return new RSocketDiscoveryClient(discoveryService);
    }
}
