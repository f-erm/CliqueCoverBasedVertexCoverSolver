import java.util.*;

public class Reduction {

    LinkedList<Node> uselessNeighbours;
    LinkedList<Node> VCNodes;
    Stack<int[]> mergedNodes;
    Stack<int[]> removedNodes;
    Stack<Integer> neighbourPosStack;
    Stack<Integer> triangleAdditions;
    Graph G;
    HopcroftKarp hk;
    long deg1Time = 0;
    int deg1cuts = 0;
    long deg2Time = 0;
    int deg2cuts = 0;
    long deg0Time = 0;
    int deg0cuts = 0;
    int domcuts = 0;
    long domtime = 0;
    long unconfTime = 0;
    int unconfcuts = 0;
    long bussTime = 0;
    int busscuts = 0;
    long lpTime = 0;
    int lpcuts = 0;
    int twincuts = 0;
    long twintime = 0;
    long cctime= 0;
    int cccuts = 0;
    long merge1 = 0;
    long merge2 = 0;
    long merge3 = 0;

    public Reduction(Graph G, HopcroftKarp hk) {
        this.G = G;
        VCNodes = new LinkedList<>();
        uselessNeighbours = new LinkedList<>();
        removedNodes = new Stack<>();
        mergedNodes = new Stack<>();
        this.hk = hk;
        neighbourPosStack = new Stack<>();
        triangleAdditions = new Stack<>();
    }

    public int rollOutAll(int k, boolean doReduction){
        int oldK = VCNodes.size();
        removedNodes.push(new int[]{0});
        removeDegreeX(k);
        removeDominating();
        long time = System.nanoTime();
        if (doReduction) removeTwin();
        twintime += System.nanoTime() - time;
        if (VCNodes.size() - oldK > k) return k + 1;
        time = System.nanoTime();
        if (doReduction) applyUnconfined();
        if (VCNodes.size() - oldK > k) return k + 1;
            unconfTime += System.nanoTime() - time;
            time = System.nanoTime();
            if (doReduction) improvedLP(G);
            if (VCNodes.size() - oldK > k) return k + 1;
            lpTime += System.nanoTime() - time;
            removeDegreeX(k);
            removeDominating();
            if (BussRule(k)) {
                return k + 1;
            }
        return VCNodes.size() - oldK;
    }

