package io.solwind.api;

import java.io.IOException;

/**
 * Created by solwind on 6/14/17.
 */
public interface IDiscovery {
    <T> T lookup(Class<T> service, String exposerName) throws IOException, InterruptedException;
}
