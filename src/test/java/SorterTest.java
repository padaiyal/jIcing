import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import sorting.Sorter;
import utilities.I18NUtility;

class SorterTest {

    void testArraySorting(Integer[] inputArray, Integer[] expectedOutputArray) {
        Sorter<Integer> sorter = new Sorter<>();
        Arrays.stream(Sorter.SortingTechnique.values()).parallel().forEach(sortingTechnique -> {
            Integer[] inputArrayDupe = (inputArray==null)?null:inputArray.clone();
            Integer[] actualOutputArray = sorter.sort(inputArrayDupe, sortingTechnique);
            Assertions.assertTrue(
                    Arrays.equals(expectedOutputArray, actualOutputArray),
                    I18NUtility.getFormattedString(
                            "test.SorterUtilityTest.sortingTechniqueFailedMessage",
                            sortingTechnique,
                            Arrays.toString(actualOutputArray)
                    )
            );
        });
    }

    @Test
    void testNonEmptyArraySorting() {
        Integer[] inputArray = IntStream.range(1, 129).map(i -> (i>64 & i<75)?70:i).boxed().sorted(Collections.reverseOrder()).toArray(Integer[]::new);
        List<Integer> expectedOutputList = Arrays.stream(inputArray).collect(Collectors.toList());
        Collections.reverse(expectedOutputList);
        Integer[] expectedOutputArray = expectedOutputList.stream().toArray(Integer[]::new);
        testArraySorting(inputArray, expectedOutputArray);
    }

    @Test
    void testEmptyArraySorting() {
        Integer[] inputArray = new Integer[]{};
        Integer[] expectedOutputArray = new Integer[]{};
        testArraySorting(inputArray, expectedOutputArray);
    }

    @Test
    void testNullArraySorting() {
        Integer[] inputArray = null;
        Integer[] expectedOutputArray = null;
        testArraySorting(inputArray, expectedOutputArray);
    }
}