    public boolean removeTwin() {
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
                        twincuts++;
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
        return false;
    }

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
            VeryStrongComponentsFinder vscf = new VeryStrongComponentsFinder(hk.B, residualGraph);
            LinkedList<LinkedList<Integer>> scc = vscf.findStrongComponents();
            if (scc.isEmpty()) break;
            for (LinkedList<Integer> component : scc) {
                for (int n : component) {
                    lpcuts++;
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

    private void removeDegreeX(int k) {
        boolean changed = true;
        while (changed) {
            changed = false;
        for (Node node : G.nodeArray) {
            if (!node.active) continue;
            if (node.activeNeighbours == 1) {
                deg1cuts++;
                int i = 0;
                while (!G.nodeArray[node.neighbours[i]].active) {
                    i++;
                }
                Node neighbour = G.nodeArray[node.neighbours[i]];
                removeVCNodes(neighbour);
                removeUselessNodes(node);
                changed = true;
                break;
            }
            if (node.activeNeighbours == 2) {
                deg2cuts++;
                long time = System.nanoTime();
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
                    removeVCNodes(node);
                    removeUselessNodes(second);
                    mergeNodes(first, second); // case v,w \not \in E
                    mergedNodes.push(new int[]{first.id, second.id, node.id});
                } else { // case v,w \in E
                    removeUselessNodes(node);
                    removeVCNodes(first);
                    removeVCNodes(second);
                }
                changed = true;
                deg2Time += System.nanoTime() - time;
                break;
            }
            if (node.activeNeighbours > k) {
                removeVCNodes(node);
                changed = true;
                break;
            }
            if (node.activeNeighbours == 0) {
                deg0cuts++;
                removeUselessNodes(node);
                changed = true;
                break;
            }
        }
    }
}

    /**
     * Goes through the dynamic dominatingNodes list and adds the dominating nodes to the vertex cover.
     * TODO: check if dominated dominating vertices should be added to the VC
     */
private void removeDominating(){
    long time = System.nanoTime();
    while (!G.dominatingNodes.isEmpty()){
        Node dominating = G.nodeArray[G.dominatingNodes.poll()];
        Node dominated = G.nodeArray[G.dominatingNodes.poll()];
        if (dominating.active && dominated.active /*&& !dominating.dominated*/ && dominated.triangleCounts[findInArray(dominated.neighbours, dominating.id)] + 1 == dominated.activeNeighbours){
            removeVCNodes(dominating);
            domcuts++;
            for (int n : dominated.neighbours){
                if (G.nodeArray[n].active && !arrayContains(dominating.neighbours, n)){
                    int aa = 0;
                }
            }
        }
    }
    domtime += System.nanoTime() - time;
}
    private int findInArray(int[] array, int el){
        for (int i = 0; i < array.length; i++) if (array[i] == el) return i;
        return -1;
    }

    public void revertReduction() {
        int[] action = removedNodes.pop();
        while (action[0] != 0) {
            Node node = G.nodeArray[action[1]];
            switch (action[0]) {
                case 1: // useless Nodes
                    G.reeaddNode(node);
                    LinkedList<Node> a = new LinkedList<>();
                    a.add(node);
                    hk.updateAddNodes(a);
                    break;
                case 2: //useful Nodes
                    G.reeaddNode(node);
                    VCNodes.remove(node);
                    LinkedList<Node> b = new LinkedList<>();
                    b.add(node);
                    hk.updateAddNodes(b);
                    break;
                case 3: //merged Nodes :(
                    //VCNodes.remove(G.nodeArray[action[3]]);
                    int[] newArray = new int[node.neighbours.length - action[3]];
                    int[] newPositionArray = new int[newArray.length];
                    int[] newTriangleArray = new int[newArray.length];
                    System.arraycopy(node.neighbours, 0, newArray, 0, newArray.length);
                    System.arraycopy(node.neighbourPositions, 0, newPositionArray, 0, newArray.length);
                    System.arraycopy(node.triangleCounts, 0, newTriangleArray, 0, newArray.length);
                    for (int i = node.neighbours.length - 1; i >= newArray.length; i--){
                        Node neighbour = G.nodeArray[node.neighbours[i]];
                        for (int j = 0; j < neighbour.neighbours.length; j++){
                            if (neighbour.neighbours[j] == node.id) {
                                neighbour.neighbourPositions[j] = neighbourPosStack.pop();
                                neighbour.triangleCounts[j] = G.nodeArray[action[2]].triangleCounts[neighbour.neighbourPositions[j]];
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
                    node.triangleCounts = newTriangleArray;
                    mergedNodes.pop();
                    for (int i = 0; i < action[4]; i++) node.triangles.removeLast();
                    while (true){
                        int n = triangleAdditions.pop();
                        if (n == -1) break;
                        G.nodeArray[n].triangles.removeLast();
                        G.nodeArray[n].triangles.removeLast();
                        G.nodeArray[n].triangles.removeLast();
                    }
                    break;
            }
            action = removedNodes.pop();
        }
    }

    private boolean BussRule(int k){
        return (G.activeNodes > ((long) k * (long) k) + (long) k || G.totalEdges > (long) k * (long) k);
    }

    public int reduceThroughCC(CliqueCover cc, int k, Graph G){
        long time = System.nanoTime();
        LinkedList<Node> cliqueNeighbours = new LinkedList<>();
        for (int i = 0; i < cc.FirstFreeColor; i++) {
            for (Integer nodeId: cc.colorclasses[i]) {
                Node v = G.nodeArray[nodeId];
                if(v.active && v.activeNeighbours > 0 && v.activeNeighbours == (cc.colorclasses[i].size()-1)){
                    if (k >= v.activeNeighbours) {
                        cccuts++;
                        for (int u : v.neighbours) {
                            Node toDelete = G.nodeArray[u];
                            if (toDelete.active) {
                                removeVCNodes(toDelete);
                                k--;
                            }
                        }
                    }
                    else {
                        cctime += System.nanoTime() - time;
                        return -1;
                    }
                    removeUselessNodes(v);
                    break;
                }
            }
        }
        cctime += System.nanoTime() - time;
        return k;
    }


    private void removeUselessNodes(Node node){
        removedNodes.push(new int[]{1, node.id});
        G.removeNode(node);
        LinkedList<Node> a = new LinkedList<>();
        a.add(node);
        //for the heuristic we don't need to compute hk, so it is null
        if(hk != null){
            hk.updateDeleteNodes(a);
        }
    }

    private void removeVCNodes(Node node){
        removedNodes.push(new int[]{2, node.id});
        VCNodes.add(node);
        G.removeNode(node);
        LinkedList<Node> a = new LinkedList<>();
        a.add(node);
        //for the heuristic we don't need to compute hk, so it is null
        if(hk != null){
            hk.updateDeleteNodes(a);
        }
    }

    /**
     * @param nodeA node to be merged and to remain active
     * @param nodeB node to be merged into nodeA
     * @implNote     merges nodeB into nodeA
     *              (Array resizing is done here!)
     */
    private void mergeNodes(Node nodeA, Node nodeB){
        triangleAdditions.push(-1);
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
        int[] newTriangleArray = new int[nodeA.neighbours.length + addedNeighbours.size()];
        System.arraycopy(nodeA.neighbours, 0, newArray, 0, nodeA.neighbours.length);
        System.arraycopy(nodeA.neighbourPositions, 0, newPositionArray, 0, nodeA.neighbours.length);
        System.arraycopy(nodeA.triangleCounts, 0, newTriangleArray, 0, nodeA.neighbours.length);
        int j = nodeA.neighbours.length;
        for (int id : addedNeighbours){
            newArray[j] = id;
            int nodeBPos = nodeBPositions.poll();
            neighbourPosStack.push(G.nodeArray[id].neighbourPositions[nodeBPos]);
            G.nodeArray[id].neighbourPositions[nodeBPos] = j;
            newPositionArray[j] = nodeBPos;
            int tn = getTriangleNumber(id, nodeA.id, nodeBPos, j); //(nodeA,id) edges may have new triangles
            G.nodeArray[id].triangleCounts[nodeBPos] = tn;
            newTriangleArray[j++] = tn;
        }
        //update triangles and triangleCount after merge.
        ListIterator<Integer> li = nodeB.triangles.listIterator();
        int count = 0;
        while(li.hasNext()) {
            int a = li.next();
            int b = li.next();
            int pos = li.next();
            int indexA = addedNeighbours.indexOf(nodeB.neighbours[a]);
            int indexB = addedNeighbours.indexOf(nodeB.neighbours[b]);
            if (indexA >= 0 && indexB >= 0) {
                nodeA.triangles.add(indexA + nodeA.neighbours.length);
                nodeA.triangles.add(indexB + nodeA.neighbours.length);
                nodeA.triangles.add(pos);
                newTriangleArray[indexA + nodeA.neighbours.length]++;
                newTriangleArray[indexB + nodeA.neighbours.length]++;
                G.nodeArray[nodeB.neighbours[a]].triangleCounts[newPositionArray[indexA + nodeA.neighbours.length]]++;
                G.nodeArray[nodeB.neighbours[b]].triangleCounts[newPositionArray[indexB + nodeA.neighbours.length]]++;
                count++;
            }
        }
        //add dominating vertices to the dynamic list.
        j = nodeA.neighbours.length;
        for (int id : addedNeighbours){
            int tn = newTriangleArray[j++];
            if (tn + 1 == G.nodeArray[id].activeNeighbours){
                G.dominatingNodes.offer(nodeA.id);
                G.dominatingNodes.offer(id);
            }
            else if (tn + 1 == nodeA.activeNeighbours + addedNeighbours.size()){
                G.dominatingNodes.offer(id);
                G.dominatingNodes.offer(nodeA.id);
            }
        }
        nodeA.neighbours = newArray;
        nodeA.neighbourPositions = newPositionArray;
        nodeA.triangleCounts = newTriangleArray;
        removedNodes.push(new int[]{3, nodeA.id, nodeB.id, addedNeighbours.size(), count * 3});
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
    private int getTriangleNumber(int a, int b, int vPos, int uPos) {
        Node u = G.nodeArray[a];
        Node v = G.nodeArray[b];
        int count = 0;
        for (int i = 0; i < u.neighbours.length; i++) {
            int cont = findInArray(v.neighbours, u.neighbours[i]);
            if (cont >= 0){
                count++;
                //change triangles and store the information on a stack for easy reverting
                u.triangles.add(vPos);
                u.triangles.add(i);
                u.triangles.add(cont);
                triangleAdditions.push(a);
                v.triangles.add(uPos);
                v.triangles.add(cont);
                v.triangles.add(i);
                triangleAdditions.push(b);
                G.nodeArray[u.neighbours[i]].triangles.add(u.neighbourPositions[i]);
                G.nodeArray[u.neighbours[i]].triangles.add(v.neighbourPositions[cont]);
                G.nodeArray[u.neighbours[i]].triangles.add(vPos);
                triangleAdditions.push(u.neighbours[i]);
            }
        }
        return count;
    }


    private boolean new_unconfined(Integer v, BitSet N_S){
        //check node for unconfined. Bitsset needs to be initialized to all zeros, all changes will be reverted in the end
        LinkedList<Integer> S = new LinkedList<Integer>();
        S.add(v);
        LinkedList<Integer> N_S_aslist = new LinkedList<Integer>();
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
                final_u = null;
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
                unconfcuts++;
                changed = true;
                removeVCNodes(G.nodeArray[i]);
                for(Integer j : G.nodeArray[i].neighbours){
                    q.offer(j);
                }
            }
        }
        while(!q.isEmpty()){
            Integer elem = q.poll();
            if (checked[elem]==true) continue;
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

}






