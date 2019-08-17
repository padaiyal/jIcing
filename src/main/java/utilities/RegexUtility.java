package utilities;

import java.nio.charset.MalformedInputException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @version 1.0.0
 * @author Ranjan Mohan
 *
 * v1.0.0
 *  Added support to perform the following operations:
 *          - Certain field regexs have been populated
 *          - Has calls to retrieve 1 or more matches
 *            from specified regex and input string
 *          - Added fillRandomValues() call which
 *            tries to generate a valid matching string
 *            from a supplied regex
 */
public class RegexUtility {

    public enum RegexField {
        DATE("regex.date"),
        DECIMAL("regex.number.decimal"),
        DOMAIN_NAME("regex.domain.name"),
        EMAIL_ADDRESS("regex.email.address"),
        INDIAN_PAN_CARD_NUMBER("regex.in.pan"),
        INDIAN_PASSPORT_NUMBER("regex.in.passport"),
        INDIAN_LICENSE_PLATE("regex.in.auto.license"),
        INTEGER("regex.number.integer"),
        IPV4_ADDRESS("regex.ip.v4.address"),
        IPV4_SUBNET_MASK("regex.ip.v4.subnet"),
        IPV6_ADDRESS("regex.ip.v6.address"),
        IPV6_SUBNET_MASK("regex.ip.v6.subnet"),
        LIKE_JSON("regex.json"),
        NATURAL_NUMBER("regex.number.natural"),
        TIMESTAMP("regex.timestamp"),
        URL("regex.domain.url"),
        US_LICENSE_PLATE("regex.us.auto.license"),
        US_PHONE_NUMBER("regex.us.phone.mobile"),
        US_SOCIAL_SECURITY_NUMBER("regex.us.ssn"),
        US_ZIP_CODE("regex.us.zipcode"),
        WHOLE_NUMBER("regex.number.whole"),
        XML("regex.xml");

        private String regex;
        RegexField(String regex) {
            this.regex = regex;
        }

        public String getRegex() {
            return regex;
        }
    }

    public static class InvalidRegexException extends Exception {

        private String regex;

        InvalidRegexException(String regex) {
            this.regex = regex;
        }

        @Override
        public String getMessage() {
            return String.format("The following regex is invalid: %s", regex);
        }
    }

    private static String wCharacters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890_";
    private static String dCharacters = "1234567890";
    /*
    TODO: Refine the dotCharacters character set
     */
    private static String dotCharacters = wCharacters;
    private static Random randomObj = new Random();

    /**
     * Checks if the specified pattern matches the given input.
     *
     * @param patternString  Pattern to match
     * @param patternOptions Pattern match flags
     * @param input          Input string
     * @return Returns true if the pattern matches the input, else false
     */
    public static boolean matches(String patternString, int patternOptions, String input) {
        return Pattern.compile(patternString, patternOptions)
                .matcher(input)
                .matches();
    }

    /**
     * Get the first N occurrences of the regex in the input string.
     *
     * @param patternString  Pattern to match.
     * @param patternOptions Regex flags.
     * @param N              -1 means get all matches, 0 means none.
     * @param input          Input string to find matches in.
     * @return List of matches where each match is a list of groups.
     */
    public static List<List<String>> getFirstNMatches(String patternString, int patternOptions, int N, String input) throws InvalidRegexException {

        // Ensure that the input regex is valid
        if(!isValidRegex(patternString)) {
            throw new InvalidRegexException(patternString);
        }

        Pattern patternObj = Pattern.compile(patternString, patternOptions);
        Matcher matcher = patternObj.matcher(input);
        List<List<String>> matches = new ArrayList<>();
        // If no matches are to be found. Not to be used.
        if (N <= 0 || !matcher.matches())
            return matches;
        int i = 0;
        while (matcher.find()) {
            matches.add(IntStream.range(0, matcher.groupCount() + 1)
                    .boxed()
                    .map(matcher::group)
                    .collect(Collectors.toList()));
            i++;
            if (i == N)
                break;
        }
        return matches;
    }

    /**
     * Get all the occurrences of the regex in the input string.
     *
     * @param patternString  Pattern to match.
     * @param patternOptions Regex flags.
     * @param input          Input string to find matches in.
     * @return List of matches where each match is a list of groups.
     */
    public static List<List<String>> getAllMatches(String patternString, int patternOptions, String input) throws InvalidRegexException {
        return getFirstNMatches(patternString, patternOptions, input.length(), input);
    }

//    /**
//     * Get the regex corresponding to the specified field.
//     *
//     * @param regexField Field whose regex needs to be returned
//     * @return Regex corresponding to the specified field. If the field is not found, a null is returned.
//     */
//    public static String getRegexFor(String regexField) {
//        return fieldRegexs.getOrDefault(regexField, null);
//    }

