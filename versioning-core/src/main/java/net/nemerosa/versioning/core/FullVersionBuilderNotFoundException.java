package net.nemerosa.versioning.core;

public class FullVersionBuilderNotFoundException extends RuntimeException {
    public FullVersionBuilderNotFoundException(String name) {
        super("Cannot find full version builder with name: " + name);
    }
}
