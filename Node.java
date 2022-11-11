public class Node implements Comparable<Node>, Cloneable{
    String name;
    int id;
    int[][] neighbours;//neighbours[0] is id of all the neighbors of this node,  neighburs[1] is index in the given neighboor's array where our current node can be found.
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

    @Override
    public Object clone(){ //to be tested!
        Node N = new Node(this.name,this.id,this.size);
        N.active = this.active;
        N.activeNeighbours = this.activeNeighbours;
        N.neighbours = java.util.Arrays.stream(this.neighbours).map(el -> el.clone()).toArray($ -> this.neighbours.clone());
        return N;

    }
}
