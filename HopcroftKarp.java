import java.util.*;

public class HopcroftKarp implements Cloneable {
    Graph B; //bipartite graph
    int totalCycleLB;
    int size; //size of one side of the bipartite graph
    int nil; //size of the bipartite graph and index position of nil node
    int[] pair; //matching partner of each node
    int[] dist;
    Queue<Node> Q;
    Stack<int[]> states;
    Stack<int[]> dists;
    Stack<int[]> actions;
    Stack<int[]> oldNeighbourLists;
    Graph G;

    /**
     * Constructs the bipartite graph used for a lower bound of vertex cover and
     * runs the Hopcroft-Karp Algorithm to find a maximum matching. The
     * lower bound is saved in lastlowerBound.
     *
     * @param G input Graph
     */
    public HopcroftKarp(Graph G) {
        this.G = G;
        B = new Graph();
        size = G.nodeArray.length;
        nil = size * 2;
        B.nodeArray = new Node[nil + 1];
        Node nilNode = new Node("nil", nil, size); // end node of BFS/DFS
        B.nodeArray[nil] = nilNode;
        for (Node node : G.nodeArray) { //construct bipartite graph
            Node left = new Node(node.name, node.id, node.neighbours.length);
            Node right = new Node(node.name, node.id + size, node.neighbours.length + 1);
            if (!node.active) {
                left.active = false;
                right.active = false;
            }
            for (int i = 0; i < node.neighbours.length; i++) {
                left.neighbours[i] = node.neighbours[i] + size;
                right.neighbours[i] = node.neighbours[i];
            }
            right.neighbours[right.neighbours.length - 1] = nil;
            nilNode.neighbours[node.id] = node.id;
            B.nodeArray[left.id] = left;
            B.nodeArray[right.id] = right;
        }
        states = new Stack<>();
        dists = new Stack<>();
        oldNeighbourLists = new Stack<>();
        actions = new Stack<>();
        pair = new int[size * 2 + 1];
        for (int j = 0; j <= nil; j++) {
            pair[j] = nil;
        }
        dist = new int[size * 2 + 1];
        searchForAMatching();
    }

    /**
     * Breadth-first-search.
     *
     * @return true if a path has been found.
     */
    boolean bfs() {
        Q = new LinkedList<>();
        for (int u = 0; u < size; u++) {
            if (pair[u] == nil && B.nodeArray[u].active) {
                dist[u] = 0;
                Q.add(B.nodeArray[u]);
            } else dist[u] = Integer.MAX_VALUE;
        }
        dist[nil] = Integer.MAX_VALUE;
        while (!Q.isEmpty()) {
            Node u = Q.poll();
            if (dist[u.id] < dist[nil]) {
                for (int neighbourInfo : u.neighbours) {
                    Node v = B.nodeArray[neighbourInfo];
                    if (dist[pair[v.id]] == Integer.MAX_VALUE && v.active) {
                        dist[pair[v.id]] = dist[u.id] + 1;
                        Q.add(B.nodeArray[pair[v.id]]);
                    }
                }
            }
        }
        return dist[nil] != Integer.MAX_VALUE;
    }

    //not a real constructor, only used for the deepcopy
    public HopcroftKarp() {
    }

    /**
     * depth-first-search.
     *
     * @param u staring node
     * @return true if successful
     */
    boolean dfs(Node u) {
        if (u.id != nil) {
            for (int neighbourInfo : u.neighbours) {
                Node v = B.nodeArray[neighbourInfo];
                if (dist[pair[v.id]] == dist[u.id] + 1 && v.active) {
                    if (dfs(B.nodeArray[pair[v.id]])) {
                        actions.push(new int[]{1,u.id,pair[u.id],v.id,pair[v.id]});
                        pair[v.id] = u.id;
                        pair[u.id] = v.id;
                        return true;
                    }
                }
            }
            dist[u.id] = Integer.MAX_VALUE;
            return false;
        }
        return true;
    }

    /**
     * deletes the corresponding nodes from a list of nodes that were deleted in the non-bipartite graph. Update the bipartite G after we updated the original G
     *
     * @param nodes list of nodes to remove
     */
    public void updateDeleteNodes(LinkedList<Node> nodes) {
        actions.push(new int[]{0});
        for (Node node : nodes) {
            Node partnerV = B.nodeArray[pair[node.id]];
            Node partnerU = B.nodeArray[pair[node.id + size]];
            actions.push(new int[]{2, node.id, partnerV.id, partnerU.id});
            pair[partnerU.id] = nil;
            pair[node.id] = nil;
            pair[node.id + size] = nil;
            pair[partnerV.id] = nil;
            B.nodeArray[node.id].active = false;
            B.nodeArray[node.id + size].active = false;
        }
    }

