package datastructures.tree;
import java.util.ArrayList;
import java.util.List;

import misc.Comparison;
import misc.TypeIndependentOperations;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utilities.I18NUtility;

public class AVLTree<T extends Comparable<T>> extends BinarySearchTree<T>{

	List<T> sorted_node_values;
	private final Logger logger = LogManager.getLogger(AVLTree.class);

	public AVLTree(T value) {
		super(value);
		sorted_node_values = new ArrayList<T>();
		sorted_node_values.add(value);
		// TODO Auto-generated constructor stub
	}
	
	public void delete(T node_value) {  
		if(find(node_value)==-1)
			logger.error(
					I18NUtility.getFormattedString(
							"datastructures.valueNotFoundMessage",
							node_value
					)
			);
		else {
			sorted_node_values.remove(find(node_value));
			buildAVLTreeFromSortedList(sorted_node_values);
		}
	}
	
	public void addChildNode(T child_value) {
		if(getInsertionIndex(child_value)==-1)
			logger.debug(
					I18NUtility.getString("datastructures.valueAlreadyPresentMessage")
			);
		else {
			sorted_node_values.add(getInsertionIndex(child_value), child_value);
			buildAVLTreeFromSortedList(sorted_node_values);
		}
		//Should insert and balance
	}
	public void buildAVLTreeFromSortedList(List<T> sorted_node_values) {
		List<T> sorted_node_values_temp = new ArrayList<T>(this.sorted_node_values);
		int centre_index = (int)Math.floor(sorted_node_values_temp.size()/2);
		value = sorted_node_values_temp.get(centre_index);
		children = new Tree[2];
		sorted_node_values_temp.remove(centre_index);
		//buildBalancedTree(sorted_node_values_temp);
		buildBalancedTree(0,centre_index);
		buildBalancedTree(centre_index+1,sorted_node_values.size()-1);
	}
	public void buildBalancedTree(int start_index,int end_index) {
		logger.debug(
				I18NUtility.getFormattedString(
						"datastructures.AVLTree.createBalancedTreeWithIndexesMessage",
						start_index,
						end_index
				)
		);
		if(start_index == end_index) {
			super.addChildNode(sorted_node_values.get(start_index));
		}
		else if(end_index-start_index == 1) {
			super.addChildNode(sorted_node_values.get(start_index));
			super.addChildNode(sorted_node_values.get(end_index));
		}
		else if(start_index < end_index){
			int centre_index = (int)((start_index + end_index)/2);
			super.addChildNode(sorted_node_values.get(centre_index));
			buildBalancedTree(start_index, centre_index);
			buildBalancedTree(centre_index+1, end_index);
		}
		else {
			logger.error(
					I18NUtility.getFormattedString(
							"datastructures.AVLTree.failedToCreateBalancedTreeMessage",
							start_index,
							end_index
					)
			);
		}
	}
	/*public void buildBalancedTree(List<T> sorted_node_values_temp) {
		logger.info("Building Balanced Tree - "+sorted_node_values_temp.toString());
		if(sorted_node_values_temp.size()>2) {
			int centre_index = (int)Math.floor(sorted_node_values_temp.size()/2),
					first_subset_center_index = (int)Math.floor(centre_index/2),
					second_subset_center_index = (int)Math.floor((centre_index+sorted_node_values_temp.size())/2);
			super.addChildNode(sorted_node_values_temp.get(centre_index));
			super.addChildNode(sorted_node_values_temp.get(first_subset_center_index));
			super.addChildNode(sorted_node_values_temp.get(second_subset_center_index));
			sorted_node_values_temp.remove(second_subset_center_index);
			sorted_node_values_temp.remove(centre_index);
			sorted_node_values_temp.remove(first_subset_center_index);
			buildBalancedTree(sorted_node_values_temp);
		}
		else {
			for(T node:sorted_node_values_temp) {
				logger.info("Adding "+node.toString()+" to the AVL Tree..");
				logger.info("Length of Array - "+children.length);
				super.addChildNode(node);
			}
			sorted_node_values_temp.clear();
		}
	}*/
	public int getInsertionIndex(T value) {
		for(int i=0;i<sorted_node_values.size();i++) {
			if(TypeIndependentOperations.compare(sorted_node_values.get(i),value) == Comparison.EQUAL) {
				return -1;
			}
			if(TypeIndependentOperations.compare(sorted_node_values.get(i),value) == Comparison.GREATER) {
				return i;
			}
		}
		return sorted_node_values.size();
	}
	
	public int find(T value) {
		for(int i=0;i<sorted_node_values.size();i++) {
			if(TypeIndependentOperations.compare(sorted_node_values.get(i),value) == Comparison.EQUAL) {
				return i;
			}
		}
		return -1;
	}
}
