package utilities;

import java.time.ZonedDateTime;
import java.util.HashMap;

public class Response {
    private HashMap<ShellUtility.TypeOfOutput, String> output;
    private ZonedDateTime executionStartTimestamp;
    private long executionDuration;
    private int returnCode;

    public Response(int returnCode, HashMap<ShellUtility.TypeOfOutput, String> output, ZonedDateTime executionStartTimestamp, long executionDuration) {
        this.output = new HashMap<>(output);
        this.returnCode = returnCode;
        this.executionStartTimestamp = executionStartTimestamp;
        this.executionDuration = executionDuration;
    }

    public int getReturnCode() {
        return returnCode;
    }

    public String getOutput(ShellUtility.TypeOfOutput typeOfOutput) {
        return output.get(typeOfOutput);
    }

    public ZonedDateTime getExecutionStartTimestamp() {
        return executionStartTimestamp;
    }

    public long getExecutionDuration() {
        return executionDuration;
    }

}
