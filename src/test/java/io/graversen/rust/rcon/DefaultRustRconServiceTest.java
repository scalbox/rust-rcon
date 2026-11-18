/*
 * Copyright (c) scalbox.
 *
 * Distributed under the terms of the Apache License, Version 2.0.
 * For more details, see the LICENSE file included in this repository.
 */

package io.graversen.rust.rcon;

import io.graversen.rust.rcon.tasks.RconTask;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultRustRconServiceTest {
    private static final int INTERNAL_TASKS = 3;

    @Test
    void stopCancelsTheTasksOnASharedSchedulerAndLeavesItRunning() throws Exception {
        final var scheduler = new ScheduledThreadPoolExecutor(1);
        scheduler.setRemoveOnCancelPolicy(true);
        try {
            final var service = new DefaultRustRconService(offlineConfiguration(), scheduler);
            service.start();
            assertEquals(INTERNAL_TASKS, scheduler.getQueue().size());

            assertTimeoutPreemptively(Duration.ofSeconds(10), service::stop);

            assertEquals(0, scheduler.getQueue().size());
            assertFalse(scheduler.isShutdown());
        } finally {
            scheduler.shutdownNow();
        }
    }

    @Test
    void stopTerminatesTheSchedulerThreadCreatedByTheService() throws Exception {
        final var configuration = offlineConfiguration();
        final var threadName = String.format("rust-rcon-scheduler-%s:%d", configuration.getHostname(), configuration.getPort());
        final var service = new DefaultRustRconService(configuration);
        service.schedule(new NoOpTask(), Duration.ofSeconds(1), null);

        final var schedulerThread = findThread(threadName);
        assertNotNull(schedulerThread, "scheduler thread not found");
        assertTrue(schedulerThread.isDaemon());

        service.stop();
        schedulerThread.join(5000);

        assertFalse(schedulerThread.isAlive());
    }

    @Test
    void stopReturnsPromptlyWhenTheServerIsUnreachable() throws Exception {
        final var service = new DefaultRustRconService(offlineConfiguration());
        service.start();

        assertTimeoutPreemptively(Duration.ofSeconds(10), service::stop);
    }

    @Test
    void stopIsAllowedWithoutStartAndMoreThanOnce() throws Exception {
        final var service = new DefaultRustRconService(offlineConfiguration());

        service.stop();
        service.stop();
    }

    @Test
    void stoppedServiceRejectsNewTasks() throws Exception {
        final var service = new DefaultRustRconService(offlineConfiguration());
        service.stop();

        assertThrows(IllegalStateException.class, () -> service.schedule(new NoOpTask(), Duration.ofSeconds(1), null));
    }

    private static RustRconConfiguration offlineConfiguration() throws Exception {
        return new RustRconConfiguration("127.0.0.1", FakeRconServer.freePort(), "secret");
    }

    private static Thread findThread(String name) {
        return Thread.getAllStackTraces().keySet().stream()
                .filter(thread -> thread.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    private static final class NoOpTask extends RconTask {
        @Override
        protected void execute() {
        }
    }
}
