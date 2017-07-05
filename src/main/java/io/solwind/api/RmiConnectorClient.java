package io.solwind.api;

/**
 * Created by theso on 6/30/2017.
 */
public interface RmiConnectorClient extends RmiConnectionClientFactory {
    <T> T lastResponse();
    void writeAndFlush(byte[] data);
    void waitForResponse() throws InterruptedException;
    void reconnect() throws InterruptedException;
    void setHost(String host);
    void setPort(Integer port);
}
