package dev.dexuby.easyconfig.core.test;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import dev.dexuby.easyconfig.core.serialization.impl.InstantSerializer;
import dev.dexuby.easyconfig.core.test.util.MultiLine;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ConfigurationSerializableTest {

    @Test
    public void testInstantSerializer() {

        final Config config = ConfigFactory.parseString(
                MultiLine.empty()
                        .appendLine("instant = {")
                        .appendLine("  timestamp = 1693051486576")
                        .appendLine("}")
                        .toString()
        );

        final Instant instant = InstantSerializer.getInstance().deserialize(config.getConfig("instant"));
        assertNotNull(instant);
        assertEquals(1693051486576L, instant.toEpochMilli());

        final Config outputConfig = InstantSerializer.getInstance().serialize(instant);
        assertNotNull(outputConfig);
        assertEquals(1693051486576L, outputConfig.getLong("timestamp"));

    }

}
