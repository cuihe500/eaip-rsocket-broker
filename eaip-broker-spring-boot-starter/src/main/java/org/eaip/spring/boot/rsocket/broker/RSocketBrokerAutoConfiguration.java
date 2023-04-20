package org.eaip.spring.boot.rsocket.broker;

import io.rsocket.RSocket;
import org.eaip.rsocket.cloudevents.CloudEventImpl;
import org.eaip.rsocket.cloudevents.CloudEventsNotifyService;
import org.eaip.rsocket.discovery.DiscoveryService;
import org.eaip.rsocket.health.RSocketServiceHealth;
import org.eaip.rsocket.listen.RSocketListener;
import org.eaip.rsocket.listen.RSocketListenerCustomizer;
import org.eaip.rsocket.route.RSocketFilter;
import org.eaip.rsocket.route.RSocketFilterChain;
import org.eaip.rsocket.rpc.LocalReactiveServiceCaller;
import org.eaip.spring.boot.rsocket.broker.cloudevents.CloudEventsNotifyServiceImpl;
import org.eaip.spring.boot.rsocket.broker.cluster.DefaultRSocketBrokerManager;
import org.eaip.spring.boot.rsocket.broker.cluster.RSocketBrokerManager;
import org.eaip.spring.boot.rsocket.broker.cluster.RSocketBrokerManagerDiscoveryImpl;
import org.eaip.spring.boot.rsocket.broker.cluster.RSocketBrokerManagerGossipImpl;
import org.eaip.spring.boot.rsocket.broker.impl.BrokerRSocketServiceHealthImpl;
import org.eaip.spring.boot.rsocket.broker.impl.DiscoveryServiceImpl;
import org.eaip.spring.boot.rsocket.broker.responder.AppStatusCloudEventProcessor;
import org.eaip.spring.boot.rsocket.broker.responder.RSocketBrokerHandlerRegistry;
import org.eaip.spring.boot.rsocket.broker.responder.RSocketBrokerHandlerRegistryImpl;
import org.eaip.spring.boot.rsocket.broker.route.ServiceMeshInspector;
import org.eaip.spring.boot.rsocket.broker.route.ServiceRoutingSelector;
import org.eaip.spring.boot.rsocket.broker.route.impl.ServiceMeshInspectorImpl;
import org.eaip.spring.boot.rsocket.broker.route.impl.ServiceRoutingSelectorImpl;
import org.eaip.spring.boot.rsocket.broker.security.AuthenticationService;
import org.eaip.spring.boot.rsocket.broker.security.AuthenticationServiceJwtImpl;
import org.eaip.spring.boot.rsocket.broker.services.AppQueryController;
import org.eaip.spring.boot.rsocket.broker.services.MetricsScrapeController;
import org.eaip.spring.boot.rsocket.broker.services.ServiceQueryController;
import org.eaip.spring.boot.rsocket.broker.supporting.RSocketLocalServiceAnnotationProcessor;
import org.eaip.spring.boot.rsocket.broker.upstream.RSocketRequesterBySubBroker;
import org.eaip.spring.boot.rsocket.broker.upstream.UpstreamBrokerCluster;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import reactor.core.publisher.Sinks;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.stream.Collectors;


/**
 * RSocket broker auto configuration
 *
 * @author CuiChangHe
 */
@SuppressWarnings("rawtypes")
@Configuration
@EnableConfigurationProperties(RSocketBrokerProperties.class)
public class RSocketBrokerAutoConfiguration {
    @Bean
    public RSocketFilterChain rsocketFilterChain(ObjectProvider<RSocketFilter> rsocketFilters) {
        return new RSocketFilterChain(rsocketFilters.orderedStream().collect(Collectors.toList()));
    }

    @Bean
    public ServiceMeshInspector serviceMeshInspector(RSocketBrokerProperties brokerProperties) {
        return new ServiceMeshInspectorImpl(brokerProperties.isAuthRequired());
    }

    @Bean
    public ServiceRoutingSelector serviceRoutingSelector(@Autowired @Qualifier("p2pServiceNotificationProcessor") Sinks.Many<String> p2pServiceNotificationProcessor) {
        return new ServiceRoutingSelectorImpl(p2pServiceNotificationProcessor);
    }

    @Bean
    public AppQueryController appQueryController() {
        return new AppQueryController();
    }

    @Bean
    public ServiceQueryController serviceQueryController() {
        return new ServiceQueryController();
    }

    @Bean
    public MetricsScrapeController metricsScrapeController() {
        return new MetricsScrapeController();
    }

    @Bean
    public RSocketBrokerHandlerRegistry rsocketResponderHandlerRegistry(@Autowired LocalReactiveServiceCaller localReactiveServiceCaller,
                                                                        @Autowired RSocketFilterChain rsocketFilterChain,
                                                                        @Autowired ServiceRoutingSelector routingSelector,
                                                                        @Autowired @Qualifier("reactiveCloudEventProcessor") Sinks.Many<CloudEventImpl> eventProcessor,
                                                                        @Autowired @Qualifier("appNotificationProcessor") Sinks.Many<String> appNotificationProcessor,
                                                                        @Autowired @Qualifier("p2pServiceNotificationProcessor") Sinks.Many<String> p2pServiceNotificationProcessor,
                                                                        @Autowired AuthenticationService authenticationService,
                                                                        @Autowired RSocketBrokerManager rSocketBrokerManager,
                                                                        @Autowired ServiceMeshInspector serviceMeshInspector,
                                                                        @Autowired RSocketBrokerProperties properties,
                                                                        @Autowired ApplicationContext applicationContext) {
        return new RSocketBrokerHandlerRegistryImpl(localReactiveServiceCaller, rsocketFilterChain, routingSelector,
                eventProcessor, appNotificationProcessor, p2pServiceNotificationProcessor, authenticationService, rSocketBrokerManager, serviceMeshInspector,
                properties.isAuthRequired(), applicationContext);
    }

