package datastructures.stack;

import datastructures.list.LinkedList;
import datastructures.list.Node;

public class Stack<T> extends LinkedList<T> {
	Node<T> last_node;
	public Stack() {
		super();
	}
	
	public void push(T value) {
		if(first_node == null) {
			insert(value);
			last_node = first_node;
		}
		else {
			insert(value);
			last_node = last_node.getNextNode();
		}
	}
	
	public T pop() {
		if(last_node == null) {
			System.err.println("Empty Stack, Nothing to pop!");
			return null;
		}
		else {
			T temp = last_node.getValue();
			removeValue(last_node.getValue()); // WHAT IF THERE ARE MULTIPLE NODES WITH THE SAME VALUE???
			last_node = getNode(size()-1);
			return temp;
		}
	}
	
	public T peek() {
		return last_node.getValue();
	}
}
