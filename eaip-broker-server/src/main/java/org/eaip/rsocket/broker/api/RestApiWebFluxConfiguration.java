package org.eaip.rsocket.broker.api;

import org.eaip.rsocket.broker.api.converter.ByteBufDecoder;
import org.eaip.rsocket.broker.api.converter.ByteBufEncoder;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.DecoderHttpMessageReader;
import org.springframework.http.codec.EncoderHttpMessageWriter;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.config.WebFluxConfigurer;

/**
 * REST API Configuration for WebFlux
 *
 * @author CuiChangHe
 */
@Configuration
public class RestApiWebFluxConfiguration implements WebFluxConfigurer {

    @Override
    public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
        configurer.customCodecs().register(new EncoderHttpMessageWriter<>(new ByteBufEncoder()));
        configurer.customCodecs().register(new DecoderHttpMessageReader<>(new ByteBufDecoder()));
    }
}
