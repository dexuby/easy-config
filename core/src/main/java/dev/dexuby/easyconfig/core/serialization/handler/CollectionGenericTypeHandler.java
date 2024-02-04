package dev.dexuby.easyconfig.core.serialization.handler;

import com.typesafe.config.*;
import dev.dexuby.easycommon.external.jetbrains.annotations.NotNull;
import dev.dexuby.easyconfig.core.ConfigurationResolver;
import dev.dexuby.easyconfig.core.common.Holder;
import dev.dexuby.easyconfig.core.serialization.ConfigurationSerializable;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Generic type handler for collection types.
 */

public final class CollectionGenericTypeHandler extends GenericTypeHandler {

    public CollectionGenericTypeHandler(@NotNull final Map<Class<?>, ConfigurationSerializable<?>> registeredSerializers,
                                        @NotNull final Field field,
                                        @NotNull final Class<?> fieldType,
                                        @NotNull final Class<?> owner) {

        super(registeredSerializers, field, fieldType, owner);

    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void readAndSet(@NotNull final Config config, @NotNull final String path) throws ReflectiveOperationException {

        Collection collection = null;
        ParameterizedType parameterizedType;
        if (super.field.getType().isAssignableFrom(Holder.class)) {
            parameterizedType = (ParameterizedType) ((ParameterizedType) super.field.getGenericType()).getActualTypeArguments()[0];
            final Holder holder = (Holder) super.field.get(super.owner);
            if (holder.getDefaultValue() != null)
                collection = (Collection) holder.getDefaultValue();
        } else {
            parameterizedType = (ParameterizedType) super.field.getGenericType();
            final Object value = super.field.get(super.owner);
            if (value != null)
                collection = (Collection) value;
        }

        if (collection == null) {
            // No default collection implementation instance found, creating new one.
            collection = (Collection) super.fieldType.getConstructor().newInstance();
        } else {
            // Re-use default collection implementation instance.
            collection.clear();
        }

        final Class<?> actualType = (Class<?>) parameterizedType.getActualTypeArguments()[0];

        final ConfigList configList = config.getList(path);
        for (final ConfigValue configValue : configList) {
            if (this.registeredSerializers.containsKey(actualType)) {
                final ConfigurationSerializable<?> serializer = this.registeredSerializers.get(actualType);
                collection.add(actualType.cast(serializer.deserialize(configValue)));
            } else {
                collection.add(actualType.cast(configValue.unwrapped()));
            }
        }

        ConfigurationResolver.setConfigurationFieldValue(super.field, super.fieldType, collection);

    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public ConfigValue toConfigValue(@NotNull final ConfigOrigin origin) throws IllegalAccessException {

        final List<?> fieldValue = (List<?>) super.getFieldValue();
        ParameterizedType parameterizedType;
        if (super.field.getType().isAssignableFrom(Holder.class)) {
            parameterizedType = (ParameterizedType) ((ParameterizedType) super.field.getGenericType()).getActualTypeArguments()[0];
        } else {
            parameterizedType = (ParameterizedType) super.field.getGenericType();
        }
        final Class<?> actualType = (Class<?>) parameterizedType.getActualTypeArguments()[0];

        final List<ConfigValue> configObjects = new ArrayList<>();
        for (final Object obj : fieldValue) {
            if (this.registeredSerializers.containsKey(actualType)) {
                final ConfigurationSerializable serializer = this.registeredSerializers.get(actualType);
                configObjects.add(serializer.serialize(obj));
            } else {
                configObjects.add(ConfigValueFactory.fromAnyRef(obj));
            }
        }

        return ConfigValueFactory.fromIterable(configObjects).withOrigin(origin);

    }

}
