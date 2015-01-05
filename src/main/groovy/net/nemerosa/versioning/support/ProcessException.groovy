package net.nemerosa.versioning.support;

public abstract class ProcessException extends RuntimeException {

    public ProcessException(String error) {
        super(error);
    }

}
