import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;
public class HopcroftKarp {
    Graph B; //bipartite graph
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
    /**
     * Constructs the bipartite graph used for a lower bound of vertex cover and
     * runs the Hopcroft-Karp Algorithm to find a maximum matching. The
     * lower bound is saved in lastlowerBound.
     * @param G input Graph
     *
     *
     */
    public HopcroftKarp(Graph G){
        B = new Graph();
        size = G.nodeArray.length;
        nil = size * 2;
        B.nodeArray = new Node[nil + 1];
        Node nilNode = new Node("nil", nil, size); // end node of BFS/DFS
        B.nodeArray[nil] = nilNode;
        for (Node node : G.nodeArray){ //construct bipartite graph
            Node left = new Node(node.name, node.id, node.neighbours.length);
            Node right = new Node(node.name, node.id + size, node.neighbours.length + 1);
            if (!node.active){
                left.active = false;
                right.active = false;
            }
            for (int i = 0; i < node.neighbours.length; i++){
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
        pair = new int[size * 2 + 1];
        for (int j = 0; j <= nil; j++){
            pair[j] = nil;
        }
        dist = new int[size * 2 + 1];
        matching = 0;
        searchForAMatching();
    }

    /**
     * Breadth-first-search.
     * @return true if a path has been found.
     */
     boolean bfs(){
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

    /**
     * depth-first-search.
     * @param u staring node
     * @return true if successful
     */
     boolean dfs(Node u){
        if (u.id != nil){
            for (int[] neighbourInfo : u.neighbours){
                Node v = B.nodeArray[neighbourInfo[0]];
                if (dist[pair[v.id]] == dist[u.id] + 1 && v.active){
                    if (dfs(B.nodeArray[pair[v.id]])){
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
     * deletes the corresponding nodes from a list of nodes that were deleted in the non-bipartite graph.
     * @param nodes list of nodes to remove
     */
    public void updateDeleteNodes(LinkedList<Node> nodes){
        states.push(pair.clone());
        dists.push(dist.clone());
        numMatching.push(matching);
        numLastLowerBound.push(lastLowerBound);
        for (Node node : nodes){
            Node partnerV = B.nodeArray[pair[node.id]];
            Node partnerU = B.nodeArray[pair[node.id + size]];
            if (partnerU.id != nil) matching --;
            if (pair[node.id] != nil) matching--;
            pair[partnerU.id] = nil;
            pair[node.id] = nil;
            pair[node.id + size] = nil;
            pair[partnerV.id] = nil;
            B.nodeArray[node.id].active = false;
            B.nodeArray[node.id + size].active = false;
        }
    }

    /**
     * readd previously deleted nodes from a stack.
     * @param nodes list of nodes to add
     */
    public void updateAddNodes(LinkedList<Node> nodes){
        for (Node node : nodes){
            B.nodeArray[node.id].active = true;
            B.nodeArray[node.id + size].active = true;
        }
        pair = states.pop();
        matching = numMatching.pop();
        dist = dists.pop();
        lastLowerBound = numLastLowerBound.pop();
    }

    /**
     * find augmenting path for currently not-matched nodes.
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
    }
}
