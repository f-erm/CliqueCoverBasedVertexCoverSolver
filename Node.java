import java.util.ArrayList;
import java.util.List;

public class Node {
	
	String name;
	List<Node> neighbors;

	public Node(String name) {
		this.name = name;
		neighbors = new ArrayList<>();
	}
	
	public void addEdge(Node to) {
		neighbors.add(to);
	}

	public boolean deleteEdge(Node node){
		return neighbors.remove(node);
	}
}


