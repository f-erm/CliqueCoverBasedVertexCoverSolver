import java.util.*;

public class Reduction {

    LinkedList<Node> uselessNeighbours;
    LinkedList<Node> VCNodes;
    Stack<int[]> mergedNodes;
    Stack<int[]> removedNodes;
    Stack<Integer> neighbourPosStack;
    Stack<Integer> packingReductions = new Stack<>();
    Graph G;
    HopcroftKarp hk;

    public Reduction(Graph G, HopcroftKarp hk) {
        this.G = G;
        VCNodes = new LinkedList<>();
        uselessNeighbours = new LinkedList<>();
        removedNodes = new Stack<>();
        mergedNodes = new Stack<>();
        this.hk = hk;
        neighbourPosStack = new Stack<>();
    }

    /**
     * @param doReduction do more time-consuming reduction rules
     * @param insol an instance of InitialSolution
     * performs reduction rules before searching for an upper bound.
     */
    public void rollOutAllHeuristic(boolean doReduction, InitialSolution insol){
        removeDegreeXHeuristic(insol);
        if (doReduction) removeTwin();
        if (doReduction) applyUnconfined();
        if (doReduction) improvedLP(G);
        removeDegreeXHeuristic(insol);
    }

    /**
     * performs reduction rules before branching.
     * @param doReduction do more time-consuming reduction rules
     * @return the number of deleted nodes by all reductions
     */
    public int rollOutAllInitial(boolean doReduction){
        int oldK = VCNodes.size();
        removedNodes.push(new int[]{0});
        removeDegreeXInitial();
        boolean again = doReduction;
        while (again) {
            again = applyUnconfined();
            again = again || improvedLP(G);
            int oldN = VCNodes.size();
            removeDegreeXInitial();
            if (oldN != VCNodes.size()) again = true;
        }
        doPackingReductions();
        return VCNodes.size() - oldK;
    }

    /**
     * Twin reduction, high running time, mediocre output.
     */
    public void removeTwin() {
        LinkedList<LinkedList<Integer>> neighbourLists = new LinkedList<>();
        LinkedList<Integer> deg3nodes = new LinkedList<>();
        for (int i = 0; i < G.nodeArray.length; i++) {
            Node n = G.nodeArray[i];
            if (n.active && n.activeNeighbours == 3) {
                LinkedList<Integer> neighbours = new LinkedList<>();
                boolean[] notPartners = new boolean[neighbourLists.size()];
                for (int j = 0; j < n.neighbours.length; j++) {
                    if (G.nodeArray[n.neighbours[j]].active) {
                        neighbours.add(n.neighbours[j]);
                        int cnt = 0;
                        for (LinkedList<Integer> list : neighbourLists) {
                            if (!list.contains(n.neighbours[j])) notPartners[cnt] = true;
                            cnt++;
                        }
                    }
                }
                boolean cut = false;
                for (int j = 0; j < notPartners.length; j++)
                    if (!notPartners[j]) {
                        int first = neighbours.getFirst();
                        int second = neighbours.get(1);
                        int third = neighbours.get(2);
                        int v = deg3nodes.get(j);
                        if (v == -1) continue;
                        if (!arrayContains(G.nodeArray[first].neighbours, second) && !arrayContains(G.nodeArray[first].neighbours,third) && !arrayContains(G.nodeArray[second].neighbours, third)){
                            removeUselessNodes(G.nodeArray[second]);
                            removeUselessNodes(G.nodeArray[third]);
                            removeVCNodes(G.nodeArray[v]);
                            removeVCNodes(G.nodeArray[i]);
                            mergeNodes(G.nodeArray[first], G.nodeArray[second]);
                            mergeNodes(G.nodeArray[first], G.nodeArray[third]);
                            mergedNodes.push(new int[]{first, second, v});
                            mergedNodes.push(new int[]{first, third, i});
                        }
                        else {
                            for (int id : neighbours) removeVCNodes(G.nodeArray[id]);
                            removeUselessNodes(G.nodeArray[i]);
                            removeUselessNodes(G.nodeArray[v]);
                        }
                        deg3nodes.set(j, -1);
                        cut = true;
                        break;
                    }
                if (cut) deg3nodes.add(-1);
                else deg3nodes.add(i);
                neighbourLists.add(neighbours);
            }
        }
    }

