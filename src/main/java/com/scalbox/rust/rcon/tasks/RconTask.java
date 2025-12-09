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

package com.scalbox.rust.rcon.tasks;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class RconTask implements Runnable {
    @Override
    public void run() {
        log.debug("Executing task: {}", name());

        try {
            execute();
        } catch (Exception e) {
            log.error(String.format("Task '%s' failed: %s", name(), e.getMessage()), e);
        }
    }

    protected String name() {
        return this.getClass().getSimpleName();
    }

    protected abstract void execute();
}
