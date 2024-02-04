package dev.dexuby.easyconfig.core.serialization.handler;

import dev.dexuby.easycommon.external.jetbrains.annotations.NotNull;
import dev.dexuby.easyconfig.core.serialization.ConfigurationSerializable;

import java.lang.reflect.Field;
import java.util.Map;

public interface GenericTypeHandlerFactory {

    /**
     * Lambda contract for {@link GenericTypeHandler} constructors.
     *
     * @param registeredSerializers The registered serializers.
     * @param field                 The target field.
     * @param fieldType             The actual field type.
     * @param owner                 The owning class.
     * @return The supplied {@link GenericTypeHandler} instance.
     */

    @NotNull
    GenericTypeHandler create(@NotNull final Map<Class<?>, ConfigurationSerializable<?>> registeredSerializers,
                              @NotNull final Field field,
                              @NotNull final Class<?> fieldType,
                              @NotNull final Class<?> owner);

}
