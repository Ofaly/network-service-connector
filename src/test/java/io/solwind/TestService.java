package io.solwind;

import io.solwind.api.Discoverable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by solwind on 6/14/17.
 */
@Discoverable
public class TestService implements ITestService {

    private String testVoidMethod = new String();

    public TestService() {
    }

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

    @Override
    public List<CustomDto> list() {
        List<CustomDto> customDtos = new ArrayList<>();
        for (int i = 0; i<=10000; i++)
        customDtos.add(new CustomDto("test1", 1));
        return customDtos;
    }

    @Override
    public String stringWithTimeout() throws InterruptedException {
        return "test";
    }

    @Override
    public String listParams(String str, Integer i, List list) {
        return String.format("%s %s %s", str, i, list);
    }

    @Override
    public String noSerializeDtoArgument(NoSerializableCustomDto noSerializableCustomDto) {
        return "empty";
    }

    @Override
    public void testVoidMethod() {
        testVoidMethod = "passed!!!";
    }

    @Override
    public void testVoidMethod(String name) {
        testVoidMethod = "passed " + name + "!!!";
    }

    @Override
    public String getTestVoidField() {
        return testVoidMethod;
    }


}
