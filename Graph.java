import java.util.HashMap;
import java.util.LinkedList;
public class Graph {

    LinkedList<OldNode> oldNodeList; //for the beginning only
    Node[] nodeArray; //stores the nodes of the graph, whether they are active or not, ordered
                      //by the highest degree.
    HashMap<String, OldNode> nodeHashMap; // for parsing only
    LinkedList<Node> partialSolution;
    int totalEdges;
    public Graph(){
        nodeHashMap = new HashMap<>();
        totalEdges = 0;
    }

    /**
     * adds an edge when reading the input.
     * @param first first node
     * @param second second node
     */
    public void addEdge(String first, String second){

        OldNode firstOldNode = nodeHashMap.get(first);
        if (firstOldNode == null) nodeHashMap.put(first, firstOldNode = new OldNode(first));
        OldNode secondOldNode = nodeHashMap.get(second);
        if (secondOldNode == null) nodeHashMap.put(second, secondOldNode = new OldNode(second));
        firstOldNode.addEdge(secondOldNode);
        secondOldNode.addEdge(firstOldNode);
        totalEdges++;

    }

    /**
     * set a node to inactive
     * @param n node to remove
     */
    public void removeNode(Node n){
        n.active = false;
        for (int i = 0; i < n.neighbours.length; i++){
            nodeArray[n.neighbours[i][0]].activeNeighbours--;
        }
        totalEdges = totalEdges - n.activeNeighbours;
    }

    /**
     * set the node to active again
     * @param n node to readd
     */
    public void reeaddNode(Node n){
        n.active = true;
        for (int i = 0; i < n.neighbours.length; i++){
            nodeArray[n.neighbours[i][0]].activeNeighbours++;
        }
        totalEdges += n.activeNeighbours;
    }


    /**
     *     ---only used for reductions/preprocessing---
     *     this function does not actually delete
     *     a node but only removes the node from the list
     *     and all the pointers from the neighbours in the list to u.
     * @param toRemove node to remove
     */
    public void removeNode(OldNode toRemove){
        for (OldNode oldNode : toRemove.neighbors){
            oldNode.deleteEdge(toRemove);
            totalEdges --;
        }
        oldNodeList.remove(toRemove);
    }
    public void setPartialSolution(LinkedList<Node> partialSolution){
        this.partialSolution = partialSolution;
    }
}
