import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;
import java.util.stream.IntStream;




public class Reduction {

    LinkedList<Node> uselessNeighbours;
    LinkedList<Node> VCNodes;

    Stack<int[]> removedNodes;

    Graph G;
    HopcroftKarp hk;

    public Reduction(Graph G, HopcroftKarp hk){
        this.G = G;
        VCNodes = new LinkedList<Node>();
        uselessNeighbours = new LinkedList<Node>();
        removedNodes = new Stack<>();
        this. hk = hk;

    }

    public int rollOutAll(){
        int oldK = VCNodes.size();
        removedNodes.push(new int[]{0});
        removeDegreeOne();
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

    public void removeDegreeTwo(Graph Gr, int k){
        removedNodes.push(new int[]{0});
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
                mergeNodes(first, second, node);
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
                case 2: //usefull Nodes
                    G.reeaddNode(node);
                    VCNodes.remove(node);
                    LinkedList<Node> b = new LinkedList<>();
                    b.add(node);
                    hk.updateAddNodes(b);
                    break;
                case 3: //merged Nodes :(
                    G.reeaddNode(G.nodeArray[action[3]]);
                    G.reeaddNode(G.nodeArray[action[2]]);
                    VCNodes.remove(G.nodeArray[action[3]]);
                    int[] newArray = new int[node.neighbours.length - action[4]];
                    System.arraycopy(node.neighbours, 0, newArray, 0, newArray.length);
                    for (int i = newArray.length; i < node.neighbours.length; i++){
                        Node neighbour = G.nodeArray[node.neighbours[i]];
                        for (int j = 0; i < neighbour.neighbours.length; i++){
                            if (neighbour.neighbours[j] == node.id) {
                                neighbour.neighbours[j] = action[2];
                                neighbour.activeNeighbours--;
                                break;
                            }
                        }
                    }
                    node.neighbours = newArray;
                    node.mergeMagic = null;
                    node.activeNeighbours -= action[4];
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
        

    public LinkedList<Node> reduceThroughCC(CliqueCover cc, int k, Graph G){
        LinkedList<Node> cliqueNeighbours = new LinkedList<>();
        LinkedList<Node> uselessNeighbours = new LinkedList<>();
        for (int i = 0; i < cc.FirstFreeColor; i++) {
            for (Integer nodeId: cc.colorclasses[i]) {
                Node v = G.nodeArray[nodeId];
                if(v.active && v.activeNeighbours > 0 && v.activeNeighbours == (cc.colorclasses[i].size()-1)){
                    if (k >= v.activeNeighbours) {
                        for (int u : v.neighbours) {
                            Node toDelete = G.nodeArray[u];
                            if (toDelete.active) {
                                cliqueNeighbours.add(toDelete);
                                k--;
                                G.removeNode(toDelete);
                            }
                        }
                    }
                    else {
                        return null;
                    }
                    uselessNeighbours.add(v);
                    G.removeNode(v);
                    break;
                }
            }
        }
        this.uselessNeighbours = uselessNeighbours;
        this.VCNodes = cliqueNeighbours;
        return null;
    }

    public void revertReduceCC(Graph G){
        for (Node node: uselessNeighbours) {
            G.reeaddNode(node);
        }
        for (Node node: VCNodes) {
            G.reeaddNode(node);
        }
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
    private void mergeNodes(Node nodeA, Node nodeB, Node nodeC){
        LinkedList<Integer> addedNeighbours = new LinkedList<>();
        G.removeNode(nodeC);
        G.removeNode(nodeB);
        VCNodes.add(nodeC);
        for (int n : nodeB.neighbours){
            Node neighbour = G.nodeArray[n];
            if (neighbour.active && IntStream.of(nodeA.neighbours).noneMatch(x -> x == n) && neighbour != nodeA){
                addedNeighbours.add(n);
                for (int i = 0; i < neighbour.neighbours.length; i++){
                    if (neighbour.neighbours[i] == nodeB.id) {
                        neighbour.neighbours[i] = nodeA.id;
                        neighbour.activeNeighbours++;
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
        if (nodeA.mergeMagic == null) nodeA.mergeMagic = new LinkedList<>();
        nodeA.activeNeighbours += addedNeighbours.size();
        nodeA.mergeMagic.add(new int[]{nodeB.id, nodeC.id});
    }

    private boolean unconfined(Graph G, int v){
        //input: G und index von v, output: v unconfined?
        LinkedList<Integer> S = new LinkedList<Integer>();
        S.add(v);
        LinkedList<Integer> N_S = new LinkedList<Integer>();
        for(Integer i : G.nodeArray[v].neighbours) N_S.add(v);//N_S = N(v)
        Integer final_u = null; //the current best u
        LinkedList<Integer> final_u_Cut = new LinkedList<Integer>(); //N(u) \ N(S) for current u and S
        while(true){
            for(Integer u : N_S){
                LinkedList<Integer> N_u = new LinkedList<>();
                for (Integer i : G.nodeArray[u].neighbours) N_u.add(i);
                if (CheckSizeOfCut(G.nodeArray[u].neighbours, S)){
                    if (final_u == null){//the first node that satisfied |N(u) \cap S| = 1
                        final_u = u;
                        final_u_Cut = GetSizeSetminus(N_u, N_S, Integer.MAX_VALUE);
                    }
                    else{//the other nodes
                        LinkedList<Integer> res = GetSizeSetminus(N_u, N_S, final_u_Cut.size());
                        if (res.size() < final_u_Cut.size()){
                            final_u = u;
                            final_u_Cut = res;
                        }
                    }
                }
            }
            //evaluate N(u) \cap S
            if (final_u==null) return false;
            if (final_u_Cut.size()==0) return true;
            if (final_u_Cut.size()==1) {
                Integer w = final_u_Cut.get(0);
                S.add(w);
                for (Integer i : G.nodeArray[w].neighbours) {
                    if(!N_S.contains(i)){//wir wollen keine Doppelungen
                        N_S.add(i);
                    }
                }
                continue;
            }
            return false;
        }
    }

    private boolean CheckSizeOfCut(int[] N_u, LinkedList<Integer> S){
        //Helper function of unconfined. Returns true if N_u \cap S = 1
        boolean found_one = false;
        for (Integer i : N_u ){
            if (S.contains(i)){
                if (found_one){
                    return false;
                }else{
                found_one = true;
                }
            }
        }
        return found_one;
    }

    private LinkedList<Integer> GetSizeSetminus(LinkedList<Integer> N_u, LinkedList<Integer> N_S, Integer tobeat){
        /*resturns Rest = N_u \ N_S . Stops if Rest.size > tobeat. If lookingforW ist set to true, we are calling the 
        function with intention of checking wether R = {w} for some w. Therefore we can stop if |Rest| > 1. */
        LinkedList<Integer> Rest = new LinkedList<Integer>();
        for (Integer i : N_u){
            if (!N_S.contains(i)) {
                Rest.add(i);
                if (Rest.size() > tobeat) return Rest;
            }
        }
        return Rest;
    }
}




