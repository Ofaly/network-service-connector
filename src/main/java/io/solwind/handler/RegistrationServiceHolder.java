package io.solwind.handler;

import java.util.Objects;

/**
 * Created by theso on 6/24/2017.
 */
public class RegistrationServiceHolder {
    private String host;
    private String version;
    private String shortDescription;

    public RegistrationServiceHolder(String host, String version, String shortDescription) {
        this.host = host;
        this.version = version;
        this.shortDescription = shortDescription;
    }

    public RegistrationServiceHolder(byte[] data) {
        Objects.nonNull(data);
        String[] splittedString = new String(data).split(",");
        this.host = splittedString[0];
        this.version = splittedString.length > 1?splittedString[1]:null;
        this.shortDescription = splittedString.length >= 3?splittedString[2]:null;
    }

    public String getHost() {
        return host;
    }

    public String getVersion() {
        return version;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    @Override
    public String toString() {
        return String.format("%s, %s, %s", host, version, shortDescription);
    }
}
