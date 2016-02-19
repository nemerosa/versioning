package net.nemerosa.versioning.core;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class ReleaseModes {

    private static final Map<String, ReleaseMode> REGISTRY = ImmutableMap.<String, ReleaseMode>builder()
            .put("tag", new TagReleaseMode())
            .put("snapshot", new SnapshotReleaseMode())
            .build();

    public static ReleaseMode get(String name) {
        ReleaseMode parser = REGISTRY.get(name);
        if (parser != null) {
            return parser;
        } else {
            throw new ReleaseModeNotFoundException(name);
        }
    }
}
