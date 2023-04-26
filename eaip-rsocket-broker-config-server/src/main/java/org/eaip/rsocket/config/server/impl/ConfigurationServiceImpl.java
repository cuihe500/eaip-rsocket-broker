package org.eaip.rsocket.config.server.impl;

import org.eaip.rsocket.RSocketService;
import org.eaip.rsocket.config.ConfigurationService;
import org.eaip.rsocket.config.server.ConfigurationServiceSupport;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@RSocketService(serviceInterface = ConfigurationService.class)
@Service
public class ConfigurationServiceImpl implements ConfigurationService {
    @Autowired
    @Lazy
    private ConfigurationServiceSupport configurationServiceSupport;

    @Override
    public Mono<String> get(@NotNull String appName, @NotNull String key) {
        return configurationServiceSupport.get(appName, key);
    }
}
