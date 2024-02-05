package dev.dexuby.easyconfig.core.test.resolvertest;

import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueFactory;
import dev.dexuby.easycommon.external.jetbrains.annotations.NotNull;
import dev.dexuby.easycommon.external.jetbrains.annotations.Nullable;
import dev.dexuby.easyconfig.core.ConfigurationSerializer;
import dev.dexuby.easyconfig.core.serialization.ConfigurationSerializable;

@ConfigurationSerializer
public final class SingletonFooSerializer implements ConfigurationSerializable<Foo> {

    private static class SingletonFooSerializerSingleton {

        private static final SingletonFooSerializer INSTANCE = new SingletonFooSerializer();

    }

    private SingletonFooSerializer() {

    }

    @Nullable
    @Override
    public ConfigValue serialize(@NotNull final Foo input) {

        return ConfigValueFactory.fromAnyRef(input.getFoo());

    }

    @Nullable
    @Override
    public Foo deserialize(@NotNull final ConfigValue configValue) {

        return new Foo((String) configValue.unwrapped());

    }

    public static SingletonFooSerializer getInstance() {

        return SingletonFooSerializerSingleton.INSTANCE;

    }

}
