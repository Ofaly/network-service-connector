package io.solwind.handler;

import io.solwind.exception.ClassNotFoundRuntimeException;

import java.util.Objects;

/**
 * Created by theso on 6/24/2017.
 */
public class RegistrationServiceHolder {
    private String host;
    private String version;
    private String shortDescription;
    private Class interfaceClass;

    public RegistrationServiceHolder(String host, String version, String shortDescription, Class interfaceClass) {
        this.host = host;
        this.version = version;
        this.shortDescription = shortDescription;
        this.interfaceClass = interfaceClass;
    }

    public RegistrationServiceHolder(byte[] data) {
        Objects.nonNull(data);
        String[] splittedString = new String(data).split(",");
        this.host = splittedString[0];
        try {
            this.interfaceClass = Class.forName(splittedString[1].trim());
        } catch (ClassNotFoundException e) {
            throw new ClassNotFoundRuntimeException(e);
        }
        this.version = splittedString.length > 2?splittedString[2]:null;
        this.shortDescription = splittedString.length >= 4?splittedString[3]:null;
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

    public Class getInterfaceClass() {
        return interfaceClass;
    }

    @Override
    public String toString() {
        return String.format("%s, %s, %s, %s", host, interfaceClass.getCanonicalName(), version, shortDescription);
    }
}
