package utilities;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

public class StringUtility {

    /**
     * Repeats the given string the specified number of times and returns it as a single string output.
     * @deprecated use {@link String#repeat(int)} instead.
     * @param str String too repeat
     * @param repeatCount Number of times to repeat str
     * @return The string with repeatCount contiguous occurrences of str.
     */
    @Deprecated
    public static String repeat(String str, long repeatCount) {
        if(repeatCount < 0) {
            throw new IllegalArgumentException(
                    I18NUtility.getFormattedString(
                            "utilities.StringUtility.repeatString.error.negativeRepeatCount",
                            repeatCount
                    )
            );
        }
        return LongStream.range(0, repeatCount)
                .mapToObj(i ->  str)
                .reduce("", (str1, str2) -> str1 + str2);
    }

    /**
     * Checks if the provided string is a substring of another
     * @param baseString The string to check in
     * @param subString The substring to check for
     * @return true if the substring is found in the base string, else false
     * @deprecated Use {@link String#contains(CharSequence)} instead.
     */
    @Deprecated
    public static boolean isSubString(String baseString, String subString) {
        Objects.requireNonNull(baseString);
        Objects.requireNonNull(subString);

        boolean result = false;
        if(subString.length() == 0) {
            result = true;
        }
        // Only if the substring is lesser in length than the base string does it
        // make sense to check further. Else, it's obvious that the provided substring
        // cannot be a substring of the provided base string.
        else if(subString.length() <= baseString.length()) {
            // Construct longest matching prefix/suffix map from the substring
            List<Integer> subStringLongestPrefixSuffixMap = IntStream.range(0, subString.length())
                .map(subStringIndex -> {
                    for(int i=subStringIndex - 1; i>0; i--) {
                        String prefix = subString.substring(0, i);
                        if(subString.substring(0, subStringIndex).endsWith(prefix)) {
                            return i;
                        }
                    }
                    return 0;
                })
                .boxed()
                .collect(Collectors.toList());

            int subsStringIndex = 0;
            for(int baseStringIndex=0; baseStringIndex<baseString.length(); baseStringIndex++) {
                if(baseString.charAt(baseStringIndex) != subString.charAt(subsStringIndex)) {
                    subsStringIndex = subStringLongestPrefixSuffixMap.get(subsStringIndex);
                    if(baseString.charAt(baseStringIndex) != subString.charAt(subsStringIndex)) {
                        subsStringIndex = 0;
                    }
                }
                if(baseString.charAt(baseStringIndex) == subString.charAt(subsStringIndex)) {
                    subsStringIndex++;
                }
                if(subString.length() == subsStringIndex) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    public static HashMap<Character, Long> getCharacterFrequencyDistribution(String str) {
        Objects.requireNonNull(str);

        Map<Character, Long> characterFrequencyDistribution = IntStream.range(0, str.length())
            .mapToObj(str::charAt)
            .collect(
                    Collectors.groupingBy(
                            token -> token,
                            Collectors.counting()
                    )
            );

        return new HashMap<>(characterFrequencyDistribution);
    }

    public static HashMap<String, Long> getWordFrequencyDistribution(String str, String ... delimiters) {
        Objects.requireNonNull(str);
        Objects.requireNonNull(delimiters);

        if(delimiters.length == 0) {
            throw new IllegalArgumentException(I18NUtility.getString("utilities.StringUtility.getWordFrequencyDistribution.error.noDelimitersSpecified"));
        }

        Map<String, Long> wordFrequencyDistribution = new HashMap<>();
        String delimiterRegex;
        if(delimiters.length == 1) {
            delimiterRegex = delimiters[0];
        }
        else {
            delimiterRegex = Arrays.stream(delimiters)
                    .map(delimiter -> String.format("(%s)", delimiter))
                    .reduce((d1, d2) -> d1 + "|" + d2)
                    .get();
        }
        String[] words = str.split(delimiterRegex);

        wordFrequencyDistribution = Arrays.stream(words)
            .collect(
                Collectors.groupingBy(
                        token -> token,
                        Collectors.counting()
                )
            );


        return new HashMap<>(wordFrequencyDistribution);
    }

    public enum StringSimilarityDistanceType {
        HAMMING,
        LEVENSHTEIN,
        JARO,
        JARO_WRINKLER,
        JACCARD_INDEX,
        SORENSEN_DICE
    }

    public static double getStringSimilarityDistance(StringSimilarityDistanceType stringSimilarityDistanceType, String str1, String str2) {
        Objects.requireNonNull(stringSimilarityDistanceType);
        Objects.requireNonNull(str1);
        Objects.requireNonNull(str2);

        double distance;
        switch(stringSimilarityDistanceType) {
            case HAMMING:
                distance = getHammingDistance(str1, str2);
                break;
            case LEVENSHTEIN:
                distance = getLevenshteinDistance(str1, str2);
                break;
            case JARO:
                distance = getJaroDistance(str1, str2);
                break;
            case JARO_WRINKLER:
                distance = getJaroWrinklerDistance(str1, str2, 0.1);
                break;
            case JACCARD_INDEX:
                distance = getJaccardIndexDistance(str1, str2, new String[]{" "});
                break;
            case SORENSEN_DICE:
                distance = getSorensenDiceDistance(str1, str2, new String[]{" "});
                break;
            default:
                throw new UnsupportedOperationException(I18NUtility.getString("utilities.StringUtility.getWordFrequencyDistribution.error.getStringSimilarityDistance"));
        }
        return  distance;
    }

    /**
     * Computes the hamming distance given two strings str1 and str2,
     * based on the following formula:
     *                      ┌
     *                      |   min(len(str1), len(str2))               if min(len(str1), len(str2)) = 0
     *                      |
     *                      |  ┌
     *                      |  |  hamm(
     *                      |  |     substr(str1, 1, len(str1)),
     *                      |  |     substr(str2, 1, len(str2))          otherwise
     *  hamm(str1, str2) =  |  |  )
     *                      | -|                +
     *                      |  |  ┌
     *                      |  |  |   1   if str1(0) ≠ str2(0)
     *                      |  | -|
     *                      |  |  |   0   otherwise
     *                      |  |  └
     *                      |  └
     *                      └
     * @param str1  String input
     * @param str2  String input
     * @return  The hamming distance between the two input strings
     */
    private static double getHammingDistance(String str1, String str2) {
        int hammingDistance = Math.abs(str1.length() - str2.length());
        hammingDistance += IntStream.range(0, Math.min(str1.length(), str2.length()))
                .filter(index -> str1.charAt(index) != str2.charAt(index))
                .count();
        return hammingDistance;
    }

    /**
     * Computes the levenshtein distance given two strings str1 and str2,
     * by first computing a distance matrix based on the following formula:
     *
     *                                      ┌
     *                                      |  ┌
     *                                      |  | lev(str1_index, str2_index - 1) + 1     if str2_index > 0
     *                                      | -|
     *                                      |  | ∞                                       otherwise
     *                                      |  └
     *                                      |
     *                                      |  ┌
     *                                      |  | lev(str1_index - 1, str2_index) + 1     if str1_index > 0
     *                                      | -|
     *                                      |  | ∞                                       otherwise
     *                                      |  └
     *                                      |  ┌
     *                                      |  |   ┌
     *  lev(str1_index, str2_index) = min - |  |   | lev(str1_index - 1, str2_index - 1)     if str1_index > 0
     *                                      |  |  -|                                         and str2_index > 0
     *                                      |  |   |
     *                                      |  |   | 0                                       if str1_index = 0
     *                                      |  |   |                                         and str2_index = 0
     *                                      |  |   |
     *                                      |  |   | ∞                                       otherwise
     *                                      |  |   └
     *                                      | -|                        +
     *                                      |  |   ┌
     *                                      |  |   | 1     if str1(str1_index) ≠ str2(str2_index)
     *                                      |  |  -|
     *                                      |  |   | 0     otherwise
     *                                      |  |   └
     *                                      |  └
     *                                      └
     *
     * Once the levenshtein distance matrix is computed, the last value in the matrix is the
     * Levenshtein distance between the two provided strings.
     * @param str1  String input
     * @param str2  String input
     * @return  The levenshtein distance between the two input strings
     */
    private static double getLevenshteinDistance(String str1, String str2) {
        int[][] distanceMatrix = new int[str1.length()][str2.length()];
        int diagonalDistance;
        int verticalDistance;
        int horizontalDistance;
        for(int i=0; i<str1.length(); i++) {
            for(int j=0; j<str2.length(); j++) {
                diagonalDistance = verticalDistance = horizontalDistance = Integer.MAX_VALUE;
                int characterMatchCount = (str1.charAt(i) == str2.charAt(j)) ? 0 : 1;
                if(i>0 && j>0) {
                    diagonalDistance = distanceMatrix[i - 1][j - 1] + characterMatchCount;
                }
                else if (i==0 && j==0){
                    diagonalDistance = characterMatchCount;
                }
                if(i>0) {
                    verticalDistance = distanceMatrix[i - 1][j] + 1;
                }
                if(j>0) {
                    horizontalDistance = distanceMatrix[i][j - 1] + 1;
                }
                distanceMatrix[i][j] = Math.min(Math.min(diagonalDistance, verticalDistance), horizontalDistance);
            }
        }
        for(int[] row:distanceMatrix) {
            System.out.println(Arrays.toString(row));
        }
        return distanceMatrix[str1.length() - 1][str2.length() - 1];
    }

    /**
     * TODO
     * @param str1
     * @param str2
     * @return
     */
    private static double getJaroDistance(String str1, String str2) {
        Objects.requireNonNull(str1);
        Objects.requireNonNull(str2);

        int matchingDistance = Math.max(str1.length(), str2.length())/2 - 1;
        HashMap<Integer, Boolean> indexesMatchedInStr2 = new HashMap<>();
        int numberOfMatchingCharacters = IntStream.range(0, str1.length())
            //.map(index -> { System.out.println("Index:" + index);return index;})
            .map(
                index -> IntStream.range(
                    Math.max(0, index - matchingDistance),
                    Math.min(
                        index + matchingDistance + 1,
                        Math.min(str1.length(), str2.length())
                    )
                )
                .filter(
                    window_index -> {
                        if(!indexesMatchedInStr2.containsKey(window_index)
                                && str2.charAt(window_index) == str1.charAt(index)) {
                            indexesMatchedInStr2.put(
                                    window_index,
                                    (window_index < str1.length())
                                            && (index < str2.length())
                                            && index != window_index
                                            && str2.charAt(index) == str1.charAt(window_index)
                            );
                            return true;
                        }
                        return false;
                    }
                )
                .findFirst()
                .isPresent()?1:0
            )
            .sum();
        int numberOfTranspositions = (int) indexesMatchedInStr2.values()
                .stream()
                .filter(doesTransposeMatch -> doesTransposeMatch)
                .count();

        double jaroSimilarity = 0.0D;

        if(numberOfMatchingCharacters != 0) {
            jaroSimilarity = (1.0/3.0)
                * (
                    (numberOfMatchingCharacters/(double)str1.length())
                    + (numberOfMatchingCharacters/(double)str2.length())
                    + (1 - (numberOfTranspositions/2.0)/(double)numberOfMatchingCharacters)
                );
        }

        return jaroSimilarity;
    }

    /**
     * TODO
     * @param str1
     * @param str2
     * @param scalingFactor
     * @return
     */
    private static double getJaroWrinklerDistance(String str1, String str2, double scalingFactor) {
        Objects.requireNonNull(str1);
        Objects.requireNonNull(str2);

        double jaroDistance = getJaroDistance(str1, str2);
        int longestMatchingPrefixLength = 0;
        for(int i=0; i<Math.min(str1.length(), str2.length()); i++) {
            if(str1.charAt(i) == str2.charAt(i)) {
                longestMatchingPrefixLength++;
            }
            else {
                break;
            }
        }
        return jaroDistance + longestMatchingPrefixLength * scalingFactor * (1 - jaroDistance);
    }

    /**
     * TODO
     * @param str1Tokens
     * @param str2Tokens
     * @return
     */
    private static long getCommonTokensCount(HashMap<String, Long> str1Tokens, HashMap<String, Long> str2Tokens) {
        return str1Tokens.keySet()
                .stream()
                .filter(str2Tokens::containsKey)
                .mapToLong(commonToken -> Math.min(
                        str1Tokens.get(commonToken),
                        str2Tokens.get(commonToken)
                        )
                )
                .sum();
    }

    /**
     * TODO
     * @param str1
     * @param str2
     * @param delimiters
     * @return
     */
    private static double getJaccardIndexDistance(String str1, String str2, String[] delimiters) {
        Objects.requireNonNull(str1);
        Objects.requireNonNull(str2);
        Objects.requireNonNull(delimiters);

        if(delimiters.length < 1) {
            // TODO
            throw new IllegalArgumentException("TODO");
        }

        HashMap<String, Long> str1Tokens =  getWordFrequencyDistribution(str1, delimiters);
        HashMap<String, Long> str2Tokens =  getWordFrequencyDistribution(str2, delimiters);
        long commonTokensCount = getCommonTokensCount(str1Tokens, str2Tokens);

        Set<String> totalUniqueTokens = new HashSet<>(str1Tokens.keySet());
        totalUniqueTokens.addAll(str2Tokens.keySet());
        int totalUniqueTokensCount = totalUniqueTokens.size();

        return commonTokensCount / (double) totalUniqueTokensCount;
    }

    public static double getSorensenDiceDistance(String str1, String str2, String[] delimiters) {
        Objects.requireNonNull(str1);
        Objects.requireNonNull(str2);
        Objects.requireNonNull(delimiters);

        if(delimiters.length < 1) {
            // TODO
            throw new IllegalArgumentException("TODO");
        }

        HashMap<String, Long> str1Tokens =  getWordFrequencyDistribution(str1, delimiters);
        HashMap<String, Long> str2Tokens =  getWordFrequencyDistribution(str2, delimiters);
        long commonTokensCount = getCommonTokensCount(str1Tokens, str2Tokens);
        return 2.0 * commonTokensCount / (double) (str1Tokens.size() + str2Tokens.size());
    }
}
