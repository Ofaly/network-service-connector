package io.solwind;

import io.solwind.api.Discoverable;

/**
 * Created by solwind on 6/14/17.
 */
@Discoverable
public class TestService1 implements ITestService {

    public String echoText() {
        return "ok1";
    }

    public Integer echoInt() {
        return 1231;
    }

    public CustomDto customDto() {
        return new CustomDto("testOk", 1234561);
    }

    public String args(String test) {
        return String.format("response for arg %s", test);
    }
}
