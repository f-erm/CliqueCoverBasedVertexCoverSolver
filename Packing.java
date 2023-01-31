import java.util.HashSet;
import java.util.LinkedList;

public class Packing {

    int left;
    int type;
    int right;
    int[] affectedNodes;
    boolean[] inVC;
    Node startNode;
    Graph G;
    HashSet<Integer> hs;
    Reduction reduction;


    public Packing(Node v, Graph G, Reduction reduction){
        type = 1;
        this.G = G;
        this.reduction = reduction;
        startNode = v;
        right = v.activeNeighbours;
        affectedNodes = new int[right];
        inVC = new boolean[right];
        int i = 0;
        for (int neighbour : v.neighbours) if (G.nodeArray[neighbour].active){
            G.nodeArray[neighbour].affectedConstraints.add(this);
            affectedNodes[i++] = neighbour;
        }
    }

    public Packing(Node v, HashSet<Integer> hs, Graph G, Reduction reduction){
        type = 2;
        this.G = G;
        this.reduction = reduction;
        this.hs = hs;
        startNode = v;
        LinkedList<Integer> ll = new LinkedList<>();
        for (int n : v.neighbours) if (G.nodeArray[n].active && !hs.contains(n)){
            right++;
            G.nodeArray[n].affectedConstraints.add(this);
            ll.add(n);
        }
        if (ll.isEmpty()){
            G.packingViolated = true;
        }
        affectedNodes = new int[ll.size()];
        int i = 0;
        for (int n : ll) affectedNodes[i++] = n;
        inVC = new boolean[ll.size()];
    }

    public void updateVC(){
        if (--right < 1) G.packingViolated = true;
        /*if (right < 2){ //packing reduction #1
            for (int n : affectedNodes) if (G.nodeArray[n].active){
                for (int m : G.nodeArray[n].neighbours) if (G.nodeArray[m].active){
                    reduction.removeVCNodes(G.nodeArray[m]);
                }
                reduction.removeUselessNodes(G.nodeArray[n]);
                break;
            }
        }*/
    }
    public void redoVC(){
        right++;
    }

    public void destroy(){
        for (int n : affectedNodes) G.nodeArray[n].affectedConstraints.removeLast();
    }
}
