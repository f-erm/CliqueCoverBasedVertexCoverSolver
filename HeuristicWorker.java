import java.util.LinkedList;
import java.util.concurrent.Callable;


public class HeuristicWorker implements Callable {

    Graph G;
    long startTime;
    long totalTime;
    boolean[] inVC;

    public HeuristicWorker(Graph G, long startTime, long totalTime, boolean[] inVC){
        this.G = G;
        this.startTime = startTime;
        this.totalTime = totalTime;
        this.inVC = inVC;
    }

    @Override
    public LinkedList<Node> call() {
        LinkedList<Node> oldvc = new LinkedList<>();
        for (int i = 0; i < G.nodeArray.length; i++) if (inVC[i]) oldvc.add(G.nodeArray[i]);
        HeuristicVC heuristicVC = new HeuristicVC(G, startTime);
        heuristicVC.TIME_LIMIT = totalTime;
        LinkedList<Node> vc = oldvc;
        while ((System.nanoTime() - startTime)/1024 < heuristicVC.TIME_LIMIT) {
            try{
                LinkedList<Node> newVC = heuristicVC.metaheuristic((LinkedList<Node>) oldvc.clone());
            if (newVC.size() < vc.size()) {
                vc = newVC;
            }
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        return vc;
    }
}   