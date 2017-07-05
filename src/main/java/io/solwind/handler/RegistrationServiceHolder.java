package io.solwind.handler;

import java.io.Serializable;

/**
 * Created by theso on 6/24/2017.
 */
public class RegistrationServiceHolder implements Serializable {
    private String host;
    private String version;
    private String shortDescription;
    private String exposerName;

    public RegistrationServiceHolder(String host, String version, String shortDescription, String exposerName) {
        this.host = host;
        this.version = version;
        this.shortDescription = shortDescription;
        this.exposerName = exposerName;
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

    public String getExposerName() {
        return exposerName;
    }
}
