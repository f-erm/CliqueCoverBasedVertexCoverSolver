import java.util.Collections;
import java.util.LinkedList;

public class Algorithms {

    int recursiveSteps;
    HopcroftKarp hk; //hk is used globaly
    public Algorithms(){
        recursiveSteps = 0;
    }

    /**
     * increment k until a vertex cover is found. "Wrapper" of vc_branch_nodes. Also initiates Hopcroft
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
        //Stop for edgeless G
        if (k < 0 ) return null;
        if (G.totalEdges == 0) {
            return new LinkedList<>();
        }
        //Only if we fell below HKs lower bound we compute HK again. If we remain under the lower bound there is no solution
        if (k < hk.lastLowerBound){
            hk.searchForAMatching();
            if (k < hk.lastLowerBound) return null;
        }

        LinkedList<Node> S;
        LinkedList<Node> neighbours = new LinkedList<>();
        Node v;
        while (true) {
            //find first node to branch. starts with first node of last iteration.
            if (G.nodeArray[firstActiveNode].activeNeighbours == 0 || !G.nodeArray[firstActiveNode].active){
                firstActiveNode++;
                continue;
            }
            //If delta(G) <= 2 there exists a simple solution. We check here and apply said solution
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
        //Branch for deleting all neighbors
        if (k >= v.activeNeighbours && v.activeNeighbours > 0){
            for (int[] u: v.neighbours) {
                Node toDelete = G.nodeArray[u[0]];
                if (toDelete.active){
                    neighbours.add(toDelete);
                    G.removeNode(toDelete);
                }
            }
            hk.updateDeleteNodes(neighbours);
            S = vc_branch_nodes(G, k - neighbours.size(), firstActiveNode); //the returned cover
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
        //branch for deleting the node instead
        G.removeNode(v);
        LinkedList<Node> ll = new LinkedList<>();
        ll.add(v);
        hk.updateDeleteNodes(ll);
        S = vc_branch_nodes(G, k - 1, firstActiveNode); //the returned cover
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