    /**
     * @param G the graph
     * @return a boolean that is true when the improvedLP rule was successfull in reducing the graph
     * performs the LP reduction.
     */
    public boolean improvedLP(Graph G) {
        boolean changed = false;
        hk.searchForAMatching();
        int size = hk.size;
        int bipartiteSize = hk.nil;
        LinkedList<Integer>[] residualGraph = new LinkedList[bipartiteSize + 2];//the residual graph is saved in an array of linked lists as adjacency lists.
        residualGraph[bipartiteSize] = new LinkedList<>(); // add s and t nodes
        residualGraph[bipartiteSize + 1] = new LinkedList<>();
        for (int i = 0; i < size; i++) {
            residualGraph[i] = new LinkedList<>();
            if (G.nodeArray[i].active) {
                for (Integer integer : G.nodeArray[i].neighbours)
                    if (G.nodeArray[integer].active) residualGraph[i].add(integer + size);
                if (hk.pair[i] == hk.nil) residualGraph[bipartiteSize].add(i);
                else residualGraph[i].add(bipartiteSize);
            }
        }
        for (int i = G.nodeArray.length; i < bipartiteSize; i++) {
            residualGraph[i] = new LinkedList<>();
            if (G.nodeArray[i - size].active) {
                if (hk.pair[i] == hk.nil) residualGraph[i].add(bipartiteSize + 1);
                else {
                    residualGraph[bipartiteSize + 1].add(i);
                    residualGraph[i].add(hk.pair[i]);
                }
            }
        }
        boolean[] reachedFromS = new boolean[bipartiteSize + 2];
        //run BFS to find nodes reached from s:
        Queue<Integer> q = new LinkedList<>();
        q.offer(bipartiteSize);
        while (!q.isEmpty()) {
            int v = q.poll();
            reachedFromS[v] = true;
            for (int w : residualGraph[v]) {
                if (!reachedFromS[w]) {
                    reachedFromS[w] = true;
                    q.offer(w);
                }
            }
        }
        for (int i = 0; i < size; i++) {
            if (G.nodeArray[i].active) {
                if (!reachedFromS[i] && reachedFromS[i + size]) removeVCNodes(G.nodeArray[i]); //LP solution = 1
                else if (reachedFromS[i] && !reachedFromS[i + size])
                    removeUselessNodes(G.nodeArray[i]); //LP solution = 0
            }
        }
        while (true) {
            StrongComponentsFinder vscf = new StrongComponentsFinder(hk.B, residualGraph);
            LinkedList<LinkedList<Integer>> scc = vscf.findStrongComponents();
            if (scc.isEmpty()) break;
            for (LinkedList<Integer> component : scc) {
                for (int n : component) {
                    changed = true;
                    if (n < size) {
                        if (G.nodeArray[n].active) removeUselessNodes(G.nodeArray[n]);
                        else break;
                    } else if (n < bipartiteSize)
                        if (G.nodeArray[n - size].active) removeVCNodes(G.nodeArray[n - size]);
                        else break;
                }
            }
        }
        return changed;
    }

