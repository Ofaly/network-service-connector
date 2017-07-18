package io.solwind;

import java.util.List;

/**
 * Created by theso on 6/18/2017.
 */
public interface ITestService {
    String echoText();
    Integer echoInt();
    CustomDto customDto();
    String args(String test);
    List<CustomDto> list();
    String stringWithTimeout() throws InterruptedException;
    String listParams(String str, Integer i, List list);
    String noSerializeDtoArgument(NoSerializableCustomDto noSerializableCustomDto);
    void testVoidMethod();
    void testVoidMethod(String name);
    String getTestVoidField();
}
