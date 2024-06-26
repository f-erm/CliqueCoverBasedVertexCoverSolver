import java.util.LinkedList;

public class Node implements Comparable<Node>, Cloneable{
    String name;
    int id;
    int[] neighbours;
    int[] neighbourPositions;
    boolean active;
    int activeNeighbours;
    LinkedList<Packing> affectedConstraints = new LinkedList<>();
    boolean inVC;
    LinkedList<int[]> mergeInfo = new LinkedList<>();

    /**
     * translates an old node to a new node.
     * @param oldNode corresponding old node from parsing.
     */
    public Node(OldNode oldNode){
        name = oldNode.name;
        active = true;
        id = oldNode.id;
        activeNeighbours = oldNode.neighbors.size();
        neighbours = new int[activeNeighbours];
        neighbourPositions = new int[activeNeighbours];
        int i = 0;
        for (OldNode n : oldNode.neighbors) {
            neighbours[i++] = n.id;
        }
    }

    /**
     * constructs a node independently from the OldNode class.
     * @param name name of the node
     * @param id id of the node
     * @param size size of the neighbour array
     */
    public Node (String name, int id, int size){
        this.name = name;
        active = true;
        this.id = id;
        this.neighbours = new int[size];
        inVC = false;
    }


    @Override
    public int compareTo(Node o) {
        return Integer.compare(o.activeNeighbours,this.activeNeighbours);
    }

    @Override
    public Object clone(){
        Node N = new Node(this.name,this.id,0);
        N.active = this.active;
        N.activeNeighbours = this.activeNeighbours;
        N.neighbours = this.neighbours.clone();
        N.neighbourPositions = this.neighbourPositions.clone();
        return N;

    }
}
