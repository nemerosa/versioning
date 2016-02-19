package net.nemerosa.versioning.core;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class DisplayModes {

    private static final Map<String, DisplayMode> REGISTRY = ImmutableMap.<String, DisplayMode>builder()
            .put("base", new BaseDisplayMode())
            .put("full", new FullDisplayMode())
            .put("snapshot", new SnapshotDisplayMode())
            .build();

    public static DisplayMode get(String name) {
        DisplayMode parser = REGISTRY.get(name);
        if (parser != null) {
            return parser;
        } else {
            throw new DisplayModeNotFoundException(name);
        }
    }
}