    @Bean
    public RSocketLocalServiceAnnotationProcessor rSocketServiceAnnotationProcessor() {
        return new RSocketLocalServiceAnnotationProcessor();
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public RSocketListener rsocketListener(ObjectProvider<RSocketListenerCustomizer> customizers) {
        RSocketListener.Builder builder = RSocketListener.builder();
        customizers.orderedStream().forEach((customizer) -> customizer.customize(builder));
        return builder.build();
    }

    @Bean
    @Order(100)
    public RSocketListenerCustomizer defaultRSocketListenerCustomizer(@Autowired RSocketBrokerHandlerRegistry registry,
                                                                      @Autowired RSocketBrokerProperties properties) {
        return builder -> {
            builder.acceptor(registry);
            builder.listen("tcp", properties.getListen());
        };
    }

    @Bean
    @Order(101)
    @ConditionalOnProperty(name = "rsocket.broker.ssl.key-store")
    RSocketListenerCustomizer rSocketListenerSSLCustomizer(@Autowired RSocketBrokerProperties properties,
                                                           @Autowired ResourceLoader resourceLoader) {
        return builder -> {
            RSocketBrokerProperties.RSocketSSL rSocketSSL = properties.getSsl();
            if (rSocketSSL != null && rSocketSSL.isEnabled() && rSocketSSL.getKeyStore() != null) {
                try {
                    KeyStore store = KeyStore.getInstance("PKCS12");
                    store.load(resourceLoader.getResource(rSocketSSL.getKeyStore()).getInputStream(), rSocketSSL.getKeyStorePassword().toCharArray());
                    String alias = store.aliases().nextElement();
                    Certificate certificate = store.getCertificate(alias);
                    KeyStore.Entry entry = store.getEntry(alias, new KeyStore.PasswordProtection(rSocketSSL.getKeyStorePassword().toCharArray()));
                    PrivateKey privateKey = ((KeyStore.PrivateKeyEntry) entry).getPrivateKey();
                    builder.sslContext(certificate, privateKey);
                    builder.listen("tcps", properties.getListen());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    @Bean
    public DiscoveryService rsocketDiscoveryService() {
        return new DiscoveryServiceImpl();
    }

    @Bean
    public CloudEventsNotifyService cloudEventsNotifyService(@Autowired RSocketBrokerHandlerRegistry handlerRegistry) {
        return new CloudEventsNotifyServiceImpl(handlerRegistry);
    }

    @Bean
    public AuthenticationService authenticationService() throws Exception {
        return new AuthenticationServiceJwtImpl();
    }

    @Bean
    public RSocketServiceHealth brokRSocketServiceHealth(@Autowired ServiceRoutingSelector routingSelector) {
        return new BrokerRSocketServiceHealthImpl(routingSelector);
    }

    @Bean
    @ConditionalOnMissingBean(RSocketBrokerManager.class)
    public RSocketBrokerManager rsocketBrokerManager() {
        return new DefaultRSocketBrokerManager();
    }

    @Bean
    @ConditionalOnExpression("'${rsocket.broker.topology}'=='gossip'")
    @Primary
    public RSocketBrokerManager rsocketGossipBrokerManager() {
        return new RSocketBrokerManagerGossipImpl();
    }

    @Bean
    @ConditionalOnExpression("'${rsocket.broker.topology}'=='discovery' || '${rsocket.broker.topology}'=='k8s'")
    @Primary
    public RSocketBrokerManager rsocketDiscoveryBrokerManager(ReactiveDiscoveryClient discoveryClient) {
        return new RSocketBrokerManagerDiscoveryImpl(discoveryClient);
    }

    @Bean
    public Sinks.Many<CloudEventImpl> reactiveCloudEventProcessor() {
        return Sinks.many().multicast().onBackpressureBuffer();
    }

    /**
     * app notification for online and offline
     *
     * @return app notification
     */
    @Bean
    public Sinks.Many<String> appNotificationProcessor() {
        return Sinks.many().multicast().onBackpressureBuffer(8);
    }

    @Bean
    public Sinks.Many<String> p2pServiceNotificationProcessor() {
        return Sinks.many().multicast().onBackpressureBuffer(10000);
    }

    @Bean(initMethod = "init")
    public AppStatusCloudEventProcessor appStatusCloudEventProcessor() {
        return new AppStatusCloudEventProcessor();
    }

    @Bean(initMethod = "init", destroyMethod = "close")
    @ConditionalOnProperty(name = "rsocket.broker.upstream-brokers")
    public UpstreamBrokerCluster upstreamBrokerCluster(@Autowired RSocketBrokerProperties properties,
                                                       @Autowired Environment env,
                                                       @Autowired RSocketFilterChain filterChain,
                                                       @Autowired ServiceRoutingSelector serviceRoutingSelector,
                                                       @Autowired RSocketBrokerHandlerRegistry rsocketResponderHandlerRegistry) {
        RSocketRequesterBySubBroker subBroker = new RSocketRequesterBySubBroker(properties, env, rsocketResponderHandlerRegistry, filterChain, serviceRoutingSelector);
        return new UpstreamBrokerCluster(subBroker);
    }

    @Bean
    @Qualifier("upstreamBrokerRSocket")
    @ConditionalOnProperty(name = "rsocket.broker.upstream-brokers")
    public RSocket upstreamBrokerRSocket(UpstreamBrokerCluster upstreamBrokerCluster) {
        return upstreamBrokerCluster.getLoadBalancedRSocket();
    }

}
