package dev.dexuby.easyconfig.core.serialization.impl;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;
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
    public Config serialize(@NotNull final Instant input) {

        Config config = ConfigFactory.empty();
        config = config.withValue("timestamp", ConfigValueFactory.fromAnyRef(input.toEpochMilli()));

        return config;

    }

    @Nullable
    @Override
    public Instant deserialize(@NotNull final Config config) {

        return config.hasPath("timestamp") ? Instant.ofEpochMilli(config.getLong("timestamp")) : null;

    }

    public static InstantSerializer getInstance() {

        return InstantSerializerSingleton.INSTANCE;

    }

}
