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

    public void revertReduction(){
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

        

    public LinkedList<Node> reduceThroughCC(CliqueCover cc, int k, Graph G){
        LinkedList<Node> cliqueNeighbours = new LinkedList<>();
        LinkedList<Node> uselessNeighbours = new LinkedList<>();
        for (int i = 0; i < cc.FirstFreeColor; i++) {
            for (Integer nodeId: cc.colorclasses[i]) {
                Node v = G.nodeArray[nodeId];
                if(v.active && v.activeNeighbours > 0 && v.activeNeighbours == (cc.colorclasses[i].size()-1)){
                    if (k >= v.activeNeighbours) {
                        for (int[] u : v.neighbours) {
                            Node toDelete = G.nodeArray[u[0]];
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
        this.VCNeighbours = cliqueNeighbours;
    }

    public void revertReduceCC(Graph G){
        for (Node node: uselessNeighbours) {
            G.reeaddNode(node);
        }
        for (Node node: VCNeighbours) {
            G.reeaddNode(node);
        }
    }


    private void removeUselessNodes(Node node){
        removedNodes.push(new int[]{1, node.id});
        G.removeNode(node);
    }

    private void removeVCNodes(Node node){
        removedNodes.push(new int[]{2, node.id});
        VCNeighbours.add(node);
        G.removeNode(node);
    }




}
