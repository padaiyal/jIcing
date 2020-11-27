package datastructures;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utilities.I18NUtility;

import java.util.ArrayList;
import java.util.List;

public class Graph<T extends Object> {

	T value;
	List<Graph> neighbors;
	Integer neighbors_count;
	private final Logger logger = LogManager.getLogger(Graph.class);

	Graph(T value, int neighbors_count) {
		this.value = value;
		neighbors = new ArrayList<Graph>();
		this.neighbors_count = neighbors_count;
	}
	public Graph getNeighbor(int index) {
		if(index<neighbors_count)
			return neighbors.get(index);
		else {
			logger.error(
					I18NUtility.getString("datastructures.Graph.neighborNotFoundErrorMessage")
			);
			return null;
		}
			
	}
	public List<Graph> getNeighbors() {
		return neighbors;
	}
	public void addNeighbor(Graph neighbor) {
		if(neighbors.size()<neighbors_count) {
			neighbors.add(new Graph(neighbor, neighbors_count));
		}
		else {
			logger.error(
					I18NUtility.getString("datastructures.Tree.maxNumberOfChildrenNodesExceededMessage")
			);
		}
			
	}
	public int getNeighborsCount() {
		return neighbors_count;
	}
}
