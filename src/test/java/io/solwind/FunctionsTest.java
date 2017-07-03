package io.solwind;

import org.junit.Before;
import org.junit.Test;

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


}