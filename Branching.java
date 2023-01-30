import java.util.*;

public class Branching {
    Graph G;
    int hkCuts = 0;
    int ccCuts = 0;
    double progress;
    HopcroftKarp hk;
    Reduction reduction;
    CliqueCover cc;
    Stack<Node> solution;
    Stack<int[]> bestMergedNodes;
    LinkedList<Node> upperBound;
    int recursiveSteps;
    public Branching(Graph G){
        this.G = G;
        recursiveSteps = 0;
        hk = new HopcroftKarp(G);
        progress = 0.0;
    }
    public LinkedList<Node> solve(){
        reduction = new Reduction(G, hk);
        reduction.rollOutAllInitial(true);
        if (G.activeNodes <= 0){
            LinkedList<Node> solution = reduction.VCNodes;
            while (!reduction.mergedNodes.isEmpty()){
                int[] merge = reduction.mergedNodes.pop();
                if (solution.contains(G.nodeArray[merge[0]])){
                    solution.add(G.nodeArray[merge[1]]);
                    solution.remove(G.nodeArray[merge[2]]);
                }
            }
            solution.addAll(G.partialSolution);
            return solution;
        }
        else reduction.revertReduction();
        //Graph OldG = G;//Fuer kleineren Graphen
        //G = G.reduceGraph();//fuer kleineren Graphen
        InitialSolution initialSolution = new InitialSolution((Graph) G.clone(), System.nanoTime());
        reduction.rollOutAllInitial(true);
        upperBound = initialSolution.vc(true);
        cc = new CliqueCover(G);
        solution = new Stack<>();
        solution.addAll(G.partialSolution);
        bestMergedNodes = new Stack<>();
        cc.cliqueCoverIterations(10, 5, null);
        hk.searchForAMatching();
        LinkedList<Integer> bestPermutation = cc.permutation;
        int bestLowerBound = cc.lowerBound;
        long time = System.nanoTime();
        for (int i = 0; i < G.activeNodes * 20 && (System.nanoTime() - time)/1024 < 10000000; i++){
            cc.cliqueCoverIterations(10, 5, null);
            if (cc.lowerBound > bestLowerBound){
                bestLowerBound = cc.lowerBound;
                bestPermutation = cc.permutation;
            }
        }
        System.out.println("# Clique Cover Quality: " + bestLowerBound);
        System.out.println("# upper bound: " + upperBound.size());
        int lb = Math.max(hk.totalCycleLB, bestLowerBound);
        System.out.println("# lower bound: " + (lb + reduction.VCNodes.size() + G.partialSolution.size()));
        if (lb == upperBound.size()) return upperBound;
        int solSize = branch(G.partialSolution.size() + reduction.VCNodes.size(), upperBound.size(), 0, bestPermutation);
        while (!bestMergedNodes.isEmpty()){
            int[] merge = bestMergedNodes.pop();
            if (upperBound.contains(G.nodeArray[merge[0]])){
                upperBound.add(G.nodeArray[merge[1]]);
                upperBound.remove(G.nodeArray[merge[2]]);
            }
        }
        return upperBound;
        //return returnModified(upperBound, OldG, G);//Fuer kleineren Graphen
    }

