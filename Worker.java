import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.*;


public class Worker implements Callable {
    
    Graph G;
    HopcroftKarp hk;
    Algorithms Alg;
    int k;
    int firstActiveNode;

    public Worker(Graph G, int k, HopcroftKarp hk,int firstActiveNode, Algorithms Alg){
        this.G = G;
        this.k = k;
        this.hk = hk;
        this.firstActiveNode = firstActiveNode;
        this.Alg = Alg;

    }

    @Override    
    public LinkedList<Node> call() throws Exception {    
        return Alg.vc_branch_nodes(G,k,firstActiveNode,hk);        
    } 
}   