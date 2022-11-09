
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

public class HopcroftKarp {

    int size;
    int nil;
    int[] pair;
    int[] dist;
    Node[] nodeArray;
    int matching;
    Queue<Node> Q;
    Stack<int[]> states;
    Stack<Integer> numMatching;
    Stack<int[]> dists;
    public HopcroftKarp(Graph G){
        Graph B = new Graph(42);
        int i = 0;
        size = G.nodeList.size();
        nil = size * 2;
        nodeArray = new Node[nil + 1];
        Node nilNode = new Node("nil"); // end node of BFS/DFS
        nilNode.id = nil;
        nodeArray[nil] = nilNode;
        for (Node node : G.nodeList){
            node.id = i;
            Node left = new Node(node.name);
            left.id = i;
            Node right = new Node(node.name);
            right.id = i + size;
            B.addEdge(right, nilNode);
            nodeArray[i] = left;
            nodeArray[size + i] = right;
            i++;
        }
        states = new Stack<>();
        dists = new Stack<>();
        numMatching = new Stack<>(); //save all changes for efficient readding
        for (Node node : G.nodeList){
            for (Node neighbour : node.neighbors){
                B.addEdge(nodeArray[node.id], nodeArray[neighbour.id + size]);
            }
        }
        pair = new int[size * 2 + 1];
        for (int j = 0; j <= nil; j++){
            pair[j] = nodeArray[nil].id;
        }
        dist = new int[size * 2 + 1];
        matching = 0;
        while (bfs()){
            for (int u = 0; u < size; u++){
                if (pair[u] == nodeArray[nil].id){
                    if (dfs(nodeArray[u])){
                        matching++;
                    }
                }
            }
        }
    }
     boolean bfs(){
        Q = new LinkedList<>();
        for (int u = 0; u < size; u++) {
            if (pair[u] == nil && nodeArray[u].active) {
                dist[u] = 0;
                Q.add(nodeArray[u]);
            } else dist[u] = Integer.MAX_VALUE;
        }
        dist[size * 2] = Integer.MAX_VALUE;
        bfsIteration();
        return dist[size * 2] != Integer.MAX_VALUE;
    }
    private boolean bfsIteration(){
        while (!Q.isEmpty()){
            Node u = Q.poll();
            if (dist[u.id] < dist[size * 2]){
                for (Node v : u.neighbors){
                    if (dist[pair[v.id]] == Integer.MAX_VALUE && v.active){
                        dist[pair[v.id]] = dist[u.id] + 1;
                        Q.add(nodeArray[pair[v.id]]);
                    }
                }
            }
        }
        return dist[size * 2] != Integer.MAX_VALUE;
    }
     boolean dfs(Node u){
        if (u != nodeArray[nil]){
            for (Node v : u.neighbors){
                if (dist[pair[v.id]] == dist[u.id] + 1 && v.active){
                    if (dfs(nodeArray[pair[v.id]])){
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

    public void updateDeleteNodes(LinkedList<Node> nodes){
        states.push(pair.clone());
        dists.push(dist.clone());
        numMatching.push(matching);
        for (Node node : nodes){
            Node partnerV = nodeArray[pair[node.id]];
            Node partnerU = nodeArray[pair[node.id + size]];
            if (partnerU != nodeArray[nil]) {
                matching --;
                dist[partnerU.id] = 0;
            }
            if (pair[node.id] != nil) matching--;
            pair[partnerU.id] = nil;
            pair[node.id] = nil;
            pair[node.id + size] = nil;
            pair[partnerV.id] = nil;
            nodeArray[node.id].active = false;
            nodeArray[node.id + size].active = false;
        }
        dist[size * 2] = Integer.MAX_VALUE;
        //System.out.println(System.nanoTime() - time);
        for (int u = 0; u < size; u++) {
            if (pair[u] == nil && nodeArray[u].active) {
                dist[u] = 0;
                Q.add(nodeArray[u]);
            } else dist[u] = Integer.MAX_VALUE;
        }
        if (bfsIteration()){
            for (Node n : nodeArray){
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
            nodeArray[node.id].active = true;
            nodeArray[node.id + size].active = true;
        }
        pair = states.pop();
        matching = numMatching.pop();
        dist = dists.pop();
    }
}
