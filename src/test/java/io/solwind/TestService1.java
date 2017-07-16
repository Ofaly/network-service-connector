package io.solwind;

import io.solwind.api.Discoverable;

import java.util.ArrayList;
import java.util.List;

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

    @Override
    public List<CustomDto> list() {
        List<CustomDto> customDtos = new ArrayList<>();
        customDtos.add(new CustomDto("test1", 1));
        customDtos.add(new CustomDto("test2", 2));
        customDtos.add(new CustomDto("test3", 3));
        customDtos.add(new CustomDto("test4", 4));
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


}
