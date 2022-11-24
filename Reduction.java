import java.util.LinkedList;
import java.util.Stack;

public class Reduction {

    LinkedList<Node> uselessNeighbours;
    LinkedList<Node> VCNodes;

    Stack<int[]> removedNodes;

    Graph G;

    public void reduction(Graph G){
        this.G = G;
        removedNodes.push(new int[]{0});
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
                    break;
                case 2: //usefull Nodes
                    G.reeaddNode(node);
                    VCNodes.remove(node);
                    break;
            }
            action = removedNodes.pop();
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
    }

    private void removeVCNodes(Node node){
        removedNodes.push(new int[]{2, node.id});
        VCNodes.add(node);
        G.removeNode(node);
    }




}
