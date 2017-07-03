package io.solwind.handler;

import io.solwind.ITestService;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by theso on 7/1/2017.
 */
public class RegistrationServiceHolderTest {

    @Test
    public void toStringTestFullString() throws Exception {
        RegistrationServiceHolder registrationServiceHolder = new RegistrationServiceHolder("host:port, io.solwind.ITestService,version,description".getBytes());
        assertEquals("host:port, io.solwind.ITestService, version, description", registrationServiceHolder.toString());
        assertEquals("host:port", registrationServiceHolder.getHost());
        assertEquals("version", registrationServiceHolder.getVersion());
        assertEquals("description", registrationServiceHolder.getShortDescription());
        assertEquals(ITestService.class, registrationServiceHolder.getInterfaceClass());
    }

    @Test
    public void toStringTestHostOnly() throws Exception {
        RegistrationServiceHolder registrationServiceHolder = new RegistrationServiceHolder("host:port, io.solwind.ITestService".getBytes());
        assertEquals("host:port, io.solwind.ITestService, null, null", registrationServiceHolder.toString());
        assertEquals("host:port", registrationServiceHolder.getHost());
        assertEquals(ITestService.class, registrationServiceHolder.getInterfaceClass());
        assertNull(registrationServiceHolder.getVersion());
        assertNull(registrationServiceHolder.getShortDescription());
    }

    @Test(expected = RuntimeException.class)
    public void createWithNotexistClass() throws Exception {
        new RegistrationServiceHolder("host:port, io.solwind.ITestService1".getBytes());
    }

}