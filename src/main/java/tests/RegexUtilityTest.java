package tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import utilities.RegexUtility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RegexUtilityTest {

    @Test
    public void testMatches() {
        Assertions.assertTrue(RegexUtility.matches("\\d+\\s+\\S+", 0, "1290 TestString"),
                "Regex should've matched the provided string."
        );
    }

    /**
     *
     * @param pattern
     * @param input
     * @param expectedMatches
     * @return
     */
    public List<List<String>> testGetGroupsIfMatches(String pattern, String input, List<List<String>> expectedMatches) {
        List<List<String>> actualMatches = RegexUtility.getFirstNMatches(pattern, 0, -1, input);
        List<List<String>> actualMinusExpected, expectedMinusActual;
        Assertions.assertTrue(actualMatches!=null, "No matches found, RegexUtility.getFirstNMatches() returned null.");
        expectedMinusActual = new ArrayList<>(expectedMatches);
        actualMatches.stream()
                .filter(actualMatch -> expectedMinusActual.indexOf(actualMatch) >= 0)
                .forEach(actualMatch -> expectedMinusActual.remove(expectedMinusActual.indexOf(actualMatch)));
        actualMinusExpected = new ArrayList<>(actualMatches);
        expectedMatches.stream()
                .filter(expectedMatch -> actualMinusExpected.indexOf(expectedMatch) >= 0)
                .forEach(expectedMatch -> actualMinusExpected.remove(actualMinusExpected.indexOf(expectedMatch)));
        Assertions.assertTrue(expectedMinusActual.size() == 0, "Actual matches don't contain all expected matches.\nExpectedMatches - ActualMatches = "+expectedMinusActual+"\n");
        Assertions.assertTrue(actualMinusExpected.size() == 0, "Expected matches don't contain all actual matches.\nActualMatches - ExpectedMatches = "+actualMinusExpected+"\n");
        return actualMatches;
    }

    /**
     *
     */
    @Test
    public void testGetGroupsIfMatches1() {
        List<List<String>> expectedMatches = new ArrayList<>();
        expectedMatches.add(Arrays.asList("99.99999999", "99", "99999999"));
        expectedMatches.add(Arrays.asList("8.666666", "8", "666666"));
        expectedMatches.add(Arrays.asList("983242342345.2545364356453", "983242342345", "2545364356453"));
        expectedMatches.add(Arrays.asList("99.99999999", "99", "99999999"));
        expectedMatches.add(Arrays.asList("88.88", "88", "88"));
        testGetGroupsIfMatches("(\\d+)\\.(\\d+)", "99.99999999 99.99999999 88.88 8.666666 \n 983242342345.2545364356453", expectedMatches);
    }

    /**
     * Test RegexUtility::getFirstNMatches with some known field regexps
     */
    @Test
    public void testFieldRegexMatches() {

    }

}
