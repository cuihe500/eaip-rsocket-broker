package org.eaip.spring.boot.rsocket.broker;

import org.eaip.rsocket.RSocketAppContext;
import org.eaip.rsocket.transport.NetworkUtil;

import java.net.URI;

/**
 * Broker App Context
 *
 * @author CuiChangHe
 */
public class BrokerAppContext {
    private static URI SOURCE = URI.create("broker://" + NetworkUtil.LOCAL_IP + "/" + "?id=" + RSocketAppContext.ID);

    public static URI identity() {
        return SOURCE;
    }
}
