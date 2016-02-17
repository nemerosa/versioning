package net.nemerosa.versioning.core.support;

public class XMLElementNotFoundException extends RuntimeException {
    public XMLElementNotFoundException(String name) {
        super("Cannot find element with name " + name);
    }
}
