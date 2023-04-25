package org.eaip.rsocket.config.client;

import org.eaip.rsocket.cloudevents.CloudEventImpl;
import org.eaip.rsocket.config.bootstrap.RSocketConfigPropertySourceLocator;
import org.eaip.rsocket.events.CloudEventSupport;
import org.eaip.rsocket.events.ConfigEvent;
import org.eaip.rsocket.observability.RsocketErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Sinks;

import java.io.StringReader;
import java.util.Properties;

/**
 * Config Event Processor
 *
 * @author CuiChangHe
 */
@SuppressWarnings("rawtypes")
@Component
public class ConfigurationEventProcessor implements InitializingBean {
    private Logger log = LoggerFactory.getLogger(ConfigurationEventProcessor.class);
    private ContextRefresher contextRefresher;
    private String applicationName;
    private Sinks.Many<CloudEventImpl> eventProcessor;

    public ConfigurationEventProcessor(Sinks.Many<CloudEventImpl> eventProcessor, ContextRefresher contextRefresher, String applicationName) {
        this.eventProcessor = eventProcessor;
        this.contextRefresher = contextRefresher;
        this.applicationName = applicationName;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        eventProcessor.asFlux().subscribe(cloudEvent -> {
            String type = cloudEvent.getAttributes().getType();
            if (ConfigEvent.class.getCanonicalName().equalsIgnoreCase(type)) {
                handleConfigurationEvent(cloudEvent);
            }
        });
    }

    public void handleConfigurationEvent(CloudEventImpl<?> cloudEvent) {
        // replyto support
        // cloudEvent.getExtensions().get("replyto"); rsocket:///REQUEST_FNF/com.xxxx.XxxService#method
        ConfigEvent configEvent = CloudEventSupport.unwrapData(cloudEvent, ConfigEvent.class);
        // validate config content
        if (configEvent != null && applicationName.equalsIgnoreCase(configEvent.getAppName())
                && !RSocketConfigPropertySourceLocator.getLastConfigText().equals(configEvent.getContent())) {
            Properties configProperties = RSocketConfigPropertySourceLocator.CONFIG_PROPERTIES.get(applicationName);
            if (configProperties != null) {
                try {
                    configProperties.load(new StringReader(configEvent.getContent()));
                    log.info(RsocketErrorCode.message("RST-202200", applicationName));
                    contextRefresher.refresh();
                    RSocketConfigPropertySourceLocator.setLastConfigText(configEvent.getContent());
                    log.info(RsocketErrorCode.message("RST-202001"));
                } catch (Exception e) {
                    log.info(RsocketErrorCode.message("RST-202501"), e);
                }
            }
        }
    }
}
