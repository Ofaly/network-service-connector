package io.solwind.api;

/**
 * Created by theso on 7/6/2017.
 */
@FunctionalInterface
public interface TokenSecurityHandler<T> {
    T handle(String token);
}
