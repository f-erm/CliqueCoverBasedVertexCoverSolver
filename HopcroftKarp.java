import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;
public class HopcroftKarp implements Cloneable {
    Graph B; //bipartite graph
    int totalCycleLB;
    int size; //size of one side of the bipartite graph
    int nil; //size of the bipartite graph and index position of nil node
    int[] pair; //matching partner of each node
    int[] dist;
    int matching;
    int lastLowerBound;
    Queue<Node> Q;
    Stack<int[]> states;
    Stack<Integer> numMatching;
    Stack<Integer> numLastLowerBound;
    Stack<int[]> dists;
    Stack<int[]> actions;

    /**
     * Constructs the bipartite graph used for a lower bound of vertex cover and
     * runs the Hopcroft-Karp Algorithm to find a maximum matching. The
     * lower bound is saved in lastlowerBound.
     *
     * @param G input Graph
     */
    public HopcroftKarp(Graph G) {
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
                left.neighbours[i][0] = node.neighbours[i][0] + size;
                left.neighbours[i][1] = node.neighbours[i][1];
                right.neighbours[i][0] = node.neighbours[i][0];
                right.neighbours[i][1] = node.neighbours[i][1];
            }
            right.neighbours[right.neighbours.length - 1][0] = nil;
            right.neighbours[right.neighbours.length - 1][1] = node.id;
            nilNode.neighbours[node.id][0] = node.id;
            nilNode.neighbours[node.id][1] = nil;
            B.nodeArray[left.id] = left;
            B.nodeArray[right.id] = right;
        }
        states = new Stack<>();
        dists = new Stack<>();
        numLastLowerBound = new Stack<>();
        numMatching = new Stack<>(); //save all changes for efficient readding
        actions = new Stack<>();
        pair = new int[size * 2 + 1];
        for (int j = 0; j <= nil; j++) {
            pair[j] = nil;
        }
        dist = new int[size * 2 + 1];
        matching = 0;
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
                for (int[] neighbourInfo : u.neighbours) {
                    Node v = B.nodeArray[neighbourInfo[0]];
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
            for (int[] neighbourInfo : u.neighbours) {
                Node v = B.nodeArray[neighbourInfo[0]];
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
        numMatching.push(matching);
        for (Node node : nodes) {
            Node partnerV = B.nodeArray[pair[node.id]];
            Node partnerU = B.nodeArray[pair[node.id + size]];
            if (partnerU.id != nil) {
                matching--;
            }
            if (pair[node.id] != nil) {
                matching--;
            }
            actions.push(new int[]{2, node.id, partnerV.id, partnerU.id});
            pair[partnerU.id] = nil;
            pair[node.id] = nil;
            pair[node.id + size] = nil;
            pair[partnerV.id] = nil;
            B.nodeArray[node.id].active = false;
            B.nodeArray[node.id + size].active = false;
        }
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
        matching = numMatching.pop();
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
                    if (dfs(B.nodeArray[i])) {
                        matching++;
                    }
                }
            }
        }
        lastLowerBound = matching/2;
        boolean[] inCycle = new boolean[size];
        totalCycleLB = 0;
        for (int i = 0; i < size; i++){
            if (!inCycle[i] && B.nodeArray[i].active && pair[i] != nil){
                int cycleLB = 1;
                int partner = pair[i];
                inCycle[i] = true;
                while (partner != i + size){
                    if (partner == nil || inCycle[partner - size]){
                        cycleLB--;
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

    @Override
    public Object clone(){
        HopcroftKarp HKn =  new HopcroftKarp();
        HKn.size = this.size;
        HKn.nil = this.nil;
        HKn.pair = (int[]) this.pair.clone();
        HKn.actions = (Stack<int[]>) this.actions.clone();
        HKn.dist = this.dist.clone();
        HKn.matching = this.matching;
        HKn.lastLowerBound = this.lastLowerBound;
        HKn.states = (Stack<int[]>) this.states.clone();
        HKn.numMatching = (Stack<Integer>) this.numMatching.clone();
        HKn.dists = (Stack<int[]>) this.dists.clone();
        HKn.numLastLowerBound = (Stack<Integer>) this.numLastLowerBound.clone();
        HKn.B = (Graph) this.B.clone();
        HKn.totalCycleLB = this.totalCycleLB;
         
        return HKn;
    }
}
