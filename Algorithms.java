import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.*;

public class Algorithms {

    int recursiveSteps;
    long totalTimeHK = 0;
    long totalTimeCC = 0;
    int totalBranchCutsHK = 0;
    int totalBranchCutsCC = 0;
    int ProcCount;
    ThreadPoolExecutor exec;
    CliqueCover cc;
    boolean WeWannaThreatYo = false;

    public Algorithms(){//minor changes to class
        recursiveSteps = 0;
        if (WeWannaThreatYo) {
            ProcCount = Runtime.getRuntime().availableProcessors();
            exec = (ThreadPoolExecutor) Executors.newFixedThreadPool(ProcCount);//create threadpool based on available cores.
        }
    }

    /**
     * increment k until a vertex cover is found. "Wrapper" of vc_branch_nodes. Also initiates Hopcroft
     * @param G graph
     * @return a smallest vertex cover
     */
    public LinkedList<Node> vc(Graph G) {
        HopcroftKarp hk = new HopcroftKarp(G);
        cc = new CliqueCover(G);
        int k = Math.max(hk.lastLowerBound, cc.cliqueCoverIterations(100, 6)); /*+ G.partialSolution.size()*/;
        while (true) {
            System.out.println("# k is " + k);
            System.out.println("# recursiveSteps " + recursiveSteps);
            LinkedList<Node> S = vc_branch_nodes(G, k /*- G.partialSolution.size()*/, 0,hk);
            if (S != null) {
                //S.addAll(G.partialSolution);
                if (WeWannaThreatYo) exec.shutdown();
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
    public LinkedList<Node> vc_branch_nodes(Graph G, int k, int firstActiveNode, HopcroftKarp hk) {
        //Stop for edgeless G
        if (k < 0) return null;
        if (G.totalEdges == 0) {
            return new LinkedList<>();
        }
        long time = System.nanoTime();
        hk.searchForAMatching();
        totalTimeHK += System.nanoTime() - time;
        if (k < hk.lastLowerBound || k < hk.totalCycleLB) {
            totalBranchCutsHK++;
            return null;
        }
        time = System.nanoTime();
        cc = new CliqueCover(G);
        if (k < cc.cliqueCoverIterations(3, 2)) {
            totalBranchCutsCC++;
            totalTimeCC += System.nanoTime() - time;
            return null;
        }
        totalTimeCC += System.nanoTime() - time;
        LinkedList<Node> S = new LinkedList<Node>();
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


        boolean threaded = false;
        Future<LinkedList<Node>> Sthread = CompletableFuture.completedFuture(new LinkedList<Node>());//init empty future

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
            if(WeWannaThreatYo) {
                if (exec.getActiveCount() < ProcCount) {//we can thread
                    threaded = true;
                    Sthread = exec.submit(new Worker((Graph) G.clone(), k - neighbours.size(), (HopcroftKarp) hk.clone(), firstActiveNode, this));
                } else {
                    S = vc_branch_nodes(G, k - neighbours.size(), firstActiveNode, hk); //the returned cover
                }
            }else{
                S = vc_branch_nodes(G, k - neighbours.size(), firstActiveNode, hk); //the returned cover
            }
            recursiveSteps++;
            Collections.reverse(neighbours);
            for (Node u : neighbours) {
                G.reeaddNode(u);
            }
            hk.updateAddNodes(neighbours);
            if (!threaded){//if we didnt thread we can already evaluate result
                if (S != null) {
                    S.addAll(neighbours);
                    return S;
                }
            }
        }
        //branch for deleting the node instead
        G.removeNode(v);
        LinkedList<Node> ll = new LinkedList<>();
        ll.add(v);
        hk.updateDeleteNodes(ll);
        S = vc_branch_nodes(G, k - 1, firstActiveNode,hk); //the returned cover
        recursiveSteps++;
        G.reeaddNode(v);
        hk.updateAddNodes(ll);
        if (threaded && WeWannaThreatYo){//we did thread, time to look at the result
            try{
                LinkedList<Node> Str = Sthread.get();
                if (Str != null) {
                    Str.addAll(neighbours);
                    return Str;
                }
            }catch(Exception e){}

        }
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
