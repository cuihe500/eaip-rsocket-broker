package org.eaip.rsocket.discovery;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * RSocket Service instance
 *
 * @author CuiCHangHe
 */
public class RSocketServiceInstance implements ServiceInstance, Serializable {
    private String instanceId;
    private String serviceId;
    private String host;
    private int port;
    private String schema = "tcp";
    private String uri;
    private boolean secure = false;
    private Map<String, String> metadata = new HashMap<>();

    public RSocketServiceInstance() {
    }

    public RSocketServiceInstance(String serviceId, String instanceId, String host, int port) {
        this();
        this.serviceId = serviceId;
        this.host = host;
        this.port = port;
        this.instanceId = instanceId;
        this.uri = schema + "://" + host + ":" + port;
    }

    @Override
    public String getInstanceId() {
        return this.instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    @Override
    public String getServiceId() {
        return this.serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    @Override
    public String getHost() {
        return this.host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    @Override
    public int getPort() {
        return this.port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public boolean isSecure() {
        return this.secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    @Override
    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    @Override
    public Map<String, String> getMetadata() {
        return this.metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    @Override
    public String getScheme() {
        return this.schema;
    }
}
