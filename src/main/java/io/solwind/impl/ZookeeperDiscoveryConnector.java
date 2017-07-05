package io.solwind.impl;

import io.solwind.Functions;
import io.solwind.api.DiscoveryConfig;
import io.solwind.exception.DedicatedRuntimeException;
import io.solwind.handler.RegistrationServiceHolder;
import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * Created by solwind on 6/14/17.
 */
public class ZookeeperDiscoveryConnector implements DiscoveryConfig {

    public static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperDiscoveryConnector.class);

    private final Properties properties;
    private String host;
    private ZooKeeper zk;
    private ZooKeeperClient zooKeeperClient;
    private static final String SLASH = "/";

    public ZookeeperDiscoveryConnector(Properties properties) {
        this.properties = properties;
    }

    public ZookeeperDiscoveryConnector() {
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("application.properties");
        if (resourceAsStream == null) {
            throw new IllegalStateException("application.properties not found in class path");
        }
        this.properties = new Properties();
        try {
            this.properties.load(resourceAsStream);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public void init() {
        this.host = properties.getProperty("zookeeper.connection.host");
        this.zooKeeperClient = new ZooKeeperClient();
    }

    public void connect() throws IOException, InterruptedException {
        this.zk = this.zooKeeperClient.connect();
    }

    public void push(Class className, RegistrationServiceHolder data) throws KeeperException, InterruptedException {
        this.zooKeeperClient.create(className, data);
    }

    public List<RegistrationServiceHolder> retrieveAll(String path) {
        try {
            byte[] data = ZookeeperDiscoveryConnector.this.zk.getData(path, true, null);
            List<RegistrationServiceHolder> holders = new ArrayList<>();
            Functions.<List<RegistrationServiceHolder>>deserialize().apply(data).ifPresent(holders::addAll);
            return holders;
        } catch (KeeperException e) {
            LOGGER.info(e.getMessage(), e);
        } catch (InterruptedException e) {
            LOGGER.info(e.getMessage(), e);
            Thread.currentThread().interrupt();
        }
        return Collections.emptyList();
    }

    public Properties props() {
        return this.properties;
    }

    private class ZooKeeperClient {

        final CountDownLatch connectedSignal = new CountDownLatch(1);
        private ZooKeeper zoo;

        public ZooKeeper connect() throws IOException, InterruptedException {

            zoo = new ZooKeeper(host, 5000, we -> {

                if (we.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    connectedSignal.countDown();
                }
            });

            connectedSignal.await();
            return zoo;
        }

        public void close() throws InterruptedException {
            zoo.close();
        }

        public void create(Class className, RegistrationServiceHolder data) throws
                KeeperException, InterruptedException {

            String path = SLASH + className.getCanonicalName();
            if (ZookeeperDiscoveryConnector.this.zk.exists(path, true) != null) {
                byte[] s = ZookeeperDiscoveryConnector.this.zk.getData(path, true, null);
                Functions.<List<RegistrationServiceHolder>>deserialize().apply(s).ifPresent(holders -> {
                    try {
                        holders.add(data);
                        Optional<Byte[]> apply = Functions.serialize.apply(holders);
                        ZookeeperDiscoveryConnector.this.zk.delete(path, 0);
                        apply.ifPresent(bytes -> createNewNode(path, bytes));
                    } catch (KeeperException | InterruptedException e) {
                        throw new DedicatedRuntimeException(e);
                    }
                });
            } else {
                List<RegistrationServiceHolder> list = new ArrayList();
                list.add(data);
                Functions.serialize.apply(list).ifPresent(bytes -> createNewNode(path, bytes));
            }
        }

        private void createNewNode(String path, Byte[] bytes) {
            try {
                ZookeeperDiscoveryConnector.this.zk.create(path, Functions.byteConverter.apply(bytes), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                        CreateMode.PERSISTENT);
            } catch (Exception e) {
                throw new DedicatedRuntimeException(e);
            }
        }

    }

}
