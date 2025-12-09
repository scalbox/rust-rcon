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

package com.scalbox.rust.rcon.protocol.oxide;

import lombok.NonNull;

public interface OxidePermission {
    OxidePermissionTypes permissionType();

    String name();

    String permission();

    default OxidePermission user(@NonNull String name, @NonNull String permission) {
        return new OxidePermission() {
            @Override
            public OxidePermissionTypes permissionType() {
                return OxidePermissionTypes.USER;
            }

            @Override
            public String name() {
                return name;
            }

            @Override
            public String permission() {
                return permission;
            }
        };
    }
}
