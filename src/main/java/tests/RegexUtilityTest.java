package tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import utilities.RegexUtility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

class RegexUtilityTest {

    @Test
    void testMatches() {
        Assertions.assertTrue(
                RegexUtility.matches("\\d+\\s+\\S+",
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
                "[abz]*"
        );
        for(String regexToTest:regexsToTest) {
            String generatedString = RegexUtility.fillRandomValues(regexToTest, 100);
            Assertions.assertTrue(
                    Pattern.matches(regexToTest, generatedString),
                    String.format("Unable generate a string matching the supplied regex.\nRegex : %s\nGenerated " +
                            "string : <%s>", regexToTest, generatedString)
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


}
