package org.solwind;

import java.io.IOException;

/**
 * Created by solwind on 6/14/17.
 */
public interface IDiscovery {
    <T> T lookup(Class<T> service) throws IOException, InterruptedException;
}
