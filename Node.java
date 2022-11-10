public class Node implements Comparable<Node>{
    String name;
    int id;
    int[][] neighbours;
    boolean active;
    int activeNeighbours;

    /**
     * translates an old node to a new node.
     * @param oldNode corresponding old node from parsing.
     */
    public Node(OldNode oldNode){
        name = oldNode.name;
        active = true;
        id = oldNode.id;
        activeNeighbours = oldNode.neighbors.size();
        neighbours = new int[activeNeighbours][2];
        int i = 0;
        for (OldNode n : oldNode.neighbors) {
            neighbours[i][0] = n.id;
            neighbours[i++][1] = n.neighbors.indexOf(oldNode);
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
        this.neighbours = new int[size][2];
    }


    @Override
    public int compareTo(Node o) {
        return Integer.compare(o.activeNeighbours,this.activeNeighbours);
    }
}
