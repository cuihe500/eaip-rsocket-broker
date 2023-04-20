package org.eaip.rsocket.broker.web.ui;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import org.eaip.rsocket.route.RSocketFilter;
import org.eaip.rsocket.route.RSocketFilterChain;
import org.eaip.spring.boot.rsocket.broker.cluster.RSocketBrokerManager;
import org.eaip.spring.boot.rsocket.broker.events.RSocketFilterEnableEvent;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URI;


/**
 * RSocket filter view
 *
 * @author CuiChangHe
 */
@Route(value = RSocketFiltersView.NAV, layout = MainLayout.class)
public class RSocketFiltersView extends VerticalLayout {
    public static final String NAV = "filtersView";
    private Grid<RSocketFilter> filterGrid = new Grid<>();
    private RSocketFilterChain filterChain;

    public RSocketFiltersView(@Autowired RSocketFilterChain filterChain, @Autowired RSocketBrokerManager brokerManager) {
        this.filterChain = filterChain;
        add(new H1("RSocket Filters"));
        //services & applications
        filterGrid.addColumn(RSocketFilter::getClass).setHeader("Filter Class").setAutoWidth(true);
        filterGrid.addColumn(RSocketFilter::name).setHeader("Name");
        filterGrid.addColumn(new ComponentRenderer<>(filter -> {
                    Checkbox checkbox = new Checkbox(filter.isEnabled());
                    checkbox.addValueChangeListener(event -> filter.setEnabled(checkbox.getValue()));
                    RSocketFilterEnableEvent filterEnableEvent = new RSocketFilterEnableEvent(filter.getClass().getCanonicalName(), checkbox.getValue());
                    brokerManager.broadcast(filterEnableEvent.toCloudEvent(URI.create("broker:" + brokerManager.localBroker().getIp()))).subscribe();
                    return checkbox;
                })
        ).setHeader("Enabled");
        add(filterGrid);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        filterGrid.setItems(filterChain.getFilters());
    }
}
