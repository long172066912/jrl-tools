package io.micrometer.core.instrument.internal;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

/**
* 重写micrometer（1.7.6）的runnable，为了获取任务本身
* @author JerryLong
*/
public class TimedRunnable implements Runnable {
    private final MeterRegistry registry;
    private final Timer executionTimer;
    private final Timer idleTimer;
    private final Runnable command;
    private final Timer.Sample idleSample;

    TimedRunnable(MeterRegistry registry, Timer executionTimer, Timer idleTimer, Runnable command) {
        this.registry = registry;
        this.executionTimer = executionTimer;
        this.idleTimer = idleTimer;
        this.command = command;
        this.idleSample = Timer.start(registry);
    }

    @Override
    public void run() {
        idleSample.stop(idleTimer);
        Timer.Sample executionSample = Timer.start(registry);
        try {
            command.run();
        } finally {
            executionSample.stop(executionTimer);
        }
    }

    public Runnable getCommand() {
        return command;
    }
}