package datastructures.queue;

import datastructures.list.Node;

public class PriorityQueue<T1,T2 extends Comparable<T2>> extends Queue<T1>{
	boolean max_val_high_priority;
	public PriorityQueue(boolean max_val_high_priority) {
		super();
		this.max_val_high_priority = max_val_high_priority;
	}
	int comparePriority(PriorityNode<T1,T2> a, PriorityNode<T1,T2> b) {
		return a.getPriority().compareTo(b.getPriority());
	}
	public void enqueue(T1 value, T2 priority) {
		if(size==0) {
			insertFirstNode(value,priority);
		}
			
		else {
			for(int i=0;i<size;i++) {
				int priority_comparison = ((PriorityNode<T1,T2>)getNode(i)).priority.compareTo(priority);
				if((max_val_high_priority && priority_comparison < 0) || (!max_val_high_priority && priority_comparison > 0)) {
					insert(i, value, priority);
					break;
				}
				else if(i==size-1) {
					insertAfter(getNode(i), value, priority);
					break;
				}
					
			}
		}
	}
	
	public void insert(int index, T1 value, T2 priority) {
		Node<T1> temp=first_node,temp1;
		if(index<size) {
			for(int i=0;i<size;i++) {
				if(index == 0) {
					insertFirstNode(value, priority);
					break;
				}
				else if(i==index-1) {
					insertAfter(temp, value, priority);
					break;
				}
				temp = temp.getNextNode();
			}
		}
		else {
			System.err.println("Invalid index for insertion.");
		}
	}
	
	public void insertFirstNode(T1 value, T2 priority) {
		Node<T1> temp1 = first_node;
		first_node = new PriorityNode<T1,T2>(value,priority);
		first_node.setNextNode(temp1);
		size++;
	}
	
	public void insertAfter(Node<T1> node, T1 value, T2 priority) {
		Node<T1> temp1 = node.getNextNode();
		node.next_node = new PriorityNode<T1,T2>(value,priority);
		node.next_node.next_node = temp1;
		size++;
	}
	
}

class PriorityNode<T1,T2 extends Comparable<T2>> extends Node<T1>{
	T2 priority;
	PriorityNode(T1 value,T2 priority) {
		super(value);
		this.priority = priority;
	}
	T2 getPriority() {
		return priority; 
	}
}
