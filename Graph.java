import java.util.HashMap;
import java.util.LinkedList;
import java.util.Stack;

public class Graph {

    LinkedList<OldNode> oldNodeList;
    //LinkedList<Node> nodeList;
    Node[] nodeArray;
    Stack<Integer> actions;
    HashMap<String, OldNode> nodeHashMap;

    int totalEdges;
    int maxNodeDegree;
    public Graph(){
        nodeHashMap = new HashMap<>();
        actions = new Stack<>();
        totalEdges = 0;
        maxNodeDegree = 0;
    }

    public void addEdge(String first, String second){

        OldNode firstOldNode = nodeHashMap.get(first);
        if (firstOldNode == null) nodeHashMap.put(first, firstOldNode = new OldNode(first));
        OldNode secondOldNode = nodeHashMap.get(second);
        if (secondOldNode == null) nodeHashMap.put(second, secondOldNode = new OldNode(second));
        addEdge(firstOldNode, secondOldNode);

    }


    public void addEdge(OldNode first, OldNode second){
        first.addEdge(second);
        second.addEdge(first);
        totalEdges++;
    }

    public void removeEdge(OldNode first, OldNode second){
        first.deleteEdge(second);
        second.deleteEdge(first);
    }

    public void removeNode(Node n){
        n.active = false;
        for (int i = 0; i < n.neighbours.length; i++){
            nodeArray[n.neighbours[i][0]].activeNeighbours--;
        }
        totalEdges = totalEdges - n.activeNeighbours;
    }

    public void reeaddNode(Node n){
        n.active = true;
        for (int i = 0; i < n.neighbours.length; i++){
            nodeArray[n.neighbours[i][0]].activeNeighbours++;
        }
        totalEdges += n.activeNeighbours;
    }


    // this function does not actually delete a node but only removes the node from the list and all the pointers from the neighbours in the list to u.
    public void removeNode(OldNode toRemove){
        for (OldNode oldNode : toRemove.neighbors){
            oldNode.deleteEdge(toRemove);
            totalEdges --;
        }
        oldNodeList.remove(toRemove);
    }

    public void reeaddNode(OldNode toAdd){
        oldNodeList.add(toAdd);
        for (OldNode oldNode : toAdd.neighbors){
            oldNode.neighbors.add(toAdd);
            totalEdges ++;
        }
    }


    public void setNodeList(LinkedList<OldNode> oldNodeList) {
        this.oldNodeList = oldNodeList;
    }

    public LinkedList<OldNode> removeDegreeOne(){
        Boolean changed = true;
        LinkedList<OldNode> solution = new LinkedList<>();
        while (changed){
            changed = false;
            for (OldNode oldNode : this.oldNodeList) {
                if(oldNode.neighbors.size()==1){
                    solution.add(oldNode.neighbors.get(0));
                    this.removeNode(oldNode.neighbors.get(0));
                    this.removeNode(oldNode);
                    changed = true;
                    break;
                }
            }
        }
        return solution;
    }
}
