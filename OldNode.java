import java.util.LinkedList;

public class OldNode implements Comparable<OldNode> {
	
	String name;
	LinkedList<OldNode> neighbors;
	boolean active;
	int id;

	public OldNode(String name) {
		this.name = name;
		neighbors = new LinkedList<>();
		this.id = -1; // is assigned later
		active = true;
	}

	
	public void addEdge(OldNode to) {
		neighbors.add(to);
	}

	public void deleteEdge(OldNode oldNode){
		 neighbors.remove(oldNode);
	}

	@Override
	public int compareTo(OldNode o) {
		return Integer.compare(o.neighbors.size(),this.neighbors.size());
	}
}


