/*
 * Copyright (c) scalbox.
 *
 * Distributed under the terms of the Apache License, Version 2.0.
 * For more details, see the LICENSE file included in this repository.
 */

package io.graversen.rust.rcon.util;

import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommonExecutorTest {
    @Test
    void workerThreadsAreNamedDaemonThreads() throws Exception {
        final var thread = CommonExecutor.getInstance().submit(Thread::currentThread).get(5, TimeUnit.SECONDS);

        assertTrue(thread.isDaemon());
        assertTrue(thread.getName().startsWith("rust-worker-"), thread.getName());
    }

    @Test
    void returnsTheSameInstance() {
        assertSame(CommonExecutor.getInstance(), CommonExecutor.getInstance());
    }
}
