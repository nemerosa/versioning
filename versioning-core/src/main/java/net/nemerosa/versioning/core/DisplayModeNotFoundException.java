package net.nemerosa.versioning.core;

public class DisplayModeNotFoundException extends RuntimeException {
    public DisplayModeNotFoundException(String name) {
        super("Cannot find display mode with name: " + name);
    }
}
