package io.solwind;

import io.solwind.handler.RegistrationServiceHolder;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.Assert.*;

/**
 * Created by theso on 7/3/2017.
 */
public class FunctionsTest {
    @Before
    public void setUp() throws Exception {
    }

    @Test(timeout = 500)
    public void asyncCall() {
        TestService testService = new TestService();
        String[] callBackResult = {null};
        Runner call = Functions.<String>asyncCall().apply(testService::echoText)
                .apply((String o) -> callBackResult[0] = o).call();
        while (!call.isCompleted());
        assertEquals("ok", callBackResult[0]);
    }

    @Test(timeout = 500)
    public void asyncDoubleCall() {
        TestService testService = new TestService();
        Integer[] callBackResult = {0};
        Runner call = Functions.<String>asyncCall().apply(testService::echoText)
                .apply((String o) -> callBackResult[0] = ++callBackResult[0]).call().call();
        while (!call.isCompleted());
        assertEquals(new Integer(1), callBackResult[0]);
    }

    @Test
    public void searchRshByNameTest() {
        Set<RegistrationServiceHolder> holders = new HashSet<>();
        holders.add(new RegistrationServiceHolder("host:port", "001", "test descr", "testExposer"));
        holders.add(new RegistrationServiceHolder("host:port", "001", "test descr", "testExposer1"));

        RegistrationServiceHolder testExposer = Functions.searchRshByName.apply(holders).apply("testExposer");
        assertNotNull(testExposer);

    }

    @Test
    public void searchRshByNameWithWrongNameTest() {
        Set<RegistrationServiceHolder> holders = new HashSet<>();
        holders.add(new RegistrationServiceHolder("host:port", "001", "test descr", "testExposer"));
        holders.add(new RegistrationServiceHolder("host:port", "001", "test descr", "testExposer1"));

        RegistrationServiceHolder testExposer = Functions.searchRshByName.apply(holders).apply("testExposer2");
        assertNull(testExposer);

    }


}