import java.util.LinkedList;

public class HeuristicVC {
    Graph G;
    Reduction reduction;

    public HeuristicVC(Graph G){
        this.G = G;
        reduction = new Reduction(G, null);
    }

    /**
     * this function return a vertex cover which might not be optimal
     * @param highestDegree if true the algorithm takes the highest degree vertex into to vertex cover if the reductions are not applicable anymore, otherwise the neighbours of a lowest degree vertex
     * @return
     */

    public LinkedList<Node> vc(boolean highestDegree){
        LinkedList<Node>vc = new LinkedList<>();
        int sizeOfOldVC = reduction.VCNodes.size();
        while (G.totalEdges > 0){

            //check if an exact reduction can be applied
            boolean changed = true;
            while (changed){
                reduction.rollOutAll(Integer.MAX_VALUE, false);
                if (reduction.VCNodes.size() == sizeOfOldVC) {
                    changed = false;
                }
                sizeOfOldVC = reduction.VCNodes.size();
            }
            if (G.totalEdges == 0){
                vc.addAll(reduction.VCNodes);
                while (!reduction.mergedNodes.isEmpty()){
                    int[] merge = reduction.mergedNodes.pop();
                    if (vc.contains(G.nodeArray[merge[0]])){
                        vc.add(G.nodeArray[merge[1]]);
                        vc.remove(G.nodeArray[merge[2]]);
                    }
                }
                vc.addAll(G.partialSolution);
                return vc;
            }

            //else use an inexact reduction;
            if (highestDegree){
                int maxDegree = 0;
                Node maxDegreeNode = null;
                for (int id = 0; id < G.nodeArray.length; id++) {
                    Node node = G.nodeArray[id];
                    if (node.active && node.activeNeighbours > maxDegree){
                        maxDegreeNode = node;
                        maxDegree = node.activeNeighbours;
                    }
                }
                vc.add(maxDegreeNode);
                G.removeNode(maxDegreeNode);
            }
            else {
                int minDegree = Integer.MAX_VALUE;
                Node minDegreeNode = null;
                for (int id = 0; id < G.nodeArray.length; id++) {
                    Node node = G.nodeArray[id];
                    if (node.active && node.activeNeighbours < minDegree){
                        minDegreeNode = node;
                        minDegree = node.activeNeighbours;
                    }
                }
                if(minDegreeNode != null){
                    for (int neighbourID: minDegreeNode.neighbours) {
                        Node u = G.nodeArray[neighbourID];
                        if(u.active){
                            vc.add(u);
                            G.removeNode(u);
                        }
                    }
                    G.removeNode(minDegreeNode);
                }
            }

        }
        vc.addAll(reduction.VCNodes);
        while (!reduction.mergedNodes.isEmpty()){
            int[] merge = reduction.mergedNodes.pop();
            if (vc.contains(G.nodeArray[merge[0]])){
                vc.add(G.nodeArray[merge[1]]);
                vc.remove(G.nodeArray[merge[2]]);
            }
        }
        vc.addAll(G.partialSolution);
        return vc;
    }

}
