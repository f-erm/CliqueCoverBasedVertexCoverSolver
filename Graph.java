import java.util.HashMap;
import java.util.Stack;

public class Graph {

    Node[] nodes;
    Stack<Integer> actions;
    HashMap<String,Node> nodeHashMap;
    public Graph(int size){
        nodeHashMap = new HashMap<>();
        actions = new Stack<>();
    }
    public void addEdge(int first, int second){
        // TODO check whether there is already an edge

        nodes[first].addEdge(nodes[second]);
        nodes[second].addEdge(nodes[first]);
    }

    public void addEdge(String first, String second){
        if (!nodeHashMap.containsKey(first)) nodeHashMap.put(first,new Node(1));
        if (!nodeHashMap.containsKey(second)) nodeHashMap.put(second, new Node(2));
        nodeHashMap.get(first).addEdge(nodeHashMap.get(second));
        nodeHashMap.get(second).addEdge(nodeHashMap.get(first));
    }

    public void removeEdge(int first, int second){
        removeEdge(nodes[first],nodes[second]);
    }

    public void removeEdge(String first, String second){
        removeEdge(nodeHashMap.get(first),nodeHashMap.get(second));
    }

    public void removeEdge(Node first, Node second){
        first.deleteEdge(second);
        second.deleteEdge(first);
    }

    public void removeNode(int num){
        removeNode(nodes[num]);
    }

    public void removeNode(String key){
        removeNode(nodeHashMap.get(key));
    }

    public void removeNode(Node toRemove){
        for (Node node : toRemove.neighbors){
            node.deleteEdge(toRemove);
            toRemove.deleteEdge(node);
        }
    }

}
