package datastructures.list;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utilities.I18NUtility;

public class DoublyLinkedList<T> extends LinkedList<T> {

	private final Logger logger = LogManager.getLogger(DoublyLinkedList.class);

	public DoublyLinkedList() {
		// TODO Auto-generated constructor stub
	}
	public void removeNextNode(Node<T> temp) {
		Node<T> temp1;
		logger.debug(
				I18NUtility.getFormattedString(
						"datastructures.Node.removeNodeInfo",
						temp.value
				)
		);
		((DoublyLinkedNode<T>)(temp.getNextNode())).previous_node = null;
		temp1 = temp.getNextNode();
		temp.setNextNode(temp.getNextNode().getNextNode());
		temp1.setNextNode(null);
		if(temp.getNextNode()!=null)
			((DoublyLinkedNode<T>)(temp.getNextNode())).previous_node = temp;
		size--;
	}
	
	public void insertFirstNode(T value) {
		Node<T> temp1 = first_node;
		first_node = new DoublyLinkedNode<T>(value);
		first_node.setNextNode(temp1);
		if(first_node.getNextNode()!=null)
			((DoublyLinkedNode<T>)first_node.getNextNode()).previous_node = first_node;
		size++;
	}
	
	public void insertAfter(Node<T> node, T value) {
		Node<T> temp1 = node.getNextNode();
		node.setNextNode(new DoublyLinkedNode<T>(value));
		node.next_node.setNextNode(temp1);
		((DoublyLinkedNode<T>)(node.getNextNode())).previous_node = node;
		if(node.getNextNode().getNextNode()!=null)
			((DoublyLinkedNode<T>)(node.getNextNode().getNextNode())).previous_node = node.getNextNode();
		size++;
	}
}

class DoublyLinkedNode<T> extends Node<T> {
	Node<T> previous_node;
	DoublyLinkedNode(T value) {
		super(value);
		// TODO Auto-generated constructor stub
	}	
	public Node<T> getPreviousNode() {
		return previous_node;
	}
	
}