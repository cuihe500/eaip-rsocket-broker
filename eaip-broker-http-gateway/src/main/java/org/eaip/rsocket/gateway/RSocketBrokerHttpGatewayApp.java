package org.eaip.rsocket.gateway;

import org.eaip.rsocket.gateway.converter.ByteBufDecoder;
import org.eaip.rsocket.gateway.converter.ByteBufEncoder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.codec.DecoderHttpMessageReader;
import org.springframework.http.codec.EncoderHttpMessageWriter;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.config.WebFluxConfigurer;

/**
 * RSocket Broker HTTP Gateway App
 *
 * @author CuiChangHe
 */
@SpringBootApplication
public class RSocketBrokerHttpGatewayApp implements WebFluxConfigurer {
    public static void main(String[] args) {
        SpringApplication.run(RSocketBrokerHttpGatewayApp.class, args);
    }

    @Override
    public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
        configurer.customCodecs().register(new EncoderHttpMessageWriter<>(new ByteBufEncoder()));
        configurer.customCodecs().register(new DecoderHttpMessageReader<>(new ByteBufDecoder()));
    }
}
