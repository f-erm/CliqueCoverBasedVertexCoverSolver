import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Queue;

public class Graph implements Cloneable{

    LinkedList<OldNode> oldNodeList; //for the beginning only
    Node[] nodeArray; //stores the nodes of the graph, whether they are active or not, ordered
                      //by the highest degree.
    HashMap<String, OldNode> nodeHashMap; // for parsing only
    LinkedList<Node> partialSolution;
    int totalEdges;
    int activeNodes;
    Queue<Integer> dominatingNodes = new LinkedList<>();
    public Graph(){
        nodeHashMap = new HashMap<>();
        totalEdges = 0;
        dominatingNodes = new LinkedList<>();
    }

    /**
     * adds an edge when reading the input. Only used in the beginning on OldNodes.
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
     * set a node to inactive, updates count of active neioghbours for all its neighbors and decreases the Graph's total edge count as well.
     * @param n node to remove
     */
    public void removeNode(Node n){
        n.active = false;
        for (int i = 0; i < n.neighbours.length; i++){
            nodeArray[n.neighbours[i]].activeNeighbours--;
            if (n.triangleCounts[i] > 0) nodeArray[n.neighbours[i]].triangleCounts[n.neighbourPositions[i]] = 0;
        }
        totalEdges = totalEdges - n.activeNeighbours;
        activeNodes --;
        //keep triangle-numbers up-to-date. Also maintain dynamic list of dominated vertices dominatingNodes.
        ListIterator<Integer> li = n.triangles.listIterator();
        while(li.hasNext()){
            int a = n.neighbours[li.next()];
            int b = n.neighbours[li.next()];
            int pos = li.next();
            if (!nodeArray[a].active || !nodeArray[b].active) continue;
            if (pos >= nodeArray[a].neighbours.length || nodeArray[a].neighbours[pos] != b) pos = findInArray(nodeArray[a].neighbours, b);
            if (nodeArray[a].active && nodeArray[b].active) {
                nodeArray[a].triangleCounts[pos]--;
                nodeArray[b].triangleCounts[nodeArray[a].neighbourPositions[pos]]--;
                if (nodeArray[a].triangleCounts[pos] + 1 == nodeArray[a].activeNeighbours){
                    dominatingNodes.offer(b);
                    dominatingNodes.offer(a);
                }
                else if (nodeArray[b].triangleCounts[nodeArray[a].neighbourPositions[pos]] + 1 == nodeArray[b].activeNeighbours){
                    dominatingNodes.offer(a);
                    dominatingNodes.offer(b);
                }
            }
        }
    }

    /**
     * set the node to active again. Opposite of removeNode
     * @param n node to readd
     */
    public void reeaddNode(Node n){
        n.active = true;
        for (int i = 0; i < n.neighbours.length; i++){
            nodeArray[n.neighbours[i]].activeNeighbours++;
            if (nodeArray[i].active && n.triangleCounts[i] > 0) nodeArray[n.neighbours[i]].triangleCounts[n.neighbourPositions[i]] = n.triangleCounts[i];
        }
        totalEdges += n.activeNeighbours;
        activeNodes ++;
        //keep triangle-numbers up-to-date.
        ListIterator<Integer> li = n.triangles.listIterator();
        while(li.hasNext()){
            int a = n.neighbours[li.next()];
            int b = n.neighbours[li.next()];
            int pos = li.next();
            if (pos >= nodeArray[a].neighbours.length || nodeArray[a].neighbours[pos] != b) pos = findInArray(nodeArray[a].neighbours, b);
            if (nodeArray[a].active && nodeArray[b].active) {
                nodeArray[a].triangleCounts[pos]++;
                nodeArray[b].triangleCounts[nodeArray[a].neighbourPositions[pos]]++;
            }
        }
    }
private int findInArray(int[] array, int el){
        for (int i = 0; i < array.length; i++) if (array[i] == el) return i;
        return -1;
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


    @Override
    public Object clone(){
        Graph Gnew = new Graph();
        Gnew.nodeArray = new Node[this.nodeArray.length];
        for (int i=0;i< this.nodeArray.length; i++ ){
            Gnew.nodeArray[i] = (Node) this.nodeArray[i].clone();
        }
        Gnew.partialSolution = this.partialSolution; //this might be janky. partialsolution refers to the uncopied nodes.
        Gnew.totalEdges = this.totalEdges;
        return Gnew;
    }
}
