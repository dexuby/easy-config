package dev.dexuby.easyconfig.core.common;

import dev.dexuby.easycommon.conditional.Preconditions;
import dev.dexuby.easycommon.external.jetbrains.annotations.NotNull;
import dev.dexuby.easycommon.external.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Holds a value.
 *
 * @param <T> The type.
 */

public class Holder<T> {

    private final T defaultValue;
    private T value = null;

    /**
     * Zero-args constructor, default value will be <code>null</code>.
     */

    public Holder() {

        this.defaultValue = null;

    }

    /**
     * Constructor that takes a nullable default value.
     *
     * @param defaultValue The default value.
     */

    public Holder(@Nullable final T defaultValue) {

        this.defaultValue = defaultValue;

    }

    /**
     * Sets the internal value that will get prioritised over the default value.
     *
     * @param value The value.
     */

    public void set(@Nullable final T value) {

        this.value = value;

    }

    /**
     * Returns the default value if present.
     *
     * @return The default value or <code>null</code> if it is not present.
     */

    @Nullable
    public T getDefaultValue() {

        return this.defaultValue;

    }

    /**
     * Returns the stored value or the default value if no value has been set. If no value is stored and the default
     * value is <code>null</code> this method will throw an exception.
     *
     * @return The value prioritising the set value.
     * @throws NullPointerException If both values were <code>null</code>.
     */

    @NotNull
    public T get() throws NullPointerException {

        if (this.value == null) {
            Preconditions.checkNotNull(defaultValue);
            return this.defaultValue;
        } else {
            return this.value;
        }

    }

    /**
     * Static factory method to create a new instance based on the provided default value.
     *
     * @param defaultValue The default value.
     * @param <T>          The type.
     * @return The created instance.
     */

    public static <T> Holder<T> of(@NotNull final T defaultValue) {

        return new Holder<>(defaultValue);

    }

    /**
     * Static factory method to create a new instance with no default value.
     *
     * @param <T> The type.
     * @return The created instance.
     */

    public static <T> Holder<T> empty() {

        return new Holder<>();

    }

    @Override
    public boolean equals(final Object obj) {

        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        final Holder<?> input = (Holder<?>) obj;
        return Objects.equals(defaultValue, input.defaultValue) && Objects.equals(value, input.value);

    }

    @Override
    public int hashCode() {

        return Objects.hash(defaultValue, value);

    }

}
