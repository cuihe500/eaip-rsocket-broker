package org.eaip.rsocket.broker;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.page.Viewport;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
import io.micrometer.core.instrument.MeterRegistry;
import org.eaip.rsocket.encoding.RSocketEncodingFacade;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;

/**
 * EAIP RSocket Broker Server
 *
 * @author CuiChangHe
 */
@SpringBootApplication
@StyleSheet("styles/styles.css")
@Theme(themeClass = Lumo.class)
@Viewport("width=device-width, minimum-scale=1, initial-scale=1, user-scalable=yes, viewport-fit=cover")
@Push
public class EAIPRSocketBrokerServer implements AppShellConfigurator {
    public static final LocalDateTime STARTED_AT = LocalDateTime.now();

    public static void main(String[] args) {
        //checking encoder first
        //noinspection ResultOfMethodCallIgnored
        RSocketEncodingFacade.getInstance();
        SpringApplication.run(EAIPRSocketBrokerServer.class, args);
    }

    @Bean
    MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config().commonTags("application", "eaip-rsocket-broker");
    }

    /*@Bean
    public RSocketListenerCustomizer websocketListenerCustomizer() {
        return builder -> {
            builder.listen("ws", 19999);
        };
    }*/
}
