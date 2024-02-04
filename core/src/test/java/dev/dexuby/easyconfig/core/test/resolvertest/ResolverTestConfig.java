package dev.dexuby.easyconfig.core.test.resolvertest;

import dev.dexuby.easycommon.tuple.Pair;
import dev.dexuby.easycommon.util.MapUtils;
import dev.dexuby.easyconfig.core.Configuration;
import dev.dexuby.easyconfig.core.ConfigurationValue;
import dev.dexuby.easyconfig.core.common.Holder;

import java.util.Map;

@Configuration(fileName = "test.conf")
public class ResolverTestConfig {

    @ConfigurationValue(path = "test-value-1", description = "Test description 1")
    public static final Holder<String> TEST_VALUE_1 = Holder.of("Hello World!");

    @ConfigurationValue(path = "test-value-2", description = "Test description 2")
    public static String TEST_VALUE_2 = "Hello World!";

    @ConfigurationValue(path = "test-value-3", description = "Test description 3")
    public static final Holder<Map<String, String>> TEST_VALUE_3 = Holder.of(MapUtils.newMap(Pair.of("1", "2")));

}
