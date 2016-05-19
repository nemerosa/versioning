package net.nemerosa.versioning.support

public final class Utils {

    private Utils() {
    }

    /**
     * Runs a command in the <code>wd</code> directory and returns its output. In case of error (exit
     * code different than 0), an exception is thrown.
     *
     * @param wd Directory where to execute the command
     * @param cmd Command to execute
     * @param args Command parameters
     * @return Output of the command
     */
    @Deprecated
    static String run(File wd, String cmd, String... args) {
        // Complete list of arguments
        List<String> list = new ArrayList<>();
        list.add(cmd);
        list.addAll(Arrays.asList(args));
        try {
            // Builds a process
            Process process = new ProcessBuilder(list).directory(wd).start();
            // Running the process and waiting for its completion
            int exit = process.waitFor();
            // In case of error
            if (exit != 0) {
                String error = process.errorStream.text.trim()
                throw new ProcessExitException(exit, error);
            } else {
                return process.inputStream.text.trim()
            }
        } catch (IOException | InterruptedException ex) {
            throw new ProcessRunException("Error while executing ${cmd} command: ${ex.getMessage()}")
        }
    }

}
