package sorting;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class Sorter<T extends Object> {
	
	int[] selectionSort(int input[]) {
		for(int i=0;i<input.length;i++) {
			for(int j=i+1;j<input.length;j++) {
				if(input[i]>input[j]) {
					//Swap both elements
					input[i] = input[i] + input[j];
					input[j] = input[i] - input[j];
					input[i] = input[i] - input[j];
				}
			}
		}
		return input;
	}
	
	int[] bubbleSort(int input[]) {
		for(int i=0;i<input.length;i++) {
			for(int j=0;j<input.length-1;j++) {
				if(input[j]>input[j+1]) {
					//Swap both elements
					input[j] = input[j] + input[j+1];
					input[j+1] = input[j] - input[j+1];
					input[j] = input[j] - input[j+1];
				}
			}
		}
		return input;
	}
	
	int[] insertionSort(int input[]) {
		List<Integer> values = toList(input);
		int temp;
		for(int i=0;i<values.size();i++) {
			for(int j=i+1;j<values.size();j++) {
				if(values.get(i)>values.get(j)) {
					temp = values.get(j);
					values.remove(j);
					values.add(i, temp);
				}
			}
		}
		return convertToIntArray(values.toArray()); 
	}
	
	int[] quickSort(int input[]) {
		if(input.length<=1)
			return input;
		int l=input.length;
		int pivot = input[l-1];
		List<Integer> left = new LinkedList<Integer>();
		List<Integer> right = new LinkedList<Integer>();
		for(int i=0;i<l-1;i++) {
			if(input[i]>pivot)
				right.add(input[i]);
			else
				left.add(input[i]);
		}
		return joinArrays(quickSort(convertToIntArray(left.toArray())), joinArrays(new int[]{pivot},quickSort(convertToIntArray(right.toArray()))));
	}
	
	private int[] convertToIntArray(Object[] inp) {
		int result[] = new int[inp.length];
		for(int i=0;i<inp.length;i++) {
			result[i] = Integer.parseInt(inp[i].toString());
		}
		return result;
	}
	
	
	private int[] joinArrays(int p[],int q[]) {
		int[] result = new int[p.length+q.length];
		int max_len = p.length>q.length?p.length:q.length;
		for(int i=0;i<max_len;i++) {
			if(i<p.length)
				result[i] = p[i];
			if(i<q.length)
				result[p.length+i] = q[i];
		}
		return result;
	}
	
	public int[] customSort(int[] input) {
		Map<Integer,Integer> sorter = new TreeMap<Integer,Integer>();//Using TreeMap because it iterates in order
		int result[] = new int[input.length];
		for(int num:input) { //m
			if(sorter.containsKey(num)) { //logn
				sorter.put(num, sorter.get(num)+1); //logn
			}
			else
				sorter.put(num, 1); //logn
		}
		int j=0;
		for(Map.Entry<Integer, Integer> entry:sorter.entrySet()) {
			for(int i=0;i<entry.getValue();i++) {
				result[j++] = entry.getKey(); //n
			}
		}
		return result;
	}
	
	private List<Integer> toList(int values[]) {
		List<Integer> result = new ArrayList<Integer>();
		for(int value:values) {
			result.add(value);
		}
		return result;
	}
	
	public static void main(String ar[]) {
		int a[] = new int[] {324,344,56,154,6,4,234,7,2,4}; 
		Sorter obj = new Sorter();
		System.out.println(Arrays.toString(obj.bubbleSort(a)));
	}
	
}
