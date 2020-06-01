package sorting;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.IntStream;

public class Sorter<T extends Comparable<T>> {
    /**
     * TODO:
     * Quick Sort
     * Heap Sort
     * Radix Sort
     * Bucket Sort
     * Comb Sort
     * Pigeonhole Sort
     * Cocktail Sort
     * Strand Sort
     * Bitonic Sort
     * Gnome Sort
     * Tree Sort
     * Sleep Sort
     * Stooge Sort
     * Bogo Sort
     * Pancake Sorting
     * Tag Sort
     */

    public enum SortingTechnique {
        SELECTION_SORT,
        BUBBLE_SORT,
        INSERTION_SORT,
        // QUICK_SORT,
        HEAP_SORT,
        TIM_SORT,
        COUNTING_SORT,
        SHELL_SORT,
        CYCLE_SORT
    }

    public T[] sort(T[] input, SortingTechnique sortingTechnique) {
        switch (sortingTechnique) {
            case SELECTION_SORT -> inPlaceSelectionSort(input);
            case BUBBLE_SORT -> inPlaceBubbleSort(input);
            case INSERTION_SORT -> inPlaceInsertionSort(input);
            case TIM_SORT -> timSort(input);
            case SHELL_SORT, CYCLE_SORT -> inPlaceShellSort(input);
            // case QUICK_SORT -> return quickSort(input);
            default -> inPlaceCountingSort(input);
        }
        return input;
    }

    private void inPlaceSelectionSort(T[] input) {
        if(input != null && input.length > 1) {
            for (int i = 0; i < input.length; i++) {
                for (int j = i + 1; j < input.length; j++) {
                    if (input[i].compareTo(input[j]) > 0) {
                        //Swap both elements
                        swap(input, i, j);
                    }
                }
            }
        }
    }

    private int findPositionInSortedArray(T[] array, T value) {
        return (int) Arrays.stream(array)
                .parallel()
                .filter(element -> element.compareTo(value) < 0)
                .count();
    }

    private void inPlaceCycleSort(T[] array) {
        if(array!=null && array.length > 1) {
            T cursor_element = array[0];
            int count = 0;
            while (count < array.length) {
                int to_index = findPositionInSortedArray(array, cursor_element);
                T temp = cursor_element;
                cursor_element = array[to_index];
                array[to_index] = temp;
                count++;
            }
        }
    }

    private void inPlaceBubbleSort(T[] input) {
        if(input!=null && input.length > 1) {
            T temp;
            for (int i = 0; i < input.length; i++) {
                for (int j = 0; j < input.length - 1; j++) {
                    if (input[j].compareTo(input[j + 1]) > 0) {
                        //Swap both elements
                        temp = input[j];
                        input[j] = input[j + 1];
                        input[j + 1] = temp;
                    }
                }
            }
        }
    }

    private T[] binaryInsertion(T[] array, T element) {
        return null;
    }

    private void inPlaceShellSort(T[] array) {
        if(array!=null && array.length > 1) {
            int gap = array.length / 2;
            while (gap > 0) {
                int finalGap = gap;
                IntStream.range(gap, array.length)
                        .parallel()
                        .filter(index -> array[index - finalGap].compareTo(array[index]) > 0)
                        .forEach(index -> swap(array, index - finalGap, index));
                gap /= 2;
            }
        }
    }

    private void inPlaceInsertionSort(T[] input) {
        if(input!=null && input.length > 1) {
            inPlaceInsertionSort(input, 0, input.length);
        }
    }

    private void inPlaceInsertionSort(T[] input, int s, int e) {
        if(input!=null && input.length > 1) {
            for (int i = s + 1; i < e; i++) {
                for (int j = i; j >= s; j--) {
                    if (j==s || input[i].compareTo(input[j - 1]) > 0) {
                        inPlaceMoveArrayElement(input, i, j);
                    }
                }
            }
        }
    }

    private ConcurrentLinkedQueue<T> quickSort(ConcurrentLinkedQueue<T> input) {
        if (input.size() <= 1)
            return input;
        T pivot = input.peek();
        ConcurrentLinkedQueue<T> left = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<T> right = new ConcurrentLinkedQueue<>();
        input.parallelStream().forEach(element -> {
            if (element.compareTo(pivot) > 0)
                right.add(element);
            else
                left.add(element);
        });
        ConcurrentLinkedQueue<T> result = quickSort(left);
        result.add(pivot);
        result.addAll(quickSort(right));
        return result;
    }

