package datastructures.list;

public interface List<T> {
	public T getNodeValue(int index);
	public Node<T> getNode(int index);
	public void insert(T value);
	public void insert(int index, T value);
	public void removeValue(T value);
	public void removeIndex(int index);
	public int size();
}
