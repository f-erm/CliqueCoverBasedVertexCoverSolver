import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Branching {
    Graph G;
    int hkCuts = 0;
    int ccCuts = 0;
    int packingCuts = 0;
    HopcroftKarp hk;
    Reduction reduction;
    CliqueCover cc;
    Stack<Node> solution;
    Stack<int[]> bestMergedNodes;
    LinkedList<Node> upperBound;
    int recursiveSteps;
    int firstLowerBound;
    public Branching(Graph G){
        this.G = G;
        recursiveSteps = 0;
        hk = new HopcroftKarp(G);
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
        reduction.rollOutAllInitial(true);
        //Fuer kleineren Graphen
        Graph OldG = G;
        G = G.reduceGraph();
        HopcroftKarp oldHK = hk;
        Reduction oldReduction = reduction;
        hk = new HopcroftKarp(G);
        reduction = new Reduction(G, hk);
        //fuer kleineren Graphen Ende
        InitialSolution initialSolution = new InitialSolution((Graph) G.clone(), System.nanoTime());
        upperBound = initialSolution.vc(true);
        cc = new CliqueCover(G);
        solution = new Stack<>();
        bestMergedNodes = new Stack<>();
        cc.cliqueCoverIterations(10, 5, null, 0);
        hk.searchForAMatchingNew();
        LinkedList<Integer> bestPermutation = cc.permutation;
        int bestLowerBound = cc.lowerBound;
        long time = System.nanoTime();
        for (int i = 0; i < G.activeNodes * 200 && (System.nanoTime() - time)/1024 < 10000000; i++){
            if (i % 2 == 1){
                int rand = ThreadLocalRandom.current().nextInt(bestPermutation.size());
                bestPermutation.remove((Integer) rand);
                bestPermutation.add(rand);
                cc.cliqueCoverIterations(10, 5, bestPermutation, bestLowerBound);
                if (cc.lowerBound < bestLowerBound){
                    int a =  bestPermutation.removeLast();
                    bestPermutation.add(rand, a);
                }
            }
            else cc.cliqueCoverIterations(10, 5, null, bestLowerBound);
            if (cc.lowerBound > bestLowerBound){
                bestLowerBound = cc.lowerBound;
                bestPermutation = cc.permutation;
            }
        }
        System.out.println("# upper bound: " + (upperBound.size() + oldReduction.VCNodes.size()));
        firstLowerBound = Math.max(hk.totalCycleLB, bestLowerBound);
        System.out.println("# lower bound: " + (firstLowerBound + reduction.VCNodes.size() + G.partialSolution.size() + oldReduction.VCNodes.size()));
        if (firstLowerBound == upperBound.size()) return returnModified(upperBound, OldG, G, oldReduction);
        if (upperBound.size() - firstLowerBound < 3){ //we are really close, try to get even better bounds
            for (int i = 0; i < 3; i++) {//try to improve upper bound
                InitialSolution is = new InitialSolution((Graph) G.clone(), System.nanoTime());
                LinkedList<Node> newUpperBound = is.vc(true);
                if (newUpperBound.size() < upperBound.size()){
                    upperBound = newUpperBound;
                    System.out.println("# upper bound - try better: " + (upperBound.size() + oldReduction.VCNodes.size()));
                    if (upperBound.size() == firstLowerBound) return returnModified(upperBound, OldG, G, oldReduction);
                }
            }
            for (int i = 0; i < G.activeNodes * 20000 && (System.nanoTime() - time)/1024 < 50000000; i++){
                if (i % 2 == 1){//try to improve lower bound
                    int rand = ThreadLocalRandom.current().nextInt(bestPermutation.size());
                    bestPermutation.remove((Integer) rand);
                    bestPermutation.add(rand);
                    cc.cliqueCoverIterations(10, 5, bestPermutation, bestLowerBound);
                    if (cc.lowerBound < bestLowerBound){
                        int a =  bestPermutation.removeLast();
                        bestPermutation.add(rand, a);
                    }
                }
                else cc.cliqueCoverIterations(10, 5, null, bestLowerBound);
                if (cc.lowerBound > bestLowerBound){
                    bestLowerBound = cc.lowerBound;
                    bestPermutation = cc.permutation;
                    if (upperBound.size() - bestLowerBound == 1){
                        System.out.println("# lower bound - try better (not perfect yet): " + (bestLowerBound + reduction.VCNodes.size() + G.partialSolution.size() + oldReduction.VCNodes.size()));
                        time = System.nanoTime();
                        i = 0;
                    }
                    else break;
                }
            }
            firstLowerBound = Math.max(hk.totalCycleLB, bestLowerBound);
            System.out.println("# lower bound - try better: " + (firstLowerBound + reduction.VCNodes.size() + G.partialSolution.size() + oldReduction.VCNodes.size()));
            if (firstLowerBound == upperBound.size()) return returnModified(upperBound, OldG, G, oldReduction);
        }
        recursiveSteps++;
        branch(G.partialSolution.size() + reduction.VCNodes.size(), upperBound.size(), 0, bestPermutation);
        while (!bestMergedNodes.isEmpty()){
            int[] merge = bestMergedNodes.pop();
            if (upperBound.contains(G.nodeArray[merge[0]])){
                upperBound.add(G.nodeArray[merge[1]]);
                upperBound.remove(G.nodeArray[merge[2]]);
            }
        }
        return returnModified(upperBound, OldG, G, oldReduction);//Fuer kleineren Graphen
    }

    // c is the current solution size, k is the upper bound
    public int branch(int c, int k, int depth, LinkedList<Integer> lastPerm){
        //if (depth == 7) System.out.println("#depth7 - 1 start");
        c += reduction.rollOutAllInitial(false);
        //c += reduction.rollOutAll();
        hk.searchForAMatching();//TODO watch out here - could be high running time
        cc = new CliqueCover(G);
        cc.cliqueCoverIterations(2,2, lastPerm, 0);
        lastPerm = cc.permutation;
        if (c + Math.max(hk.totalCycleLB, cc.lowerBound) >= k) {
            if (hk.totalCycleLB >= cc.lowerBound) hkCuts++;
            else ccCuts++;
            G.packingViolated = false;
            return k;
        }
        if (G.packingViolated){
            G.packingViolated = false;
            packingCuts++;
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
        reduction.updatePackingOfMergedNodes(v, 0);
        solution.push(v);
        v.inVC = true;
        LinkedList<Node> ll = new LinkedList<>();
        ll.add(v);
        Stack<Packing> packingList = new Stack<>();
        if (!mirrors.isEmpty()){
            for (Node m : mirrors){
                G.removeNode(m);
                reduction.updatePackingOfMergedNodes(m, 0);
                ll.add(m);
                solution.push(m);
                m.inVC = true;
                for (Packing q : m.affectedConstraints) q.updateVC();
            }
            for (Node m : mirrors) packingList.push(new Packing(m, G, reduction));
        }
        for (Packing q : v.affectedConstraints) q.updateVC();
        packingList.push(new Packing(v, G, reduction));
        hk.updateDeleteNodes(ll);
        k = branch(c + 1 + mirrors.size(), k, depth + 1, lastPerm); //the returned cover
        if(reduction.removedNodes != null && !reduction.removedNodes.isEmpty()){
            reduction.revertReduction();
        }
        recursiveSteps++;
        Collections.reverse(ll);
        for (Node m : ll){
            reduction.redoPackingOfMergedNodes(m, 0);
            G.reeaddNode(m);
            solution.pop();
            m.inVC = false;
            for (Packing q : m.affectedConstraints) q.redoVC();
        }
        while (!packingList.empty()) packingList.pop().destroy();
        hk.updateAddNodes(ll);
        //Branch for deleting all neighbors
        LinkedList<Node> neighbours = new LinkedList<>();
        for (int u: v.neighbours) {
            Node toDelete = G.nodeArray[u];
            if (toDelete.active){
                neighbours.add(toDelete);
                G.removeNode(toDelete);
                reduction.updatePackingOfMergedNodes(toDelete, 0);
                solution.push(toDelete);
                toDelete.inVC = true;
            }
        }
        hk.updateDeleteNodes(neighbours);
        HashSet<Integer> hs = new HashSet<>();
        for (int n : v.neighbours) hs.add(n);
        hs.add(v.id);
        for (Node n : neighbours) {
            packingList.push(new Packing(n, hs, G, reduction));
            for (Packing q : n.affectedConstraints) q.updateVC();
        }
        k = branch(c + neighbours.size(), k, depth + 1, lastPerm); //the returned cover
        if(reduction.removedNodes != null && !reduction.removedNodes.isEmpty()){
            reduction.revertReduction();
        }
        recursiveSteps++;
        Collections.reverse(neighbours);
        for (Node n : neighbours) {
            reduction.redoPackingOfMergedNodes(n, 0);
            G.reeaddNode(n);
            solution.pop();
            n.inVC = false;
            for (Packing q : n.affectedConstraints) q.redoVC();
        }
        while (!packingList.empty()) packingList.pop().destroy();
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

    LinkedList<Node> returnModified(LinkedList<Node> vc, Graph oldG, Graph newG, Reduction oldRed){
        //substitutes the fake nodes of newG obtained by reducing for the real nodes of oldG
        //vc.addAll(G.partialSolution);return vc //auskommentieren um ohne kleineren Graphen zu testen
        LinkedList<Node> r = new LinkedList<>();
        for (Node n : vc){
            r.add(oldG.nodeArray[newG.translationNewToOld[n.id]]);
        }
        r.addAll(oldRed.VCNodes);
        //entmerge gemergete Knoten
        bestMergedNodes =oldRed.mergedNodes;
        while (!bestMergedNodes.isEmpty()){
            int[] merge = bestMergedNodes.pop();
            if (r.contains(oldG.nodeArray[merge[0]])){
                r.add(oldG.nodeArray[merge[1]]);
                r.remove(oldG.nodeArray[merge[2]]);
            }
        }
        r.addAll(oldG.partialSolution);
        G.packingViolated = false;
        return r;
    }
}
