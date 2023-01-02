import java.util.LinkedList;


public class HeuristicVC {
    Graph G;
    Reduction reduction;
    int counterOfInexactRed;
    int inexactRed;
    int exactRed;
    LinkedList<Node>[] nodeDegree;
    int maxDegree;

    public HeuristicVC(Graph G){
        this.G = G;
        //HopcroftKarp hk = new HopcroftKarp(G);
        reduction = new Reduction(G, null);
        inexactRed = 0;
        exactRed = 0;
        nodeDegree = new LinkedList[G.activeNodes];
        maxDegree = 0;

        for (int i = 0; i < G.activeNodes; i++) {
            nodeDegree[i] = new LinkedList<Node>();
        }


        for (int id = 0; id < G.nodeArray.length; id++) {
            Node node = G.nodeArray[id];
            if (node.active){
                nodeDegree[node.activeNeighbours].add(node);
                if (maxDegree < node.activeNeighbours){
                    maxDegree = node.activeNeighbours;
                }
            }
        }
    }

    /**
     * this function return a vertex cover which might not be optimal
     * @param highestDegree if true the algorithm takes the highest degree vertex into to vertex cover if the reductions are not applicable anymore, otherwise the neighbours of a lowest degree vertex
     * @return
     */

    public LinkedList<Node> vc(boolean highestDegree, boolean coolDataStruc, boolean fastDataStruc){
        LinkedList<Node>vc = new LinkedList<>();
        int sizeOfOldVC = reduction.VCNodes.size();
        counterOfInexactRed = 0;
        int firstActiveNode = 0;
        //initial reduction
        //reduction.rollOutAll(Integer.MAX_VALUE,true);

        while (G.totalEdges > 0) {

            //check if an exact reduction can be applied

            /*int l = reduction.rollOutAll(Integer.MAX_VALUE, false);
            if (l > 0) {
                exactRed = exactRed + l;
                //counterOfInexactRed = 0;
            }
            sizeOfOldVC = reduction.VCNodes.size();
            if (G.totalEdges == 0) {
                vc.addAll(reduction.VCNodes);
                while (!reduction.mergedNodes.isEmpty()) {
                    int[] merge = reduction.mergedNodes.pop();
                    if (vc.contains(G.nodeArray[merge[0]])) {
                        vc.add(G.nodeArray[merge[1]]);
                        vc.remove(G.nodeArray[merge[2]]);
                    }
                }
                vc.addAll(G.partialSolution);
                return vc;
            }*/

            //else use an inexact reduction;
            if (highestDegree && !coolDataStruc && !fastDataStruc){
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
                counterOfInexactRed ++;
                inexactRed ++;
            }
            if (highestDegree && coolDataStruc && !fastDataStruc) {
                Node node = null;
                boolean highestDegNodeFound = false;
                while (!highestDegNodeFound) {
                    while (nodeDegree[maxDegree].isEmpty()) {
                        if (maxDegree > 0){
                            maxDegree--;
                        }
                        else maxDegree = G.activeNodes;
                    }
                    node = nodeDegree[maxDegree].pop();
                    if (!node.active){
                        continue;
                    }
                    if (node.activeNeighbours >= maxDegree) {
                        highestDegNodeFound = true;
                    } else if (node.activeNeighbours >= 0 && node.activeNeighbours < maxDegree){
                        nodeDegree[node.activeNeighbours].add(node);
                    } else {
                        nodeDegree[0].add(node);
                    }
                }
                vc.add(node);
                G.removeNode(node);
                counterOfInexactRed++;
                inexactRed++;
            }
            if (highestDegree && !coolDataStruc && fastDataStruc){
                while (!G.nodeArray[firstActiveNode].active){
                    firstActiveNode++;
                }
                Node node = G.nodeArray[firstActiveNode];
                vc.add(node);
                G.removeNode(node);
                counterOfInexactRed++;
                inexactRed++;
            }
            else {
                int minDegree = Integer.MAX_VALUE;
                Node minDegreeNode = null;
                for (int id = 0; id < G.nodeArray.length; id++) {
                    Node node = G.nodeArray[id];
                    if (node.active && node.activeNeighbours < minDegree) {
                        minDegreeNode = node;
                        minDegree = node.activeNeighbours;
                    }
                }
                if (minDegreeNode != null) {
                    for (int neighbourID : minDegreeNode.neighbours) {
                        Node u = G.nodeArray[neighbourID];
                        if (u.active) {
                            vc.add(u);
                            G.removeNode(u);
                        }
                    }
                    G.removeNode(minDegreeNode);
                    counterOfInexactRed++;
                    inexactRed++;
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
