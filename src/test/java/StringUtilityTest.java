import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ConvertWith;
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
        @SuppressWarnings("deprecation")
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
        @SuppressWarnings("deprecation")
        Executable executable = () -> StringUtility.isSubString(baseString, subString);
        Assertions.assertThrows(
            NullPointerException.class,
            executable
        );
    }

    @ParameterizedTest
    @CsvSource(
        {
            "a b a c a b c, ' '"
        }
    )
    void testGetWordFrequencyDistribution(String str, @ConvertWith(StringArrayConverter.class) String[] delimiters) {
        // TODO: Add assertion statement and more test cases
        System.out.println(StringUtility.getWordFrequencyDistribution("a b a c a b c", new String[] {" "}));
    }

    @ParameterizedTest
    @CsvSource(
        {
            "JACCARD_INDEX, HELLO WORLD, WORLD HELLO, 1.0",
            "JACCARD_INDEX, HELLO WORLD, HELLO NEW WORLD, 0.6666666666666666",
            "JARO, TUNPS, TUNSP, 0.9333333333333332",
            "JARO, TUNSPS, TUNSEP, 0.888888888888889",
            "JARO, SASASASASASAS, ASASASASASASA, 0.782051282051282",
            "JARO_WRINKLER, DwAyNE, DuANE, 0.84",
            "JARO_WRINKLER, TRACE, TRATE, 0.9066666666666667",
            "LEVENSHTEIN,   HONDA,      HYUNDAI,    3",
            "LEVENSHTEIN,   KITTEN,     SITTING,    3",
            "LEVENSHTEIN,   INTENTION,  EXECUTION,  5",
            "SORENSEN_DICE,  HELLO WORLD, WORLD HELLO, 1.0",
            "SORENSEN_DICE,  HELLO WORLD, HELLO NEW WORLD, 0.8"
        }
    )
    void testStringSimilarityDistance(StringUtility.StringSimilarityDistanceType stringSimilarityDistanceType, String str1, String str2, Double expectedDistance) {
        Assertions.assertEquals(
            expectedDistance,
            StringUtility.getStringSimilarityDistance(stringSimilarityDistanceType, str1, str2)
        );
    }

    @ParameterizedTest
    @CsvSource(
            {
                    "lol,",
                    ", sub",
                    ","
            }
    )
    void testStringSimilarityDistanceWithNullInput(String str1, String str2) {
        for(StringUtility.StringSimilarityDistanceType stringSimilarityDistanceType: StringUtility.StringSimilarityDistanceType.values()) {
            Assertions.assertThrows(
                    NullPointerException.class,
                    () -> StringUtility.getStringSimilarityDistance(stringSimilarityDistanceType, str1, str2)
            );
        }
    }
}
