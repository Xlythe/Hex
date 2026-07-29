package com.xlythe.hex;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/** Process-scoped bounded executor for app-private disk and preference work. */
public final class AppExecutors {
    private static final AtomicInteger THREAD_NUMBER = new AtomicInteger();
    private static final ExecutorService IO = Executors.newFixedThreadPool(
            2,
            runnable -> {
                Thread thread = new Thread(
                        runnable, "Hex-io-" + THREAD_NUMBER.incrementAndGet());
                thread.setDaemon(true);
                return thread;
            });

    private AppExecutors() {}

    public static ExecutorService io() {
        return IO;
    }
}
