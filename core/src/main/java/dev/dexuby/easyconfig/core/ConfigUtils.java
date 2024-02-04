package dev.dexuby.easyconfig.core;

import com.typesafe.config.Config;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public final class ConfigUtils {

    /**
     * Returns the config value from the specified path if present, if not it returns <code>null</code>.
     *
     * @param config The config.
     * @param path   The path.
     * @param method The read method.
     * @param <T>    The return type.
     * @return The config value or <code>null</code> if not present.
     */

    @Nullable
    public static <T> T getOrNull(@NotNull final Config config, @NotNull final String path, @NotNull final Function<String, T> method) {

        if (config.hasPath(path))
            return method.apply(path);
        return null;

    }

    /**
     * Returns the config value from the specified path if present, if not it returns the provided default value.
     *
     * @param config       The config.
     * @param path         The path.
     * @param method       The read method.
     * @param defaultValue The default value.
     * @param <T>          The return type.
     * @return The config value or the default value if not present.
     */

    @NotNull
    public static <T> T getOrDefault(@NotNull final Config config, @NotNull final String path, @NotNull final Function<String, T> method, @NotNull final T defaultValue) {

        if (config.hasPath(path))
            return method.apply(path);
        return defaultValue;

    }

    /**
     * Returns the config value from the specified path if present, if not it throws a {@link NullPointerException}.
     *
     * @param config The config.
     * @param path   The path.
     * @param method The read method.
     * @param <T>    The return type.
     * @return The config value.
     * @throws NullPointerException If the path has no config value.
     */

    @NotNull
    public static <T> T getOrThrow(@NotNull final Config config, @NotNull final String path, @NotNull final Function<String, T> method) throws NullPointerException {

        if (config.hasPath(path))
            return method.apply(path);
        throw new NullPointerException("No config value at path '" + path + "'");

    }

}
