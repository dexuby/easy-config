package dev.dexuby.easyconfig.core.serialization.handler;

import com.typesafe.config.*;
import dev.dexuby.easycommon.external.jetbrains.annotations.NotNull;
import dev.dexuby.easyconfig.core.ConfigurationResolver;
import dev.dexuby.easyconfig.core.common.Holder;
import dev.dexuby.easyconfig.core.serialization.ConfigurationSerializable;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Generic type handler for map types.
 */

public final class MapGenericTypeHandler extends GenericTypeHandler {

    public MapGenericTypeHandler(@NotNull final Map<Class<?>, ConfigurationSerializable<?>> registeredSerializers,
                                 @NotNull final Field field,
                                 @NotNull final Class<?> fieldType,
                                 @NotNull final Class<?> owner) {

        super(registeredSerializers, field, fieldType, owner);

    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void readAndSet(@NotNull final Config config, @NotNull final String path) throws ReflectiveOperationException {

        Map map = null;
        ParameterizedType parameterizedType;
        if (super.field.getType().isAssignableFrom(Holder.class)) {
            parameterizedType = (ParameterizedType) ((ParameterizedType) super.field.getGenericType()).getActualTypeArguments()[0];
            final Holder holder = (Holder) super.field.get(super.owner);
            if (holder.getDefaultValue() != null)
                map = (Map) holder.getDefaultValue();
        } else {
            parameterizedType = (ParameterizedType) super.field.getGenericType();
            final Object value = super.field.get(super.owner);
            if (value != null)
                map = (Map) value;
        }

        if (map == null) {
            // No default map implementation instance found, creating new one.
            map = (Map) super.fieldType.getConstructor().newInstance();
        } else {
            // Re-use default map implementation instance.
            map.clear();
        }

        final Class<?> keyType = (Class<?>) parameterizedType.getActualTypeArguments()[0];
        final Class<?> valueType = (Class<?>) parameterizedType.getActualTypeArguments()[1];

        final ConfigList configList = config.getList(path);
        for (final ConfigValue configValue : configList) {
            final ConfigObject configObject = (ConfigObject) configValue;
            final ConfigValue keyConfigValue = configObject.get("key");
            final ConfigValue valueConfigValue = configObject.get("value");

            Object key;
            if (this.registeredSerializers.containsKey(keyType)) {
                final ConfigurationSerializable<?> serializer = this.registeredSerializers.get(keyType);
                key = keyType.cast(serializer.deserialize(keyConfigValue));
            } else {
                key = keyType.cast(keyConfigValue.unwrapped());
            }

            Object value;
            if (this.registeredSerializers.containsKey(valueType)) {
                final ConfigurationSerializable<?> serializer = this.registeredSerializers.get(valueType);
                value = valueType.cast(serializer.deserialize(valueConfigValue));
            } else {
                value = valueType.cast(valueConfigValue.unwrapped());
            }

            map.put(key, value);
        }

        ConfigurationResolver.setConfigurationFieldValue(super.field, super.fieldType, map);

    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public ConfigValue toConfigValue(@NotNull final ConfigOrigin origin) throws IllegalAccessException {

        final Map<?, ?> fieldValue = (Map<?, ?>) super.getFieldValue();
        ParameterizedType parameterizedType;
        if (super.field.getType().isAssignableFrom(Holder.class)) {
            parameterizedType = (ParameterizedType) ((ParameterizedType) super.field.getGenericType()).getActualTypeArguments()[0];
        } else {
            parameterizedType = (ParameterizedType) super.field.getGenericType();
        }
        final Class<?> keyType = (Class<?>) parameterizedType.getActualTypeArguments()[0];
        final Class<?> valueType = (Class<?>) parameterizedType.getActualTypeArguments()[1];

        final List<ConfigValue> configObjects = new ArrayList<>();
        for (final Map.Entry<?, ?> entry : fieldValue.entrySet()) {
            Config config = ConfigFactory.empty();
            if (this.registeredSerializers.containsKey(keyType)) {
                final ConfigurationSerializable serializer = this.registeredSerializers.get(keyType);
                config = config.withValue("key", serializer.serialize(entry.getKey()));
            } else {
                config = config.withValue("key", ConfigValueFactory.fromAnyRef(entry.getKey()));
            }
            if (this.registeredSerializers.containsKey(valueType)) {
                final ConfigurationSerializable serializer = this.registeredSerializers.get(valueType);
                config = config.withValue("value", serializer.serialize(entry.getValue()));
            } else {
                config = config.withValue("value", ConfigValueFactory.fromAnyRef(entry.getValue()));
            }
            configObjects.add(ConfigValueFactory.fromMap(config.root().unwrapped()));
        }

        return ConfigValueFactory.fromIterable(configObjects).withOrigin(origin);

    }

}
