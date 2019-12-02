package datastructures.tree;
import misc.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Tree<T extends Comparable<T>> {
	T value;
	Tree<T>[] children;
	int children_count,current_index;
	boolean tree_changed;
	TypeIndependentOperations<T> TIO;
	int tree_nodes_count;
	Tree(T value, int children_count) {
		this.value = value;
		children = new Tree[children_count];
		for(int i=0;i<children_count;i++)
			children[i]=null;
		current_index = 0;
		TIO = new TypeIndependentOperations<T>();
		this.children_count = children_count;
		tree_changed=false;
		tree_nodes_count=1;
	}
	public T getValue() {
		return value;
	}
	public Tree getChildNode(int index) {
		return children[index];
	}
	public Tree[] getChildren() {
		List<Tree<T>> children_list = new ArrayList<Tree<T>>(Arrays.asList(children));
		children_list.removeAll(Collections.singleton(null));
		return children_list.toArray(new Tree[children_list.size()]);
	}
	public Tree[] getChildrenWithPosition() {
		return children;
	}
	public void addChildNode(T child) {
		if(children.length<children_count) {
			children[current_index]=new Tree(child, children_count);
			tree_changed=true;
		}
		else {
			System.err.println("Node Capacity Full, Cannot have more children!");
		}
			
	}
	public int getChildrenCount() {
		if(tree_changed) {
			tree_nodes_count = getChildren().length;
			tree_changed=false;
		}
		return tree_nodes_count;
	}
	public void delete(T child_value) {
		removeLeaf(child_value);
	}
	public boolean removeLeaf(T child_value) {
		
		for(Tree child:children) {
			if(child.value == child_value && child.getChildrenCount()==0) {
				child=null;
				tree_changed=true;
				return true;
			}
			else {
				if(child.removeLeaf(child_value))
					return true;
			}
		}
		return false;
	}
	public void removeAllLeaves(T child_value) {
		while(removeLeaf(child_value));
	}
	public T min() {
			return null;
	}
	public T max() {
			return null;
	}
	public int getHeight() {
		int subtree_heights[] = new int[children_count];
		for(int i=0;i<children_count;i++) {
			if(children[i]!=null) {
				subtree_heights[i] = children[i].getHeight();
			}
			else 
				subtree_heights[i] = 0;
		}
		int max_subtree_height = Arrays.stream(subtree_heights).max().getAsInt();
		if(this.getChildrenCount()==0)
			return max_subtree_height;
		else 
			return max_subtree_height+1;
	}
	
	public int size() {
		return breadthFirstTraversal().size();
	}
	
	public List<Tree<T>> breadthFirstTraversal() {
		List<Tree<T>> bfs = new ArrayList<Tree<T>>();
		int head=0;
		bfs.add(this);
		while(head<(bfs.size())) {
			if(bfs.get(head).getChildren()!=null) {
				
				for(Tree<T> child:bfs.get(head).getChildren()) {
					bfs.add(child);
				
				}
			}
			head++;
			
		}
		return bfs;
	}
	
}