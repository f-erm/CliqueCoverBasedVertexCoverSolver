import java.util.LinkedList;
import java.util.Stack;

public class Preprossessing {
    /**
     * removes all nodes with degree one and their neighbour iteratively and
     * sets partialSolution to a list of all neighbours of the degree-one-nodes.
     * The idea is to reduce the size of the data structures encoding the graph
     * before applying the more complicated reduction rules (where deleting a
     * node will only result in setting the node to inactive and not in deleting
     * in from the data structure, as here).
     * @param G graph
     */

    public static LinkedList<Node> doAllThePrep(Graph G){
        LinkedList<OldNode> oldSolution = removeDegreeOneBetter(G);
        removeDegreeZero(G);
        //removes deg 0 and deg 1 nodes
        G.oldNodeList.removeIf(node -> !node.active);

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

    public static LinkedList<OldNode> removeDegreeOneBetter(Graph G){
        Stack<OldNode> notCheckedNodes = new Stack<>();
        LinkedList<OldNode> solution = new LinkedList<>();
        for (OldNode oldNode : G.oldNodeList) {
            while (!notCheckedNodes.empty()){
                OldNode w = notCheckedNodes.pop();
                if(w.neighbors.size()==1){
                    OldNode neighbour = w.neighbors.remove();
                    G.totalEdges --;
                    solution.add(neighbour);
                    for (OldNode v: neighbour.neighbors) {
                        if (v != w){
                            notCheckedNodes.push(v);
                            v.neighbors.remove(neighbour);
                            G.totalEdges --;
                        }
                    }
                    neighbour.neighbors.clear();
                }
            }

            if(oldNode.neighbors.size()==1){
                OldNode neighbour = oldNode.neighbors.remove();
                G.totalEdges --;
                solution.add(neighbour);
                for (OldNode v: neighbour.neighbors) {
                    if (v != oldNode){
                        notCheckedNodes.push(v);
                        G.totalEdges --;
                        v.neighbors.remove(neighbour);
                    }
                }
                neighbour.neighbors.clear();
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
            //G.removeNode(node);

            //this is not well written but effective. we dont use active and inactive for the old nodes and we just
            // want to ignore the deg 0 nodes so instead of deleting them we set them to inactive
            // this only works because they dont appear in any neigbourhood list

            node.active = false;
        }
    }
}
