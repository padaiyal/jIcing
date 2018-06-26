package datastructures.queue;
import datastructures.list.LinkedList;
import datastructures.list.Node;

public class Queue<T> extends LinkedList<T> {
	Node<T> last_node;
	public Queue() {
		super();
		last_node = null;
	}
	
	public void enqueue(T value) {
		if(first_node == null) {
			insert(value);
			last_node = first_node;
		}
		else {
			insert(value);
			last_node = last_node.getNextNode();
		}
	}
	
	public T dequeue() {
		T temp = first_node.getValue();
		removeValue(first_node.getValue());
		return temp;
	}
	
	public T peek() {
		return first_node.getValue();
	}
}
