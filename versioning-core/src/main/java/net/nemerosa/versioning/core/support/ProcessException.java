package net.nemerosa.versioning.core.support;

public abstract class ProcessException extends RuntimeException {

    public ProcessException(String error) {
        super(error);
    }

}
