package io.solwind.impl;

import io.solwind.Functions;
import io.solwind.api.DiscoveryConfig;
import io.solwind.exception.DedicatedRuntimeException;
import io.solwind.handler.RegistrationServiceHolder;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

/**
 * Created by solwind on 6/14/17.
 */
public class ZookeeperDiscoveryConnector implements DiscoveryConfig {

    public static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperDiscoveryConnector.class);

    private final Properties properties;
    private String host;
    private ZooKeeper zk;
    private ZooKeeperClient zooKeeperClient;
    private static final String ROOT = "/io.solwnd.interfaces";
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
        try {
            if (ZookeeperDiscoveryConnector.this.zk.exists(ROOT, true) == null)
                ZookeeperDiscoveryConnector.this.zk.create(ZookeeperDiscoveryConnector.ROOT, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                        CreateMode.PERSISTENT);
        } catch (Exception e) {
            throw new DedicatedRuntimeException(e);
        }
    }

    public void push(Class className, RegistrationServiceHolder data) throws KeeperException, InterruptedException {
        this.zooKeeperClient.create(className, data);
    }

    public Set<RegistrationServiceHolder> retrieveAll(String path, Consumer<Set<RegistrationServiceHolder>> consumer, String exposerNameIfNewNeeded) {
        try {
            Stat exists = ZookeeperDiscoveryConnector.this.zk.exists(ROOT + path, true);
            if (exists == null && exposerNameIfNewNeeded != null) {
                Set<RegistrationServiceHolder> set = new HashSet<>();
                set.add(new RegistrationServiceHolder("emptyhost:0", null, null, exposerNameIfNewNeeded));
                Functions.serialize.apply(set).ifPresent(bytes -> ZookeeperDiscoveryConnector.this.zooKeeperClient.createNewNode(ROOT + path, bytes));
            }
            byte[] data = ZookeeperDiscoveryConnector.this.zk.getData(ROOT + path, new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    try {
                        if (watchedEvent.getType() != Event.EventType.NodeDeleted) {
                            LOGGER.info("Node was changed: {}", watchedEvent);
                            byte[] tmp = ZookeeperDiscoveryConnector.this.zk.getData(watchedEvent.getPath(), this, null);
                            consumer.accept(getRegistrationServiceHoldersFromRawData(tmp));
                        } else {
                            LOGGER.info("Node was deleted: {}", watchedEvent);
                        }
                    } catch (KeeperException e) {
                        LOGGER.info(e.getMessage(), e);
                    } catch (InterruptedException e) {
                        LOGGER.info(e.getMessage(), e);
                        Thread.currentThread().interrupt();
                    }
                }
            }, null);
            return getRegistrationServiceHoldersFromRawData(data);
        } catch (KeeperException e) {
            LOGGER.info(e.getMessage(), e);
        } catch (InterruptedException e) {
            LOGGER.info(e.getMessage(), e);
            Thread.currentThread().interrupt();
        }
        return Collections.emptySet();
    }

    @Override
    public Set<RegistrationServiceHolder> retrieveAll(String path, String exposerNameIfNewNeeded) {
        try {
            Stat exists = ZookeeperDiscoveryConnector.this.zk.exists(ROOT + path, true);
            if (exists == null && exposerNameIfNewNeeded != null) {
                Set<RegistrationServiceHolder> set = new HashSet<>();
                set.add(new RegistrationServiceHolder("emptyhost:0", null, null, exposerNameIfNewNeeded));
                Functions.serialize.apply(set).ifPresent(bytes -> ZookeeperDiscoveryConnector.this.zooKeeperClient.createNewNode(ROOT + path, bytes));
            }
            byte[] data = ZookeeperDiscoveryConnector.this.zk.getData(ROOT + path, true, null);
            return getRegistrationServiceHoldersFromRawData(data);
        } catch (KeeperException e) {
            LOGGER.info(e.getMessage(), e);
        } catch (InterruptedException e) {
            LOGGER.info(e.getMessage(), e);
            Thread.currentThread().interrupt();
        }
        return Collections.emptySet();
    }

    private Set<RegistrationServiceHolder> getRegistrationServiceHoldersFromRawData(byte[] data) {
        Set<RegistrationServiceHolder> holders = new HashSet<>();
        Functions.<Set<RegistrationServiceHolder>>deserialize().apply(data).ifPresent(holders::addAll);
        return holders;
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

            String path = ROOT + SLASH + className.getCanonicalName();
            if (ZookeeperDiscoveryConnector.this.zk.exists(path, true) != null) {
                byte[] s = ZookeeperDiscoveryConnector.this.zk.getData(path, true, null);
                List<RegistrationServiceHolder> tmp = new ArrayList<>();
                Functions.<Set<RegistrationServiceHolder>>deserialize().apply(s).ifPresent(holders -> {
                    holders.forEach(registrationServiceHolder -> {
                        if (registrationServiceHolder.getExposerName().equals(data.getExposerName())) {
                            tmp.add(registrationServiceHolder);
                            LOGGER.info("Removes {}", registrationServiceHolder);
                        }
                    });

                    holders.removeAll(tmp);

                    holders.add(data);
                    Optional<Byte[]> apply = Functions.serialize.apply(holders);
                    apply.ifPresent(bytes -> {
                        try {
                            Stat exists = ZookeeperDiscoveryConnector.this.zk.exists(path, true);
                            ZookeeperDiscoveryConnector.this.zk.setData(path, Functions.byteConverter.apply(bytes), exists.getVersion());
                        } catch (KeeperException e) {
                            LOGGER.info(e.getMessage(), e);
                        } catch (InterruptedException e) {
                            LOGGER.info(e.getMessage(), e);
                            Thread.currentThread().interrupt();
                        }
                    });
                });
            } else {
                Set<RegistrationServiceHolder> list = new HashSet<>();
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
