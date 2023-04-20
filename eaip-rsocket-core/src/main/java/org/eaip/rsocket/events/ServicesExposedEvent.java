package org.eaip.rsocket.events;

import org.eaip.rsocket.RSocketAppContext;
import org.eaip.rsocket.ServiceLocator;
import org.eaip.rsocket.cloudevents.CloudEventImpl;
import org.eaip.rsocket.cloudevents.RSocketCloudEventBuilder;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * services exposed event: register service on routing table
 *
 * @author CuiCHangHe
 */
public class ServicesExposedEvent implements CloudEventSupport<ServicesExposedEvent> {
    /**
     * app UUID
     */
    private String appId;
    /**
     * exposed services
     */
    private Set<ServiceLocator> services = new HashSet<>();

    public ServicesExposedEvent() {
    }

    public static CloudEventImpl<ServicesExposedEvent> convertServicesToCloudEvent(Collection<ServiceLocator> serviceLocators) {
        ServicesExposedEvent servicesExposedEvent = new ServicesExposedEvent();
        for (ServiceLocator serviceLocator : serviceLocators) {
            servicesExposedEvent.addService(serviceLocator);
        }
        servicesExposedEvent.setAppId(RSocketAppContext.ID);
        return RSocketCloudEventBuilder
                .builder(servicesExposedEvent)
                .build();
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public Set<ServiceLocator> getServices() {
        return services;
    }

    public void setServices(Set<ServiceLocator> services) {
        this.services = services;
    }

    public void addService(ServiceLocator serviceLocator) {
        this.services.add(serviceLocator);
    }
}
