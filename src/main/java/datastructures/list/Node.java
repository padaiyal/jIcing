package datastructures.list;

public class Node<T> {
	T value;
	public Node<T> next_node;
	public Node(T value) {
		this.value = value;
	}
	public Node<T> getNextNode() {
		return next_node;
	}
	public T getValue() {
		return value;
	}
	public void setNextNode(Node<T> next_node) {
		this.next_node = next_node;
	}
}
