package dev.dexuby.easyconfig.core.serialization;

import com.typesafe.config.ConfigValue;
import dev.dexuby.easycommon.external.jetbrains.annotations.NotNull;
import dev.dexuby.easycommon.external.jetbrains.annotations.Nullable;
import dev.dexuby.easycommon.serialization.Serializer;

public interface ConfigurationSerializer<T> extends Serializer<ConfigValue, T> {

    @Nullable
    @Override
    ConfigValue serialize(@NotNull final T input);

}
