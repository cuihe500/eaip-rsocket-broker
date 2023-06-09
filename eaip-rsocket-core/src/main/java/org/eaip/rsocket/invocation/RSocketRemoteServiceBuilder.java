package org.eaip.rsocket.invocation;

import brave.Tracing;
import org.eaip.rsocket.ServiceLocator;
import org.eaip.rsocket.ServiceMapping;
import org.eaip.rsocket.metadata.RSocketMimeType;
import org.eaip.rsocket.upstream.UpstreamManager;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Proxy;
import java.net.URI;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

/**
 * RSocket remote service builder
 *
 * @author CuiCHangHe
 */
public class RSocketRemoteServiceBuilder<T> {
    public static final Set<ServiceLocator> CONSUMED_SERVICES = new HashSet<>();
    public static boolean byteBuddyAvailable = true;

    static {
        try {
            Class.forName("net.bytebuddy.ByteBuddy");
        } catch (Exception e) {
            byteBuddyAvailable = false;
        }
    }

    private URI sourceUri;
    private String group;
    private String service;
    private String version;
    private Duration timeout = Duration.ofMillis(15000);
    private String endpoint;
    private boolean sticky;
    private boolean p2p;
    private Class<T> serviceInterface;
    private RSocketMimeType encodingType = RSocketMimeType.Hessian;
    private RSocketMimeType acceptEncodingType;
    private UpstreamManager upstreamManager;
    /**
     * zipkin brave tracing client
     */
    private boolean braveTracing = true;
    private Tracing tracing;

    public static <T> RSocketRemoteServiceBuilder<T> client(Class<T> serviceInterface) {
        RSocketRemoteServiceBuilder<T> builder = new RSocketRemoteServiceBuilder<T>();
        builder.serviceInterface = serviceInterface;
        builder.service = serviceInterface.getCanonicalName();
        ServiceMapping serviceMapping = serviceInterface.getAnnotation(ServiceMapping.class);
        if (serviceMapping != null) {
            if (!serviceMapping.group().isEmpty()) {
                builder.group = serviceMapping.group();
            }
            if (!serviceMapping.version().isEmpty()) {
                builder.version = serviceMapping.group();
            }
            if (!serviceMapping.value().isEmpty()) {
                builder.service = serviceMapping.value();
            }
            if (!serviceMapping.endpoint().isEmpty()) {
                builder.endpoint = serviceMapping.endpoint();
            }
            if (!serviceMapping.paramEncoding().isEmpty()) {
                builder.encodingType = RSocketMimeType.valueOfType(serviceMapping.paramEncoding());
            }
            if (!serviceMapping.resultEncoding().isEmpty()) {
                builder.acceptEncodingType = RSocketMimeType.valueOfType(serviceMapping.resultEncoding());
            }
            builder.sticky = serviceMapping.sticky();
        }
        try {
            Class.forName("brave.propagation.TraceContext");
        } catch (ClassNotFoundException e) {
            builder.braveTracing = false;
        }
        return builder;
    }

    public RSocketRemoteServiceBuilder<T> group(String group) {
        this.group = group;
        return this;
    }

    public RSocketRemoteServiceBuilder<T> service(String service) {
        this.service = service;
        return this;
    }

    public RSocketRemoteServiceBuilder<T> version(String version) {
        this.version = version;
        return this;
    }

    /**
     * timeout configuration, and default timeout is 3000 millis
     * if the call is long time task, please set it to big value
     *
     * @param millis millis
     * @return builder
     */
    public RSocketRemoteServiceBuilder<T> timeoutMillis(int millis) {
        this.timeout = Duration.ofMillis(millis);
        return this;
    }

    public RSocketRemoteServiceBuilder<T> endpoint(String endpoint) {
        assert endpoint.contains(":");
        this.endpoint = endpoint;
        return this;
    }

    public RSocketRemoteServiceBuilder<T> sticky(boolean sticky) {
        this.sticky = sticky;
        return this;
    }

    public RSocketRemoteServiceBuilder<T> p2p(boolean p2p) {
        this.p2p = p2p;
        return this;
    }

    public RSocketRemoteServiceBuilder<T> tracing(Tracing tracing) {
        this.tracing = tracing;
        return this;
    }

    public RSocketRemoteServiceBuilder<T> encodingType(RSocketMimeType encodingType) {
        this.encodingType = encodingType;
        return this;
    }

    public RSocketRemoteServiceBuilder<T> acceptEncodingType(RSocketMimeType encodingType) {
        this.acceptEncodingType = encodingType;
        return this;
    }

    /**
     * GraalVM nativeImage support: set encodeType and acceptEncodingType to Json
     *
     * @return this
     */
    public RSocketRemoteServiceBuilder<T> nativeImage() {
        this.encodingType = RSocketMimeType.Json;
        this.acceptEncodingType = RSocketMimeType.Json;
        return this;
    }

    public RSocketRemoteServiceBuilder<T> upstreamManager(UpstreamManager upstreamManager) {
        this.upstreamManager = upstreamManager;
        this.sourceUri = upstreamManager.requesterSupport().originUri();
        return this;
    }

    public T build() {
        if (byteBuddyAvailable) {
            return buildByteBuddyProxy();
        } else {
            return buildJdkProxy();
        }
    }

    @NotNull
    private RSocketRequesterRpcProxy getRequesterProxy() {
        if (this.p2p) {
            this.upstreamManager.addP2pService(ServiceLocator.serviceId(this.group, this.service, this.version));
        }
        if (this.braveTracing && this.tracing != null) {
            return new RSocketRequesterRpcZipkinProxy(tracing, upstreamManager, group, serviceInterface, service, version,
                    encodingType, acceptEncodingType, timeout, endpoint, sticky, sourceUri, !byteBuddyAvailable);
        } else {
            return new RSocketRequesterRpcProxy(upstreamManager, group, serviceInterface, service, version,
                    encodingType, acceptEncodingType, timeout, endpoint, sticky, sourceUri, !byteBuddyAvailable);
        }
    }

    @SuppressWarnings("unchecked")
    public T buildJdkProxy() {
        CONSUMED_SERVICES.add(new ServiceLocator(group, service, version));
        RSocketRequesterRpcProxy proxy = getRequesterProxy();
        return (T) Proxy.newProxyInstance(
                serviceInterface.getClassLoader(),
                new Class[]{serviceInterface},
                proxy);
    }

    public T buildByteBuddyProxy() {
        CONSUMED_SERVICES.add(new ServiceLocator(group, service, version));
        RSocketRequesterRpcProxy proxy = getRequesterProxy();
        return ByteBuddyUtils.build(this.serviceInterface, proxy);
    }
}
