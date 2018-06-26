package utilities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RegexUtility {

    static Map<String, String> fieldRegexs;
    static {
        fieldRegexs = new HashMap<>();
        try {
            Files.lines(Paths.get(".").toAbsolutePath().resolve("regex.ini"))
                    .forEachOrdered(line -> {
                                String[] keyValue = line.split(":", 2);
                                fieldRegexs.put(keyValue[0].trim(), keyValue[1].trim());
                            }
                    );
            fieldRegexs = Collections.unmodifiableMap(fieldRegexs);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param patternString
     * @param patternOptions
     * @param input
     * @return
     */
    public static boolean matches(String patternString, int patternOptions, String input) {
        return Pattern.compile(patternString, patternOptions).matcher(input).matches();
    }

    /**
     * Get the first N occurrences of the regex in the input string.
     * @param patternString Pattern to match
     * @param patternOptions Regex flags
     * @param N -1 means get all matches, 0 means none
     * @param input Input string to find matches in
     * @return List of matches where each match is a list of groups
     */
    public static List<List<String>> getFirstNMatches(String patternString, int patternOptions, int N, String input) {
        Pattern patternObj = Pattern.compile(patternString, patternOptions);
        Matcher matcher = patternObj.matcher(input);
        List<List<String>> matches = new ArrayList<>();
        // If no matches are to be found. Not to be used.
        if(N==0)
            return matches;
        int i = 0;
        while (matcher.find()) {
            matches.add(IntStream.range(0, matcher.groupCount()+1)
                    .boxed()
                    .map(index -> matcher.group(index))
                    .collect(Collectors.toList()));
            i++;
            if (i == N)
                break;
        }
        return matches;
    }

    /**
     *
     * @param regexField
     * @return
     */
    public String getRegexFor(String regexField) {
            return fieldRegexs.getOrDefault(regexField, null);
    }

    /**
     *
     * @param data
     * @return
     */
    public List<String> probeRegexField(String data) {
        return fieldRegexs.keySet()
                .parallelStream()
                .filter(regexField -> RegexUtility.matches(regexField, 0, data))
                .collect(Collectors.toList());
    }

    /**
     *
     * @param regex
     * @return
     */
    public String fillRandomValues(String regex) {
        return null;
    }

}
