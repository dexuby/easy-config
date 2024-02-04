package dev.dexuby.easyconfig.core.serialization.impl;

import com.typesafe.config.*;
import dev.dexuby.easycommon.external.jetbrains.annotations.NotNull;
import dev.dexuby.easycommon.external.jetbrains.annotations.Nullable;
import dev.dexuby.easyconfig.core.serialization.ConfigurationSerializable;

import java.time.Instant;

/**
 * Singleton {@link Instant} (de)serializer.
 */

public final class InstantSerializer implements ConfigurationSerializable<Instant> {

    private static class InstantSerializerSingleton {

        private static final InstantSerializer INSTANCE = new InstantSerializer();

    }

    private InstantSerializer() {

    }

    @Nullable
    @Override
    public ConfigValue serialize(@NotNull final Instant input) {

        return ConfigValueFactory.fromAnyRef(input.toEpochMilli());

    }

    @Nullable
    @Override
    public Instant deserialize(@NotNull final ConfigValue configValue) {

        return Instant.ofEpochMilli((long) configValue.unwrapped());

    }

    public static InstantSerializer getInstance() {

        return InstantSerializerSingleton.INSTANCE;

    }

}
