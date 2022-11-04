import java.util.LinkedList;

public class Node implements Comparable<Node> {
	
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

	@Override
	public int compareTo(Node o) {
		return Integer.compare(o.neighbors.size(),this.neighbors.size());
	}
}


