package utilities;

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

}
