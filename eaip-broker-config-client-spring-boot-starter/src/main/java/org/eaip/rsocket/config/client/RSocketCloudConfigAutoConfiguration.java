package org.eaip.rsocket.config.client;

import org.eaip.rsocket.cloudevents.CloudEventImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import reactor.core.publisher.Sinks;

/**
 * RSocket cloud config auto configuration
 *
 * @author CuiChangHe
 */
@Configuration
public class RSocketCloudConfigAutoConfiguration {
    @Value("${spring.application.name}")
    private String applicationName;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Bean
    public ConfigurationEventProcessor rSocketConfigListener(@Autowired ContextRefresher contextRefresher,
                                                             @Autowired @Qualifier("reactiveCloudEventProcessor") Sinks.Many<CloudEventImpl> eventProcessor) {
        return new ConfigurationEventProcessor(eventProcessor, contextRefresher, applicationName);
    }
}
