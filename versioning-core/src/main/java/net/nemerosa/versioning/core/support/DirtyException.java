package net.nemerosa.versioning.core.support;

public class DirtyException extends RuntimeException {

    public DirtyException() {
        super("Dirty working copy - cannot compute version.");
    }

}
