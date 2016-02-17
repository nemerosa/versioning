package net.nemerosa.versioning.core.support;

public class XMLElementException extends RuntimeException {
    public XMLElementException(Exception ex) {
        super("Cannot parse XML", ex);
    }
}
