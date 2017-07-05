package io.solwind.api;

import io.solwind.handler.RegistrationServiceHolder;
import org.apache.zookeeper.KeeperException;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * Created by solwind on 6/14/17.
 */
public interface DiscoveryConfig {
    void init();
    void connect() throws IOException, InterruptedException;
    void push(Class className, RegistrationServiceHolder data) throws KeeperException, InterruptedException;
    List<RegistrationServiceHolder> retrieveAll(String path);
    Properties props();
}
