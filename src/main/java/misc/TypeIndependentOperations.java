package misc;
import datastructures.tree.NodeTemplate;
public class TypeIndependentOperations<T> {
	public static <T> Comparison compare(T value1,T value2) {
		if(value1.getClass().equals(Byte.class)
		 ||value1.getClass().equals(Short.class)
		 ||value1.getClass().equals(Character.class)
		 ||value1.getClass().equals(Integer.class)
		 ||value1.getClass().equals(Long.class))
			return (Long.parseLong(value1.toString())>Long.parseLong(value2.toString()))?Comparison.GREATER:(Long.parseLong(value1.toString())==Long.parseLong(value2.toString()))?Comparison.EQUAL:Comparison.LESSER;
		else if(value1.getClass().equals(Float.class)
			  ||value1.getClass().equals(Double.class))
			return ((Double)value1>(Double)value2)?Comparison.GREATER:((Double)value1==(Double)value2)?Comparison.EQUAL:Comparison.LESSER;
		else if(value1.getClass().equals(Boolean.class))
			return ((Boolean)value1==(Boolean)value2)?Comparison.EQUAL:Comparison.UNEQUAL;
		else if(value1.getClass().equals(String.class)) {
			int temp = ((String)value1).compareTo((String)value2);
			if(temp==0)
				return Comparison.EQUAL;
			else if(temp>0)
				return Comparison.GREATER;
			else
				return Comparison.LESSER;
		}
		else if(value1 instanceof NodeTemplate && value2 instanceof NodeTemplate)
			return ((NodeTemplate)value1).compare((NodeTemplate)value1,(NodeTemplate)value2);
		else
			throw new UnsupportedOperationException();
	}
}