    private T[] quickSort(T[] input) {
        T[] result;
        if (input.length <= 1)
            result = input.clone();
        else {
            ConcurrentLinkedQueue<T> queue = new ConcurrentLinkedQueue<>();
            Arrays.stream(input)
                    .parallel()
                    .forEach(queue::add);
            result = (T[]) queue.toArray();
        }
        return result;
    }

    private void inPlaceTimSort(T[] input, int startIndex, int runSize) {
        if(input != null && input.length > 1) {
            if ((startIndex + runSize) > input.length) {
                // In-place insertion sort
                inPlaceInsertionSort(input, startIndex, input.length);
            } else {
                inPlaceInsertionSort(input, startIndex, startIndex + runSize);
                inPlaceTimSort(input, startIndex + runSize, runSize);
                inPlaceMergeSort(input, startIndex, startIndex + runSize, startIndex + runSize, input.length);
            }
        }
    }

    private void timSort(T[] input) {
        if(input != null && input.length > 1) {
            int runSize = 64;
            inPlaceTimSort(input, 0, runSize);
        }
    }

    private void inPlaceMoveArrayElement(T[] array, int from, int to) {
        T temp = array[from];
        if(to != from) {
            if (to < from) {
                for (int i = from; i > to; i--) {
                    array[i] = array[i - 1];
                }
                array[to] = temp;
            }
            else {
                for(int i=from; i < to; i++) {
                    array[i] = array[i+1];
                }
                array[to] = temp;
            }
        }
    }

    /**
     * @param array
     * @param s1
     * @param e1    End of the first index range (exclusive)
     * @param s2
     * @param e2    End of the second index range (exclusive)
     * @throws Exception
     */
    private void inPlaceMergeSort(T[] array, int s1, int e1, int s2, int e2) throws NullPointerException {
        /*
		Pre-conditions: Arrays have to be sorted.
		(s1, e1) and (s2, e2) shouldn't overlap.
		(s1, e1) and (s2, e2) should be such that s1 <= e1 <= s2 <=e2
		 */
        if(array != null && array.length > 1 && ((e1 - s1) > 0) && ((e2 - s2) > 0)) {
            int i1 = s1, i2 = s2;
            while (i1 < array.length && i2 < array.length) {
                if (array[i1].compareTo(array[i2]) > 0) {
                    inPlaceMoveArrayElement(array, i2, i1);
                    i1++;
                    i2++;
                } else {
                    i1++;
                }
            }
        }
    }

    private void swap(T[] array, int i1, int i2) {
        T temp = array[i1];
        array[i1] = array[i2];
        array[i2] = temp;
    }

    private <T extends Comparable<T>> List<T> toList(T[] values) {
        List<T> result = new ArrayList<>();
        Collections.addAll(result, values);
        return result;
    }

    private T[] joinArrays(T p[], T q[]) {
        if (p.length == 0)
            return q.clone();
        if (q.length == 0)
            return p.clone();
        else {
            T[] result = (T[]) Array.newInstance(p[0].getClass(), p.length + q.length);
            int max_len = p.length > q.length ? p.length : q.length;
            for (int i = 0; i < max_len; i++) {
                if (i < p.length)
                    result[i] = p[i];
                if (i < q.length)
                    result[p.length + i] = q[i];
            }
            return result;
        }
    }

    private void inPlaceCountingSort(T[] input) {
        if(input != null && input.length > 1) {
            Map<T, Integer> sorter = new TreeMap<>();//Using TreeMap because it iterates in order
            for (T num : input) { //m
                if (sorter.containsKey(num)) { //logn
                    sorter.put(num, sorter.get(num) + 1); //logn
                } else
                    sorter.put(num, 1); //logn
            }
            int j = 0;
            for (Map.Entry<T, Integer> entry : sorter.entrySet()) {
                for (int i = 0; i < entry.getValue(); i++) {
                    input[j++] = entry.getKey(); //n
                }
            }
        }
    }
}
