package net.nemerosa.versioning.support;

public class ProcessExitException extends ProcessException {

    private final int exit;

    public ProcessExitException(int exit, String error) {
        super("[${exit}] ${error}");
        this.exit = exit;
    }

    public int getExit() {
        return exit;
    }

}
