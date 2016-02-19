package net.nemerosa.versioning.core;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class BranchParsers {

    private static final Map<String, BranchParser> REGISTRY = ImmutableMap.<String, BranchParser>builder()
            .put("default", DefaultBranchParser.INSTANCE)
            .build();

    public static BranchParser get(String name) {
        BranchParser parser = REGISTRY.get(name);
        if (parser != null) {
            return parser;
        } else {
            throw new BranchParserNotFoundException(name);
        }
    }
}
