package io.solwind;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Created by theso on 7/3/2017.
 */
public class Runner {

    private final Thread task;

    public Runner(Supplier supplier, Consumer consumer) {
        task = new Thread(() -> consumer.accept(supplier.get()));
    }

    public Runner call() {
        if (!task.isAlive()) {
            task.start();
            return this;
        } else {
            return this;
        }
    }

    public boolean isCompleted() {
        return !task.isAlive();
    }
}
