import java.util.LinkedList;

public class Preprossessing {
    /**
     * removes all nodes with degree one and their neighbour iteratively and
     * sets partialSolution to a list of all neighbours of the degree-one-nodes.
     * @param G graph
     */

    public static LinkedList<Node> doAllThePrep(Graph G){
        LinkedList<OldNode> oldSolution = removeDegreeOne(G);
        removeDegreeZero(G);
        int i = 0;
        LinkedList <Node> goodSolution = new LinkedList<>();
        for (OldNode oldNode : oldSolution){
            oldNode.id = i;
            Node n = new Node(oldNode);
            goodSolution.add(n);
            i ++;
        }
        return goodSolution;
    }

    public static LinkedList<OldNode> removeDegreeOne(Graph G){
        boolean changed = true;
        LinkedList<OldNode> solution = new LinkedList<>();
        while (changed){
            changed = false;
            for (OldNode oldNode : G.oldNodeList) {
                if(oldNode.neighbors.size()==1){
                    solution.add(oldNode.neighbors.get(0));
                    G.removeNode(oldNode.neighbors.get(0));
                    G.removeNode(oldNode);
                    changed = true;
                    break;
                }
            }
        }
        return solution;
    }

    private static void removeDegreeZero(Graph G){
        LinkedList<OldNode> toDelete = new LinkedList<>();
        for (OldNode node : G.oldNodeList) {
            if(node.neighbors.size() == 0 && node.active){
                toDelete.add(node);
            }
        }
        for (OldNode node: toDelete) {
            G.removeNode(node);
        }
    }
}
