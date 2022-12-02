import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;
import java.util.stream.IntStream;
import java.util.Collections;
import java.util.BitSet;




public class Reduction {

    LinkedList<Node> uselessNeighbours;
    LinkedList<Node> VCNodes;
    Stack<int[]> mergedNodes;
    Stack<int[]> removedNodes;

    Graph G;
    HopcroftKarp hk;

    public Reduction(Graph G, HopcroftKarp hk){
        this.G = G;
        VCNodes = new LinkedList<>();
        uselessNeighbours = new LinkedList<>();
        removedNodes = new Stack<>();
        mergedNodes = new Stack<>();
        this. hk = hk;

    }

    public int rollOutAll(int k){
        int oldK = VCNodes.size();
        removedNodes.push(new int[]{0});
        removeDegreeOne();
        applyUnconfined();
        removeDegreeTwo();
        removeDegreeZero();
        if (BussRule(k)){
            return k+1;
        }
        return VCNodes.size() - oldK;
    }


    public void improvedLP(Graph G){
        HopcroftKarp hk = new HopcroftKarp(G);
        int size = hk.size;
        int bipartiteSize = hk.nil;
        LinkedList<Integer>[] residualGraph = new LinkedList[bipartiteSize + 2];//the residual graph is saved in an array of linked lists as adjacency lists.
        Node s = new Node("s", bipartiteSize,bipartiteSize/2);//add s and t nodes
        Node t = new Node("t", bipartiteSize + 1,bipartiteSize/2);
        residualGraph[s.id] = new LinkedList<>();
        residualGraph[t.id] = new LinkedList<>();
        for (int i = 0; i < size; i++){
            residualGraph[i] = new LinkedList<>();
            if (G.nodeArray[i].active) {
                for (Integer integer : G.nodeArray[i].neighbours) residualGraph[i].add(integer + size);
                if (hk.pair[i] == hk.nil) residualGraph[s.id].add(i);
                else residualGraph[i].add(s.id);
            }
        }
        for (int i = G.nodeArray.length; i < bipartiteSize; i++){
            residualGraph[i] = new LinkedList<>();
            if (G.nodeArray[i - size].active){
                if (hk.pair[i] == hk.nil) residualGraph[i].add(t.id);
                else {
                    residualGraph[t.id].add(i);
                    residualGraph[i].add(hk.pair[i]);
                }
            }
        }
        boolean[] reachedFromS = new boolean[bipartiteSize + 2];
        //run BFS to find nodes reached from s:
        Queue<Integer> q = new LinkedList<>();
        q.offer(s.id);
        while (!q.isEmpty()){
            int v = q.poll();
            reachedFromS[v] = true;
            for (int w : residualGraph[v]){
                if (!reachedFromS[w]){
                    reachedFromS[w] = true;
                    q.offer(w);
                }
            }
        }
        for (int i = 0; i < size; i++){
            if (reachedFromS[i] && !reachedFromS[i + size]) removeUselessNodes(G.nodeArray[i]); //LP solution = 0
            else if (!reachedFromS[i] && reachedFromS[i + size]) removeVCNodes(G.nodeArray[i]); //LP solution = 1
        }
    }

    public void removeDegreeTwo(){
        for (Node node : G.nodeArray){
            if (node.active && node.activeNeighbours == 2){
                Node first = null, second = null;
                int it = 0;
                while (first == null){
                    Node current = G.nodeArray[node.neighbours[it++]];
                    if (current.active) first = current;
                }
                while (second == null){
                    Node current = G.nodeArray[node.neighbours[it++]];
                    if (current.active) second = current;
                }
                if (!arrayContains(first.neighbours, second.id)) mergeNodes(first, second, node); // case v,w \in E
                else{ // case v,w \not \in E
                    removeUselessNodes(node);
                    removeVCNodes(first);
                    removeVCNodes(second);
                }
            }
        }
    }

