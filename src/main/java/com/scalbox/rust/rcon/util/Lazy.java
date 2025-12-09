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

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Synchronized;

import java.util.function.Supplier;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class Lazy<T> implements Supplier<T> {
    private final @NonNull Supplier<T> supplier;
    private T value;

    public static <T> Lazy<T> of(@NonNull Supplier<T> supplier) {
        return new Lazy<>(supplier);
    }

    @Override
    @Synchronized
    public T get() {
        if (value == null) {
            value = supplier.get();
        }

        return value;
    }
}
