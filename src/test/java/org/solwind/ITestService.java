package org.solwind;

/**
 * Created by theso on 6/18/2017.
 */
public interface ITestService {
    String echoText();
    Integer echoInt();
    CustomDto customDto();
    String args(String test);
}
