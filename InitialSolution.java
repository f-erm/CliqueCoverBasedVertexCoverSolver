import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

public class InitialSolution {
    Graph G;
    Queue<Node> randomPerm;
    Reduction reduction;
    Queue<Integer> reduceDegZeroQueue;
    Queue<Integer> reduceDegOneQueue;
    Queue<Integer> reduceDegTwoQueue;
    int counterOfInexactRed;
    int inexactRed;
    int exactRed;
    int firstActiveNode = 0;
    Node[] permutation;
    int[] posInPermutation;
    int[] borderIndices;
    int[][] neighbourArrays;
    long startTime;
    public InitialSolution(Graph G, long startTime){
        this.G = G;
        neighbourArrays = new int[G.nodeArray.length][];
        for (int i = 0; i < G.nodeArray.length; i++){
            neighbourArrays[i] = G.nodeArray[i].neighbours.clone();
        }
        reduction = new Reduction(G, null);
        inexactRed = 0;
        exactRed = 0;
        reduceDegZeroQueue = new LinkedList<>();
        reduceDegOneQueue = new LinkedList<>();
        reduceDegTwoQueue = new LinkedList<>();
        this.startTime = startTime;
    }

    /**
     * this function return a vertex cover which might not be optimal
     * @param highestDegree if true the algorithm takes the highest degree vertex into to vertex cover if the reductions are not applicable anymore, otherwise the neighbours of a lowest degree vertex
     * @return a minimal (but not necessarily minimum) vertex cover.
     */

    public LinkedList<Node> vc(boolean highestDegree){
        if (!highestDegree) {
            LinkedList<Node> toShuffle = new LinkedList<>(Arrays.asList(G.nodeArray));
            Collections.shuffle(toShuffle);
            randomPerm = new LinkedList<>(toShuffle);
        }
        LinkedList<Node>vc = new LinkedList<>();
        counterOfInexactRed = 0;
        //initial reduction
        reduction.rollOutAllInitial(false);
        permutation = new Node[G.activeNodes];//keeps the nodes sorted
        posInPermutation = new int[G.nodeArray.length]; //stores the position of a node in permutation
        int j = 0;
        for (int i = 0; i < G.nodeArray.length; i++) if (G.nodeArray[i].active) permutation[j++] = G.nodeArray[i];
        Arrays.sort(permutation);
        int degree;
        if (permutation.length > 0) degree = permutation[0].activeNeighbours;
        else degree = -1;
        borderIndices = new int[G.activeNodes];
        for (int i = 0; i < permutation.length; i++){
            posInPermutation[permutation[i].id] = i;
            while (degree > permutation[i].activeNeighbours){
                borderIndices[degree--] = i - 1;
            }
        }
        for (int i = degree; i >= 0; i--) borderIndices[i] = permutation.length - 1;
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
        while (G.totalEdges > 0){
            //check if an exact reduction can be applied
            reduction.rollOutAllHeuristic(false, this);
            if (G.totalEdges == 0){
                return threadedLocalSearch(vc);
            }

            //else use an inexact reduction;
            if (highestDegree){
                while (!permutation[firstActiveNode].active) firstActiveNode++;
                Node maxDegreeNode = permutation[firstActiveNode++];
                vc.add(maxDegreeNode);
                G.removeNode(maxDegreeNode);
                reduceDegree(maxDegreeNode);
                counterOfInexactRed ++;
                inexactRed ++;
            }
            else {
                Node randomNode;
                do{
                    randomNode = randomPerm.poll();
                } while (!randomNode.active);
                vc.add(randomNode);
                G.removeNode(randomNode);
                reduceDegree(randomNode);
            }
        }
        return threadedLocalSearch(vc);
    }

    public void reduceDegree(Node node){
        for (int i = 0; i < node.neighbours.length; i++) if (G.nodeArray[node.neighbours[i]].active){
            reduceSingleDegree(node.neighbours[i]);
        }
    }
    private void reduceSingleDegree(int n){
        Node u = G.nodeArray[n];
        if (u.activeNeighbours == 0) reduceDegZeroQueue.offer(u.id);
        if (u.activeNeighbours == 1) reduceDegOneQueue.offer(u.id);
        if (u.activeNeighbours == 2) reduceDegTwoQueue.offer(u.id);
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
    private LinkedList<Node> threadedLocalSearch(LinkedList<Node> vc){
        vc.addAll(reduction.VCNodes);
        boolean[] inVC = new boolean[G.nodeArray.length];
        for (Node n : vc) inVC[n.id] = true;
        while (!reduction.mergedNodes.isEmpty()){
            int[] merge = reduction.mergedNodes.pop();
            if (inVC[merge[0]]){
                inVC[merge[1]] = true;
                inVC[merge[2]] = false;
            }
        }
        vc.clear();
        for (int i = 0; i < G.nodeArray.length; i++) if (inVC[i]) vc.add(G.nodeArray[i]);
        for (Node n : G.nodeArray) n.neighbours = neighbourArrays[n.id];
        // --- start threading ---
        int procCount = Runtime.getRuntime().availableProcessors();
        ThreadPoolExecutor exec = (ThreadPoolExecutor) Executors.newFixedThreadPool(procCount);//create threadpool based on available cores.
        Future<LinkedList<Node>>[] allResults = new Future[procCount];
        for (int i = 0; i < allResults.length; i++){
            allResults[i] = exec.submit(new HeuristicWorker((Graph) G.clone(), startTime, inVC.clone()));
        }
        try {
            vc = allResults[0].get();
            for (int i = 1; i < allResults.length; i++) {
                LinkedList<Node> thisVC = allResults[i].get();
                if (thisVC.size() < vc.size()) vc = thisVC;
            }
        }catch (Exception ignored){}
        exec.shutdown();
        // --- end threading ---
        vc.addAll(G.partialSolution);
        return vc;
    }
}