    /**
     * Applies degree 0, 1 and 2 reductions before branching.
     */
    private void removeDegreeXInitial() {
        Queue<Integer> deg0 = new LinkedList<>();
        Queue<Integer> deg1 = new LinkedList<>();
        Queue<Integer> deg2 = new LinkedList<>();
        for (Node node : G.nodeArray) {
            if (!node.active) continue;
            if (node.activeNeighbours == 1) {
                int i = 0;
                while (!G.nodeArray[node.neighbours[i]].active) {
                    i++;
                }
                Node neighbour = G.nodeArray[node.neighbours[i]];
                removeVCNodes(neighbour);
                for (int n : neighbour.neighbours) {
                    if (!G.nodeArray[n].active) continue;
                    if (G.nodeArray[n].activeNeighbours == 0) deg0.offer(n);
                    if (G.nodeArray[n].activeNeighbours == 1) deg1.offer(n);
                    if (G.nodeArray[n].activeNeighbours == 2) deg2.offer(n);
                }
                removeUselessNodes(node);
                continue;
            }
            if (node.activeNeighbours == 2) {
                Node first = null, second = null;
                int it = 0;
                while (first == null) {
                    Node current = G.nodeArray[node.neighbours[it++]];
                    if (current.active) first = current;
                }
                while (second == null) {
                    Node current = G.nodeArray[node.neighbours[it++]];
                    if (current.active) second = current;
                }
                if (!arrayContains(first.neighbours, second.id)) {
                    int oldNumNeighbours = first.neighbours.length;
                    //removeVCNodes(node);
                    //BEGIN alternative
                    removedNodes.push(new int[]{2, node.id, 0});
                    VCNodes.add(node);
                    G.removeNode(node);
                    LinkedList<Node> a = new LinkedList<>();
                    a.add(node);
                    if(hk != null){
                        hk.updateDeleteNodes(a);
                    }
                    node.inVC = true;
                    //END alternative
                    removeUselessNodesWithoutDegreeReducing(second);
                    mergeNodes(first, second); // case v,w \not \in E
                    G.reduceDegreeMerge(first, second, first.neighbours.length - oldNumNeighbours);
                    for (int n : second.neighbours) {
                        if (!G.nodeArray[n].active) continue;
                        if (G.nodeArray[n].activeNeighbours == 0) deg0.offer(n);
                        if (G.nodeArray[n].activeNeighbours == 1) deg1.offer(n);
                        if (G.nodeArray[n].activeNeighbours == 2) deg2.offer(n);
                    }
                    if (G.nodeArray[first.id].activeNeighbours == 0) deg0.offer(first.id);
                    if (G.nodeArray[first.id].activeNeighbours == 1) deg1.offer(first.id);
                    if (G.nodeArray[first.id].activeNeighbours == 2) deg2.offer(first.id);
                    mergedNodes.push(new int[]{first.id, second.id, node.id});
                    first.mergeInfo.add(new int[]{second.id, node.id});
                } else { // case v,w \in E
                    removeUselessNodes(node);
                    removeVCNodes(first);
                    for (int n : first.neighbours) {
                        if (!G.nodeArray[n].active) continue;
                        if (G.nodeArray[n].activeNeighbours == 0) deg0.offer(n);
                        if (G.nodeArray[n].activeNeighbours == 1) deg1.offer(n);
                        if (G.nodeArray[n].activeNeighbours == 2) deg2.offer(n);
                    }
                    removeVCNodes(second);
                    for (int n : second.neighbours) {
                        if (!G.nodeArray[n].active) continue;
                        if (G.nodeArray[n].activeNeighbours == 0) deg0.offer(n);
                        if (G.nodeArray[n].activeNeighbours == 1) deg1.offer(n);
                        if (G.nodeArray[n].activeNeighbours == 2) deg2.offer(n);
                    }
                }
                continue;
            }
            if (node.activeNeighbours == 0) {
                removeUselessNodes(node);
            }
        }
        while (!deg0.isEmpty() || !deg1.isEmpty() || !deg2.isEmpty()) {
            while (!deg1.isEmpty()) {
                Node node = G.nodeArray[deg1.poll()];
                if (!node.active) continue;
                if (node.activeNeighbours == 1) {
                    int i = 0;
                    while (!G.nodeArray[node.neighbours[i]].active) {
                        i++;
                    }
                    Node neighbour = G.nodeArray[node.neighbours[i]];
                    removeVCNodes(neighbour);
                    for (int n : neighbour.neighbours) {
                        if (!G.nodeArray[n].active) continue;
                        if (G.nodeArray[n].activeNeighbours == 0) deg0.offer(n);
                        if (G.nodeArray[n].activeNeighbours == 1) deg1.offer(n);
                        if (G.nodeArray[n].activeNeighbours == 2) deg2.offer(n);
                    }
                    removeUselessNodes(node);
                }
            }
            while (!deg2.isEmpty()){
                Node node = G.nodeArray[deg2.poll()];
                if (node.activeNeighbours == 2 && node.active) {
                    Node first = null, second = null;
                    int it = 0;
                    while (first == null) {
                        Node current = G.nodeArray[node.neighbours[it++]];
                        if (current.active) first = current;
                    }
                    while (second == null) {
                        Node current = G.nodeArray[node.neighbours[it++]];
                        if (current.active) second = current;
                    }
                    if (!arrayContains(first.neighbours, second.id)) {
                        int oldNumNeighbours = first.neighbours.length;
                        //removeVCNodes(node);
                        //BEGIN alternative
                        removedNodes.push(new int[]{2, node.id, 0});
                        VCNodes.add(node);
                        G.removeNode(node);
                        LinkedList<Node> a = new LinkedList<>();
                        a.add(node);
                        if(hk != null){
                            hk.updateDeleteNodes(a);
                        }
                        node.inVC = true;
                        //END alternative
                        removeUselessNodesWithoutDegreeReducing(second);
                        mergeNodes(first, second); // case v,w \not \in E
                        G.reduceDegreeMerge(first, second, first.neighbours.length - oldNumNeighbours);
                        for (int n : second.neighbours) {
                            if (!G.nodeArray[n].active) continue;
                            if (G.nodeArray[n].activeNeighbours == 0) deg0.offer(n);
                            if (G.nodeArray[n].activeNeighbours == 1) deg1.offer(n);
                            if (G.nodeArray[n].activeNeighbours == 2) deg2.offer(n);
                        }
                        if (G.nodeArray[first.id].activeNeighbours == 0) deg0.offer(first.id);
                        if (G.nodeArray[first.id].activeNeighbours == 1) deg1.offer(first.id);
                        if (G.nodeArray[first.id].activeNeighbours == 2) deg2.offer(first.id);
                        mergedNodes.push(new int[]{first.id, second.id, node.id});
                        first.mergeInfo.add(new int[]{second.id, node.id});
                    } else { // case v,w \in E
                        removeUselessNodes(node);
                        removeVCNodes(first);
                        for (int n : first.neighbours) {
                            if (!G.nodeArray[n].active) continue;
                            if (G.nodeArray[n].activeNeighbours == 0) deg0.offer(n);
                            if (G.nodeArray[n].activeNeighbours == 1) deg1.offer(n);
                            if (G.nodeArray[n].activeNeighbours == 2) deg2.offer(n);
                        }
                        removeVCNodes(second);
                        for (int n : second.neighbours) {
                            if (!G.nodeArray[n].active) continue;
                            if (G.nodeArray[n].activeNeighbours == 0) deg0.offer(n);
                            if (G.nodeArray[n].activeNeighbours == 1) deg1.offer(n);
                            if (G.nodeArray[n].activeNeighbours == 2) deg2.offer(n);
                        }
                    }
                }
            }
            while (!deg0.isEmpty()){
                Node node = G.nodeArray[deg0.poll()];
                if (node.activeNeighbours == 0 && node.active) {
                    removeUselessNodes(node);
                }
            }
        }
    }

