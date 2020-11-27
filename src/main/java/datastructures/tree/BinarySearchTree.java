package datastructures.tree;
import misc.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utilities.I18NUtility;

public class BinarySearchTree<T extends Comparable<T>> extends BinaryTree<T> {

	private final Logger logger = LogManager.getLogger(BinarySearchTree.class);

	public BinarySearchTree(T value) {
		super(value);
		// TODO Auto-generated constructor stub
	}
	public BinarySearchTree<T> getLeftSubTree() {
		return (BinarySearchTree<T>)children[0];
	}
	public BinarySearchTree<T> getRightSubTree() {
		return (BinarySearchTree<T>)children[1];
	}
	public void addChildNode(T child_value) {
		if(value.equals(child_value))
			logger.error(
					I18NUtility.getString("datastructures.BinarySearchTree.valueExistInBSTMessage")
			);
		else {
			if(TypeIndependentOperations.compare(child_value, value) == Comparison.LESSER) {
				if(children[0]==null) {
					children[0] = new BinarySearchTree<T>(child_value);
				}
				else
					children[0].addChildNode(child_value); //Add to LEFT
			}
			else if(TypeIndependentOperations.compare(child_value, value) == Comparison.GREATER) {
				if(children[1]==null) {
					children[1] = new BinarySearchTree<T>(child_value);
				}
				else
					children[1].addChildNode(child_value); //Add to RIGHT
			}
		}
	}
	public void delete(T node_value) {  
		
		if(TypeIndependentOperations.compare(value,node_value) == Comparison.EQUAL) {
			if(children[0]!=null) {
				value = children[0].max();
				if(children[0].children[1]!=null || children[0].children[0]!=null)
					((BinarySearchTree<T>)children[0]).delete(value);
				else {
					children[0]=null;
				}
			}
			else if(children[1]!=null) {
				value = children[1].min();
				if(children[1].children[1]!=null || children[1].children[0]!=null)
					children[1].delete(value);			
				else {
					children[1]=null;
				}
			}
			else {
				logger.error(
						I18NUtility.getFormattedString(
								"datastructures.BinarySearchTree.deleteLastElementInBSTMessage",
								node_value
						)
				);
			}
		}
		else if(TIO.compare(node_value,value) == Comparison.GREATER) {
			if(children[1]!=null) {
				if(children[1].value==node_value && children[1].children[1]==null && children[1].children[0]==null) {
					children[1]=null;
				}
				else
					children[1].delete(node_value);
			}
			else {
				logger.error(
						I18NUtility.getFormattedString(
								"datastructures.valueNotFoundMessage",
								node_value
						)
				);
			}
		}
		else if(TIO.compare(node_value,value) == Comparison.LESSER) {
			if(children[0]!=null) {
				if(children[0].value==node_value && children[0].children[1]==null && children[0].children[0]==null) {
					children[0]=null;
				}
				else
					children[0].delete(node_value);
			}
			else
				logger.error(
						I18NUtility.getFormattedString(
								"datastructures.valueNotFoundMessage",
								node_value
						)
				);
		}
		// fill in the code for delete

     }
	
	/*
	public Tree find(int n) {
		if(n==value)
			return this;
		else if(n>value) {
			if(right!=null)
				return right.find(n);
			else
				logger.error("Not found!");
		}
		else if(n<value) {
			if(left!=null)
				return left.find(n);
			else
				logger.error("Not found!");
		}
		return null;
	}
	*/
	
	public T max() {
		if(children[1]==null)
			return value;
		else
			return children[1].max();
	}
	
	public T min() {
		if(children[0]==null)
			return value;
		else
			return children[0].min();
	}
}
