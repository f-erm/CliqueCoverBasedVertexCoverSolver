import java.util.LinkedList;

public class Algorithms {

    int recursiveSteps;

    public Algorithms(){
        recursiveSteps = 0;
    }

    public LinkedList<Node> vc(Graph G) {
        int k = 0;
        while (true) {
            System.out.println("# k is " + k);
            LinkedList<Node> S = vc_branch_edges(G, k);
            if (S != null) return S;
            k++;
        }
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
    private LinkedList<Node> vc_branch_nodes(Graph G, int k){
        if (k < 0 ) return null;
        if (G.totalEdges == 0) {
            return new LinkedList<Node>();
        }
        Node v = null;
        LinkedList<Node> neighbours = null;
        int numberOfNeighbours = 0;
        for (Node myNode: G.nodeList) {
            if (myNode.neighbors.isEmpty()) continue;
            v = myNode;
            neighbours = myNode.neighbors;
            numberOfNeighbours = neighbours.size();
            break;
        }
        for (Node u: neighbours) {
            G.removeNode(u);
        }
        // S is the vertex cover
        LinkedList<Node> S = vc_branch_edges(G, k - numberOfNeighbours);
        recursiveSteps++;
        for (Node u: neighbours) {
            G.reeaddNode(u);
        }
        if (S != null) {
            for (Node u: neighbours) {
                S.add(u);
            }
            return S;
        }
        G.removeNode(v);
        // S is the vertex cover
        S = vc_branch_edges(G, k - 1);
        recursiveSteps++;
        G.reeaddNode(v);
        if (S != null) {
            S.add(v);
            return S;
        }
        return null;
    }

}
