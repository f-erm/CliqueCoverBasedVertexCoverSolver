public class Node implements Comparable<Node>{
    String name;
    int id;
    int[][] neighbours;
    boolean active;
    int activeNeighbours;

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


    @Override
    public int compareTo(Node o) {
        return Integer.compare(o.activeNeighbours,this.activeNeighbours);
    }
}
