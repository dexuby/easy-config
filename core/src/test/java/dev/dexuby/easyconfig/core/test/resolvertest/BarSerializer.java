package dev.dexuby.easyconfig.core.test.resolvertest;

import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueFactory;
import dev.dexuby.easycommon.external.jetbrains.annotations.NotNull;
import dev.dexuby.easycommon.external.jetbrains.annotations.Nullable;
import dev.dexuby.easyconfig.core.ConfigurationSerializer;
import dev.dexuby.easyconfig.core.serialization.ConfigurationSerializable;

@ConfigurationSerializer
public class BarSerializer implements ConfigurationSerializable<Bar> {

    @Nullable
    @Override
    public ConfigValue serialize(@NotNull final Bar input) {

        return ConfigValueFactory.fromAnyRef(input.getBar());

    }

    @Nullable
    @Override
    public Bar deserialize(@NotNull final ConfigValue configValue) {

        return new Bar((String) configValue.unwrapped());

    }

}
