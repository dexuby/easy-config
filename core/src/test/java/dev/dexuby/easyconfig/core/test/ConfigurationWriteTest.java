package dev.dexuby.easyconfig.core.test;

import com.typesafe.config.*;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConfigurationWriteTest {

    @Test
    public void testWriting() {

        ConfigOrigin origin = ConfigOriginFactory.newSimple();
        origin = origin.withComments(Collections.singletonList("fix comment"));

        Config config = ConfigFactory.empty();
        config = config.withValue("example", ConfigValueFactory.fromAnyRef("example-value").withOrigin(origin));
        config = config.withValue("example-complex", ConfigValueFactory.fromMap(
                ConfigFactory.empty().withValue("example-inner-key", ConfigValueFactory.fromAnyRef("example-inner-value")).root().unwrapped()
        ).withOrigin(origin));

        final ConfigRenderOptions configRenderOptions = ConfigRenderOptions.defaults()
                .setOriginComments(false)
                .setFormatted(false)
                .setComments(true)
                .setJson(false);

        final String content = config.root().render(configRenderOptions);
        assertEquals("# fix comment\n" +
                "example=example-value,# fix comment\n" +
                "example-complex{example-inner-key=example-inner-value}", content);

    }

}
