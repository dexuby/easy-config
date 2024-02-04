package dev.dexuby.easyconfig.core.test.util;

import dev.dexuby.easycommon.external.jetbrains.annotations.NotNull;

public class MultiLine {

    private final StringBuilder stringBuilder = new StringBuilder();

    private MultiLine() {

    }

    @NotNull
    public MultiLine appendLine(@NotNull final String line) {

        this.stringBuilder.append(line).append(System.lineSeparator());
        return this;

    }

    @NotNull
    @Override
    public String toString() {

        return this.stringBuilder.toString();

    }

    @NotNull
    public static MultiLine empty() {

        return new MultiLine();

    }

}
