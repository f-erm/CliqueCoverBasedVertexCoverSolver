import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

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
    Node[] lsPermutation;
    int[] posInLsPermutation;
    int[][] neighbourArrays;
    boolean[] isCandidate;
    int vcBorder;
    int freeBorder;
    long startTime;
    Vector<Integer> candidates;
    public HeuristicVC(Graph G, long startTime){
        this.G = G;
        neighbourArrays = new int[G.nodeArray.length][];
        for (int i = 0; i < G.nodeArray.length; i++){
            neighbourArrays[i] = G.nodeArray[i].neighbours.clone();
        }
        //HopcroftKarp hk = new HopcroftKarp(G);
        reduction = new Reduction(G, null);
        inexactRed = 0;
        exactRed = 0;
        this.startTime = startTime;
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
        /*if ((System.nanoTime() - startTime)/1024  < 20000000)*/ reduction.rollOutAll(Integer.MAX_VALUE,false);
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
        int iterativeSteps = 0;
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
            iterativeSteps++;
            //check if an exact reduction can be applied
            /*if ((System.nanoTime() - startTime)/1024  < 30000000)*/ reduction.rollOutAllHeuristic(false, this);
            if (G.totalEdges == 0){
                vc.addAll(reduction.VCNodes);
                while (!reduction.mergedNodes.isEmpty()){
                    int[] merge = reduction.mergedNodes.pop();
                    if (vc.contains(G.nodeArray[merge[0]])){
                        vc.add(G.nodeArray[merge[1]]);
                        vc.remove(G.nodeArray[merge[2]]);
                    }
                }
                int cnt = 0;
                while ((System.nanoTime() - startTime)/1024 < 55000000 && cnt < 100) {
                    LinkedList<Node> newVC = metaheuristic(vc);
                    if (newVC.size() < vc.size()) {
                        vc = newVC;
                        cnt = 0;
                    }
                    else cnt++;
                }
                vc.addAll(G.partialSolution);
                return vc;
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
        int cnt = 0;
        while ((System.nanoTime() - startTime)/1024 < 55000000 && cnt < 1000) {
            System.out.println(cnt);
            LinkedList<Node> newVC = metaheuristic(vc);
            if (newVC.size() < vc.size()) {
                vc = newVC;
                cnt = 0;
            }
            else cnt++;
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

    private LinkedList<Node> localSearch(LinkedList<Node> vc){
        for (Node n : G.nodeArray){ //setup: nodes in the VC are active, activeNeighbours is #VC-neighbours
            n.neighbours = neighbourArrays[n.id];//use a copy of the original graph
            n.active = false;
            n.activeNeighbours = 0;
        }
        candidates = new Vector<>();//allows fast random accessing & deleting
        isCandidate = new boolean[G.nodeArray.length];
        lsPermutation = new Node[G.nodeArray.length];//array consists of three blocks: 1)non-free VC nodes 2)free VC nodes 3)not-VC-nodes (free means they can be removed from the VC)
        posInLsPermutation = new int[G.nodeArray.length];
        int j = 0;
        boolean[] inVC = new boolean[G.nodeArray.length];
        for (Node n : vc) inVC[n.id] = true;
        for (Node n : vc){
            boolean ok = false;
            for (int v : n.neighbours) if (!inVC[v]){//check whether there are free vertices in the initial solution
                ok = true; //...and remove them from the solution
                break;
            }
            if (!ok) continue;
            lsPermutation[j] = n;
            posInLsPermutation[n.id] = j++;
            G.nodeArray[n.id].active = true;
            for (int neighbour : n.neighbours){
                G.nodeArray[neighbour].activeNeighbours++;
            }
        }
        vcBorder = j;
        freeBorder = j;
        for (int i = 0; i < G.nodeArray.length; i++){
            Node n = G.nodeArray[i];
            if (!n.active) {
                lsPermutation[j] = n;
                posInLsPermutation[n.id] = j++;
            }
            else {
                if (n.activeNeighbours == n.neighbours.length - 1) {
                    for (int neighbour : n.neighbours) {
                        if (!G.nodeArray[neighbour].active) {
                            addToCandidates(neighbour);
                            break;
                        }
                    }
                }
            }
        }
        int oldBorder = -1;
        while(oldBorder != vcBorder) {//actual local search begins here
            oldBorder = vcBorder;
            twoImprovements();//look for 2-improvements
            for (Node u : lsPermutation) {//look for 3-improvements
                if (u.activeNeighbours == u.neighbours.length - 2) {
                    Node x = null, y = null;
                    for (j = 0; j < u.neighbours.length; j++)
                        if (!G.nodeArray[u.neighbours[j]].active) {
                            x = G.nodeArray[u.neighbours[j]];
                            break;
                        }
                    for (j = j + 1; j < u.neighbours.length; j++)
                        if (!G.nodeArray[u.neighbours[j]].active) {
                            y = G.nodeArray[u.neighbours[j]];
                            break;
                        }
                    addToVC(x);
                    addToVC(y);
                    removeFromVC(u, false);
                    if (freeBorder - vcBorder < 2) {
                        addToVC(u);
                        removeFromVC(y, false);
                        removeFromVC(x, false);
                    } else {
                        boolean success = false;
                        for (int i = vcBorder; i < freeBorder; i++) {
                            Node v = lsPermutation[i];
                            removeFromVC(v, false);
                            if (freeBorder == vcBorder) addToVC(v);
                            else {
                                while (freeBorder != vcBorder) removeFromVC(lsPermutation[vcBorder], false);
                                success = true;
                                break;
                            }
                        }
                        if (!success) {
                            addToVC(u);
                            removeFromVC(y, false);
                            removeFromVC(x, false);
                        }
                    }
                }
            }
        }
        return new LinkedList<>(Arrays.asList(lsPermutation).subList(0, vcBorder));
    }

    private LinkedList<Integer> addToVC(Node u){//adds a node to the VC , the node becomes free
        LinkedList<Integer> preCandidates = new LinkedList<>();
        u.active = true;
        lsPermutation[posInLsPermutation[u.id]] = lsPermutation[freeBorder];
        posInLsPermutation[lsPermutation[freeBorder].id] = posInLsPermutation[u.id];
        lsPermutation[freeBorder] = u;
        posInLsPermutation[u.id] = freeBorder++;
        for (int neighbour : u.neighbours) {
            Node v = G.nodeArray[neighbour];
            if (v.active && v.activeNeighbours == v.neighbours.length - 2){
                for (int w : v.neighbours){
                    if (!G.nodeArray[w].active){
                        preCandidates.add(w);
                        break;
                    }
                }
            }
            if (v.active && v.activeNeighbours == v.neighbours.length - 1){
                lsPermutation[posInLsPermutation[v.id]] = lsPermutation[--vcBorder];
                posInLsPermutation[lsPermutation[vcBorder].id] = posInLsPermutation[v.id];
                lsPermutation[vcBorder] = v;
                posInLsPermutation[v.id] = vcBorder;
            }
            v.activeNeighbours++;
        }
        return preCandidates;
    }
    private void removeFromVC(Node u, boolean permanent){ //remove a free vertex from the VC (must be free!)
        if (posInLsPermutation[u.id] < vcBorder){
            System.out.println(u.id + " - " + posInLsPermutation[u.id]);
        }
        u.active = false;
        lsPermutation[posInLsPermutation[u.id]] = lsPermutation[--freeBorder];
        posInLsPermutation[lsPermutation[freeBorder].id] = posInLsPermutation[u.id];
        lsPermutation[freeBorder] = u;
        posInLsPermutation[u.id] = freeBorder;
        for (int neighbour : u.neighbours){
            if (posInLsPermutation[neighbour] < freeBorder && posInLsPermutation[neighbour] >=vcBorder){
                lsPermutation[posInLsPermutation[neighbour]] = lsPermutation[vcBorder];
                posInLsPermutation[lsPermutation[vcBorder].id] = posInLsPermutation[neighbour];
                lsPermutation[vcBorder] = G.nodeArray[neighbour];
                posInLsPermutation[neighbour] = vcBorder++;
            }
            G.nodeArray[neighbour].activeNeighbours--;
        }
        if (permanent) addToCandidates(u.id);
    }
    private void addToCandidates(int n){
        if (!isCandidate[n]){
            candidates.add(n);
            isCandidate[n] = true;
        }
    }
    private int pollRandomCandidate(){
        int rand = ThreadLocalRandom.current().nextInt(0, candidates.size());
        int a = candidates.get(rand);
        candidates.set(rand, candidates.lastElement());
        candidates.setSize(candidates.size() - 1);
        return a;
    }
    private LinkedList<Node> metaheuristic(LinkedList<Node> vc){//main function
        int steps = 0;
        LinkedList<Node> bestSolution = localSearch(vc);
        int bestSolutionSize = vcBorder;
        int lastTimeGotWorse = 0;
        while ((System.nanoTime() - startTime)/1024 < 55000000 && steps < 10000){
            if (vcBorder == 0) return vc;//perturb the solution
            int rand = ThreadLocalRandom.current().nextInt(0, vcBorder);
            Node u = lsPermutation[rand];
            LinkedList<Integer> preCandidates = new LinkedList<>();
            Stack<Node> addedNodes = new Stack<>();
            for (int n : u.neighbours){
                Node v = G.nodeArray[n];
                if (!v.active){
                    preCandidates.addAll(addToVC(v));
                    addedNodes.push(v);
                }
            }
            for (int el : preCandidates) addToCandidates(el);
            removeFromVC(u, true);
            Stack<Node> removedNodes = new Stack<>();
            while (vcBorder != freeBorder){
                removedNodes.push(lsPermutation[vcBorder]);
                removeFromVC(lsPermutation[vcBorder], true);
            }
            Stack[] actionStack = twoImprovements(); // improve solution
            if (vcBorder > bestSolutionSize && steps - lastTimeGotWorse < bestSolutionSize){
                Stack<Integer> actions = actionStack[0];
                Stack<Boolean> addOrDelete = actionStack[1];
                while (!actions.isEmpty()){
                    boolean remove = addOrDelete.pop();
                    int toDo = actions.pop();
                    if (remove) removeFromVC(G.nodeArray[toDo], false);
                    else addToVC(G.nodeArray[toDo]); //revert old solution if solution was bad and we recently took a bad solution
                }
                while (!removedNodes.isEmpty()) addToVC(removedNodes.pop());
                addToVC(u);
                while (!addedNodes.isEmpty()) removeFromVC(addedNodes.pop(), false);
            }
            else if (vcBorder > bestSolutionSize) lastTimeGotWorse = steps;
            if (vcBorder < bestSolutionSize) {
                bestSolution = new LinkedList<>(Arrays.asList(lsPermutation).subList(0, vcBorder));
                bestSolutionSize = vcBorder;
            }
            steps++;
        }
        return bestSolution;
    }
    private Stack<Integer>[] twoImprovements(){
        Stack<Integer> actions = new Stack<>();
        Stack<Boolean> addOrDelete = new Stack<>();//true for add
        while (!candidates.isEmpty()) {
            int n = pollRandomCandidate();
            isCandidate[n] = false;
            Node toAdd = G.nodeArray[n];
            if (!toAdd.active) {
                actions.push(toAdd.id);
                addOrDelete.push(true);
                LinkedList<Integer> preCandidates = addToVC(toAdd);
                if (freeBorder - vcBorder < 2) { //no improvement
                    addOrDelete.push(false);
                    actions.push(toAdd.id);
                    removeFromVC(toAdd, false);
                    continue;
                }
                int lastPreCandidate = lsPermutation[vcBorder].id;
                addOrDelete.push(false);
                actions.push(lsPermutation[vcBorder].id);
                removeFromVC(lsPermutation[vcBorder], false);
                if (vcBorder == freeBorder) { //no improvement
                    addOrDelete.push(true);
                    actions.push(lsPermutation[vcBorder].id);
                    addToVC(lsPermutation[vcBorder]);
                    int pos2 = posInLsPermutation[toAdd.id];
                    addOrDelete.push(false);
                    actions.push(toAdd.id);
                    removeFromVC(toAdd, false);
                } else {
                    for (int el : preCandidates) { //the new solution will be used
                        addToCandidates(el);
                    }
                    addToCandidates(lastPreCandidate);
                    while (vcBorder != freeBorder) {
                        addOrDelete.push(false);
                        actions.push(lsPermutation[vcBorder].id);
                        removeFromVC(lsPermutation[vcBorder], true);
                    }
                }

            }
        }
        Stack[] a = new Stack[2];
        a[0] = actions;
        a[1] = addOrDelete;
        return a;
    }
}
