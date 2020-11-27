import datastructures.tree.BinarySearchTree;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utilities.OS;
import utilities.Response;
import utilities.ShellUtility;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

class ShellUtilityTest {

    private static ShellUtility.Command command;
    private static ShellUtility.Command nonTerminatingCommand;
    private static Duration timeoutDuration;
    private static ShellUtility.TypeOfShell typeOfShell;
    private final Logger logger = LogManager.getLogger(ShellUtilityTest.class);

    @BeforeEach
    void prepare() {
        command = new ShellUtility.Command();
        command.setCommand(ShellUtility.TypeOfShell.BASH, "ls -al /");
        command.setCommand(ShellUtility.TypeOfShell.CMD, "dir");
        command.setCommand(ShellUtility.TypeOfShell.POWERSHELL, "dir");
        command.setCommand(ShellUtility.TypeOfShell.ZSH, "ls -al /");

        nonTerminatingCommand = new ShellUtility.Command();
        nonTerminatingCommand.setCommand(ShellUtility.TypeOfShell.BASH, "cat");
        nonTerminatingCommand.setCommand(ShellUtility.TypeOfShell.CMD, "ping 127.0.0.1 -n 4294967295");
        nonTerminatingCommand.setCommand(ShellUtility.TypeOfShell.POWERSHELL, "ping 127.0.0.1 -n 4294967295");
        nonTerminatingCommand.setCommand(ShellUtility.TypeOfShell.ZSH, "cat");

        timeoutDuration = Duration.ofSeconds(5);

        OS os = OS.getOs();

        typeOfShell = switch(os) {
            case WINDOWS -> ShellUtility.TypeOfShell.CMD;
            case LINUX -> ShellUtility.TypeOfShell.BASH;
            case MAC_OS_X -> ShellUtility.TypeOfShell.ZSH;
            default -> null;
        };
    }

    @Test
    public void testExecuteCommandString() throws IOException, InterruptedException, TimeoutException, ShellUtility.ShellNotFoundException {
        String commandString = command.getCommand(typeOfShell);
        Response response = ShellUtility.executeCommand(commandString);
        Assertions.assertEquals(0, response.getReturnCode());
    }

    @Test
    void testExecuteCommandStringWithTimeout() throws IOException, InterruptedException, TimeoutException, ShellUtility.ShellNotFoundException {
        String commandString = command.getCommand(typeOfShell);
        Response response = ShellUtility.executeCommand(commandString, timeoutDuration);
        Assertions.assertEquals(0, response.getReturnCode());
    }

    @Test
    void testExecuteNonTerminatingCommandStringWithTimeout() throws IOException, InterruptedException, ShellUtility.ShellNotFoundException {
        boolean timeoutExceptionThrown = false;
        String nonTerminatingCommandString = nonTerminatingCommand.getCommand(typeOfShell);
        try {
            ShellUtility.executeCommand(nonTerminatingCommandString, timeoutDuration);
        }
        catch(TimeoutException e) {
            timeoutExceptionThrown = true;
        }
        Assertions.assertTrue(timeoutExceptionThrown);
    }

    @Test
    void testExecuteCommand() throws IOException, InterruptedException, TimeoutException, ShellUtility.OsNotFoundException, ShellUtility.ShellNotFoundException {
        Response response = ShellUtility.executeCommand(command);
        Assertions.assertEquals(0, response.getReturnCode());
    }

    @Test
    void testExecuteCommandWithTimeout() throws IOException, InterruptedException, TimeoutException, ShellUtility.OsNotFoundException, ShellUtility.ShellNotFoundException {
        Response response = ShellUtility.executeCommand(command, timeoutDuration);
        Assertions.assertEquals(0, response.getReturnCode());
    }

