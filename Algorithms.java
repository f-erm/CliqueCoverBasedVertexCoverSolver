
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
        LinkedList<Node> partialVC = G.removeDegreeOne();
        hk = new HopcroftKarp(G);
        k = hk.matching/2 + partialVC.size();
        while (true) {
            System.out.println("# k is " + k);
            System.out.println(recursiveSteps);
            hk = new HopcroftKarp(G);
            LinkedList<Node> S = vc_branch_nodes(G, k-partialVC.size());
            if (S != null) {
                S.addAll(partialVC);
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
        if (k < hk.matching/2) return null;
        Node v = null;
        LinkedList<Node> S;
        LinkedList<Node> neighbours = new LinkedList<>();
        int numberOfNeighbours = 0;
        for (Node myNode: G.nodeList) {
            if (myNode.neighbors.isEmpty()) continue;
            v = myNode;
            if (v.neighbors.size() < 3){
                Collections.sort(G.nodeList);
                if (G.nodeList.getFirst().neighbors.size() < 3) return solveSimpleGraph(G, k);
            }
            break;
        }
        if (k >= v.neighbors.size()){
            neighbours.addAll(v.neighbors);
            numberOfNeighbours = neighbours.size();
            for (Node u: neighbours) {
                G.removeNode(u);
            }
            hk.updateDeleteNodes(neighbours);
            // S is the vertex cover
            S = vc_branch_nodes(G, k - numberOfNeighbours);
            recursiveSteps++;
            Collections.reverse(neighbours);
            for (Node u : neighbours) {
                G.reeaddNode(u);
            }
            hk.updateAddNodes(neighbours);
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
        hk.updateDeleteNodes(ll);
        // S is the vertex cover
        S = vc_branch_nodes(G, k - 1);
        recursiveSteps++;
        G.reeaddNode(v);
        hk.updateAddNodes(ll);
        if (S != null) {
            S.add(v);
            return S;
        }
        return null;
    }
    public LinkedList<Node> solveSimpleGraph(Graph G, int k){
        if (k < 0) return null;
        for (Node node : G.nodeList){
            if (node.neighbors.size() > 1){
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
        for (Node node : G.nodeList){
            if (node.neighbors.size() > 0){
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
        return new LinkedList<Node>();
    }







    private LinkedList<Node> vc_branch_edges(Graph G, int k){
        if (k < 0 ) return null;
        if (G.totalEdges == 0) {
            return new LinkedList<Node>();
        }
        Node v = null;
        Node u = null;
        for (Node myNode: G.nodeList){
            if (myNode.neighbors.isEmpty()) continue;
            v = myNode;
            u = myNode.neighbors.get(0);
        }
        G.removeNode(u);
        // S is the vertex cover
        LinkedList<Node> S =  vc_branch_edges(G, k-1);
        recursiveSteps++;
        G.reeaddNode(u);
        if (S != null) {
            S.add(u);
            return S;
        }
        G.removeNode(v);
        // S is the vertex cover
        S =  vc_branch_edges(G, k-1);
        recursiveSteps++;
        G.reeaddNode(v);
        if (S != null) {
            S.add(v);
            return S;
        }
        return null;
    }


}
