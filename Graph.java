import java.util.HashMap;
import java.util.LinkedList;
import java.util.Stack;

public class Graph {

    LinkedList<Node> nodeList;
    Stack<Integer> actions;
    HashMap<String,Node> nodeHashMap;
    int totalEdges;
    public Graph(int size){
        nodeHashMap = new HashMap<>();
        actions = new Stack<>();
        totalEdges = 0;
    }

    public void addEdge(String first, String second){
        if (!nodeHashMap.containsKey(first)) nodeHashMap.put(first,new Node(first));
        if (!nodeHashMap.containsKey(second)) nodeHashMap.put(second, new Node(second));
        addEdge(nodeHashMap.get(first),nodeHashMap.get(second));
    }

    public void addEdge(Node first, Node second){
        first.addEdge(second);
        second.addEdge(first);
        totalEdges++;
    }

    public void addNode(String name){
        Node n = new Node(name);
        nodeList.add(n);
    }

    public void removeEdge(String first, String second){
        removeEdge(nodeHashMap.get(first),nodeHashMap.get(second));
    }

    public void removeEdge(Node first, Node second){
        first.deleteEdge(second);
        second.deleteEdge(first);
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

    public void setNodeList(LinkedList<Node> nodeList) {
        this.nodeList = nodeList;
    }
}
