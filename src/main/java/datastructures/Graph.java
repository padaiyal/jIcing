package datastructures;

import java.util.ArrayList;
import java.util.List;

public class Graph<T extends Object> {
	T value;
	List<Graph> neighbors;
	Integer neighbors_count;
	Graph(T value, int neighbors_count) {
		this.value = value;
		neighbors = new ArrayList<Graph>();
		this.neighbors_count = neighbors_count;
	}
	public Graph getNeighbor(int index) {
		if(index<neighbors_count)
			return neighbors.get(index);
		else {
			System.err.println("Neighbor not present!");
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
			System.err.println("Node Capacity Full, Cannot have more children!");
		}
			
	}
	public int getNeighborsCount() {
		return neighbors_count;
	}
}
