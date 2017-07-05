package io.solwind.api;

/**
 * Created by theso on 7/5/2017.
 */
public interface RmiConnectionClientFactory {
    RmiConnectorClient newClient(String host, int port) throws InterruptedException;
}