    /**
     * @param insol the initial solution instance.
     * performs all degree reduction rules before searching for an upper bound.
     */
private void removeDegreeXHeuristic(InitialSolution insol){
        while (!insol.reduceDegTwoQueue.isEmpty() || !insol.reduceDegZeroQueue.isEmpty() || !insol.reduceDegOneQueue.isEmpty()) {
            while (!insol.reduceDegOneQueue.isEmpty()) {
                Node node = G.nodeArray[insol.reduceDegOneQueue.poll()];
                if (node.active && node.activeNeighbours == 1) {
                    int i = 0;
                    while (!G.nodeArray[node.neighbours[i]].active) {
                        i++;
                    }
                    Node neighbour = G.nodeArray[node.neighbours[i]];
                    removeVCNodes(neighbour);
                    insol.reduceDegree(neighbour);
                    removeUselessNodes(node);
                    insol.reduceDegree(node);
                }
            }
            while (!insol.reduceDegTwoQueue.isEmpty()) {
                Node node = G.nodeArray[insol.reduceDegTwoQueue.poll()];
                if (node.active && node.activeNeighbours == 2) {
                    Node first = null, second = null;
                    int it = 0;
                    while (first == null) {
                        Node current = G.nodeArray[node.neighbours[it++]];
                        if (current.active) first = current;
                    }
                    while (second == null) {
                        Node current = G.nodeArray[node.neighbours[it++]];
                        if (current.active) second = current;
                    }
                    if (!arrayContains(first.neighbours, second.id)) {
                        int oldNumNeighbours = first.neighbours.length;
                        //removeVCNodes(node);
                        //BEGIN alternative
                        removedNodes.push(new int[]{2, node.id, 0});
                        VCNodes.add(node);
                        G.removeNode(node);
                        LinkedList<Node> a = new LinkedList<>();
                        a.add(node);
                        if(hk != null){
                            hk.updateDeleteNodes(a);
                        }
                        node.inVC = true;
                        //END alternative
                        removeUselessNodesWithoutDegreeReducing(second);
                        mergeNodes(first, second); // case v,w \not \in E
                        insol.reduceDegreeMerge(first, second, first.neighbours.length - oldNumNeighbours);
                        G.reduceDegreeMerge(first, second, first.neighbours.length - oldNumNeighbours);
                        mergedNodes.push(new int[]{first.id, second.id, node.id});
                    } else { // case v,w \in E
                        removeUselessNodes(node);
                        insol.reduceDegree(node);
                        removeVCNodes(first);
                        insol.reduceDegree(first);
                        removeVCNodes(second);
                        insol.reduceDegree(second);
                    }
                }
            }
            while (!insol.reduceDegZeroQueue.isEmpty()) {
                Node node = G.nodeArray[insol.reduceDegZeroQueue.poll()];
                if (node.active && node.activeNeighbours == 0) {
                    removeUselessNodes(node);
                    insol.reduceDegree(node);
                }
            }
        }
}

