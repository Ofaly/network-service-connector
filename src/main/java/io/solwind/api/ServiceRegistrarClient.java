package io.solwind.api;

import java.io.IOException;
import java.util.List;

/**
 * Created by solwind on 6/14/17.
 */
public interface ServiceRegistrarClient {
    <T> T create(Class<T> service, String exposerName, RmiConnectorClient rmiConnectorClient) throws IOException, InterruptedException;
    <T> T create(Class<T> service, String exposerName, RmiConnectorClient rmiConnectorClient, String token) throws IOException, InterruptedException;
    <T> List<T> createForAll(Class<T> service, RmiConnectorClient rmiConnectorClient) throws IOException, InterruptedException;
}
