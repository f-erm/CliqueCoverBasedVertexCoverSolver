import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public class Graph implements Cloneable{

    LinkedList<OldNode> oldNodeList; //for the beginning only
    Node[] nodeArray; //stores the nodes of the graph, whether they are active or not, ordered
                      //by the highest degree
    Node[] permutation;
    int[] posInPermutation;
    int[] borderIndices;
    Queue<Integer> reduceDegZeroQueue;
    Queue<Integer> reduceDegOneQueue;
    Queue<Integer> reduceDegTwoQueue;
    int firstActiveNode;
    HashMap<String, OldNode> nodeHashMap; // for parsing only
    LinkedList<Node> partialSolution;
    int totalEdges;

    int activeNodes;
    Queue<Integer> dominatingNodes = new LinkedList<>();
    public Graph(){
        nodeHashMap = new HashMap<>();
        totalEdges = 0;
        dominatingNodes = new LinkedList<>();
        reduceDegZeroQueue = new LinkedList<>();
        reduceDegOneQueue = new LinkedList<>();
        reduceDegTwoQueue = new LinkedList<>();
    }
    public Graph(boolean hi){

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
        //checkCorrectness();
        removeNodeWithoutDegreeReducing(n);
        reduceDegree(n);
        //checkCorrectness();
    }

    /**
     * set the node to active again. Opposite of removeNode
     * @param n node to readd
     */
    public void reeaddNode(Node n){
        //checkCorrectness();
        n.active = true;
        for (int i = 0; i < n.neighbours.length; i++){
            if (nodeArray[n.neighbours[i]].active) nodeArray[n.neighbours[i]].activeNeighbours++;
        }
        totalEdges += n.activeNeighbours;
        activeNodes ++;
        increaseDegree(n);
        //checkCorrectness();
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
    public void removeNodeWithoutDegreeReducing(Node n){
        n.active = false;
        for (int i = 0; i < n.neighbours.length; i++){
            if (nodeArray[n.neighbours[i]].active) nodeArray[n.neighbours[i]].activeNeighbours--;
            if (nodeArray[n.neighbours[i]].activeNeighbours < 0){
                throw new RuntimeException("a node has less than 0 active neighbours");
            }
        }
        totalEdges = totalEdges - n.activeNeighbours;
        activeNodes --;
    }
    public void setPartialSolution(LinkedList<Node> partialSolution){
        this.partialSolution = partialSolution;
    }

    public void checkTotalEdgesNewGraph(){
        int edgecount = 0;
        for (Node node: nodeArray) {
            for (int neighbourID: node.neighbours) {
                Node neighbour = nodeArray[neighbourID];
                if (neighbour.active){
                    edgecount ++;
                }
            }
        }
        edgecount = edgecount/2;
        if (edgecount != totalEdges){
            throw new RuntimeException("the total edges are wrong");
        }
    }

    public boolean checkTotalEdgesOldGraphBool(){
        int edgecount = 0;
        for (OldNode node: oldNodeList) {
            for (OldNode neighbour: node.neighbors) {
                edgecount ++;
            }
        }
        edgecount = edgecount/2;
        if (edgecount > totalEdges){
            //throw new RuntimeException("the total edges not enough");
            return true;
        }
        if (edgecount < totalEdges){
            //throw new RuntimeException("the total edges are too many");
            return true;
        }
        return false;
    }
    public void checkTotalEdgesOldGraph(){
        int edgecount = 0;
        for (OldNode node: oldNodeList) {
            for (OldNode neighbour: node.neighbors) {
                edgecount ++;
            }
        }
        edgecount = edgecount/2;
        if (edgecount > totalEdges){
            throw new RuntimeException("the total edges not enough");
        }
        if (edgecount < totalEdges){
            throw new RuntimeException("the total edges are too many");
        }
    }


    public Graph reduceGraph(){
        Graph G = new Graph(true);
        G.nodeArray = new Node[this.activeNodes];
        int[] translation = new int[G.activeNodes];
        int i = 0;
        for (Node n : G.nodeArray){
            if (n.active){
                Node newNode = new Node(n.name,i,n.activeNeighbours);
                newNode.color = n.color;
                newNode.activeNeighbours = n.activeNeighbours;
                G.nodeArray[i] = newNode;
                translation[i] = n.id;
            }
            i++;
        }
        //translation in beide richtung erlauben um...
        //die Neighbors der neuen Nodes richtig zu uebersetzen.
        //in neuen Graph zurueck uebersetzen.
        return G;
    }


    @Override
    public Object clone(){
        Graph Gnew = new Graph(false);
        Gnew.nodeArray = new Node[this.nodeArray.length];
        for (int i=0;i< this.nodeArray.length; i++ ){
            Gnew.nodeArray[i] = (Node) this.nodeArray[i].clone();
        }
        Gnew.partialSolution = this.partialSolution; //this might be janky. partialsolution refers to the uncopied nodes.
        Gnew.totalEdges = this.totalEdges;
        Gnew.activeNodes = this.activeNodes;
        Gnew.posInPermutation = this.posInPermutation.clone();
        Gnew.permutation = new Node[this.permutation.length];
        for (int i = 0; i < Gnew.permutation.length; i++) Gnew.permutation[i] = Gnew.nodeArray[this.permutation[i].id];
        reduceDegZeroQueue = new LinkedList<>();
        reduceDegOneQueue = new LinkedList<>();
        reduceDegTwoQueue = new LinkedList<>();
        Gnew.borderIndices = this.borderIndices.clone();
        Gnew.firstActiveNode = this.firstActiveNode;
        Gnew.dominatingNodes = new LinkedList<>(this.dominatingNodes);
        return Gnew;
    }
    public void reduceDegree(Node node){
        for (int i = 0; i < node.neighbours.length; i++) if (nodeArray[node.neighbours[i]].active){
            reduceSingleDegree(node.neighbours[i]);
        }
    }
    private void increaseDegree(Node node){
        firstActiveNode = Math.min(firstActiveNode, posInPermutation[node.id]);
        for (int i = 0; i < node.neighbours.length; i++) if (nodeArray[node.neighbours[i]].active){
            increaseSingleDegree(node.neighbours[i]);
            firstActiveNode = Math.min(firstActiveNode, posInPermutation[node.neighbours[i]]);
        }
    }
    private void reduceSingleDegree(int n){
        Node u = nodeArray[n];
        /*if (u.activeNeighbours == 0) reduceDegZeroQueue.offer(u.id);
        if (u.activeNeighbours == 1) reduceDegOneQueue.offer(u.id);
        if (u.activeNeighbours == 2) reduceDegTwoQueue.offer(u.id);*/
        int oldDegree = nodeArray[n].activeNeighbours + 1;
        permutation[posInPermutation[n]] = permutation[borderIndices[oldDegree]];
        posInPermutation[permutation[borderIndices[oldDegree]].id] = posInPermutation[n];
        permutation[borderIndices[oldDegree]] = nodeArray[n];
        posInPermutation[n] = borderIndices[oldDegree];
        borderIndices[oldDegree]--;
    }
    private void increaseSingleDegree(int n){
        Node u = nodeArray[n];
        /*if (u.activeNeighbours == 0) reduceDegZeroQueue.offer(u.id);
        if (u.activeNeighbours == 1) reduceDegOneQueue.offer(u.id);
        if (u.activeNeighbours == 2) reduceDegTwoQueue.offer(u.id);*/
        int degree = nodeArray[n].activeNeighbours;
        permutation[posInPermutation[n]] = permutation[borderIndices[degree] + 1];
        posInPermutation[permutation[borderIndices[degree] + 1].id] = posInPermutation[n];
        permutation[borderIndices[degree] + 1] = nodeArray[n];
        posInPermutation[n] = borderIndices[degree] + 1;
        borderIndices[degree]++;
    }
    public void reduceDegreeMerge(Node node, Node second, int newNeighbours){
        int k = node.neighbours.length - newNeighbours;
        for (int i = 0; i < second.neighbours.length; i++){
            if (!nodeArray[second.neighbours[i]].active) continue;
            if (k >= node.neighbours.length || node.neighbours[k] != second.neighbours[i]){
                reduceSingleDegree(second.neighbours[i]);
            }
            else k++;
        }
        for (int deg = node.activeNeighbours + 1 - newNeighbours; deg <= node.activeNeighbours; deg++){
            permutation[posInPermutation[node.id]] = permutation[borderIndices[deg] + 1];
            posInPermutation[permutation[borderIndices[deg] + 1].id] = posInPermutation[node.id];
            permutation[borderIndices[deg] + 1] = node;
            posInPermutation[node.id] = borderIndices[deg] + 1;
            borderIndices[deg]++;
        }
        firstActiveNode = Math.min(posInPermutation[node.id], firstActiveNode);
        //checkCorrectness();
    }
    public void increaseDegreeMerge(Node node, LinkedList<Integer> newNeighbours){
        for (int a : newNeighbours) reduceSingleDegree(a);
        for (int deg = node.activeNeighbours + newNeighbours.size(); deg > node.activeNeighbours; deg--){
            permutation[posInPermutation[node.id]] = permutation[borderIndices[deg]];
            posInPermutation[permutation[borderIndices[deg]].id] = posInPermutation[node.id];
            permutation[borderIndices[deg]] = node;
            posInPermutation[node.id] = borderIndices[deg];
            borderIndices[deg]--;
        }
        //checkCorrectness();
    }
    private void checkCorrectness(){
        int deg = 100000;
        for (int i = 0; i < firstActiveNode; i++)  if (permutation[i].active){
            int aa = 0;
        }
        for (Node n : permutation){
            if (!n.active) {
                if (borderIndices[n.activeNeighbours] < posInPermutation[n.id]){
                    int eeehhhh = 1;
                }
                continue;
            }
            if (n.activeNeighbours > deg){
                int aaaa = 0;
            }
            if (n.activeNeighbours < deg) deg = n.activeNeighbours;
        }
    }
}