    /**
     * revert all reductions until the last recursive call, for branching
     */
    public void revertReduction() {
        int[] action = removedNodes.pop();
        while (action[0] != 0) {
            Node node = G.nodeArray[action[1]];
            switch (action[0]) {
                case 1: // useless Nodes
                    redoPackingOfMergedNodes(node, 1);
                    G.reeaddNode(node);
                    LinkedList<Node> a = new LinkedList<>();
                    a.add(node);
                    if (hk != null) hk.updateAddNodes(a);
                    break;
                case 2: //useful Nodes
                    redoPackingOfMergedNodes(node, 0);
                    if (action.length == 2) for (Packing p : node.affectedConstraints) p.redoVC();
                    node.inVC = false;
                    G.reeaddNode(node);
                    VCNodes.removeLast();
                    LinkedList<Node> b = new LinkedList<>();
                    b.add(node);
                    if (hk != null) hk.updateAddNodes(b);
                    break;
                case 3: //merged Nodes :(
                    node.mergeInfo.removeLast();
                    int[] newArray = new int[node.neighbours.length - action[3]];
                    int[] newPositionArray = new int[newArray.length];
                    System.arraycopy(node.neighbours, 0, newArray, 0, newArray.length);
                    System.arraycopy(node.neighbourPositions, 0, newPositionArray, 0, newArray.length);
                    LinkedList<Integer> oldNeighbours = new LinkedList<>();
                    for (int i = node.neighbours.length - 1; i >= newArray.length; i--){
                        oldNeighbours.add(node.neighbours[i]);
                        Node neighbour = G.nodeArray[node.neighbours[i]];
                        for (int j = 0; j < neighbour.neighbours.length; j++){
                            if (neighbour.neighbours[j] == node.id) {
                                neighbour.neighbourPositions[j] = neighbourPosStack.pop();
                                neighbour.neighbours[j] = action[2];
                                neighbour.activeNeighbours--;
                                node.activeNeighbours--;
                                G.totalEdges--;
                                break;
                            }
                        }
                    }
                    node.neighbours = newArray;
                    node.neighbourPositions = newPositionArray;
                    if (hk != null) mergedNodes.pop();
                    G.increaseDegreeMerge(node, oldNeighbours);
                    break;
            }
            action = removedNodes.pop();
        }
    }

    /**
     * removes a node from the graph that is not in the vertex cover.
     * @param node node to remove
     */
    public void removeUselessNodes(Node node){
        removedNodes.push(new int[]{1, node.id});
        G.removeNode(node);
        LinkedList<Node> a = new LinkedList<>();
        a.add(node);
        //for the heuristic we don't need to compute hk, so it is null
        if(hk != null){
            hk.updateDeleteNodes(a);
        }
        updatePackingOfMergedNodes(node, 1);
    }

    /**
     * Removes a node from the graph that is not in the vertex cover
     * without updating the max degree data structure.
     * @param node node to remove
     */
    private void removeUselessNodesWithoutDegreeReducing(Node node){
        removedNodes.push(new int[]{1, node.id});
        G.removeNodeWithoutDegreeReducing(node);
        LinkedList<Node> a = new LinkedList<>();
        a.add(node);
        //for the heuristic we don't need to compute hk, so it is null
        if(hk != null){
            hk.updateDeleteNodes(a);
        }
    }

