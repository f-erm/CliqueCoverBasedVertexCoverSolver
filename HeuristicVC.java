import java.util.Arrays;
import java.util.LinkedList;

public class HeuristicVC {
    Graph G;
    Reduction reduction;
    int counterOfInexactRed;
    int inexactRed;
    int exactRed;
    int firstActiveNode = 0;
    Node[] permutation;
    int[] posInPermutation;
    int[] borderIndices;
    public HeuristicVC(Graph G){
        this.G = G;
        //HopcroftKarp hk = new HopcroftKarp(G);
        reduction = new Reduction(G, null);
        inexactRed = 0;
        exactRed = 0;
    }

    /**
     * this function return a vertex cover which might not be optimal
     * @param highestDegree if true the algorithm takes the highest degree vertex into to vertex cover if the reductions are not applicable anymore, otherwise the neighbours of a lowest degree vertex
     * @return
     */

    public LinkedList<Node> vc(boolean highestDegree){
        LinkedList<Node>vc = new LinkedList<>();
        int sizeOfOldVC = reduction.VCNodes.size();
        counterOfInexactRed = 0;
        //initial reduction
        //reduction.rollOutAll(Integer.MAX_VALUE,true);
        permutation = new Node[G.activeNodes];//keeps the nodes sorted
        posInPermutation = new int[G.nodeArray.length]; //stores the position of a node in permutation
        int j = 0;
        for (int i = 0; i < G.nodeArray.length; i++) if (G.nodeArray[i].active) permutation[j++] = G.nodeArray[i];
        Arrays.sort(permutation);
        int degree;
        if (permutation.length > 0) degree = permutation[0].activeNeighbours;
        else return reduction.VCNodes; // TODO das ist nur für jetzt
        borderIndices = new int[G.activeNodes];
        for (int i = 0; i < permutation.length; i++){
            posInPermutation[permutation[i].id] = i;
            while (degree > permutation[i].activeNeighbours){
                borderIndices[degree--] = i - 1;
            }
        }
        for (int i = degree; i >= 0; i--) borderIndices[i] = permutation.length - 1;
        int iterativeSteps = 0;
        while (G.totalEdges > 0){
            iterativeSteps++;
            //check if an exact reduction can be applied
            //reduction.rollOutAllHeuristic(false, this);
            /*boolean changed = true;
            while (changed && counterOfInexactRed < 100){
                reduction.rollOutAll(Integer.MAX_VALUE, false);
                if (reduction.VCNodes.size() == sizeOfOldVC) {
                    changed = false;
                }
                else {
                    exactRed ++;
                    counterOfInexactRed = 0;
                }
                sizeOfOldVC = reduction.VCNodes.size();
            }*/
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
                Node maxDegreeNode = null;
                while (!permutation[firstActiveNode].active) firstActiveNode++;
                maxDegreeNode = permutation[firstActiveNode++];
                vc.add(maxDegreeNode);
                G.removeNode(maxDegreeNode);
                reduceDegree(maxDegreeNode);
                counterOfInexactRed ++;
                inexactRed ++;
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
                    counterOfInexactRed ++;
                    inexactRed ++;
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

    public void reduceDegree(Node node){
        for (int i = 0; i < node.neighbours.length; i++) if (G.nodeArray[node.neighbours[i]].active){
            reduceSingleDegree(node.neighbours[i]);
        }
    }
    private void reduceSingleDegree(int n){
        int oldDegree = G.nodeArray[n].activeNeighbours + 1;
        permutation[posInPermutation[n]] = permutation[borderIndices[oldDegree]];
        posInPermutation[permutation[borderIndices[oldDegree]].id] = posInPermutation[n];
        permutation[borderIndices[oldDegree]] = G.nodeArray[n];
        posInPermutation[n] = borderIndices[oldDegree];
        if (borderIndices[oldDegree] > 0) borderIndices[oldDegree]--;
    }
    public void reduceDegreeMerge(Node node, Node second, int newNeighbours){
        int k = node.neighbours.length - newNeighbours;
        for (int i = 0; i < second.neighbours.length; i++){
            if (!G.nodeArray[second.neighbours[i]].active) continue;
            if (k >= node.neighbours.length || node.neighbours[k] != second.neighbours[i]){
                reduceSingleDegree(second.neighbours[i]);
            }
            else k++;
        }
        if (newNeighbours == 0){
            reduceSingleDegree(node.id);
        }
        for (int deg = node.activeNeighbours + 2 - newNeighbours; deg <= node.activeNeighbours; deg++){
            if (borderIndices[deg] == 0){
                borderIndices[deg] = 1;
                continue;
            }
            permutation[posInPermutation[node.id]] = permutation[borderIndices[deg] + 1];
            posInPermutation[permutation[borderIndices[deg] + 1].id] = posInPermutation[node.id];
            permutation[borderIndices[deg] + 1] = node;
            posInPermutation[node.id] = borderIndices[deg] + 1;
            firstActiveNode = Math.min(borderIndices[deg] + 1, firstActiveNode);
            borderIndices[deg]++;
        }
    }

}
