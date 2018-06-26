import datastructures.list.DoublyLinkedList;
import datastructures.list.LinkedList;
import datastructures.list.List;
import datastructures.queue.PriorityQueue;
import datastructures.queue.Queue;
import datastructures.stack.Stack;
import datastructures.tree.*;
public class Main {
	public static void main(String ar[]){
		/*Tree<Integer> tr = new AVLTree<Integer>(100);
		tr.addChildNode(160);
		tr.addChildNode(10);
		tr.addChildNode(100);
		tr.addChildNode(70);
		tr.addChildNode(20);
		tr.addChildNode(16);
		tr.addChildNode(190);
		
		//tr.delete(10);
		
		tr.delete(900);
		*/
		//System.out.println("Total number of nodes - "+tr.size());
		//System.out.println("Height of tree - "+tr.getHeight());
		//for(Tree<Integer> val:tr.breadthFirstTraversal()) {
			//System.out.print(val.getValue()+", ");
		//}
		
		/*List<Integer> l= new DoublyLinkedList<Integer>();
		l.insert(23);
		l.insert(45);
		l.insert(33);
		l.insert(100);
		l.insert(0,123);
		*/
		//l.removeIndex(4);
		//l.removeValue(45);
		
		
		/*Stack<Integer> s = new Stack<Integer>();
		s.pop();
		s.push(22);
		s.push(33);
		s.push(45);
		s.pop();
		*/
		
		PriorityQueue<Double,Integer> q = new PriorityQueue<Double,Integer>(false);
		q.enqueue(33.9,3);
		q.enqueue(9.9,4);
		q.enqueue(100.9,3);
		q.enqueue(18.0,2);
		q.dequeue();
		q.enqueue(44.1,1);
		
		//l.insert(20,93);
	}
}