    /**
     * removes a node from the graph that is part of the vertex cover.
     * @param node node to remove
     */
    public void removeVCNodes(Node node){
        removedNodes.push(new int[]{2, node.id});
        VCNodes.add(node);
        G.removeNode(node);
        LinkedList<Node> a = new LinkedList<>();
        a.add(node);
        //for the heuristic we don't need to compute hk, so it is null
        if(hk != null){
            hk.updateDeleteNodes(a);
        }
        node.inVC = true;
        for (Packing p : node.affectedConstraints) p.updateVC();
        updatePackingOfMergedNodes(node, 0);
    }
    private void doPackingReductions(){
        while (!packingReductions.isEmpty()){
            Node n = G.nodeArray[packingReductions.pop()];
            if (n.active){
                for (int m : n.neighbours) if (G.nodeArray[m].active){
                    removeVCNodes(G.nodeArray[m]);
                }
                removeUselessNodes(n);
            }
        }
    }

    /**
     * @param nodeA node to be merged and to remain active
     * @param nodeB node to be merged into nodeA
     * @implNote     merges nodeB into nodeA
     *              (Array resizing is done here!)
     */
    private void mergeNodes(Node nodeA, Node nodeB){
        LinkedList<Integer> addedNeighbours = new LinkedList<>();
        Queue<Integer> nodeBPositions = new LinkedList<>();
        for (int n : nodeB.neighbours){
            Node neighbour = G.nodeArray[n];
            if (neighbour.active && !arrayContains(nodeA.neighbours, n)){
                addedNeighbours.add(n);
                for (int i = 0; i < neighbour.neighbours.length; i++){
                    if (neighbour.neighbours[i] == nodeB.id) {
                        nodeBPositions.offer(i);
                        neighbour.neighbours[i] = nodeA.id;
                        neighbour.activeNeighbours++;
                        G.totalEdges++;
                        break;
                    }
                }
            }
        }
        int[] newArray = new int[nodeA.neighbours.length + addedNeighbours.size()];
        int[] newPositionArray = new int[nodeA.neighbours.length + addedNeighbours.size()];
        System.arraycopy(nodeA.neighbours, 0, newArray, 0, nodeA.neighbours.length);
        System.arraycopy(nodeA.neighbourPositions, 0, newPositionArray, 0, nodeA.neighbours.length);
        int j = nodeA.neighbours.length;
        for (int id : addedNeighbours){
            newArray[j] = id;
            int nodeBPos = nodeBPositions.poll();
            neighbourPosStack.push(G.nodeArray[id].neighbourPositions[nodeBPos]);
            G.nodeArray[id].neighbourPositions[nodeBPos] = j;
            newPositionArray[j++] = nodeBPos;
        }
        nodeA.neighbours = newArray;
        nodeA.neighbourPositions = newPositionArray;
        int c = 0;
        removedNodes.push(new int[]{3, nodeA.id, nodeB.id, addedNeighbours.size(), c});
        nodeA.activeNeighbours += addedNeighbours.size();
        //for the heuristic we don't need to compute hk, so it is null
        if(hk != null){
            hk.updateMergeNodes(nodeA.id, nodeB.id, addedNeighbours);
        }
    }

    private boolean arrayContains(int[] array, int el){
        for (int i : array) if (i == el) return true;
        return false;
    }



