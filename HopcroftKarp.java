
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

public class HopcroftKarp {
    Graph B;
    int size;
    int nil;
    int[] pair;
    int[] dist;
    int matching;
    Queue<Node> Q;
    Stack<int[]> states;
    Stack<Integer> numMatching;
    Stack<int[]> dists;
    public HopcroftKarp(Graph G){
        B = new Graph();
        size = G.nodeArray.length;
        nil = size * 2;
        B.nodeArray = new Node[nil + 1];
        Node nilNode = new Node("nil", nil, size); // end node of BFS/DFS
        B.nodeArray[nil] = nilNode;
        for (Node node : G.nodeArray){
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
        numMatching = new Stack<>(); //save all changes for efficient readding
        pair = new int[size * 2 + 1];
        for (int j = 0; j <= nil; j++){
            pair[j] = nil;
        }
        dist = new int[size * 2 + 1];
        matching = 0;
        while (bfs()){
            for (int u = 0; u < size; u++){
                if (pair[u] == nil){
                    if (dfs(B.nodeArray[u])){
                        matching++;
                    }
                }
            }
        }
    }
     boolean bfs(){
        Q = new LinkedList<>();
        for (int u = 0; u < size; u++) {
            if (pair[u] == nil && B.nodeArray[u].active) {
                dist[u] = 0;
                Q.add(B.nodeArray[u]);
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
                for (int[] neighbourInfo : u.neighbours){
                    Node v = B.nodeArray[neighbourInfo[0]];
                    if (dist[pair[v.id]] == Integer.MAX_VALUE && v.active){
                        dist[pair[v.id]] = dist[u.id] + 1;
                        Q.add(B.nodeArray[pair[v.id]]);
                    }
                }
            }
        }
        return dist[size * 2] != Integer.MAX_VALUE;
    }
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

    public void updateDeleteNodes(LinkedList<Node> nodes){
        states.push(pair.clone());
        dists.push(dist.clone());
        numMatching.push(matching);
        for (Node node : nodes){
            Node partnerV = B.nodeArray[pair[node.id]];
            Node partnerU = B.nodeArray[pair[node.id + size]];
            if (partnerU.id != nil) {
                matching --;
                dist[partnerU.id] = 0;
            }
            if (pair[node.id] != nil) matching--;
            pair[partnerU.id] = nil;
            pair[node.id] = nil;
            pair[node.id + size] = nil;
            pair[partnerV.id] = nil;
            B.nodeArray[node.id].active = false;
            B.nodeArray[node.id + size].active = false;
        }
        dist[size * 2] = Integer.MAX_VALUE;
        //System.out.println(System.nanoTime() - time);
        for (int u = 0; u < size; u++) {
            if (pair[u] == nil && B.nodeArray[u].active) {
                dist[u] = 0;
                Q.add(B.nodeArray[u]);
            } else dist[u] = Integer.MAX_VALUE;
        }
        if (bfsIteration()){
            for (int i = 0; i <= nil; i++){
                if (pair[i] == nil && B.nodeArray[i].active){
                    if (dfs(B.nodeArray[i])) {
                        matching++;
                    }
                }
            }
        }
        //System.out.println(System.nanoTime() - time);
    }
    public void updateAddNodes(LinkedList<Node> nodes){
        for (Node node : nodes){
            B.nodeArray[node.id].active = true;
            B.nodeArray[node.id + size].active = true;
        }
        pair = states.pop();
        matching = numMatching.pop();
        dist = dists.pop();
    }

}
