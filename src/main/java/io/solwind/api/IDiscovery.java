package io.solwind.api;

import java.io.IOException;
import java.util.List;

/**
 * Created by solwind on 6/14/17.
 */
public interface IDiscovery {
    <T> T lookup(Class<T> service, String exposerName, RmiConnectorClient rmiConnectorClient) throws IOException, InterruptedException;
    <T> T lookup(Class<T> service, String exposerName, RmiConnectorClient rmiConnectorClient, String token) throws IOException, InterruptedException;
    <T> List<T> lookupAll(Class<T> service, RmiConnectorClient rmiConnectorClient) throws IOException, InterruptedException;
}
