package dev.dexuby.easyconfig.core;

import com.typesafe.config.*;
import dev.dexuby.easycommon.external.jetbrains.annotations.NotNull;
import dev.dexuby.easycommon.external.jetbrains.annotations.Nullable;
import dev.dexuby.easycommon.tuple.Pair;
import dev.dexuby.easycommon.util.MapUtils;
import dev.dexuby.easyconfig.core.common.Holder;
import dev.dexuby.easyconfig.core.serialization.ConfigurationSerializable;
import dev.dexuby.easyconfig.core.serialization.handler.*;
import dev.dexuby.easyconfig.core.serialization.impl.InstantSerializer;
import dev.dexuby.easyconfig.core.util.ResourceManager;
import dev.dexuby.easyreflect.EasyReflect;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.*;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class ConfigurationResolver {

    // Default generic type handlers.
    private final Map<Predicate<Class<?>>, GenericTypeHandlerFactory> registeredGenericTypeHandlers = MapUtils.newMap(
            Pair.of(Class::isArray, ArrayGenericTypeHandler::new),
            Pair.of(Collection.class::isAssignableFrom, CollectionGenericTypeHandler::new),
            Pair.of(Map.class::isAssignableFrom, MapGenericTypeHandler::new)
    );

    // Default serializers.
    private final Map<Class<?>, ConfigurationSerializable<?>> registeredSerializers = MapUtils.newMap(
            Pair.of(Instant.class, InstantSerializer.getInstance())
    );

    private final EasyReflect easyReflect;

    public ConfigurationResolver(@NotNull final EasyReflect easyReflect) {

        this.easyReflect = easyReflect;

    }

    public ConfigurationResolver(@NotNull final EasyReflect easyReflect,
                                 @Nullable final Map<Class<?>, ConfigurationSerializable<?>> defaultSerializers) {

        this.easyReflect = easyReflect;
        if (defaultSerializers != null)
            this.registeredSerializers.putAll(defaultSerializers);

    }

    public ConfigurationResolver(@NotNull final EasyReflect easyReflect,
                                 @Nullable final Map<Predicate<Class<?>>, GenericTypeHandlerFactory> defaultGenericTypeHandlers,
                                 @Nullable final Map<Class<?>, ConfigurationSerializable<?>> defaultSerializers) {

        this.easyReflect = easyReflect;
        if (defaultGenericTypeHandlers != null)
            this.registeredGenericTypeHandlers.putAll(defaultGenericTypeHandlers);
        if (defaultSerializers != null)
            this.registeredSerializers.putAll(defaultSerializers);

    }

    /**
     * Automatically resolves and adds annotated serializers.
     */

    public void resolveSerializers() {

        final Map<Class<?>, ConfigurationSerializer> classes = this.easyReflect.findAnnotatedClasses(ConfigurationSerializer.class);
        for (final Map.Entry<Class<?>, ConfigurationSerializer> classEntry : classes.entrySet()) {
            final Class<?> type = classEntry.getKey();
            if (!(ConfigurationSerializable.class.isAssignableFrom(type)))
                continue;
            final Class<?> targetType = (Class<?>) ((ParameterizedType) type.getGenericInterfaces()[0]).getActualTypeArguments()[0];
            try {
                final Constructor<?> constructor = type.getConstructor();
                try {
                    // Create new instance and register.
                    final ConfigurationSerializable<?> instance = (ConfigurationSerializable<?>) constructor.newInstance();
                    this.registerSerializer(targetType, instance);
                } catch (final InvocationTargetException | InstantiationException | IllegalAccessException ex) {
                    Constants.LOGGER.error("Failed to create new instance of serializer class " + type.getName(), ex);
                }
            } catch (final NoSuchMethodException ex) {
                // No public zero-args constructor, check if it's a singleton.
                for (final Method method : type.getMethods()) {
                    if (!Modifier.isStatic(method.getModifiers()))
                        continue;
                    if (!method.getReturnType().equals(type))
                        continue;
                    try {
                        // Try to obtain the singleton instance from the static getter.
                        final ConfigurationSerializable<?> instance = (ConfigurationSerializable<?>) method.invoke(null);
                        this.registerSerializer(targetType, instance);
                        break;
                    } catch (final IllegalAccessException | InvocationTargetException innerException) {
                        Constants.LOGGER.error("Failed to obtain singleton instance of serializer class: " + type.getName(), innerException);
                    }
                }
            }
        }

    }

    /**
     * Automatically resolves and loads all configurations.
     */

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void resolveAndLoad() {

        final Map<Class<?>, Configuration> classes = this.easyReflect.findAnnotatedClasses(Configuration.class);
        for (final Map.Entry<Class<?>, Configuration> classEntry : classes.entrySet()) {
            final Class<?> type = classEntry.getKey();
            final File file = this.getFile(classEntry.getValue());
            if (!file.exists())
                this.saveDefaultConfiguration(file, classEntry.getValue());

            boolean saveFile = false;
            Config config = this.loadConfigFromFile(file);
            final Map<Field, ConfigurationValue> fields = this.easyReflect.findAnnotatedFields(type, ConfigurationValue.class);
            for (final Map.Entry<Field, ConfigurationValue> fieldEntry : fields.entrySet()) {
                try {
                    final Field field = fieldEntry.getKey();
                    final String path = fieldEntry.getValue().path();
                    final String description = fieldEntry.getValue().description();
                    Class<?> fieldType = field.getType();
                    Object fieldValue = field.get(type);
                    if (fieldType.isAssignableFrom(Holder.class)) {
                        // Override with the first generic type from the lockable value.
                        final ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
                        final Type actualType = parameterizedType.getActualTypeArguments()[0];
                        if (actualType instanceof ParameterizedType) {
                            fieldType = (Class<?>) ((ParameterizedType) actualType).getRawType();
                        } else {
                            fieldType = (Class<?>) actualType;
                        }
                        // Override field value with holder value.
                        fieldValue = ((Holder<?>) fieldValue).get();
                    }
                    if (config.hasPath(path)) {
                        // Complex objects like collections require generic type handlers.
                        boolean requiresFieldWriter = false;
                        for (final Map.Entry<Predicate<Class<?>>, GenericTypeHandlerFactory> entry : this.registeredGenericTypeHandlers.entrySet()) {
                            if (entry.getKey().test(fieldType)) {
                                requiresFieldWriter = true;
                                final GenericTypeHandler genericTypeHandler = entry.getValue().create(this.registeredSerializers, field, fieldType, type);
                                genericTypeHandler.readAndSet(config, path);
                                break;
                            }
                        }
                        if (requiresFieldWriter) continue;

                        if (this.registeredSerializers.containsKey(fieldType)) {
                            final ConfigurationSerializable<?> serializer = this.registeredSerializers.get(fieldType);
                            setConfigurationFieldValue(field, type, serializer.deserialize(config.getValue(path)));
                        } else {
                            setConfigurationFieldValue(field, type, config.getValue(path).unwrapped());
                        }
                    } else {
                        // Write default values.
                        // Complex objects like collections require generic type handlers.
                        boolean requiresFieldWriter = false;
                        for (final Map.Entry<Predicate<Class<?>>, GenericTypeHandlerFactory> entry : this.registeredGenericTypeHandlers.entrySet()) {
                            if (entry.getKey().test(fieldType)) {
                                requiresFieldWriter = true;
                                final GenericTypeHandler genericTypeHandler = entry.getValue().create(this.registeredSerializers, field, fieldType, type);
                                config = config.withValue(path, genericTypeHandler.toConfigValue(this.createOrigin(description)));
                                break;
                            }
                        }
                        if (requiresFieldWriter) continue;
                        if (this.registeredSerializers.containsKey(fieldType)) {
                            final ConfigurationSerializable serializer = this.registeredSerializers.get(fieldType);
                            config = config.withValue(path, serializer.serialize(fieldValue).withOrigin(this.createOrigin(description)));
                        } else {
                            config = config.withValue(path, ConfigValueFactory.fromAnyRef(fieldValue).withOrigin(this.createOrigin(description)));
                        }
                        saveFile = true;
                    }
                } catch (final ReflectiveOperationException ex) {
                    Constants.LOGGER.warn("Failed to load configuration value.", ex);
                }
            }
            if (saveFile)
                this.saveConfigToFile(config, file);
        }

    }

    /**
     * Creates a config origin with the provided description as a comment.
     *
     * @param description The description.
     * @return The created config origin.
     */

    private ConfigOrigin createOrigin(@NotNull final String description) {

        final ConfigOrigin origin = ConfigOriginFactory.newSimple();
        return origin.withComments(Collections.singletonList(description));

    }

    /**
     * Attempts to load and parse a config from the provided file.
     *
     * @param file The file.
     * @return The loaded config or <code>null</code> if the file didn't exist.
     */

    @Nullable
    public Config loadConfigFromFile(@NotNull final File file) {

        if (!file.exists()) return null;
        return ConfigFactory.parseFile(file);

    }

    /**
     * Writes the provided config to the provided file.
     *
     * @param config The config.
     * @param file   The file.
     */

    public void saveConfigToFile(@NotNull final Config config, @NotNull final File file) {

        final ConfigRenderOptions configRenderOptions = ConfigRenderOptions.defaults()
                .setOriginComments(false)
                .setComments(true)
                .setJson(false);
        final String content = config.root().render(configRenderOptions);
        try (final Writer writer = new FileWriter(file)) {
            writer.write(content);
            Constants.LOGGER.info("Updated configuration file: " + file.toPath().toAbsolutePath());
        } catch (final IOException ex) {
            Constants.LOGGER.error("Failed to update configuration file.", ex);
        }

    }

    /**
     * Saves the default configuration file resource of a configuration.
     *
     * @param file          The file.
     * @param configuration The configuration annotation.
     */

    public void saveDefaultConfiguration(@NotNull final File file, final Configuration configuration) {

        boolean success = true;
        File targetDirectory = file.getParentFile();
        while (!targetDirectory.exists()) {
            success = targetDirectory.mkdirs();
            if (success) {
                Constants.LOGGER.info("Created directory: " + targetDirectory.getAbsolutePath());
            } else {
                Constants.LOGGER.warn("Failed to create directory: " + targetDirectory.getAbsolutePath());
                break;
            }

            if (targetDirectory.getParentFile() != null)
                targetDirectory = targetDirectory.getParentFile();
        }
        if (success) {
            if (ResourceManager.hasResourceFile(this.getClass().getClassLoader(), configuration.fileName())) {
                success = ResourceManager.saveResourceFile(this.getClass().getClassLoader(), configuration.fileName(), file.toPath().toAbsolutePath(), false);
                if (success) {
                    Constants.LOGGER.info("Saved configuration resource: " + file.toPath().toAbsolutePath());
                } else {
                    Constants.LOGGER.warn("Failed to save configuration resource: " + file.toPath().toAbsolutePath());
                }
            } else {
                Constants.LOGGER.info("Couldn't find configuration resource '" + configuration.fileName() + "', created empty configuration file...");
                try {
                    success = file.createNewFile();
                } catch (final IOException ex) {
                    Constants.LOGGER.error("Failed to create empty configuration file.", ex);
                    return;
                }
                if (success)
                    Constants.LOGGER.info("Created empty configuration file: " + file.toPath().toAbsolutePath());
            }
        } else {
            Constants.LOGGER.warn("Failed to save configuration resource: " + file.toPath().toAbsolutePath());
        }

    }

    /**
     * Get the actual full file based on the {@link Configuration} annotation values.
     *
     * @param configuration The annotation instance.
     * @return The full file.
     */

    @NotNull
    private File getFile(@NotNull final Configuration configuration) {

        File file = new File(System.getProperty("user.dir"));
        for (final String subPath : configuration.subPaths())
            file = new File(file, subPath);

        return new File(file, configuration.fileName());

    }

    /**
     * Registers a new generic type handler.
     *
     * @param predicate   The identification predicate.
     * @param initializer The initializer.
     */

    public void registerGenericTypeHandler(@NotNull final Predicate<Class<?>> predicate, @NotNull final GenericTypeHandlerFactory initializer) {

        this.registeredGenericTypeHandlers.put(predicate, initializer);

    }

    /**
     * Returns a copy of the internal registered generic type handler map.
     *
     * @return The internal map copy.
     */

    public Map<Predicate<Class<?>>, GenericTypeHandlerFactory> getRegisteredGenericTypeHandlers() {

        return new HashMap<>(registeredGenericTypeHandlers);

    }

    /**
     * Registers a new serializable type.
     *
     * @param type       The type.
     * @param serializer The serializer.
     */

    public void registerSerializer(@NotNull final Class<?> type, @NotNull final ConfigurationSerializable<?> serializer) {

        this.registerSerializer(type, serializer, false);

    }

    /**
     * Registers a new serializable type.
     *
     * @param type       The type.
     * @param serializer The serializer.
     * @param override   Should override.
     */

    public void registerSerializer(@NotNull final Class<?> type, @NotNull final ConfigurationSerializable<?> serializer, final boolean override) {

        if (!this.registeredSerializers.containsKey(type) || override)
            this.registeredSerializers.put(type, serializer);

    }

    /**
     * Returns a copy of the internal registered serializer map.
     *
     * @return The internal map copy.
     */

    public Map<Class<?>, ConfigurationSerializable<?>> getRegisteredSerializers() {

        return new HashMap<>(registeredSerializers);

    }

    /**
     * Sets the value of the field respecting {@link Holder} fields.
     *
     * @param field The field.
     * @param type  The type.
     * @param value The value.
     * @throws IllegalAccessException If the set operation failed.
     */

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void setConfigurationFieldValue(@NotNull final Field field, @NotNull final Class<?> type,
                                                  @Nullable final Object value) throws IllegalAccessException {

        if (field.getType().isAssignableFrom(Holder.class)) {
            final Holder holder = (Holder) field.get(type);
            holder.set(value);
        } else {
            field.set(type, value);
        }

    }

}
