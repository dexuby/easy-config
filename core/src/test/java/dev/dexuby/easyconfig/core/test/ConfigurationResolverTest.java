package dev.dexuby.easyconfig.core.test;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import dev.dexuby.easycommon.external.jetbrains.annotations.NotNull;
import dev.dexuby.easyconfig.core.ConfigurationResolver;
import dev.dexuby.easyconfig.core.test.resolvertest.ResolverTestConfig;
import dev.dexuby.easyconfig.core.test.util.MultiLine;
import dev.dexuby.easyreflect.EasyReflect;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class ConfigurationResolverTest {

    private final EasyReflect easyReflect;

    public ConfigurationResolverTest() {

        this.easyReflect = EasyReflect.builder()
                .classLoader(this.getClass().getClassLoader())
                .resolvePackage(ResolverTestConfig.class.getPackage().getName())
                .build();

    }

    @Test
    public void testResolve() {
        
        final Config config = ConfigFactory.parseString(
                MultiLine.empty()
                        .appendLine("test-value-1 = test1")
                        .appendLine("test-value-2 = test2")
                        .toString()
        );

        final ConfigurationResolver configurationResolver = this.createConfigurationResolver(config);
        configurationResolver.resolveAndLoad();

        assertEquals("test1", ResolverTestConfig.TEST_VALUE_1.get());
        assertEquals("test2", ResolverTestConfig.TEST_VALUE_2);

    }

    @Test
    public void testResolveMap() {

        final Config config = ConfigFactory.parseString(
                MultiLine.empty()
                        .appendLine("test-value-3 = [")
                        .appendLine("  {")
                        .appendLine("    key = test-key")
                        .appendLine("    value = test-value")
                        .appendLine("  }")
                        .appendLine("]")
                        .toString()
        );

        final ConfigurationResolver configurationResolver = this.createConfigurationResolver(config);
        configurationResolver.resolveAndLoad();

        final Map<String, String> map = ResolverTestConfig.TEST_VALUE_3.get();
        assertEquals(1, map.size());
        assertTrue(map.containsKey("test-key"));
        assertEquals("test-value", map.get("test-key"));

    }

    private ConfigurationResolver createConfigurationResolver(@NotNull final Config config) {

        final ConfigurationResolver configurationResolver = spy(new ConfigurationResolver(this.easyReflect));
        doReturn(config).when(configurationResolver).loadConfigFromFile(any());
        when(configurationResolver.loadConfigFromFile(any())).thenReturn(config);
        doNothing().when(configurationResolver).saveDefaultConfiguration(any(), any());
        doNothing().when(configurationResolver).saveConfigToFile(any(), any());
        doCallRealMethod().when(configurationResolver).resolveAndLoad();

        return configurationResolver;

    }

}
