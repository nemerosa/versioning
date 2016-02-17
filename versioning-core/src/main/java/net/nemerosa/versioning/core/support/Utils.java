package net.nemerosa.versioning.core.support;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Utils {

    private Utils() {
    }

    /**
     * Runs a command in the <code>wd</code> directory and returns its output. In case of error (exit
     * code different than 0), an exception is thrown.
     *
     * @param wd   Directory where to execute the command
     * @param cmd  Command to execute
     * @param args Command parameters
     * @return Output of the command
     */
    public static String run(File wd, String cmd, List<String> args) {
        return run(wd, cmd, args.toArray(new String[args.size()]));
    }

    /**
     * Runs a command in the <code>wd</code> directory and returns its output. In case of error (exit
     * code different than 0), an exception is thrown.
     *
     * @param wd   Directory where to execute the command
     * @param cmd  Command to execute
     * @param args Command parameters
     * @return Output of the command
     */
    public static String run(File wd, String cmd, String... args) {
        try {
            // Builds a process
            Process process = process(wd, cmd, args);
            // Running the process and waiting for its completion
            int exit = process.waitFor();
            // In case of error
            if (exit != 0) {
                String error = StringUtils.trim(
                        IOUtils.toString(process.getErrorStream())
                );
                throw new ProcessExitException(exit, error);
            } else {
                return StringUtils.trim(
                        IOUtils.toString(process.getInputStream())
                );
            }
        } catch (IOException | InterruptedException ex) {
            throw new ProcessRunException(
                    String.format(
                            "Error while executing %s command: %s",
                            cmd,
                            ex.getMessage()
                    )
            );
        }
    }

    /**
     * Runs a command in the <code>wd</code> directory and waits for its return code. If OK,
     * return <code>true</code>.
     *
     * @param wd   Directory where to execute the command
     * @param cmd  Command to execute
     * @param args Command parameters
     * @return Output of the command
     */
    public static boolean processOk(File wd, String cmd, String... args) {
        try {
            // Builds a process
            Process process = process(wd, cmd, args);
            // Running the process and waiting for its completion
            int exit = process.waitFor();
            // In case of error
            return (exit == 0);
        } catch (InterruptedException ex) {
            throw new ProcessRunException(
                    String.format(
                            "Error while executing %s command: %s",
                            cmd,
                            ex.getMessage()
                    )
            );
        }
    }

    /**
     * Builds a process for a command in the <code>wd</code> directory.
     *
     * @param wd   Directory where to execute the command
     * @param cmd  Command to execute
     * @param args Command parameters
     * @return Output of the command
     */
    public static Process process(File wd, String cmd, String... args) {
        // Complete list of arguments
        List<String> list = new ArrayList<>();
        list.add(cmd);
        list.addAll(Arrays.asList(args));
        // Builds a process
        try {
            return new ProcessBuilder(list).directory(wd).start();
        } catch (IOException ex) {
            throw new ProcessRunException(
                    String.format(
                            "Error while executing %s command: %s",
                            cmd,
                            ex.getMessage()
                    )
            );
        }
    }

}
