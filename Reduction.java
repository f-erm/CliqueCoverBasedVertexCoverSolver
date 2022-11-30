import java.util.LinkedList;
import java.util.Stack;

public class Reduction {

    LinkedList<Node> uselessNeighbours;
    LinkedList<Node> VCNodes;

    Stack<int[]> removedNodes;

    Graph G;
    HopcroftKarp hk;

    public Reduction(Graph G, HopcroftKarp hk){
        this.G = G;
        VCNodes = new LinkedList<Node>();
        uselessNeighbours = new LinkedList<Node>();
        removedNodes = new Stack<>();
        this. hk = hk;

    }

    public int rollOutAll(){
        int oldK = VCNodes.size();
        removedNodes.push(new int[]{0});
        removeDegreeOne();
        removeDegreeZero();
        return VCNodes.size() - oldK;
    }
    public void removeDegreeLargerKAndZero(Graph G, int k){
        LinkedList<Node> partialSolution = new LinkedList<>();
        for (Node node : G.nodeArray){
            if (node.activeNeighbours > k){
                partialSolution.add(node);
                G.removeNode(node);
                removedNodes.push(new int[]{2,node.id});
            }
            else if (node.activeNeighbours == 0){
                G.removeNode(node);
                removedNodes.push(new int[]{1, node.id});
            }
        }
        G.partialSolution.addAll(partialSolution);
    }

    public void revertReduction() {
        int[] action = removedNodes.pop();
        while (action[0] != 0) {
            Node node = G.nodeArray[action[1]];
            switch (action[0]) {
                case 1: // useless Nodes
                    G.reeaddNode(node);
                    LinkedList<Node> a = new LinkedList<>();
                    a.add(node);
                    hk.updateAddNodes(a);
                    break;
                case 2: //usefull Nodes
                    G.reeaddNode(node);
                    VCNodes.remove(node);
                    LinkedList<Node> b = new LinkedList<>();
                    b.add(node);
                    hk.updateAddNodes(b);
                    break;
            }
            action = removedNodes.pop();
        }
    }

    private void removeDegreeOne(){
        boolean changed = true;
        while (changed){
            changed = false;
            for (Node node : G.nodeArray) {
                if(node.activeNeighbours == 1 && node.active){
                    int i = 0;
                    while (!G.nodeArray[node.neighbours[i]].active){
                        i++;
                    }
                    Node neighbour = G.nodeArray[node.neighbours[i]];
                    removeVCNodes(neighbour);
                    removeUselessNodes(node);
                    changed = true;
                    break;
                }
            }
        }
    }

    private void removeDegreeZero(){
        for (Node node : G.nodeArray) {
            if(node.activeNeighbours == 0 && node.active){
                removeUselessNodes(node);
            }
        }
    }
        

    public LinkedList<Node> reduceThroughCC(CliqueCover cc, int k, Graph G){
        LinkedList<Node> cliqueNeighbours = new LinkedList<>();
        LinkedList<Node> uselessNeighbours = new LinkedList<>();
        for (int i = 0; i < cc.FirstFreeColor; i++) {
            for (Integer nodeId: cc.colorclasses[i]) {
                Node v = G.nodeArray[nodeId];
                if(v.active && v.activeNeighbours > 0 && v.activeNeighbours == (cc.colorclasses[i].size()-1)){
                    if (k >= v.activeNeighbours) {
                        for (int u : v.neighbours) {
                            Node toDelete = G.nodeArray[u];
                            if (toDelete.active) {
                                cliqueNeighbours.add(toDelete);
                                k--;
                                G.removeNode(toDelete);
                            }
                        }
                    }
                    else {
                        return null;
                    }
                    uselessNeighbours.add(v);
                    G.removeNode(v);
                    break;
                }
            }
        }
        this.uselessNeighbours = uselessNeighbours;
        this.VCNodes = cliqueNeighbours;
        return null;
    }

    public void revertReduceCC(Graph G){
        for (Node node: uselessNeighbours) {
            G.reeaddNode(node);
        }
        for (Node node: VCNodes) {
            G.reeaddNode(node);
        }
    }




    private void removeUselessNodes(Node node){
        removedNodes.push(new int[]{1, node.id});
        G.removeNode(node);
        LinkedList<Node> a = new LinkedList<>();
        a.add(node);
        hk.updateDeleteNodes(a);
    }

    private void removeVCNodes(Node node){
        removedNodes.push(new int[]{2, node.id});
        VCNodes.add(node);
        G.removeNode(node);
        LinkedList<Node> a = new LinkedList<>();
        a.add(node);
        hk.updateDeleteNodes(a);
    }




}
