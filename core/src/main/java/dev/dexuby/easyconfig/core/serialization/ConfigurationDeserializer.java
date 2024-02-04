package dev.dexuby.easyconfig.core.serialization;

import com.typesafe.config.Config;
import dev.dexuby.easycommon.external.jetbrains.annotations.NotNull;
import dev.dexuby.easycommon.external.jetbrains.annotations.Nullable;
import dev.dexuby.easycommon.serialization.Deserializer;

public interface ConfigurationDeserializer<T> extends Deserializer<T, Config> {

    @Nullable
    @Override
    T deserialize(@NotNull final Config config);

}
