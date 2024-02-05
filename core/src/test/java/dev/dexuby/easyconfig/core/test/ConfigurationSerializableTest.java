package dev.dexuby.easyconfig.core.test;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import dev.dexuby.easyconfig.core.ConfigurationResolver;
import dev.dexuby.easyconfig.core.serialization.impl.InstantSerializer;
import dev.dexuby.easyconfig.core.test.resolvertest.ResolverTestConfig;
import dev.dexuby.easyconfig.core.test.util.MultiLine;
import dev.dexuby.easyreflect.EasyReflect;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ConfigurationSerializableTest {

    private final EasyReflect easyReflect;

    public ConfigurationSerializableTest() {

        this.easyReflect = EasyReflect.builder()
                .classLoader(this.getClass().getClassLoader())
                .resolvePackage(ResolverTestConfig.class.getPackage().getName())
                .build();

    }

    @Test
    public void testInstantSerializer() {

        final Config config = ConfigFactory.parseString(
                MultiLine.empty()
                        .appendLine("instant = 1693051486576")
                        .toString()
        );

        final Instant instant = InstantSerializer.getInstance().deserialize(config.getValue("instant"));
        assertNotNull(instant);
        assertEquals(1693051486576L, instant.toEpochMilli());

        final ConfigValue outputConfig = InstantSerializer.getInstance().serialize(instant);
        assertNotNull(outputConfig);
        assertEquals(1693051486576L, outputConfig.unwrapped());

    }

    @Test
    public void testAutoResolve() {

        final ConfigurationResolver configurationResolver = new ConfigurationResolver(this.easyReflect);
        assertEquals(1, configurationResolver.getRegisteredSerializers().size());

        configurationResolver.resolveSerializers();
        assertEquals(3, configurationResolver.getRegisteredSerializers().size());

    }

}