    // c is the current solution size, k is the upper bound
    public int branch(int c, int k, int depth, LinkedList<Integer> lastPerm){
        c += reduction.rollOutAllInitial(false);
        hk.searchForAMatching();
        cc = new CliqueCover(G);
        cc.cliqueCoverIterations(1,2, lastPerm);
        lastPerm = cc.permutation;
        if (c + Math.max(hk.totalCycleLB, cc.lowerBound) >= k) {
            if (hk.totalCycleLB >= cc.lowerBound) hkCuts++;
            else ccCuts++;
            progress += 1.0 / Math.pow(2.0, depth);
            return k;
        }
        if (G.totalEdges <= 0 || G.activeNodes <= 0){
            if (G.partialSolution.size() + reduction.VCNodes.size() + solution.size() < upperBound.size()){
                upperBound = new LinkedList<>(solution);
                upperBound.addAll(G.partialSolution);
                upperBound.addAll(reduction.VCNodes);
                bestMergedNodes = (Stack<int[]>) reduction.mergedNodes.clone();
                System.out.println("# current best: " + upperBound.size());
            }
            progress += 1.0 / Math.pow(2.0, depth);
            return c;
        }
        Node v;
        while (true) {
            if (G.permutation[G.firstActiveNode].activeNeighbours == 0 || !G.permutation[G.firstActiveNode].active){
                G.firstActiveNode++;
                continue;
            }
            v = G.permutation[G.firstActiveNode];
            break;
        }
        //branch for deleting the node
        LinkedList<Node> mirrors = new LinkedList<>();
        G.removeNode(v);
        solution.push(v);
        LinkedList<Node> ll = new LinkedList<>();
        ll.add(v);
        if (!mirrors.isEmpty()) for (Node m : mirrors){
            G.removeNode(m);
            ll.add(m);
            solution.push(m);
        }
        hk.updateDeleteNodes(ll);
        k = branch(c + 1 + mirrors.size(), k, depth + 1, lastPerm); //the returned cover
        if(reduction.removedNodes != null && !reduction.removedNodes.isEmpty()){
            reduction.revertReduction();
        }
        recursiveSteps++;
        Collections.reverse(ll);
        for (Node m : ll){
            G.reeaddNode(m);
            solution.pop();
        }
        hk.updateAddNodes(ll);
        //Branch for deleting all neighbors
        LinkedList<Node> neighbours = new LinkedList<>();
        for (int u: v.neighbours) {
            Node toDelete = G.nodeArray[u];
            if (toDelete.active){
                neighbours.add(toDelete);
                G.removeNode(toDelete);
                solution.push(toDelete);
            }
        }
        hk.updateDeleteNodes(neighbours);
        k = branch(c + neighbours.size(), k, depth + 1, lastPerm); //the returned cover
        if(reduction.removedNodes != null && !reduction.removedNodes.isEmpty()){
            reduction.revertReduction();
        }
        recursiveSteps++;
        Collections.reverse(neighbours);
        for (Node u : neighbours) {
            G.reeaddNode(u);
            solution.pop();
        }
        hk.updateAddNodes(neighbours);
        return k;
    }
    private LinkedList<Node> findMirrors(Node v){
        LinkedList<Node> mirrors = new LinkedList<>();
        HashSet<Integer> used = new HashSet<>();
        used.add(v.id);
        int[] ps = new int[G.nodeArray.length];
        Arrays.fill(ps, -2);
        for (int u : v.neighbours) if (G.nodeArray[u].active){
            used.add(u);
            ps[u] = -1;
        }
        for (int u : v.neighbours) if (G.nodeArray[u].active){
            for (int w : G.nodeArray[u].neighbours) if (G.nodeArray[w].active && used.add(w)){
                int degV = v.activeNeighbours;
                for (int z : G.nodeArray[w].neighbours) if (G.nodeArray[z].active && ps[z] != -2){
                    ps[z] = w;
                    degV--;
                }
                boolean ok = true;
                for (int u2 : v.neighbours) if (G.nodeArray[u2].active && ps[u2] != w){
                    int degU2 = 0;
                    for (int w2 : G.nodeArray[u2].neighbours) if (G.nodeArray[w2].active && ps[w2] != w && ps[w2] != -2) degU2++;
                    if (degU2 != degV - 1){
                        ok = false;
                        break;
                    }
                }
                if (ok){
                    //System.out.println("#mirror");
                    mirrors.add(G.nodeArray[w]);
                }
            }
        }
        return mirrors;
    }

    LinkedList<Node> returnModified(LinkedList<Node> vc, Graph oldG, Graph newG){
        //substitutes the fake nodes of newG obtained by reducing for the real nodes of oldG
        LinkedList<Node> r = new LinkedList<>();
        for (Node n : vc){
            r.add(oldG.nodeArray[newG.translationNewToOld[n.id]]);
        }
        return r;
    }
}
