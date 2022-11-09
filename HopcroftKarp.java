
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

public class HopcroftKarp {

   /* int size;
    int nil;
    int[] pair;
    int[] dist;
    OldNode[] oldNodeArray;
    int matching;
    Queue<OldNode> Q;
    Stack<int[]> states;
    Stack<Integer> numMatching;
    Stack<int[]> dists;
    public HopcroftKarp(Graph G){
        Graph B = new Graph();
        int i = 0;
        size = G.nodeArray.length;
        nil = size * 2;
        nodeArray = new Node[nil + 1];
        Node nilNode = new Node(new OldNode("nil")); // end node of BFS/DFS
        nilNode.id = nil;
        oldNodeArray[nil] = nilOldNode;
        for (Node oldNode : G.oldNodeList){
            Node left = new OldNode(oldNode.name);
            left.id = i;
            OldNode right = new OldNode(oldNode.name);
            right.id = i + size;
            B.addEdge(right, nilOldNode);
            oldNodeArray[i] = left;
            oldNodeArray[size + i] = right;
            i++;
        }
        states = new Stack<>();
        dists = new Stack<>();
        numMatching = new Stack<>(); //save all changes for efficient readding
        for (OldNode oldNode : G.oldNodeList){
            for (OldNode neighbour : oldNode.neighbors){
                B.addEdge(oldNodeArray[oldNode.id], oldNodeArray[neighbour.id + size]);
            }
        }
        pair = new int[size * 2 + 1];
        for (int j = 0; j <= nil; j++){
            pair[j] = oldNodeArray[nil].id;
        }
        dist = new int[size * 2 + 1];
        matching = 0;
        while (bfs()){
            for (int u = 0; u < size; u++){
                if (pair[u] == oldNodeArray[nil].id){
                    if (dfs(oldNodeArray[u])){
                        matching++;
                    }
                }
            }
        }
    }
     boolean bfs(){
        Q = new LinkedList<>();
        for (int u = 0; u < size; u++) {
            if (pair[u] == nil && oldNodeArray[u].active) {
                dist[u] = 0;
                Q.add(oldNodeArray[u]);
            } else dist[u] = Integer.MAX_VALUE;
        }
        dist[size * 2] = Integer.MAX_VALUE;
        bfsIteration();
        return dist[size * 2] != Integer.MAX_VALUE;
    }
    private boolean bfsIteration(){
        while (!Q.isEmpty()){
            OldNode u = Q.poll();
            if (dist[u.id] < dist[size * 2]){
                for (OldNode v : u.neighbors){
                    if (dist[pair[v.id]] == Integer.MAX_VALUE && v.active){
                        dist[pair[v.id]] = dist[u.id] + 1;
                        Q.add(oldNodeArray[pair[v.id]]);
                    }
                }
            }
        }
        return dist[size * 2] != Integer.MAX_VALUE;
    }
     boolean dfs(OldNode u){
        if (u != oldNodeArray[nil]){
            for (OldNode v : u.neighbors){
                if (dist[pair[v.id]] == dist[u.id] + 1 && v.active){
                    if (dfs(oldNodeArray[pair[v.id]])){
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

    public void updateDeleteNodes(LinkedList<Node> oldNodes){
        states.push(pair.clone());
        dists.push(dist.clone());
        numMatching.push(matching);
        for (Node Node : oldNodes){
            OldNode partnerV = oldNodeArray[pair[Node.id]];
            OldNode partnerU = oldNodeArray[pair[Node.id + size]];
            if (partnerU != oldNodeArray[nil]) {
                matching --;
                dist[partnerU.id] = 0;
            }
            if (pair[Node.id] != nil) matching--;
            pair[partnerU.id] = nil;
            pair[Node.id] = nil;
            pair[Node.id + size] = nil;
            pair[partnerV.id] = nil;
            oldNodeArray[Node.id].active = false;
            oldNodeArray[Node.id + size].active = false;
        }
        dist[size * 2] = Integer.MAX_VALUE;
        //System.out.println(System.nanoTime() - time);
        for (int u = 0; u < size; u++) {
            if (pair[u] == nil && oldNodeArray[u].active) {
                dist[u] = 0;
                Q.add(oldNodeArray[u]);
            } else dist[u] = Integer.MAX_VALUE;
        }
        if (bfsIteration()){
            for (OldNode n : oldNodeArray){
                if (pair[n.id] == nil && n.active){
                    if (dfs(n)) {
                        matching++;
                    }
                }
            }
        }
        //System.out.println(System.nanoTime() - time);
    }
    public void updateAddNodes(LinkedList<Node> nodes){
        for (Node node : nodes){
            oldNodeArray[node.id].active = true;
            oldNodeArray[node.id + size].active = true;
        }
        pair = states.pop();
        matching = numMatching.pop();
        dist = dists.pop();
    }
    */
}
