package utilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Ranjan Mohan
 * @version 1.0.0
 * <p>
 * v1.0.0
 * Added support to:
 * - Execute commands in 3 types of shells/terminals - Bash, CMD, Powershell
 * - Configured command execution with timeouts
 * - Intelligently assign a shell based on the OS detected if no TypeOfShell is specified
 * <p>
 * TODO:
 * - Add documentation comments to all methods
 * - Internationalize all error messages
 * - NPE
 * - ShellNotFoundE
 * - OSNotFoundE
 * - Progress updates
 */

public class ShellUtility {

    private static Logger logger = LogManager.getLogger(ShellUtility.class);
    private static OS os = OS.getOs();
    private static HashMap<TypeOfShell, Path> shells = new HashMap<>();

    static {
        String command;
        Response response;
        for (TypeOfShell typeOfShell : TypeOfShell.values()) {
            try {
                if (os == OS.WINDOWS) {
                    command = String.format(
                            PropertyUtility.getProperty("utilities.ShellUtility.windows.where"),
                            typeOfShell.toString().toLowerCase()
                    );
                } else if (os == OS.LINUX) {
                    command = String.format(
                            PropertyUtility.getProperty("utilities.ShellUtility.linux.which"),
                            typeOfShell.toString().toLowerCase()
                    );
                } else {
                    throw new OsNotFoundException(os);
                }
                response = executeCommand(command);
                if (response.getReturnCode() == 0) {
                    shells.put(typeOfShell, Paths.get(response.getOutput(TypeOfOutput.STDOUT).split("\n")[0].trim()));
                }
            } catch (IOException | InterruptedException | OsNotFoundException | TimeoutException e) {
                logger.error(e);
            }
        }
    }

    public enum TypeOfOutput {
        STDOUT,
        STDERR
    }

    public enum TypeOfShell {
        POWERSHELL,
        CMD,
        BASH,
    }

    public static class ShellNotFoundException extends Exception {

        private TypeOfShell typeOfShell;

        ShellNotFoundException(TypeOfShell typeOfShell) {
            this.typeOfShell = typeOfShell;
        }

        public String toString() {
            return I18NUtility.getFormattedString(
                    "exception.ShellNotFoundException",
                    typeOfShell
            );
        }

    }

    public static class OsNotFoundException extends Exception {

        private OS os;

        OsNotFoundException(OS os) {
            this.os = os;
        }

        public String toString() {
            return I18NUtility.getFormattedString(
                    "exception.OsNotFoundException",
                    os
            );
        }

    }

    public static class Command {

        private HashMap<TypeOfShell, String> commands;

        public Command() {
            commands = new HashMap<>();
        }

        /**
         * Set the command to execute for the specified type of shell
         *
         * @param shellType Type of shell
         * @param command   Command to execute in the specified shell type
         */
        public void setCommand(TypeOfShell shellType, String command) {
            commands.put(shellType, command);
        }

        /**
         * Get the command to execute for the specified type of shell
         *
         * @param shellType Type of shell
         * @return Command to execute in the specified shell type
         * @throws ShellNotFoundException Thrown if no command is set for the specified shell type
         */
        public String getCommand(TypeOfShell shellType) throws ShellNotFoundException {
            if (commands.containsKey(shellType)) {
                return commands.get(shellType);
            } else {
                throw new ShellNotFoundException(shellType);
            }
        }
    }

