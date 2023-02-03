import java.util.LinkedList;
import java.util.concurrent.Callable;


public class HeuristicWorker implements Callable {

    Graph G;
    long startTime;
    boolean[] inVC;

    public HeuristicWorker(Graph G, long startTime, boolean[] inVC){
        this.G = G;
        this.startTime = startTime;
        this.inVC = inVC;
    }

    @Override
    public LinkedList<Node> call() {
        int cnt = 0;
        LinkedList<Node> vc = new LinkedList<>();
        for (int i = 0; i < G.nodeArray.length; i++) if (inVC[i]) vc.add(G.nodeArray[i]);
        HeuristicVC heuristicVC = new HeuristicVC(G, startTime);
        while ((System.nanoTime() - startTime)/1024 < heuristicVC.TIME_LIMIT) {
            try{
            LinkedList<Node> newVC = heuristicVC.metaheuristic(vc);
            if (newVC.size() < vc.size()) {
                vc = newVC;
                cnt = 0;
            }
            else cnt++;}
            catch (Exception e){
                e.printStackTrace();
            }
        }
        return vc;
    }
}   