    public void updateMergeNodes(int nodeA, int nodeB, LinkedList<Integer> toAdd) {
        Node a = B.nodeArray[nodeA];
        int[] newArray = new int[B.nodeArray[nodeA].neighbours.length + toAdd.size()];
        System.arraycopy(a.neighbours, 0, newArray, 0, a.neighbours.length);
        int[] newArray2 = new int[B.nodeArray[nodeA + size].neighbours.length + toAdd.size()];
        System.arraycopy(a.neighbours, 0, newArray2, 0, a.neighbours.length);
        int j = a.neighbours.length;
        for (int neighbour : toAdd) {
            newArray[j] = neighbour + size;
            newArray2[j++] = neighbour;
            Node n = B.nodeArray[neighbour];
            for (int i = 0; i < n.neighbours.length; i++) {
                if (n.neighbours[i] == nodeB + size) {
                    n.neighbours[i] = nodeA + size;
                    B.nodeArray[neighbour + size].neighbours[i] = nodeA;
                    break;
                }
            }
        }
        oldNeighbourLists.push(a.neighbours);
        oldNeighbourLists.push(B.nodeArray[nodeA + size].neighbours);
        a.neighbours = newArray;
        B.nodeArray[nodeA + size].neighbours = newArray2;
        int[] actionArray = new int[3 + toAdd.size()];
        actionArray[0] = 3;
        actionArray[1] = nodeA;
        actionArray[2] = nodeB;
        int k = 3;
        for (int i : toAdd) actionArray[k++] = i;
        actions.push(actionArray);
    }

