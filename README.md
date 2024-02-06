[![](https://jitpack.io/v/dexuby/easy-config.svg)](https://jitpack.io/#dexuby/easy-config)
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.dexuby.easy-config</groupId>
    <artifactId>easy-config-core</artifactId>
    <version>1.0.3</version>
</dependency>
```
# EasyConfig

EasyConfig is an easy to use configuration system built on top of https://github.com/lightbend/config. The goal of the project is to provide a configuration library that is extremely easy to use while still being
powerful and flexible enough to be viable for all sorts of projects.

Feel free to check out other open-source libraries from the easy series, two example that are being used in this library:
- https://github.com/dexuby/easy-common
- https://github.com/dexuby/easy-reflect

Requirements:
- Java 8+

## How to create a config:
```java
@Configuration(fileName = "config.conf")
public final class BaseConfig {

  @ConfigurationValue(path = "example.value", description = "This is an example value."
  public static final Holder<String> EXAMPLE_VALUE = Holder.of("A default value!");

  @ConfigurationValue(path = "example.map", description = "This is an example map."
  public static final Holder<Map<String, Integer>> EXAMPLE_MAP = Holder.of(new HashMap<>());

}
```
It's highly recommended to use a `Holder` wrapper instance for your configuration values however the system technically doesn't require them. Arrays, collections & maps are supported (nesting of those types is currently not supported).

If possible always provide a default value for collections and maps since the system will re-use them. If no default value is present it'll create a new instance based on the field type so you'll have to specify the actual implementation you want in that case.

The system will write all configuration values that are not present to the specified file including the description as a comment. You can optionally provide a default config file with some default values as a resource however this is generally not necessary.

By default the file will always be created in the startup folder, you can change this behaviour by specifying the optional subPaths value of the `@Configuration` annotation.

## How to register and load all configs:
```java
final String ignoredPackage = "this.is.an.example";
final EasyReflect easyReflect = EasyReflect.builder()
                .classLoader(this.getClass().getClassLoader())
                .ignoredPackage(ignoredPackage)
                .resolvePackage(this.getClass().getPackage().getName())
                .build();

final ConfigurationResolver configurationResolver = new ConfigurationResolver(easyReflect);
configurationResolver.resolveAndLoad();
```
All configurations will get auto resolved and loaded in the target package and all subpackages that are not ignored. In order to reload all configurations simply re-use the same `ConfigurationResolver` instance and call `#resolveAndLoad` again.

## How to implement a (de)serializer:
```java
public final class InstantSerializer implements ConfigurationSerializable<Instant> {

    private static class InstantSerializerSingleton {

        private static final InstantSerializer INSTANCE = new InstantSerializer();

    }

    private InstantSerializer() {

    }

    @Nullable
    @Override
    public ConfigValue serialize(@NotNull final Instant input) {

        return ConfigValueFactory.fromAnyRef(input.toEpochMilli());

    }

    @Nullable
    @Override
    public Instant deserialize(@NotNull final ConfigValue configValue) {

        return Instant.ofEpochMilli((long) configValue.unwrapped());

    }

    public static InstantSerializer getInstance() {

        return InstantSerializerSingleton.INSTANCE;

    }

}
```
If you want to utilize auto serializer resolving you'll have to add the `@ConfigurationSerializer` annotation to your serializer class. This class then also needs to provide either a singleton getter method or a public zero-args constructor.

## How to register a (de)serializer:
You can either provide a (de)serializer map during the construction of your `ConfigurationResolver` instance, use the `#registerSerializer` method or use the auto serializer resolver:
```java
final Map<Class<?>, ConfigurationSerializable<?>> defaultSerializers =
            MapUtils.newMap(
                    Pair.of(Foo.class, FooSerializer.getInstance()),
                    Pair.of(Bar.class, BarSerializer.getInstance()),
            );
final ConfigurationResolver configurationResolver = new ConfigurationResolver(easyReflect, defaultSerializers);
```
```java
configurationResolver.registerSerializer(Foo.class, FooSerializer.getInstance());
```
```java
configurationResolver.resolveSerializers();
```
Make sure to register all (de)serializers before you load your configurations.
