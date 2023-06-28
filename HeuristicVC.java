import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class HeuristicVC {
    Graph G;
    final int TIME_LIMIT = 300000000; // in microseconds
    Node[] lsPermutation;
    int[] posInLsPermutation;
    boolean[] isCandidate;
    int vcBorder;
    int freeBorder;
    long startTime;
    Queue<Integer> candidates;
    public HeuristicVC(Graph G, long startTime){
        this.G = G;
        this.startTime = startTime;
    }
    LinkedList<Node> metaheuristic(LinkedList<Node> vc){//main function
        int PLATEAU_DESCENDANCE_LIMIT = (int) Math.sqrt(vc.size()) * ThreadLocalRandom.current().nextInt(20, 30);
        LinkedList<Node> bestSolution = localSearch(vc);
        int bestSolutionSize = vcBorder;
        int lastTimeGotWorse = 0;
        int notImprovedFor = 0;
        while ((System.nanoTime() - startTime)/1024 < TIME_LIMIT && notImprovedFor < 3000000){
            int lastBorder = vcBorder;
            if (vcBorder == 0) return vc;//perturb the solution
            Stack<Integer> actions = new Stack<>();
            Stack<Boolean> addOrDelete = new Stack<>();
            int rand = ThreadLocalRandom.current().nextInt(0, vcBorder);
            Node u = lsPermutation[rand];
            LinkedList<Integer> preCandidates = new LinkedList<>();
            for (int n : u.neighbours){
                Node v = G.nodeArray[n];
                if (!v.active){
                    preCandidates.addAll(addToVC(v));
                    actions.push(v.id);
                    addOrDelete.push(true);
                }
            }
            for (int el : preCandidates) addToCandidates(el);
            removeFromVC(u, false);
            actions.push(u.id);
            addOrDelete.push(false);
            while (vcBorder != freeBorder){
                actions.push(lsPermutation[vcBorder].id);
                addOrDelete.push(false);
                removeFromVC(lsPermutation[vcBorder], true);
            }
            twoImprovements(actions, addOrDelete, u.id); // improve solution
            if (vcBorder > lastBorder && (lastTimeGotWorse < PLATEAU_DESCENDANCE_LIMIT || ThreadLocalRandom.current().nextDouble() > 1.0 / (1.0 + (lastBorder - vcBorder) * (bestSolutionSize - vcBorder)))){
                lastTimeGotWorse++;
                while (!actions.isEmpty()){
                    boolean remove = addOrDelete.pop();
                    int toDo = actions.pop();
                    if (remove) removeFromVC(G.nodeArray[toDo], false);
                    else addToVC(G.nodeArray[toDo]); //revert old solution if solution was bad and we recently took a bad solution
                }
            }
            else if (vcBorder > lastBorder) lastTimeGotWorse = 0;
            if (vcBorder < lastBorder) lastTimeGotWorse = 0;
            else lastTimeGotWorse++;
            if (vcBorder < bestSolutionSize) {
                if (notImprovedFor > 100000){
                    threeImprovements();
                    twoImprovements(null, null, -1);
                }
                bestSolution = new LinkedList<>(Arrays.asList(lsPermutation).subList(0, vcBorder));
                bestSolutionSize = vcBorder;
                notImprovedFor = 0;
            }
            else notImprovedFor++;
        }
        return bestSolution;
    }
    private LinkedList<Node> localSearch(LinkedList<Node> vc){
        for (Node n : G.nodeArray){ //setup: nodes in the VC are active, activeNeighbours is #VC-neighbours
            n.active = false;
            n.activeNeighbours = 0;
        }
        candidates = new LinkedList<>();
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
            if (!ok) {
                inVC[n.id] = false;
                continue;
            }
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
        LinkedList<Integer> candidatesToShuffle = new LinkedList<>(candidates);
        Collections.shuffle(candidatesToShuffle);
        candidates = new LinkedList<>(candidatesToShuffle);
        int oldBorder = -1;
        while(oldBorder != vcBorder) {//actual local search begins here
            oldBorder = vcBorder;
            twoImprovements(null, null, -1);
            if ((System.nanoTime() - startTime)/1024 > TIME_LIMIT) return new LinkedList<>(Arrays.asList(lsPermutation).subList(0, vcBorder));
            threeImprovements();
            if ((System.nanoTime() - startTime)/1024 > TIME_LIMIT) return new LinkedList<>(Arrays.asList(lsPermutation).subList(0, vcBorder));
            fourImprovements();
            if ((System.nanoTime() - startTime)/1024 > TIME_LIMIT) return new LinkedList<>(Arrays.asList(lsPermutation).subList(0, vcBorder));
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
            candidates.offer(n);
            isCandidate[n] = true;
        }
    }
    private int pollCandidate(){
        int c = candidates.poll();
        isCandidate[c] = false;
        return c;
    }

    private void twoImprovements(Stack<Integer> actionsGiven, Stack<Boolean> addOrDelGiven, int checkLast){
        Stack<Integer> actions;
        Stack<Boolean> addOrDelete;
        if (actionsGiven == null) actions = new Stack<>();
        else actions = actionsGiven;
        if (addOrDelGiven == null) addOrDelete = new Stack<>();
        else addOrDelete = addOrDelGiven; //true for add
        while (!candidates.isEmpty()) {
            int n = pollCandidate();
            if (n == checkLast){
                addToCandidates(n);
                n = pollCandidate();
            }
            Node toAdd = G.nodeArray[n];
            if (!toAdd.active) {
                LinkedList<Integer> preCandidates = addToVC(toAdd);
                if (freeBorder - vcBorder < 2) { //no improvement
                    removeFromVC(toAdd, false);
                    continue;
                }
                int lastPreCandidate = lsPermutation[vcBorder].id;
                removeFromVC(lsPermutation[vcBorder], false);
                if (vcBorder == freeBorder) { //no improvement
                    addToVC(G.nodeArray[lastPreCandidate]);
                    removeFromVC(toAdd, false);
                } else {
                    for (int el : preCandidates) { //the new solution will be used
                        addToCandidates(el);
                    }
                    addOrDelete.push(true);
                    actions.push(toAdd.id);
                    addOrDelete.push(false);
                    actions.push(lastPreCandidate);
                    addToCandidates(lastPreCandidate);
                    while (vcBorder != freeBorder) {
                        addOrDelete.push(false);
                        actions.push(lsPermutation[vcBorder].id);
                        removeFromVC(lsPermutation[vcBorder], true);
                    }
                }

            }
        }
    }
    private void threeImprovements(){
        LinkedList<Integer> candidatesToAdd = new LinkedList<>();
        for (Node u : lsPermutation) {//look for 3-improvements
            if (u.activeNeighbours == u.neighbours.length - 2) {
                LinkedList<Integer> preCandidates = new LinkedList<>();
                Node x = null, y = null;
                int j;
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
                preCandidates.addAll(addToVC(x));
                preCandidates.addAll(addToVC(y));
                removeFromVC(u, false);
                preCandidates.add(u.id);
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
                            preCandidates.add(v.id);
                            while (freeBorder != vcBorder) {
                                preCandidates.add(lsPermutation[vcBorder].id);
                                removeFromVC(lsPermutation[vcBorder], false);
                            }
                            success = true;
                            candidatesToAdd.addAll(preCandidates);
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
        for (int el : candidatesToAdd) addToCandidates(el);
    }
    private void fourImprovements(){
        LinkedList<Integer> candidatesToAdd = new LinkedList<>();
        for (Node u : lsPermutation) {//look for 4-improvements
            if ((System.nanoTime() - startTime)/1024 > 55500000) return;
            if (u.activeNeighbours == u.neighbours.length - 3) {
                LinkedList<Integer> preCandidates = new LinkedList<>();
                Node x = null, y = null, z = null;
                int j;
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
                for (j = j + 1; j < u.neighbours.length; j++)
                    if (!G.nodeArray[u.neighbours[j]].active) {
                        z = G.nodeArray[u.neighbours[j]];
                        break;
                    }
                preCandidates.addAll(addToVC(x));
                preCandidates.addAll(addToVC(y));
                preCandidates.addAll(addToVC(z));
                removeFromVC(u, false);
                preCandidates.add(u.id);
                if (freeBorder - vcBorder < 3) {
                    addToVC(u);
                    removeFromVC(z, false);
                    removeFromVC(y, false);
                    removeFromVC(x, false);
                } else {
                    boolean success = false;
                    for (int i = vcBorder; i < freeBorder; i++) {
                        Node v = lsPermutation[i];
                        removeFromVC(v, false);
                        if (freeBorder - vcBorder < 2) addToVC(v);
                        else {
                            for (j = vcBorder; j < freeBorder; j++){
                                Node w = lsPermutation[j];
                                removeFromVC(w, false);
                                if (freeBorder == vcBorder) addToVC(w);
                                else{
                                    preCandidates.add(v.id);
                                    preCandidates.add(w.id);
                                    while (freeBorder != vcBorder) {
                                        preCandidates.add(lsPermutation[vcBorder].id);
                                        removeFromVC(lsPermutation[vcBorder], false);
                                    }
                                    success = true;
                                    candidatesToAdd.addAll(preCandidates);
                                    break;
                                }
                            }
                            if (success) break;
                            else addToVC(v);
                        }
                    }
                    if (!success) {
                        addToVC(u);
                        removeFromVC(z, false);
                        removeFromVC(y, false);
                        removeFromVC(x, false);
                    }
                }
            }
        }
        for (int el : candidatesToAdd) addToCandidates(el);
    }
}
