import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Node {
	
	String name;
	LinkedList<Node> neighbors;

	public Node(String name) {
		this.name = name;
		neighbors = new LinkedList<>();
	}
	
	public void addEdge(Node to) {
		neighbors.add(to);
	}

	public boolean deleteEdge(Node node){
		return neighbors.remove(node);
	}
}


