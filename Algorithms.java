
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;

public class Algorithms {

    int recursiveSteps;
    HopcroftKarp hk;

    public Algorithms(){
        recursiveSteps = 0;
    }

    public LinkedList<Node> vc(Graph G) {
        int k = 0;
        //LinkedList<OldNode> partialVC = G.removeDegreeOne();
        //hk = new HopcroftKarp(G);
        //k = hk.matching/2 + partialVC.size();
        while (true) {
            System.out.println("# k is " + k);
            //System.out.println(recursiveSteps);
            //hk = new HopcroftKarp(G);
            //LinkedList<OldNode> S = vc_branch_nodes(G, k-partialVC.size());
            LinkedList<Node> S = vc_branch_nodes(G, k);
            if (S != null) {
                //S.addAll(partialVC);
                return S;
            }
            k++;
        }
    }

    /*public LinkedList<Node> vc(Graph G) {
        int k = 0;
        while (true) {
            System.out.println("# k is " + k);
            System.out.println(recursiveSteps);
            hk = new HopcroftKarp(G);
            LinkedList<Node> S = vc_branch_nodes(G, k);
            if (S != null) {
                return S;
            }
            k++;
        }
    }*/

    private LinkedList<Node> vc_branch_nodes(Graph G, int k){
        if (k < 0 ) return null;
        if (G.totalEdges == 0) {
            return new LinkedList<>();
        }
        //HopcroftKarp a = new HopcroftKarp(G);
        /*a.updateDelete(0);
        a.updateDelete(2);
        a.updateAdd(2);
        a.updateAdd(0);*/
        //if (k < hk.matching/2) return null;
        Node v = null;
        LinkedList<Node> S;
        LinkedList<Node> neighbours = new LinkedList<>();
        int numberOfNeighbours = 0;
        for (Node myNode : G.nodeArray) {
            if (myNode.activeNeighbours == 0 || !myNode.active) continue;
            v = myNode;
            if (v.activeNeighbours < 3){
                boolean graphIsSimple = true;
                for (Node n : G.nodeArray){
                    if (n.active && n.activeNeighbours > 2) {
                        graphIsSimple = false;
                        break;
                    }
                }
                if (graphIsSimple) return solveSimpleGraph(G, k);
            }
            break;
        }
        if (k >= v.activeNeighbours && v.activeNeighbours > 0){
            for (int[] u: v.neighbours) {
                Node toDelete = G.nodeArray[u[0]];
                if (toDelete.active){
                    neighbours.add(toDelete);
                    G.removeNode(toDelete);
                }
            }
            numberOfNeighbours = neighbours.size();
            //hk.updateDeleteNodes(neighbours);
            // S is the vertex cover
            S = vc_branch_nodes(G, k - numberOfNeighbours);
            recursiveSteps++;
            Collections.reverse(neighbours);
            for (Node u : neighbours) {
                G.reeaddNode(u);
            }
            //hk.updateAddNodes(neighbours);
            if (S != null) {
                for (Node u: neighbours) {
                    S.add(u);
                }
                return S;
            }
        }

        G.removeNode(v);
        LinkedList<Node> ll = new LinkedList<>();
        ll.add(v);
        //hk.updateDeleteNodes(ll);
        // S is the vertex cover
        S = vc_branch_nodes(G, k - 1);
        recursiveSteps++;
        G.reeaddNode(v);
        //hk.updateAddNodes(ll);
        if (S != null) {
            S.add(v);
            return S;
        }
        return null;
    }
    public LinkedList<Node> solveSimpleGraph(Graph G, int k){
        if (k < 0) return null;
        for (Node node : G.nodeArray){
            if (!node.active) continue;
            if (node.activeNeighbours > 1){
                G.removeNode(node);
                LinkedList<Node> S = solveSimpleGraph(G, k - 1);
                recursiveSteps++;
                G.reeaddNode(node);
                if (S != null){
                    S.add(node);
                }
                return S;
            }
        }
        for (Node node : G.nodeArray){
            if (!node.active) continue;
            if (node.activeNeighbours > 0){
                G.removeNode(node);
                LinkedList<Node> S = solveSimpleGraph(G, k - 1);
                recursiveSteps++;
                G.reeaddNode(node);
                if (S != null){
                    S.add(node);
                }
                return S;
            }
        }
        return new LinkedList<>();
    }

}
