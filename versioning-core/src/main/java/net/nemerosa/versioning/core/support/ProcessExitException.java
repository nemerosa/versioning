package net.nemerosa.versioning.core.support;

public class ProcessExitException extends ProcessException {

    private final int exit;

    public ProcessExitException(int exit, String error) {
        super(String.format("[%s] %s", exit, error));
        this.exit = exit;
    }

    public int getExit() {
        return exit;
    }

}
