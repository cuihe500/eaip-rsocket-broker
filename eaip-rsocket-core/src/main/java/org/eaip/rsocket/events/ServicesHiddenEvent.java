package org.eaip.rsocket.events;

import org.eaip.rsocket.RSocketAppContext;
import org.eaip.rsocket.ServiceLocator;
import org.eaip.rsocket.cloudevents.CloudEventImpl;
import org.eaip.rsocket.cloudevents.RSocketCloudEventBuilder;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * services hidden event: remove services from routing table
 *
 * @author CuiCHangHe
 */
public class ServicesHiddenEvent implements CloudEventSupport<ServicesHiddenEvent> {
    /**
     * app UUID
     */
    private String appId;
    /**
     * hidden services
     */
    private Set<ServiceLocator> services = new HashSet<>();

    public ServicesHiddenEvent() {
    }

    public static CloudEventImpl<ServicesHiddenEvent> convertServicesToCloudEvent(Collection<ServiceLocator> serviceLocators) {
        ServicesHiddenEvent servicesHiddenEvent = new ServicesHiddenEvent();
        for (ServiceLocator serviceLocator : serviceLocators) {
            servicesHiddenEvent.addService(serviceLocator);
        }
        servicesHiddenEvent.setAppId(RSocketAppContext.ID);
        return RSocketCloudEventBuilder
                .builder(servicesHiddenEvent)
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
