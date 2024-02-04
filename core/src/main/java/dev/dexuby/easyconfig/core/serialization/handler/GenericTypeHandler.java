package dev.dexuby.easyconfig.core.serialization.handler;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigOrigin;
import com.typesafe.config.ConfigValue;
import dev.dexuby.easycommon.external.jetbrains.annotations.NotNull;
import dev.dexuby.easycommon.external.jetbrains.annotations.Nullable;
import dev.dexuby.easyconfig.core.common.Holder;
import dev.dexuby.easyconfig.core.serialization.ConfigurationSerializable;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Used to read and write complex configuration values to a configuration field. The parent is currently unused but if
 * we plan to add nested collection support in the future it will be necessary together with some major writer changes.
 * With those changes writers would probably return whatever they create in the process (like a map) so it can be used
 * by its parent to resolved nested complex values.
 */

public abstract class GenericTypeHandler {

    protected final Map<Class<?>, ConfigurationSerializable<?>> registeredSerializers;
    protected final Field field;
    protected final Class<?> fieldType;
    protected final Class<?> owner;
    protected final GenericTypeHandler parent;

    public GenericTypeHandler(@NotNull final Map<Class<?>, ConfigurationSerializable<?>> registeredSerializers,
                              @NotNull final Field field,
                              @NotNull final Class<?> fieldType,
                              @NotNull final Class<?> owner) {

        this.registeredSerializers = registeredSerializers;
        this.field = field;
        this.fieldType = fieldType;
        this.owner = owner;
        this.parent = null;

    }

    public GenericTypeHandler(@NotNull final Map<Class<?>, ConfigurationSerializable<?>> registeredSerializers,
                              @NotNull final Field field,
                              @NotNull final Class<?> fieldType,
                              @NotNull final Class<?> owner,
                              @Nullable final GenericTypeHandler parent) {

        this.registeredSerializers = registeredSerializers;
        this.field = field;
        this.fieldType = fieldType;
        this.owner = owner;
        this.parent = parent;

    }

    public Map<Class<?>, ConfigurationSerializable<?>> getRegisteredSerializers() {

        return this.registeredSerializers;

    }

    public Field getField() {

        return this.field;

    }

    public Class<?> getFieldType() {

        return this.fieldType;

    }

    public Class<?> getOwner() {

        return this.owner;

    }

    public GenericTypeHandler getParent() {

        return this.parent;

    }

    public abstract void readAndSet(@NotNull final Config config, @NotNull final String path) throws ReflectiveOperationException;

    public abstract ConfigValue toConfigValue(@NotNull final ConfigOrigin origin) throws IllegalAccessException;

    protected Object getFieldValue() throws IllegalAccessException {

        Object fieldValue = this.field.get(this.owner);
        if (fieldValue instanceof Holder)
            fieldValue = ((Holder<?>) fieldValue).get();

        return fieldValue;

    }

}
