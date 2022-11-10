import java.util.Collections;
import java.util.LinkedList;

public class Algorithms {

    int recursiveSteps;
    HopcroftKarp hk;
    public Algorithms(){
        recursiveSteps = 0;
    }

    /**
     * increment k until a vertex cover is found.
     * @param G graph
     * @return a smallest vertex cover
     */
    public LinkedList<Node> vc(Graph G) {
        hk = new HopcroftKarp(G);
        int k = hk.lastLowerBound + G.partialSolution.size();
        while (true) {
            System.out.println("# k is " + k);
            System.out.println(recursiveSteps);
            LinkedList<Node> S = vc_branch_nodes(G, k - G.partialSolution.size(), 0);
            if (S != null) {
                S.addAll(G.partialSolution);
                return S;
            }
            k++;
        }
    }

    /**
     * computes a vertex cover in Graph G up to size k.
     * @param G graph
     * @param k vertex cover size
     * @param firstActiveNode first node in G.nodeArray that is still active
     * @return a LinkedList of the nodes in the vertex cover.
     */
    private LinkedList<Node> vc_branch_nodes(Graph G, int k, int firstActiveNode){
        if (k < 0 ) return null;
        if (G.totalEdges == 0) {
            return new LinkedList<>();
        }
        if (k < hk.lastLowerBound){
            hk.searchForAMatching();
            if (k < hk.lastLowerBound) return null;
        }
        LinkedList<Node> S;
        LinkedList<Node> neighbours = new LinkedList<>();
        Node v;
        while (true) {
            if (G.nodeArray[firstActiveNode].activeNeighbours == 0 || !G.nodeArray[firstActiveNode].active){
                firstActiveNode++;
                continue;
            }
            v = G.nodeArray[firstActiveNode];
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
            hk.updateDeleteNodes(neighbours);
            // S is the vertex cover
            S = vc_branch_nodes(G, k - neighbours.size(), firstActiveNode);
            recursiveSteps++;
            Collections.reverse(neighbours);
            for (Node u : neighbours) {
                G.reeaddNode(u);
            }
            hk.updateAddNodes(neighbours);
            if (S != null) {
                S.addAll(neighbours);
                return S;
            }
        }

        G.removeNode(v);
        LinkedList<Node> ll = new LinkedList<>();
        ll.add(v);
        hk.updateDeleteNodes(ll);
        // S is the vertex cover
        S = vc_branch_nodes(G, k - 1, firstActiveNode);
        recursiveSteps++;
        G.reeaddNode(v);
        hk.updateAddNodes(ll);
        if (S != null) {
            S.add(v);
            return S;
        }
        return null;
    }

    /**
     * computes a vertex cover, given a graph with maximum degree of 2.
     * @param G graph
     * @param k vertex cover size
     * @return a LinkedList of nodes in the vertex cover.
     */
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
