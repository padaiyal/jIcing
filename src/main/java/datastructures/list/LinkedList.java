package datastructures.list;

public class LinkedList<T> implements List<T> {
	protected Node<T> first_node;
	protected int size;
	public LinkedList() {
		first_node = null;
		size = 0;
	}
	@Override
	public T getNodeValue(int index) {
		Node<T> temp = first_node;
		// TODO Auto-generated method stub
		for(int i=0;i<size;i++) {
			if(i==index)
				return temp.getValue();
			temp = temp.getNextNode();
		}
		System.err.println("Invalid Index");
		return null;
		
	}
	
	@Override
	public Node<T> getNode(int index) {
		Node<T> temp = first_node;
		// TODO Auto-generated method stub
		for(int i=0;i<size;i++) {
			if(i==index)
				return temp;
			temp = temp.getNextNode();
		}
		System.err.println("Invalid Index");
		return null;
		
	}
	@Override
	public void insert(int index, T value) {
		Node<T> temp=first_node,temp1;
		if(index<size) {
			for(int i=0;i<size;i++) {
				if(index == 0) {
					insertFirstNode(value);
					break;
				}
				else if(i==index-1) {
					insertAfter(temp, value);
					break;
				}
				temp = temp.getNextNode();
			}
		}
		else {
			System.err.println("Invalid index for insertion.");
		}
	}
	@Override
	public void insert(T value) {
		// TODO Auto-generated method stub
		if(first_node == null)
			insertFirstNode(value);
		//first_node = new Node(value);
		else {
			Node<T> temp = first_node;
			while(temp.getNextNode()!=null) {
				temp = temp.getNextNode();
			}
			//temp.next_node = new Node(value);
			insertAfter(temp, value);
		}
	}
	@Override
	public int size() {
		return size;
	}
	@Override
	public void removeValue(T value) {
		// TODO Auto-generated method stub
		Node<T> temp=first_node;
		for(int i=0;i<size;i++) {
			if(first_node.getValue() == value) {
				removeFirstNode();
				break;
			}
			else if(temp.getNextNode()!=null && temp.getNextNode().getValue() == value) {
				removeNextNode(temp);
				break;
			}
			temp = temp.getNextNode();
		}
		
	}
	@Override
	public void removeIndex(int index) {
		// TODO Auto-generated method stub
		Node<T> temp=first_node;
		if(index<size) {
			for(int i=0;i<size;i++) {
				if(index == 0) {
					removeFirstNode();
					break;
				}
				if(i==index-1) {
					removeNextNode(temp);
					break;
				}
				temp = temp.getNextNode();
			}
		}
		else {
			System.err.println("Invalid index for Deletion.");
		}
		
	}
	
	public void removeFirstNode() {
		Node<T> temp;
		temp=first_node;
		first_node = first_node.getNextNode();
		temp.setNextNode(null);
		size--;
	}
	
	public void removeNextNode(Node<T> temp) {
		Node<T> temp1;
		temp1 = temp.getNextNode();
		temp.setNextNode(temp.getNextNode().getNextNode());
		temp1.setNextNode(null);
		size--;
	}
	
	public void insertFirstNode(T value) {
		Node<T> temp1 = first_node;
		first_node = new Node<T>(value);
		first_node.setNextNode(temp1);
		size++;
	}
	
	public void insertAfter(Node<T> node, T value) {
		Node<T> temp1 = node.getNextNode();
		node.setNextNode(new Node<T>(value));
		node.next_node.setNextNode(temp1);
		size++;
	}
}