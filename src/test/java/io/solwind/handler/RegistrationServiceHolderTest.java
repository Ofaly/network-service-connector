package io.solwind.handler;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by theso on 7/1/2017.
 */
public class RegistrationServiceHolderTest {

    @Test
    public void toStringTestFullString() throws Exception {
        RegistrationServiceHolder registrationServiceHolder = new RegistrationServiceHolder("host:port,version,description".getBytes());
        assertEquals("host:port, version, description", registrationServiceHolder.toString());
        assertEquals("host:port", registrationServiceHolder.getHost());
        assertEquals("version", registrationServiceHolder.getVersion());
        assertEquals("description", registrationServiceHolder.getShortDescription());
    }

    @Test
    public void toStringTestHostOnly() throws Exception {
        RegistrationServiceHolder registrationServiceHolder = new RegistrationServiceHolder("host:port".getBytes());
        assertEquals("host:port, null, null", registrationServiceHolder.toString());
        assertEquals("host:port", registrationServiceHolder.getHost());
        assertNull(registrationServiceHolder.getVersion());
        assertNull(registrationServiceHolder.getShortDescription());
    }

}