    /**
     * readd previously deleted nodes from a stack. Inverse of updateDeleteNodes
     *
     * @param nodes list of nodes to add
     */
    public void updateAddNodes(LinkedList<Node> nodes) {
        for (Node node : nodes) {
            B.nodeArray[node.id].active = true;
            B.nodeArray[node.id + size].active = true;
        }
        int[] action = actions.pop();
        while (action[0] != 0) {
            switch (action[0]) {
                case 1:
                    pair[action[1]] = action[2];
                    pair[action[3]] = action[4];
                    break;
                case 2:
                    pair[action[1]] = action[2];
                    if (action[2] != nil) pair[action[2]] = action[1];
                    pair[action[1] + size] = action[3];
                    if (action[3] != nil) pair[action[3]] = action[1] + size;
                    B.nodeArray[action[1]].active = true;
                    B.nodeArray[action[1] + size].active = true;
                    break;
                case 3:
                    int nodeA = action[1];
                    int nodeB = action[2];
                    LinkedList<Integer> toAdd = new LinkedList<>();
                    for (int i = 3; i < action.length; i++) toAdd.add(action[i]);
                    B.nodeArray[nodeB].active = true;
                    B.nodeArray[nodeB + size].active = true;
                    B.nodeArray[nodeA + size].neighbours = oldNeighbourLists.pop();
                    B.nodeArray[nodeA].neighbours = oldNeighbourLists.pop();
                    for (int neighbour : toAdd) {
                        Node n = B.nodeArray[neighbour];
                        for (int i = 0; i < n.neighbours.length; i++) {
                            if (n.neighbours[i] == nodeA + size) {
                                n.neighbours[i] = nodeB + size;
                                B.nodeArray[neighbour + size].neighbours[i] = nodeB;
                                break;
                            }
                        }
                    }
                    break;
            }
            action = actions.pop();
        }
    }
    /**
     * find augmenting path for currently not-matched nodes. Update Matching
     */
    public void searchForAMatching(){
        while (bfs()){
            for (int i = 0; i < size; i++){
                if (pair[i] == nil && B.nodeArray[i].active){
                    dfs(B.nodeArray[i]);
                }
            }
        }
        boolean[] inCycle = new boolean[size];
        totalCycleLB = 0;
        for (int i = 0; i < size; i++){
            if (!inCycle[i] && B.nodeArray[i].active && pair[i] != nil){
                int cycleLB = 1;
                int partner = pair[i];
                inCycle[i] = true;
                while (partner != i + size){
                    if (partner == nil){
                        cycleLB--;
                        break;
                    }
                    if (inCycle[partner - size]){
                        cycleLB -= 2;
                        break;
                    }
                    inCycle[partner - size] = true;
                    partner = pair[partner - size];
                    cycleLB++;
                }
                totalCycleLB += (cycleLB + 1)/ 2;
            }
        }
    }
    public void searchForAMatchingNew(){
        if (G.activeNodes == 0) return;
        while (bfs()){
            for (int i = 0; i < size; i++){
                if (pair[i] == nil && B.nodeArray[i].active){
                    dfs(B.nodeArray[i]);
                }
            }
        }
        boolean[] inCycle = new boolean[size];//cycle cover lower bound, similar to Akiba/Iwata
        int[] cycle = new int[size];
        int[] base = new int[size];
        Arrays.fill(base, size);
        int[] pos = new int[size];
        int cyclCnt = 0;
        totalCycleLB = 0;
        for (int i = 0; i < size; i++){
            if (!inCycle[i] && B.nodeArray[i].active && pair[i] != nil){
                int cycleLB = 1;
                int count = 1;
                int partner = pair[i];
                inCycle[i] = true;
                base[i] = i;
                cycle[0] = i;
                pos[cyclCnt++] = i;
                while (partner != i + size){
                    if (partner == nil){
                        cycleLB--;
                        break;
                    }
                    if (inCycle[partner - size]){
                        cycleLB -= 2;
                        break;
                    }
                    inCycle[partner - size] = true;
                    base[partner - size] = i;
                    cycle[count++] = partner - size;
                    pos[partner - size] = count;
                    partner = pair[partner - size];
                    cycleLB++;
                }
                boolean clique = true;
                for (int j = 0; j < count; j++) {
                    Node v = G.nodeArray[cycle[j]];
                    int num = 0;
                    for (int u : v.neighbours) if (G.nodeArray[u].active && base[u] == base[v.id]) num++;
                    if (num != count - 1) {
                        clique = false;
                        break;
                    }
                }
                if (clique) {
                    totalCycleLB += count - 1;
                }
                else {
                    int[] S2 = new int[size];
                    while (count >= 6) { //TODO finish cycle bound
                        HashSet<Integer> isNeighbour = new HashSet<>();
                        int minSize = count, s = 0, t = count;
                        for (int j = 0; j < count; j++) {
                            int v = cycle[j];
                            isNeighbour.clear();
                            for (int u : G.nodeArray[v].neighbours) if (G.nodeArray[u].active && base[u] == base[v]) {
                                isNeighbour.add(u);
                            }
                            v = cycle[(j + 1) % count];
                            for (int u : G.nodeArray[v].neighbours) if (G.nodeArray[u].active && base[u] == base[v]) {
                                if (isNeighbour.contains(cycle[(pos[u] + 1) % count])) {
                                    int size2 = (pos[u] - j + count) % count;
                                    if (minSize > size2 && size2 % 2 != 0) {
                                        minSize = size2;
                                        s = (j + 1) % count;
                                        t = (pos[u] + 1) % count;
                                    }
                                }
                            }
                        }
                        if (minSize == count) break;
                        int p = 0;
                        for (int j = t; j != s; j = (j + 1) % count) {
                            S2[p++] = cycle[j];
                        }
                        for (int j = s; j != t; j = (j + 1) % count) {
                            base[cycle[j]] = size;
                        }
                        int[] S3 = cycle; cycle = S2; S2 = S3;
                        count -= minSize;
                        totalCycleLB += (minSize + 1) / 2;
                        for (int j = 0; j < count; j++) pos[cycle[j]] = j;
                    }
                    totalCycleLB += (count + 1) / 2;
                }
            }
        }
    }

    @Override
    public Object clone(){
        HopcroftKarp HKn =  new HopcroftKarp();
        HKn.size = this.size;
        HKn.nil = this.nil;
        HKn.pair = (int[]) this.pair.clone();
        HKn.actions = (Stack<int[]>) this.actions.clone();
        HKn.dist = this.dist.clone();
        HKn.states = (Stack<int[]>) this.states.clone();
        HKn.dists = (Stack<int[]>) this.dists.clone();
        HKn.B = (Graph) this.B.clone();
        HKn.totalCycleLB = this.totalCycleLB;
         
        return HKn;
    }
}
