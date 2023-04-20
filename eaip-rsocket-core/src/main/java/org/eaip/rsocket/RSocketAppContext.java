package org.eaip.rsocket;

import java.util.Map;
import java.util.UUID;

/**
 * RSocket application context: exposed service, requested service, global information
 *
 * @author CuiCHangHe
 */
public class RSocketAppContext {
    public static final String ID = UUID.randomUUID().toString();
    public static int webPort = 0;
    public static int managementPort = 0;
    public static Map<Integer, String> rsocketPorts;
}
