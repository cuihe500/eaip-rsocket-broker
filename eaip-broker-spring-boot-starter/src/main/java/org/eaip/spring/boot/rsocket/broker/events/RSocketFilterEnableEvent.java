package org.eaip.spring.boot.rsocket.broker.events;

import org.eaip.rsocket.events.CloudEventSupport;

/**
 * RSocket filter enable event
 *
 * @author CuiChangHe
 */
public class RSocketFilterEnableEvent implements CloudEventSupport<RSocketFilterEnableEvent> {
    private String filterClassName;
    private boolean enabled;

    public RSocketFilterEnableEvent() {
    }

    public RSocketFilterEnableEvent(String filterClassName, boolean enabled) {
        this.filterClassName = filterClassName;
        this.enabled = enabled;
    }

    public String getFilterClassName() {
        return filterClassName;
    }

    public void setFilterClassName(String filterClassName) {
        this.filterClassName = filterClassName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
