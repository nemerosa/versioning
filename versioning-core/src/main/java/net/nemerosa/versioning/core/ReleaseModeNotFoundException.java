package net.nemerosa.versioning.core;

public class ReleaseModeNotFoundException extends RuntimeException {
    public ReleaseModeNotFoundException(String name) {
        super("Cannot find release mode with name: " + name);
    }
}