    private static String[] splitArgs(String command) {
        command = Objects.requireNonNull(command);
        String trimmedCmd = command.trim();
        List<Character> delimiters = Arrays.asList('"', '\'');
        List<String> args = new ArrayList<>();
        char tempDelim = ' ';
        int startIndex = -1;
        int endIndex = -1;
        boolean flag = false;

        if (trimmedCmd.length() == 1)
            return new String[]{trimmedCmd};

        for (int i = 0; i < trimmedCmd.length(); i++) {
            if (tempDelim == ' ' && ((i == 0) || (trimmedCmd.charAt(i - 1) == ' ')) && delimiters.contains(trimmedCmd.charAt(i))) {
                tempDelim = trimmedCmd.charAt(i);
                startIndex = i + 1;
                flag = true;
            } else if (flag && tempDelim != ' ' && (trimmedCmd.charAt(i) == tempDelim) && ((i == trimmedCmd.length() - 1) || (trimmedCmd.charAt(i + 1) == ' '))) {
                tempDelim = ' ';
                endIndex = i;
                flag = false;
            }
            // Space is the delimiter
            else if (i == 0 && startIndex == -1) {
                startIndex = i;
            } else if (i == trimmedCmd.length() - 1 && startIndex != -1) {
                endIndex = i + 1;
            } else if (trimmedCmd.charAt(i) == ' ' && startIndex == -1) {
                startIndex = i;
            } else if (!flag && trimmedCmd.charAt(i + 1) == ' ' && endIndex == -1) {
                endIndex = i + 1;
            }

            if (startIndex >= 0 && endIndex >= 0) {
                args.add(trimmedCmd.substring(startIndex, endIndex).trim());
                startIndex = -1;
                endIndex = -1;
            }
        }
        return args.toArray(new String[0]);
    }

    /**
     * Executes the specified command
     *
     * @param command         Command string to execute
     * @param timeOutDuration Time out for the cmmand execution
     * @return The response of the command
     * @throws IOException          Thrown by ProcessBuilder::waitFor
     * @throws InterruptedException Thrown if the execution of the command is interrupted
     * @throws TimeoutException     Thrown f the command execution exceeds specified timeout
     */
    public static synchronized Response executeCommand(String command, Duration timeOutDuration) throws IOException, InterruptedException, TimeoutException {
        Objects.requireNonNull(command,
                I18NUtility.getFormattedString(
                        "input.validation.nonnull",
                        "Command"
                )
        );
        Objects.requireNonNull(timeOutDuration,
                I18NUtility.getFormattedString(
                        "input.validation.nonnull",
                        "Duration"
                )
        );
        System.out.println(
                I18NUtility.getFormattedString(
                        "utilities.ShellUtility.executing",
                        command
                )
        );
        HashMap<TypeOfOutput, String> output = new HashMap<>();
        Process process;
        ZonedDateTime executionStartTimestamp;
        long executionDuration;
        boolean timedOut;
        executionStartTimestamp = ZonedDateTime.now();
        process = new ProcessBuilder(splitArgs(command)).start();
        timedOut = !process.waitFor(timeOutDuration.getSeconds(), TimeUnit.SECONDS);
        if (timedOut) {
            executionDuration = Duration.between(executionStartTimestamp, ZonedDateTime.now()).getSeconds();
            throw new TimeoutException(
                    I18NUtility.getFormattedString("exception.TimeoutException",
                            command,
                            executionDuration,
                            timeOutDuration.getSeconds()
                    )
            );
        }
        int returnCode = process.waitFor();
        executionDuration = Duration.between(executionStartTimestamp, ZonedDateTime.now()).get(ChronoUnit.NANOS);
        output.put(TypeOfOutput.STDOUT, StreamUtility.convertInputStreamToString(process.getInputStream()));
        output.put(TypeOfOutput.STDERR, StreamUtility.convertInputStreamToString(process.getErrorStream()));
        process.destroy();
        return new Response(returnCode, output, executionStartTimestamp, executionDuration);
    }

    /**
     * Executes the specified command
     *
     * @param command Command string to execute
     * @return The response of the command
     * @throws IOException          Thrown by ProcessBuilder::waitFor
     * @throws InterruptedException Thrown if the execution of the command is interrupted
     * @throws TimeoutException     Thrown f the command execution exceeds specified timeout
     */
    public static Response executeCommand(String command) throws IOException, InterruptedException, TimeoutException {
        return executeCommand(command, Duration.ofSeconds(Long.parseLong(PropertyUtility.getProperty("utilities.ShellUtility.timeout"))));
    }

