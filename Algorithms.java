import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.*;
public class Algorithms {
    int recursiveSteps;
    long totalTimeHK = 0;
    long totalTimeCC = 0;
    int totalBranchCutsHK = 0;
    int totalBranchCutsCC = 0;
    boolean doCliqueCover = true;
    Reduction reduction;
    int ProcCount;
    LinkedList<Integer> bestPermutation;
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
        reduction = new Reduction(G, hk);
        cc = new CliqueCover(G);
        cc.cliqueCoverIterations(10, 5, null);
        bestPermutation = cc.permutation;
        int bestLowerBound = cc.lowerBound;
        for (int i = 0; i < 250; i++){
            cc.cliqueCoverIterations(10, 5, null);
            if (cc.lowerBound > bestLowerBound){
                bestLowerBound = cc.lowerBound;
                bestPermutation = cc.permutation;
            }

        }
        int k = Math.max(hk.lastLowerBound, bestLowerBound) + G.partialSolution.size();
        k = reduction.reduceThroughCC(cc, k, G);

        if (k < 0) return null;
        if (G.totalEdges == 0) {
            G.partialSolution.addAll(reduction.VCNodes);
            return G.partialSolution;
        }
        while (true) {
            if (totalBranchCutsHK > 50 && totalBranchCutsHK > totalBranchCutsCC) doCliqueCover = false;
            System.out.println("# k is " + k);
            System.out.println("# recursiveSteps " + recursiveSteps);
            LinkedList<Node> S = vc_branch_nodes(G, k - G.partialSolution.size(), 0,hk, bestPermutation, 0);
            if (S != null) {
                S.addAll(reduction.VCNodes);
                while (!reduction.mergedNodes.isEmpty()){
                    int[] merge = reduction.mergedNodes.pop();
                    if (S.contains(G.nodeArray[merge[0]])){
                        S.add(G.nodeArray[merge[1]]);
                        S.remove(G.nodeArray[merge[2]]);
                    }
                }
                if (WeWannaThreatYo) exec.shutdown();
                S.addAll(G.partialSolution);
                return S;
            }
            if (reduction.removedNodes!= null && reduction.removedNodes.size() > 0) reduction.revertReduction(); // reverts first use of the reductions
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
    public LinkedList<Node> vc_branch_nodes(Graph G, int k, int firstActiveNode, HopcroftKarp hk, LinkedList<Integer> lastPerm, int depth) {
        //Stop for edgeless G
        if (k < 0) return null;
        if (G.totalEdges <= 0 || G.activeNodes <= 0) {
            return new LinkedList<>();
        }
        int l = reduction.rollOutAll(k, depth % 2 == 0);
        k -= l;
        if (k < 0) return null;
        if (G.totalEdges <= 0 || G.activeNodes <= 0) {
            return new LinkedList<>();
        }
        long time = System.nanoTime();
        //hk.searchForAMatching();
        totalTimeHK += System.nanoTime() - time;
        /*if (k < hk.lastLowerBound || k < hk.totalCycleLB) {
            totalBranchCutsHK++;
            return null;
        }*/
        time = System.nanoTime();
        if (doCliqueCover) {
            cc = new CliqueCover(G);
            if (k < cc.cliqueCoverIterations(1, 2, lastPerm)) {
                totalBranchCutsCC++;
                totalTimeCC += System.nanoTime() - time;
                return null;
            }
            //k = reduction.reduceThroughCC(cc, k, G);

            if (k < 0) return null;
            if (G.totalEdges == 0) {
                return new LinkedList<>();
            }

            lastPerm = cc.permutation;
            totalTimeCC += System.nanoTime() - time;
        }
        LinkedList<Node> S = new LinkedList<>();
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
            /*if (v.activeNeighbours < 3){
                boolean graphIsSimple = true;
                for (Node n : G.nodeArray){
                    if (n.active && n.activeNeighbours > 2) {
                        graphIsSimple = false;
                        break;
                    }
                }
                if (graphIsSimple) return solveSimpleGraph(G, k);
            }*/
            break;
        }


        boolean threaded = false;
        Future<LinkedList<Node>> Sthread = CompletableFuture.completedFuture(new LinkedList<>());//init empty future

        //Branch for deleting all neighbors
        if (k >= v.activeNeighbours && v.activeNeighbours > 0){
            for (int u: v.neighbours) {
                Node toDelete = G.nodeArray[u];
                if (toDelete.active){
                    neighbours.add(toDelete);
                    G.removeNode(toDelete);
                }
            }
            hk.updateDeleteNodes(neighbours);
            if(WeWannaThreatYo) {
                if (exec.getActiveCount() < ProcCount) {//we can thread
                    threaded = true;
                    LinkedList<Integer> lastPerm_copy;
                    if (lastPerm==null){
                        lastPerm_copy = null;
                    }else{
                        lastPerm_copy = (LinkedList<Integer>) lastPerm.clone();
                    }
                    Sthread = exec.submit(new Worker((Graph) G.clone(), k - neighbours.size(), (HopcroftKarp) hk.clone(), firstActiveNode, this, lastPerm_copy));
                } else {
                    S = vc_branch_nodes(G, k - neighbours.size(), firstActiveNode, hk, lastPerm, ++depth); //the returned cover
                    if(S==null){
                        reduction.revertReduction();
                    }
                }
            }else{
                S = vc_branch_nodes(G, k - neighbours.size(), firstActiveNode, hk, lastPerm, ++depth); //the returned cover
                if(S==null){
                    reduction.revertReduction();
                }
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
        S = vc_branch_nodes(G, k - 1, firstActiveNode,hk, lastPerm,depth); //the returned cover
        if(S==null){
            reduction.revertReduction();
        }
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
        LinkedList<Node> S = new LinkedList<>();
        LinkedList<Node> remember = new LinkedList<>();
        boolean changed = true;
        while (changed) {
            changed = false;
            for (Node node : G.nodeArray) {
                if (!node.active) continue;
                if (k < 0) {
                    for (Node n : remember) G.reeaddNode(n);
                    return null;
                }
                if (node.activeNeighbours == 1) {
                    changed = true;
                    S.add(G.nodeArray[node.neighbours[0]]);
                    remember.add(node);
                    G.removeNode(node);
                    remember.add(G.nodeArray[node.neighbours[0]]);
                    G.removeNode(G.nodeArray[node.neighbours[0]]);
                    k--;
                }
            }
        }
        for (Node n : remember) G.reeaddNode(n);
        return new LinkedList<>();
    }

}
