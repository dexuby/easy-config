package dev.dexuby.easyconfig.core.serialization;

import com.typesafe.config.Config;
import dev.dexuby.easycommon.external.jetbrains.annotations.NotNull;
import dev.dexuby.easycommon.external.jetbrains.annotations.Nullable;
import dev.dexuby.easycommon.serialization.Serializer;

public interface ConfigurationSerializer<T> extends Serializer<Config, T> {

    @Nullable
    @Override
    Config serialize(@NotNull final T input);

}
