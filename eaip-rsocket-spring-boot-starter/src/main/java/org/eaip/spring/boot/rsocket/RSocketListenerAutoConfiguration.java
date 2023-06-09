package org.eaip.spring.boot.rsocket;

import io.rsocket.SocketAcceptor;
import org.eaip.rsocket.listen.RSocketListener;
import org.eaip.rsocket.listen.RSocketListenerCustomizer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RSocket listener configuration
 *
 * @author CuiChangHe
 */
@Configuration
@ConditionalOnExpression("${rsocket.port:0}!=0 && ${rsocket.disabled:false}==false")
public class RSocketListenerAutoConfiguration {

    @Bean(initMethod = "start", destroyMethod = "stop")
    public RSocketListener rsocketListener(ObjectProvider<RSocketListenerCustomizer> customizers) {
        RSocketListener.Builder builder = RSocketListener.builder();
        customizers.orderedStream().forEach((customizer) -> customizer.customize(builder));
        return builder.build();
    }

    @Bean
    public RSocketListenerCustomizer defaultRSocketListenerCustomizer(@Autowired SocketAcceptor socketAcceptor, @Autowired RSocketProperties properties) {
        return builder -> {
            builder.acceptor(socketAcceptor);
            builder.listen(properties.getSchema(), properties.getPort());
        };
    }

}
