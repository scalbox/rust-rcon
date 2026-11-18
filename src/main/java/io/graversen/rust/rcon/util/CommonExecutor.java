/*
 * This file is based on the open-source project "Rust RCON"
 * (https://github.com/MrGraversen/rust-rcon), created by
 * Martin Graversen and originally released under the MIT license.
 *
 * Original base package: io.graversen.rust.rcon
 *
 * The code in this fork
 * (https://github.com/scalbox/rust-rcon) has been adapted and is
 * maintained by scalbox and is distributed under the terms of the
 * Apache License, Version 2.0. For more details, see the LICENSE
 * file included in this repository.
 */

package io.graversen.rust.rcon.util;

import lombok.NonNull;
import lombok.Synchronized;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Holder of the scheduled executor shared by all the clients in the JVM, used for reconnection attempts and for
 * the callbacks of completed RCON requests.
 */
@Slf4j
@UtilityClass
public class CommonExecutor {
    private static final String GROUP_NAME = "rust";
    private static ScheduledExecutorService instance;

    /**
     * Returns the shared executor, creating it on first use. The executor has one daemon thread per available
     * processor, named {@code rust-worker-N}, so it never keeps the JVM alive on its own.
     *
     * @return the shared scheduled executor
     */
    @Synchronized
    public static ScheduledExecutorService getInstance() {
        if (instance == null) {
            final var processors = CommonUtils.processors();
            log.debug("Initialising common ExecutorService instance with {} workers", processors);
            instance = Executors.newScheduledThreadPool(processors, namedThreadFactory(new AtomicInteger()));
        }

        return instance;
    }

    /**
     * Creates a factory of named daemon threads.
     *
     * @param indexer counter used to number the threads
     * @return the thread factory
     */
    private static ThreadFactory namedThreadFactory(@NonNull AtomicInteger indexer) {
        return runnable -> {
            final var thread = new Thread(runnable, getName(indexer));
            thread.setDaemon(true);
            return thread;
        };
    }

    /**
     * Returns the name of the next worker thread.
     *
     * @param indexer counter used to number the threads
     * @return a name in the form {@code rust-worker-N}
     */
    private static String getName(@NonNull AtomicInteger indexer) {
        return String.format("%s-worker-%d", GROUP_NAME, indexer.getAndIncrement());
    }
}
