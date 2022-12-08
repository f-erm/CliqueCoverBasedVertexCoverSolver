import java.util.LinkedList;
import java.util.Stack;

public class Preprossessing {
    static Stack<OldNode[]> mergedNodes = new Stack<>();
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
        LinkedList<OldNode> toRemove = new LinkedList<>();
        while (changed){
            for (OldNode nodeToRemove : toRemove) {
                G.removeNode(nodeToRemove);
            }
            toRemove.clear();
            changed = false;
            for (OldNode oldNode : G.oldNodeList) {
                if(oldNode.neighbors.size()==1){
                    solution.add(oldNode.neighbors.get(0));
                    toRemove.add(oldNode.neighbors.get(0));
                    toRemove.add(oldNode);
                    changed = true;
                    break;
                }
                /*if (oldNode.neighbors.size() == 2){
                    OldNode first = oldNode.neighbors.getFirst();
                    OldNode second = oldNode.neighbors.get(1);
                    mergedNodes.push(new OldNode[]{first, second, oldNode});
                    if (!first.neighbors.contains(second)){ // case v,w \not \in E
                        solution.add(oldNode);
                        G.removeNode(oldNode);
                        G.removeNode(second);
                        for (OldNode neighbour : second.neighbors){
                            if (!first.neighbors.contains(neighbour)){
                                first.neighbors.add(neighbour);
                                neighbour.neighbors.add(first);
                                G.totalEdges++;
                            }
                        }
                    }
                    else{ // case v,w \in E
                        solution.add(first);
                        toRemove.add(first);
                        solution.add(second);
                        toRemove.add(second);
                        toRemove.add(oldNode);
                    }
                    changed = true;
                    break;
                }*/
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