    @Test
    void testExecuteNonTerminatingCommandWithTimeout() throws IOException, InterruptedException, ShellUtility.OsNotFoundException, ShellUtility.ShellNotFoundException {
        boolean timeoutExceptionThrown = false;
        try {
            ShellUtility.executeCommand(nonTerminatingCommand, timeoutDuration);
        }
        catch(TimeoutException e) {
            timeoutExceptionThrown = true;
        }
        Assertions.assertTrue(timeoutExceptionThrown);
    }

    @Test
    void testExecuteCommandWithTypeOfShell() throws IOException, InterruptedException, TimeoutException, ShellUtility.ShellNotFoundException {
        Response response = ShellUtility.executeCommand(command, typeOfShell);
        Assertions.assertEquals(0, response.getReturnCode());
    }

    @Test
    void testExecuteCommandWithTypeOfShellAndTimeout() throws IOException, InterruptedException, TimeoutException, ShellUtility.ShellNotFoundException {
        Response response = ShellUtility.executeCommand(command, typeOfShell, timeoutDuration);
        Assertions.assertEquals(0, response.getReturnCode());
    }

    @Test
    void testExecuteNonTerminatingCommandWithTypeOfShellAndTimeout() throws IOException, InterruptedException, ShellUtility.ShellNotFoundException {
        boolean timeoutExceptionThrown = false;
        try {
            ShellUtility.executeCommand(nonTerminatingCommand, typeOfShell, timeoutDuration);
        }
        catch(TimeoutException e) {
            timeoutExceptionThrown = true;
        }
        Assertions.assertTrue(timeoutExceptionThrown);
    }

    @Test
    void testExecuteNullCommandString() throws IOException, InterruptedException, TimeoutException {
        boolean npeThrown = false;
        try {
            ShellUtility.executeCommand((String) null);
        }
        catch(NullPointerException e) {
            npeThrown = true;
        }
        Assertions.assertTrue(npeThrown);
    }

    @Test
    void testExecuteNullCommandStringWithNullTimeout() throws IOException, InterruptedException, TimeoutException {
        boolean npeThrown = false;
        try {
            ShellUtility.executeCommand((String) null, null);
        }
        catch(NullPointerException e) {
            npeThrown = true;
        }
        Assertions.assertTrue(npeThrown);
    }

    @Test
    void testExecuteNullCommand() throws IOException, InterruptedException, TimeoutException, ShellUtility.ShellNotFoundException, ShellUtility.OsNotFoundException {
        boolean npeThrown = false;
        try {
            ShellUtility.executeCommand((ShellUtility.Command) null);
        }
        catch(NullPointerException e) {
            npeThrown = true;
        }
        Assertions.assertTrue(npeThrown);
    }

    @Test
    void testExecuteNullCommandWithNullTimeout() throws IOException, InterruptedException, TimeoutException, ShellUtility.OsNotFoundException, ShellUtility.ShellNotFoundException {
        boolean npeThrown = false;
        try {
            ShellUtility.executeCommand((ShellUtility.Command) null, (Duration) null);
        }
        catch(NullPointerException e) {
            npeThrown = true;
        }
        Assertions.assertTrue(npeThrown);
    }

    @Test
    void testExecuteNullCommandWithNullTypeOfShell() throws IOException, InterruptedException, TimeoutException, ShellUtility.ShellNotFoundException {
        boolean npeThrown = false;
        try {
            ShellUtility.executeCommand(null, (ShellUtility.TypeOfShell) null);
        }
        catch(NullPointerException e) {
            npeThrown = true;
        }
        Assertions.assertTrue(npeThrown);
    }

    @Test
    void testExecuteNullCommandWithNullTypeOfShellAndNullTimeout() throws IOException, InterruptedException, TimeoutException, ShellUtility.ShellNotFoundException {
        boolean npeThrown = false;
        try {
            ShellUtility.executeCommand(null, null, null);
        }
        catch(NullPointerException e) {
            npeThrown = true;
        }
        Assertions.assertTrue(npeThrown);
    }
}

