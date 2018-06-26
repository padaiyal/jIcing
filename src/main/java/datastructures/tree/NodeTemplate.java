package datastructures.tree;
import misc.Comparison;
public interface NodeTemplate<T> {
	public Comparison compare(T obj1, T obj2);
}
