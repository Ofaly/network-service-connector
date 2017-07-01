package io.solwind.api;

import java.util.Map;

/**
 * Created by theso on 7/1/2017.
 */
public interface RmiConnectorServer extends Runnable {
    void port(int port);
    void serviceTable(Map<Class, Object> serviceTable);
    void stop();
}
