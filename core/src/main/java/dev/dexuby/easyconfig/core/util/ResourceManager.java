package dev.dexuby.easyconfig.core.util;

import dev.dexuby.easycommon.external.jetbrains.annotations.NotNull;
import dev.dexuby.easyconfig.core.Constants;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ResourceManager {

    /**
     * Saves a contained resource to the specified target path if not existing.
     *
     * @param classLoader The class loader.
     * @param fileName    The name of the resource.
     * @param targetPath  The target path.
     * @return <code>true</code> if the resource got saved, <code>false</code> otherwise.
     */

    public static boolean saveResourceFile(@NotNull final ClassLoader classLoader, @NotNull final String fileName, @NotNull final Path targetPath, final boolean override) {

        if (targetPath.toFile().exists() && !override) return false;
        try (final InputStream inputStream = classLoader.getResourceAsStream(fileName)) {
            if (inputStream == null)
                throw new RuntimeException("Failed to load " + fileName + " resource.");
            Files.copy(
                    inputStream,
                    targetPath
            );
            return true;
        } catch (final Exception ex) {
            Constants.LOGGER.warn("Failed to save resource file " + fileName, ex);
            return false;
        }

    }

    /**
     * Checks if the provided resource name exists as a resource in the provided class loader.
     *
     * @param classLoader The class loader.
     * @param fileName    The name of the resource.
     * @return <code>true</code> if the resource has been found, <code>false</code> otherwise.
     */

    public static boolean hasResourceFile(@NotNull final ClassLoader classLoader, @NotNull final String fileName) {

        return classLoader.getResource(fileName) != null;

    }

}
