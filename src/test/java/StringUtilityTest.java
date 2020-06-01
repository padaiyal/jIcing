import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import utilities.StringUtility;


class StringUtilityTest {

    /**
     * Test repeatString functionality with positive, negative, 0 repeat count and null string value.
     * @param string String to repeat.
     * @param repeatCount Number of times too repeat.
     * @param expectedOutput Expected output.
     */
    @ParameterizedTest
    @CsvSource(
        {
            "lol, 5, lollollollollol",
            "lol, 0, ''",
            "lol, -1, ''",
            "'', 10000, ''"
        }
    )
    void testRepeatString(String string, long repeatCount, String expectedOutput) {
        try {
            @SuppressWarnings("deprecation")
            String actualOutput = StringUtility.repeat(string, repeatCount);
            Assertions.assertEquals(expectedOutput, actualOutput);
        }
        catch(IllegalArgumentException e) {
            Assertions.assertTrue(repeatCount < 0);
        }
    }
}

