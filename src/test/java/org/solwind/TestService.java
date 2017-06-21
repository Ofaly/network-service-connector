package org.solwind;

/**
 * Created by solwind on 6/14/17.
 */
@Discoverable
public class TestService implements ITestService {

    public String echoText() {
        return "ok";
    }

    public Integer echoInt() {
        return 123;
    }

    public CustomDto customDto() {
        return new CustomDto("testOk", 123456);
    }

    public String args(String test) {
        return String.format("response for arg %s", test);
    }
}
