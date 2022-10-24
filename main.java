import java.util.ArrayList;
import java.util.List;

public class Node {
	
	int value;
	List<Node> neighbors;

	public Node(int value) {
		this.value = value;
		neighbors = new ArrayList<>();
	}
	
	public void addEdge(Node to) {
		neighbors.add(to);
	}
}


class Main {
    public static void main(String[] args) {
		System.out.println("Hello, World!"); 
    }
}