    private boolean new_unconfined(Integer v, BitSet N_S){
        //check node for unconfined. Bitsset needs to be initialized to all zeros, all changes will be reverted in the end
        LinkedList<Integer> S = new LinkedList<>();
        S.add(v);
        LinkedList<Integer> N_S_aslist = new LinkedList<>();
        N_S.set(v);
        N_S_aslist.add(v);
        for(int i : G.nodeArray[v].neighbours){
            if(G.nodeArray[i].active){
                N_S.set(i);
                N_S_aslist.add(i);
            }
        }
        Integer[] final_u_Cut = new Integer[2];
        final_u_Cut[0] = 0;
        final_u_Cut[1] = 0;
        while (true) {
            Integer final_u = null; //the current best u
            for(Integer u : N_S_aslist){
                if (checkCutSize(u,S)){
                    if (final_u == null){
                        final_u = u;
                        final_u_Cut = getSetminus(u, N_S, Integer.MAX_VALUE);
                    }else{
                        Integer[] res = getSetminus(u, N_S, final_u_Cut[0]);
                        if (res[0] < final_u_Cut[0]){
                            final_u = u;
                            final_u_Cut = res;
                        }
                    }
                }
            }
            //evaluate N(u) \cap S
            if (final_u==null) {reset_bitset(N_S, N_S_aslist); return false;}
            if (final_u_Cut[0]==0) {reset_bitset(N_S, N_S_aslist); return true;}
            if (final_u_Cut[0]==1) {
                Integer w = final_u_Cut[1];
                S.add(w);
                N_S.set(w);
                N_S_aslist.add(w);
                for (Integer i : G.nodeArray[w].neighbours) {
                    if(G.nodeArray[i].active && !N_S.get(i)) {
                        N_S.set(i);
                        N_S_aslist.add(i);
                    }
                }
                continue;
            }
            reset_bitset(N_S, N_S_aslist);
            return false;
        }
    }

    private boolean checkCutSize(int u , LinkedList<Integer> S){
        //check if N(u) cap S = 1
        int[] N_u = G.nodeArray[u].neighbours;
        boolean found_one = false;
        for (Integer i : N_u ){
            if (G.nodeArray[i].active && S.contains(i)){
                if (found_one){return false;}
                else{found_one = true;}
            }
        }
        return found_one;
    }

    private void reset_bitset(BitSet B, LinkedList<Integer> L){
        //reset the bitsset to all zeros
        for (Integer i : L) B.clear(i);
    }

    private Integer[] getSetminus(int u, BitSet B,int tobeat){
        //returns size of N(u) \ B in res[0], one of the elements in N(u) \ B in res[1]. Stops if result is bigger than tobeat
        int[] N_u = G.nodeArray[u].neighbours;
        int count = 0;
        Integer[] res = new Integer[2];
        for (Integer i : N_u ){
            if (G.nodeArray[i].active && !B.get(i)){
                res[1] = i;
                count ++;
                if(count > tobeat) break;
            }
        }
        res[0] = count;
        return res;
    }

    private boolean applyUnconfined(){
        boolean changed = false;
        //remove all unconfined nodes. also checks Neighbors of every removed node
        BitSet N_S = new BitSet(G.nodeArray.length);
        Queue<Integer> q = new LinkedList<>();
        boolean[] checked = new boolean[G.nodeArray.length];
        for(int i=0;i< G.nodeArray.length;i++){//geht das besser?
            if (G.nodeArray[i].active && new_unconfined(i,N_S)) {
                changed = true;
                removeVCNodes(G.nodeArray[i]);
                for(Integer j : G.nodeArray[i].neighbours){
                    q.offer(j);
                }
            }
        }
        while(!q.isEmpty()){
            Integer elem = q.poll();
            if (checked[elem]) continue;
            //if (G.nodeArray[elem].active && unconfined(G, elem)) {
            if (G.nodeArray[elem].active && new_unconfined(elem,N_S)) {
                //System.out.println("unconfined");
                removeVCNodes(G.nodeArray[elem]);
                checked[elem] = true;
                for(Integer j : G.nodeArray[elem].neighbours){
                    if (G.nodeArray[j].active) q.offer(j);
                }
            }
        }
        return changed;
    }
    public void updatePackingOfMergedNodes(Node node, int type) {
        for (int[] merge : node.mergeInfo) {
            for (Packing p : G.nodeArray[merge[type]].affectedConstraints) p.updateVC();
            updatePackingOfMergedNodes(G.nodeArray[merge[type]], 0);
            updatePackingOfMergedNodes(G.nodeArray[merge[1 - type]], 1);
        }
    }
    public void redoPackingOfMergedNodes(Node node, int type){
        for (int[] merge : node.mergeInfo) {
            for (Packing p : G.nodeArray[merge[type]].affectedConstraints) p.redoVC();
            redoPackingOfMergedNodes(G.nodeArray[merge[type]], 0);
            redoPackingOfMergedNodes(G.nodeArray[merge[1 - type]], 1);
        }

}
}






