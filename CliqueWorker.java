import java.util.concurrent.Callable;
import java.util.*;



public class CliqueWorker implements Callable {
    Graph G;
    LinkedList<Integer> lastPerm;

    public CliqueWorker(Graph G, LinkedList<Integer> lastPerm){
        this.G = G;
        this.lastPerm = lastPerm;
    }

    @Override
    public Object[] call() {
        CliqueCover cc = new CliqueCover(G);
        cc.cliqueCoverIterations(2,2, lastPerm, 4);
        return new Object[]{cc.permutation, cc.lowerBound};
    }
    
}

