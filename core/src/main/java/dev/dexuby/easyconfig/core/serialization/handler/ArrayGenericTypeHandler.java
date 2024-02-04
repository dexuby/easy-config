package dev.dexuby.easyconfig.core.serialization.handler;

import com.typesafe.config.*;
import dev.dexuby.easycommon.external.jetbrains.annotations.NotNull;
import dev.dexuby.easyconfig.core.ConfigurationResolver;
import dev.dexuby.easyconfig.core.serialization.ConfigurationSerializable;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Generic type handler for arrays.
 */

public final class ArrayGenericTypeHandler extends GenericTypeHandler {

    public ArrayGenericTypeHandler(@NotNull final Map<Class<?>, ConfigurationSerializable<?>> registeredSerializers,
                                   @NotNull final Field field,
                                   @NotNull final Class<?> fieldType,
                                   @NotNull final Class<?> owner) {

        super(registeredSerializers, field, fieldType, owner);

    }

    @Override
    public void readAndSet(@NotNull final Config config, @NotNull final String path) throws ReflectiveOperationException {

        final Class<?> componentType = super.fieldType.getComponentType();
        final ConfigList configList = config.getList(path);
        final Object array = Array.newInstance(componentType, configList.size());
        for (int i = 0; i < configList.size(); i++) {
            final ConfigValue configValue = configList.get(i);
            if (this.registeredSerializers.containsKey(componentType)) {
                final ConfigurationSerializable<?> serializer = this.registeredSerializers.get(componentType);
                Array.set(array, i, componentType.cast(serializer.deserialize(((ConfigObject) configValue).toConfig())));
            } else {
                Array.set(array, i, componentType.cast(configValue.unwrapped()));
            }
        }

        ConfigurationResolver.setConfigurationFieldValue(super.field, super.fieldType, array);

    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public ConfigValue toConfigValue(@NotNull final ConfigOrigin origin) throws IllegalAccessException {

        final Object[] fieldValue = (Object[]) super.getFieldValue();
        final Class<?> componentType = super.fieldType.getComponentType();
        final List<ConfigValue> configObjects = new ArrayList<>();
        for (final Object obj : fieldValue) {
            if (this.registeredSerializers.containsKey(componentType)) {
                final ConfigurationSerializable serializer = this.registeredSerializers.get(componentType);
                configObjects.add(ConfigValueFactory.fromMap(serializer.serialize(obj).root().unwrapped()));
            } else {
                configObjects.add(ConfigValueFactory.fromAnyRef(obj));
            }
        }

        return ConfigValueFactory.fromIterable(configObjects).withOrigin(origin);

    }

}
