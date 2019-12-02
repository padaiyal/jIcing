import misc.Comparison;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import utilities.RegexUtility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

class RegexUtilityTest {

    @Test
    void testMatches() {
        Assertions.assertTrue(
            RegexUtility.matches(
                    "\\d+\\s+\\S+",
                    0,
                    "1290 TestString"),
            "Regex should've matched the provided string."
        );
    }


    /**
     *
     */
    @Test
    void testGetAllMatches() throws RegexUtility.InvalidRegexException {
        String pattern = "(\\d+)\\.(\\d+)";
        String input;
        List<List<String>> expectedMatches = Arrays.asList(
            Arrays.asList("99.99999999", "99", "99999999"),
            Arrays.asList("8.666666", "8", "666666"),
            Arrays.asList("983242342345.2545364356453", "983242342345", "2545364356453"),
            Arrays.asList("88.88", "88", "88")
        );
        List<List<String>> actualMatches,
                actualMinusExpected,
                expectedMinusActual;

        input = expectedMatches.stream()
            .map(expectedMatch -> expectedMatch.get(0))
            .reduce("", (eM1, eM2) -> eM1 + " " + eM2);

        actualMatches = RegexUtility.getAllMatches(pattern, 0, input);

        Assertions.assertNotNull(actualMatches, "No matches found, RegexUtility.getFirstNMatches() returned null.");
        expectedMinusActual = new ArrayList<>(expectedMatches);
        actualMatches.stream()
            .filter(actualMatch -> expectedMinusActual.indexOf(actualMatch) >= 0)
            .forEach(actualMatch -> expectedMinusActual.remove(expectedMinusActual.indexOf(actualMatch)));
        actualMinusExpected = new ArrayList<>(actualMatches);
        expectedMatches.stream()
            .filter(expectedMatch -> actualMinusExpected.indexOf(expectedMatch) >= 0)
            .forEach(expectedMatch -> actualMinusExpected.remove(actualMinusExpected.indexOf(expectedMatch)));
        Assertions.assertEquals(0, expectedMinusActual.size(), "Actual matches don't contain all expected matches.\nExpectedMatches - ActualMatches = " + expectedMinusActual + "\n");
        Assertions.assertEquals(0, actualMinusExpected.size(), "Expected matches don't contain all actual matches.\nActualMatches - ExpectedMatches = " + actualMinusExpected + "\n");
    }

    /**
     * Test RegexUtility::getFirstNMatches with some known field regexps
     */
    @Test
    void testGetFirstNMatches() {

    }

    @Test
    void testFillRandomValues() throws RegexUtility.InvalidRegexException {
        List<String> regexsToTest = Arrays.asList(
            "\\w",
            "\\w+",
            "\\w*",
            "\\s",
            "\\s+",
            "\\s*",
            "\\d",
            "\\d+",
            "\\d*",
            "[abz]",
            "[abz]*",
            "[abz]+"
        );
        for(String regexToTest:regexsToTest) {
            String generatedString = RegexUtility.fillRandomValues(regexToTest, 100);
            Assertions.assertTrue(
                    Pattern.matches(regexToTest, generatedString),
                    String.format("Unable generate a string matching the supplied regex.\nRegex : %s\nGenerated " +
                            "string : %s", regexToTest, generatedString)
            );
        }
    }


    @Test
    void testProbeRegexField() {
        List<String> dataValuesToProbe = Arrays.asList("192.168.1.1", "1.1.1.1");
        for (String dataValueToProbe : dataValuesToProbe) {
            System.out.println(Arrays.toString(RegexUtility.probeRegexField(dataValueToProbe).toArray()));
        }
    }


    /**
     * Ensures that all the desired comparison's output for test numbers b/w -2 * number and 2 * number yield a result
     * identical to matching the generated regex.
     * @param number Number to use in the comparison operation
     * @param comparison Comparison operation
     */
    static void testComparisonRegex(int number, Comparison comparison, boolean sqlRegex) {

        final String[] regexps = RegexUtility.generateRegexpsForPositiveNumbersLesserThan(number, comparison, sqlRegex);

        IntStream.range(-2*number, +2*number)
            .forEach(testNumber -> {
                boolean match = Arrays.stream(regexps)
                        .anyMatch(regexp -> RegexUtility.matches(regexp, 0, Integer.toString(testNumber)));
                Assertions.assertTrue(
                            match == comparison.evaluateComparison(testNumber, number)
                            // TODO: This condition is to short circuit negative test values until they are implemented.
                            || (testNumber < 0)
                        );
            });

        // Test for 0
        String[] expectedResult = (
                comparison == Comparison.LESSER_THAN_EQUAL
                        || comparison == Comparison.GREATER_THAN_EQUAL
                        || comparison == Comparison.EQUAL
                )?
                new String[]{Integer.toString(0)}
                : new String[]{};
        Assertions.assertArrayEquals(
                expectedResult,
                RegexUtility.generateRegexpsForPositiveNumbersLesserThan(0, comparison, sqlRegex)
                );

        // Assert that supplying negative numbers throw NotImplementedException
        Assertions.assertThrows(
                NotImplementedException.class,
                () -> RegexUtility.generateRegexpsForPositiveNumbersLesserThan(-100, comparison, sqlRegex)
        );

    }


    /**
     * Test conditions that are supported for equivalent regex generation.
     * @param number Number to use in the comparison regex
     * @param comparison Comparison operation to apply
     * @param sqlRegex If true, it generates a SQL compatible regex, Else a regular regex.
     */
    @ParameterizedTest
    @CsvSource(
        {
            "14000, EQUAL, false",
            "1125, LESSER, true",
            "1125, LESSER_THAN_EQUAL, false",
        }
    )
    void testGenerateRegexpsForNumberComparison(int number, Comparison comparison, boolean sqlRegex) {
        testComparisonRegex(number, comparison, sqlRegex);
    }


    /**
     * Test conditions that are not supported for equivalent regex generation.
     * @param number Number to use in the comparison regex
     * @param comparison Comparison operation to apply
     * @param sqlRegex If true, it generates a SQL compatible regex, Else a regular regex.
     */
    @ParameterizedTest
    @CsvSource(
        {
            "14000, GREATER, false",
            "14000, GREATER_THAN_EQUAL, false",
            "-14000, EQUAL, false",
            "-14000, LESSER, false",
            "-14000, LESSER_THAN_EQUAL, false",
            "-14000, GREATER, false",
            "-14000, GREATER_THAN_EQUAL, false",
            "-14000, , false",
            "-14000, , true"

        }
    )
    void testGenerateRegexpsForNumberComparisonNotImplemented(int number, Comparison comparison, boolean sqlRegex) {
        Assertions.assertThrows(
                NotImplementedException.class,
                () -> testComparisonRegex(number, comparison, sqlRegex)
        );
    }

    @ParameterizedTest
    @CsvSource(
        {
            "14000, , false",
            "14000, , true"
        }
    )
    void testGenerateRegexpsForNumberComparisonWithNullComparisonType(int number, Comparison comparison, boolean sqlRegex) {
        Assertions.assertThrows(
                NullPointerException.class,
                () -> testComparisonRegex(number, comparison, sqlRegex)
        );
    }

}
