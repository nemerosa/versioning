package net.nemerosa.versioning.core;

public class VersioningException extends RuntimeException {
    public VersioningException(String pattern, Object... parameters) {
        super(String.format(pattern, parameters));
    }
}