    public void removeDegreeLargerKAndZero(Graph G, int k){
        LinkedList<Node> partialSolution = new LinkedList<>();
        for (Node node : G.nodeArray){
            if (node.activeNeighbours > k){
                partialSolution.add(node);
                G.removeNode(node);
                removedNodes.push(new int[]{2,node.id});
            }
            else if (node.activeNeighbours == 0){
                G.removeNode(node);
                removedNodes.push(new int[]{1, node.id});
            }
        }
        G.partialSolution.addAll(partialSolution);
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
                    VCNodes.remove(G.nodeArray[action[3]]);
                    int[] newArray = new int[node.neighbours.length - action[4]];
                    System.arraycopy(node.neighbours, 0, newArray, 0, newArray.length);
                    for (int i = newArray.length; i < node.neighbours.length; i++){
                        Node neighbour = G.nodeArray[node.neighbours[i]];
                        for (int j = 0; j < neighbour.neighbours.length; j++){
                            if (neighbour.neighbours[j] == node.id) {
                                neighbour.neighbours[j] = action[2];
                                neighbour.activeNeighbours--;
                                G.totalEdges--;
                                break;
                            }
                        }
                    }
                    node.neighbours = newArray;
                    mergedNodes.pop();
                    node.activeNeighbours -= action[4];
                    G.reeaddNode(G.nodeArray[action[2]]);
                    G.reeaddNode(G.nodeArray[action[3]]);
                    break;
            }
            action = removedNodes.pop();
        }
    }

    private void removeDegreeOne(){
        boolean changed = true;
        while (changed){
            changed = false;
            for (Node node : G.nodeArray) {
                if(node.activeNeighbours == 1 && node.active){
                    int i = 0;
                    while (!G.nodeArray[node.neighbours[i]].active){
                        i++;
                    }
                    Node neighbour = G.nodeArray[node.neighbours[i]];
                    removeVCNodes(neighbour);
                    removeUselessNodes(node);
                    changed = true;
                    break;
                }
            }
        }
    }

    private void removeDegreeZero(){
        for (Node node : G.nodeArray) {
            if(node.activeNeighbours == 0 && node.active){
                removeUselessNodes(node);
            }
        }
    }

    private boolean BussRule(int k){
        return (G.activeNodes > (k * k) + k || G.totalEdges > k * k);
    }


    public int reduceThroughCC(CliqueCover cc, int k, Graph G){
        LinkedList<Node> cliqueNeighbours = new LinkedList<>();
        for (int i = 0; i < cc.FirstFreeColor; i++) {
            for (Integer nodeId: cc.colorclasses[i]) {
                Node v = G.nodeArray[nodeId];
                if(v.active && v.activeNeighbours > 0 && v.activeNeighbours == (cc.colorclasses[i].size()-1)){
                    if (k >= v.activeNeighbours) {
                        for (int u : v.neighbours) {
                            Node toDelete = G.nodeArray[u];
                            if (toDelete.active) {
                                removeVCNodes(toDelete);
                                k--;
                            }
                        }
                    }
                    else {
                        return -1;
                    }
                    removeUselessNodes(v);
                    break;
                }
            }
        }
        return k;
    }


    private void removeUselessNodes(Node node){
        removedNodes.push(new int[]{1, node.id});
        G.removeNode(node);
        LinkedList<Node> a = new LinkedList<>();
        a.add(node);
        hk.updateDeleteNodes(a);
    }

    private void removeVCNodes(Node node){
        removedNodes.push(new int[]{2, node.id});
        VCNodes.add(node);
        G.removeNode(node);
        LinkedList<Node> a = new LinkedList<>();
        a.add(node);
        hk.updateDeleteNodes(a);
    }

    /**
     * @param nodeA node to be merged and to remain active
     * @param nodeB node to be merged into nodeA
     * @param nodeC node with two neighbours nodeA and nodeB
     *              merges nodeB into nodeA, adds nodeC to the vertex cover and saves the action
     *              (Array resizing is done here!)
     */
    private void mergeNodes(Node nodeA, Node nodeB, Node nodeC){
        LinkedList<Integer> addedNeighbours = new LinkedList<>();
        G.removeNode(nodeC);
        G.removeNode(nodeB);
        VCNodes.add(nodeC);
        for (int n : nodeB.neighbours){
            Node neighbour = G.nodeArray[n];
            if (neighbour.active && IntStream.of(nodeA.neighbours).noneMatch(x -> x == n)){
                addedNeighbours.add(n);
                for (int i = 0; i < neighbour.neighbours.length; i++){
                    if (neighbour.neighbours[i] == nodeB.id) {
                        neighbour.neighbours[i] = nodeA.id;
                        neighbour.activeNeighbours++;
                        G.totalEdges++;
                        break;
                    }
                }
            }
        }
        int[] newArray = new int[nodeA.neighbours.length + addedNeighbours.size()];
        System.arraycopy(nodeA.neighbours, 0, newArray, 0, nodeA.neighbours.length);
        int j = nodeA.neighbours.length;
        for (int id : addedNeighbours){
            newArray[j++] = id;
        }
        nodeA.neighbours = newArray;
        removedNodes.push(new int[]{3, nodeA.id, nodeB.id, nodeC.id, addedNeighbours.size()});
        nodeA.activeNeighbours += addedNeighbours.size();
        mergedNodes.push(new int[]{nodeA.id, nodeB.id, nodeC.id});
    }

    private boolean arrayContains(int[] array, int el){
        for (int i : array) if (i == el) return true;
        return false;
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

    
    private void applyUnconfined(){
        //remove all unconfined nodes. also checks Neighbors of every removed node
        BitSet N_S = new BitSet(G.nodeArray.length);
        Queue<Integer> q = new LinkedList<>();
        boolean[] checked = new boolean[G.nodeArray.length];
        for(int i=0;i< G.nodeArray.length;i++){//geht das besser?
            if (G.nodeArray[i].active && new_unconfined(i,N_S)) {
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
    }


            



}






