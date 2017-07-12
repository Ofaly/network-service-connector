package io.solwind.handler;

import java.io.Serializable;

/**
 * Created by theso on 6/24/2017.
 */
public class RegistrationServiceHolder implements Serializable {

    private static final long serialVersionUID = 1999L;

    private String host;
    private String version;
    private String shortDescription;
    private String exposerName;

    public RegistrationServiceHolder() {
    }

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

    @Override
    public String toString() {
        return "RegistrationServiceHolder{" +
                "host='" + host + '\'' +
                ", version='" + version + '\'' +
                ", shortDescription='" + shortDescription + '\'' +
                ", exposerName='" + exposerName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RegistrationServiceHolder holder = (RegistrationServiceHolder) o;

        if (host != null ? !host.equals(holder.host) : holder.host != null) return false;
        if (version != null ? !version.equals(holder.version) : holder.version != null) return false;
        if (shortDescription != null ? !shortDescription.equals(holder.shortDescription) : holder.shortDescription != null)
            return false;
        return exposerName != null ? exposerName.equals(holder.exposerName) : holder.exposerName == null;
    }

    @Override
    public int hashCode() {
        int result = host != null ? host.hashCode() : 0;
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (shortDescription != null ? shortDescription.hashCode() : 0);
        result = 31 * result + (exposerName != null ? exposerName.hashCode() : 0);
        return result;
    }
}