    /**
     * Executes the specified command
     *
     * @param command         Command string to execute
     * @param typeOfShell     The type of shell in which the command needs to be executed in
     * @param timeOutDuration Time out for the cmmand execution
     * @return The response of the command
     * @throws IOException          Thrown by ProcessBuilder::waitFor
     * @throws InterruptedException Thrown if the execution of the command is interrupted
     * @throws TimeoutException     Thrown f the command execution exceeds specified timeout
     */
    public static Response executeCommand(Command command, TypeOfShell typeOfShell, Duration timeOutDuration) throws IOException, InterruptedException, ShellNotFoundException, TimeoutException {
        Objects.requireNonNull(command,
                I18NUtility.getFormattedString(
                        "input.validation.nonnull",
                        "Command"
                )
        );
        Objects.requireNonNull(typeOfShell,
                I18NUtility.getFormattedString(
                        "input.validation.nonnull",
                        "TypeOfShell"
                )
        );
        Objects.requireNonNull(timeOutDuration,
                I18NUtility.getFormattedString(
                        "input.validation.nonnull",
                        "Duration"
                )
        );
        if (shells.containsKey(typeOfShell)) {
            return executeCommand(
                    String.format(
                            I18NUtility.getString("utilities.ShellUtility.execute"),
                            shells.get(typeOfShell).toAbsolutePath().toString(),
                            command.getCommand(typeOfShell)
                    ),
                    timeOutDuration
            );
        } else {
            throw new ShellNotFoundException(typeOfShell);
        }
    }

    /**
     * Executes the specified command
     *
     * @param command     Command to execute
     * @param typeOfShell The type of shell in which the command needs to be executed in
     * @return The response of the command
     * @throws IOException          Thrown by ProcessBuilder::waitFor
     * @throws InterruptedException Thrown if the execution of the command is interrupted
     * @throws TimeoutException     Thrown f the command execution exceeds specified timeout
     */
    public static Response executeCommand(Command command, TypeOfShell typeOfShell) throws IOException, InterruptedException, TimeoutException, ShellNotFoundException {
        return executeCommand(command, typeOfShell, Duration.ofSeconds(Long.parseLong(PropertyUtility.getProperty("utilities.ShellUtility.timeout"))));
    }

    /**
     * Executes the specified command
     *
     * @param command         Command to execute
     * @param timeOutDuration Time out for the cmmand execution
     * @return The response of the command
     * @throws IOException          Thrown by ProcessBuilder::waitFor
     * @throws InterruptedException Thrown if the execution of the command is interrupted
     * @throws TimeoutException     Thrown f the command execution exceeds specified timeout
     */
    public static Response executeCommand(Command command, Duration timeOutDuration) throws IOException, InterruptedException, ShellNotFoundException, OsNotFoundException, TimeoutException {
        if (os == OS.LINUX) {
            return executeCommand(command, TypeOfShell.BASH);
        } else if (os == OS.WINDOWS) {
            Response response = executeCommand(command, TypeOfShell.CMD);
            if (response.getReturnCode() == 127) { // If the executable isn't found
                response = executeCommand(command, TypeOfShell.POWERSHELL, timeOutDuration);
            }
            return response;
        } else {
            throw new OsNotFoundException(os);
        }
    }

    /**
     * Executes the specified command
     *
     * @param command Command to execute
     * @return The response of the command
     * @throws IOException          Thrown by ProcessBuilder::waitFor
     * @throws InterruptedException Thrown if the execution of the command is interrupted
     * @throws TimeoutException     Thrown f the command execution exceeds specified timeout
     */
    public static Response executeCommand(Command command) throws IOException, InterruptedException, ShellNotFoundException, OsNotFoundException, TimeoutException {
        return executeCommand(command, Duration.ofSeconds(Long.parseLong(PropertyUtility.getProperty("utilities.ShellUtility.timeout"))));
    }

}
