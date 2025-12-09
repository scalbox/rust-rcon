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

package com.scalbox.rust.rcon.util;

import lombok.NonNull;
import lombok.Synchronized;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@UtilityClass
public class CommonExecutor {
    private static final String GROUP_NAME = "rust";
    private static ScheduledExecutorService instance;

    @Synchronized
    public static ScheduledExecutorService getInstance() {
        if (instance == null) {
            final var processors = CommonUtils.processors();
            log.debug("Initialising common ExecutorService instance with {} workers", processors);
            instance = Executors.newScheduledThreadPool(processors, namedThreadFactory(new AtomicInteger()));
        }

        return instance;
    }

    private static ThreadFactory namedThreadFactory(@NonNull AtomicInteger indexer) {
        return runnable -> new Thread(runnable, getName(indexer));
    }

    private static String getName(@NonNull AtomicInteger indexer) {
        return String.format("%s-worker-%d", GROUP_NAME, indexer.getAndIncrement());
    }
}