    /**
     * Get the regex fields that the provided input match.
     *
     * @param data Input string.
     * @return List of matching field names.
     */
    public static List<RegexField> probeRegexField(String data) {
        return Arrays.stream(RegexField.values())
                .parallel()
                .filter(regexField -> {
                    boolean result = RegexUtility.matches(regexField.getRegex(), 0, data);
                    System.out.println(regexField + " - <" + regexField.getRegex() + "> - " + result);
                    return result;
                })
                .collect(Collectors.toList());
    }

    private static String generateRandomCharacters(int length, String characterSet) {
        StringBuilder buffer = new StringBuilder();
        IntStream.range(0, length)
                .forEach(i -> buffer.append(
                        characterSet
                                .charAt(randomObj.nextInt(characterSet.length()))
                        )
                );
        return buffer.toString();
    }

    public static boolean isValidRegex(String regex) {
        boolean result;
        try {
            Pattern.compile(regex);
            result = true;
        }
        catch (PatternSyntaxException e) {
            result = false;
        }
        return result;
    }

    /**
     * Generate a string that matches the provided regex.
     *
     * @param regex             Regex to match.
     * @param randomValueLength Maximum length of value to insert in the generated string.
     * @return The generated string.
     */
    public static String fillRandomValues(String regex, int randomValueLength) throws InvalidRegexException {
        String result = regex;
        List<String> regexCharacterSets;

        /*
        TODO: Character sets
            - Add support for ranges Eg: a-z, 1-9
            - Add support for special characters Eg: $, & etc
        */

        // Ensure that the input regex is valid
        if(!isValidRegex(regex)) {
            throw new InvalidRegexException(regex);
        }

        //[]*
        regexCharacterSets = getAllMatches("\\[(.+)\\]\\*", 0, result)
                .stream()
                .map(matchList -> matchList.get(0))
                .distinct()
                .collect(Collectors.toList());
        for (String regexCharacterSet : regexCharacterSets) {
            result = result.replaceAll(String.format("[%s]*", regexCharacterSet),
                    fillRandomValues(regexCharacterSet, randomObj.nextInt(randomValueLength + 1)));
        }

        //[]+
        regexCharacterSets = getAllMatches("\\[(.+)\\]\\+", -1, result)
                .stream()
                .map(matchList -> matchList.get(0))
                .distinct()
                .collect(Collectors.toList());
        for (String regexCharacterSet : regexCharacterSets) {
            result = result.replaceAll(String.format("[%s]+", regexCharacterSet),
                    fillRandomValues(regexCharacterSet, randomValueLength));
        }

        //[]
        regexCharacterSets = getAllMatches("\\[(.*)\\]", -1, result)
                .stream()
                .map(matchList -> matchList.get(0))
                .distinct()
                .collect(Collectors.toList());
        for (String regexCharacterSet : regexCharacterSets) {
            result = result.replaceAll(String.format("[%s]", regexCharacterSet),
                    fillRandomValues(regexCharacterSet, 1));
        }

        result = result.replaceAll("\\.\\*", generateRandomCharacters(randomObj.nextInt(randomValueLength + 1), dotCharacters));
        result = result.replaceAll("\\.\\+", generateRandomCharacters(randomValueLength, dotCharacters));
        result = result.replaceAll("\\.", generateRandomCharacters(1, dotCharacters));


        result = result.replaceAll("\\\\w\\*", generateRandomCharacters(randomObj.nextInt(randomValueLength + 1), wCharacters));
        result = result.replaceAll("\\\\w\\+", generateRandomCharacters(randomValueLength, wCharacters));
        result = result.replaceAll("\\\\w", generateRandomCharacters(1, wCharacters));

        result = result.replaceAll("\\\\d\\*", generateRandomCharacters(randomObj.nextInt(randomValueLength + 1), dCharacters));
        result = result.replaceAll("\\\\d\\+", generateRandomCharacters(randomValueLength, dCharacters));
        result = result.replaceAll("\\\\d", generateRandomCharacters(1, dCharacters));

        result = result.replaceAll("\\\\s\\*", String.join("", Collections.nCopies(randomObj.nextInt(randomValueLength + 1), " ")));
        result = result.replaceAll("\\\\s\\+", String.join("", Collections.nCopies(randomValueLength, " ")));
        result = result.replaceAll("\\\\s", " ");

        return result;
    }

}
