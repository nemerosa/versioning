package net.nemerosa.versioning.core;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class FullVersionBuilders {

    private static final Map<String, FullVersionBuilder> REGISTRY = ImmutableMap.<String, FullVersionBuilder>builder()
            .put("default", DefaultFullVersionBuilder.INSTANCE)
            .build();

    public static FullVersionBuilder get(String name) {
        FullVersionBuilder parser = REGISTRY.get(name);
        if (parser != null) {
            return parser;
        } else {
            throw new FullVersionBuilderNotFoundException(name);
        }
    }
}
