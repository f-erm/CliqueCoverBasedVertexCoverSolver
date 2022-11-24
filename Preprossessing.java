import java.util.LinkedList;

public class Preprossessing {
    /**
     * removes all nodes with degree one and their neighbour iteratively and
     * sets partialSolution to a list of all neighbours of the degree-one-nodes.
     * @param G graph
     */
    public static void removeDegreeOne(Graph G){
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
        int i = 0;
        LinkedList <Node> goodSolution = new LinkedList<>();
        for (OldNode oldNode : solution){
            oldNode.id = i;
            Node n = new Node(oldNode);
            goodSolution.add(n);
        }
        G.setPartialSolution(goodSolution);
    }

}
