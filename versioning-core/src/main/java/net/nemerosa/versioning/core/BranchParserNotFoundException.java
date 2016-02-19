package net.nemerosa.versioning.core;

public class BranchParserNotFoundException extends RuntimeException {
    public BranchParserNotFoundException(String name) {
        super("Cannot find branch parser with name: " + name);
    }
}
