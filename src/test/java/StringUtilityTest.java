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
            String actualOutput = StringUtility.repeat(string, repeatCount);
            Assertions.assertEquals(expectedOutput, actualOutput);
        }
        catch(IllegalArgumentException e) {
            Assertions.assertTrue(repeatCount < 0);
        }
    }

    @ParameterizedTest
    @CsvSource(
        {
            "lollollollollol, lolololol, false",
            "lolololololool, lool, true",
            "pololololol121, po, true",
            "pololololol121, pololololok, false",
            "pololololol121, ololololol1, true",
            "lololololol, lololololololololol, false",
            "mississippi, issip, true",
            "'', '', true",
            "abcd, '', true",
            "'', abcd, false",

        }
    )
    void testIsSubString(String baseString, String subString, boolean expectedResult) {
        boolean actualResult = StringUtility.isSubString(baseString, subString);
        Assertions.assertEquals(expectedResult, actualResult);
    }

    @ParameterizedTest
    @CsvSource(
        {
            "lol,",
            ", sub",
            ","
        }
    )
    void testIsSubStringWithNullInput(String baseString, String subString) {
        Assertions.assertThrows(NullPointerException.class, () -> StringUtility.isSubString(baseString, subString));
    }

    @ParameterizedTest
    @CsvSource(
        {
            "LEVENSHTEIN,   HONDA,      HYUNDAI,    3",
            "LEVENSHTEIN,   KITTEN,     SITTING,    3",
            "LEVENSHTEIN,   INTENTION,  EXECUTION,  5"
        }
    )
    void testStringSimilarityDistance(StringUtility.StringSimilarityDistanceType stringSimilarityDistanceType, String str1, String str2, Double expectedDistance) {
        Assertions.assertEquals(
            expectedDistance,
            StringUtility.getStringSimilarityDistance(stringSimilarityDistanceType, str1, str2)
        );
    }
}
