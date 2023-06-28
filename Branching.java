import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Stack;
import java.util.concurrent.ThreadLocalRandom;


public class Branching {
    Graph G;
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

    /**
     * This is the main function which finds the vertex cover.
     * @return the smallest vertex cover in G.
     */
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
        //delete inactive nodes to obtain a better upper bound via local search
        Graph OldG = G;
        G = G.reduceGraph();
        Reduction oldReduction = reduction;
        hk = new HopcroftKarp(G);
        reduction = new Reduction(G, hk);

        //calculate initial upper bound
        InitialSolution initialSolution = new InitialSolution((Graph) G.clone(), System.nanoTime());
        upperBound = initialSolution.vc(true);
        cc = new CliqueCover(G);
        solution = new Stack<>();
        bestMergedNodes = new Stack<>();
        cc.cliqueCoverIterations(10, 5, null, 5);
        hk.searchForAMatchingNew();
        LinkedList<Integer> bestPermutation = cc.permutation;
        int bestLowerBound = cc.lowerBound;
        long time = System.nanoTime();
        //compute initial clique cover
        for (int i = 0; i < G.activeNodes * 200 && (System.nanoTime() - time)/1024 < 10000000; i++){
            if (i % 2 == 1){
                //in every second step, do clique cover local search
                int rand = ThreadLocalRandom.current().nextInt(bestPermutation.size());
                bestPermutation.remove((Integer) rand); //This seems completely useless, but it drastically improves the results
                bestPermutation.add(rand);
                cc.cliqueCoverIterations(10, 5, bestPermutation, 5);
                if (cc.lowerBound < bestLowerBound){
                    int a =  bestPermutation.removeLast();
                    bestPermutation.add(rand, a);
                }
            }
            //else, use a random permutation
            else cc.cliqueCoverIterations(10, 5, null, 5);
            if (cc.lowerBound > bestLowerBound){
                bestLowerBound = cc.lowerBound;
                bestPermutation = cc.permutation;
            }
        }
        System.out.println("# upper bound: " + (upperBound.size() + oldReduction.VCNodes.size()));
        firstLowerBound = Math.max(hk.totalCycleLB, bestLowerBound);
        System.out.println("# lower bound: " + (firstLowerBound + reduction.VCNodes.size() + G.partialSolution.size() + oldReduction.VCNodes.size()));
        //if we matched upper and lowe bound and can return without branching:
        if (firstLowerBound == upperBound.size()) return returnModified(upperBound, OldG, G, oldReduction);
        if (upperBound.size() - firstLowerBound < 3){ //we are really close, try to get even better bounds
            for (int i = 0; i < 4; i++) {//try to improve upper bound
                InitialSolution is = new InitialSolution((Graph) G.clone(), System.nanoTime());
                LinkedList<Node> newUpperBound = is.vc(i % 2 == 0);
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
                    cc.cliqueCoverIterations(10, 5, bestPermutation, 5);
                    if (cc.lowerBound < bestLowerBound){
                        int a =  bestPermutation.removeLast();
                        bestPermutation.add(rand, a);
                    }
                }
                else cc.cliqueCoverIterations(10, 5, null, 5);
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
        //  --- start of main branching function ---
        branch(G.partialSolution.size() + reduction.VCNodes.size(), upperBound.size(), 0, bestPermutation);
        //  --- end of main branching function ---

        while (!bestMergedNodes.isEmpty()){ //de-merge degree-2-reductions
            int[] merge = bestMergedNodes.pop();
            if (upperBound.contains(G.nodeArray[merge[0]])){
                upperBound.add(G.nodeArray[merge[1]]);
                upperBound.remove(G.nodeArray[merge[2]]);
            }
        }
        return returnModified(upperBound, OldG, G, oldReduction);
    }

    /**
     * @param c current solution size
     * @param k upper bound
     * @param depth recursion depth
     * @param lastPerm best known permutation of nodes which is then usedused to compute the clique cover
     * @return size of the vertex cover. The solution is stored in _____
     */
    public int branch(int c, int k, int depth, LinkedList<Integer> lastPerm){
        c += reduction.rollOutAllInitial(false);
        hk.searchForAMatching(); // find maximum matching in the bipartite representation of G
        cc.cliqueCoverIterations(2,2, lastPerm, 4); // find clique cover
        int bestLowerBound = cc.lowerBound;
        if (c + bestLowerBound > k - 2 && c + bestLowerBound < k) {
            int bonus = 0;
            if (depth < 10) {
                bonus = 20 - depth;
            }
            // do local search on clique cover
            if (cc.permutation.size() > 0) for (int i = 0; i < 5 + bonus; i++) {
                int rand = ThreadLocalRandom.current().nextInt(cc.permutation.size());
                cc.permutation.remove((Integer) rand);
                cc.permutation.add(rand);
                cc.cliqueCoverIterations(2, 2, cc.permutation, 5);
                if (cc.lowerBound < bestLowerBound) {
                    int a = cc.permutation.removeLast();
                    cc.permutation.add(rand, a);
                }
                if (cc.lowerBound > bestLowerBound) {
                    bestLowerBound = cc.lowerBound;
                    if (c + bestLowerBound >= k) break;
                }
            }
        }
        lastPerm = cc.permutation;
        // we can return since the lower bound is violated
        if (c + Math.max(hk.totalCycleLB, bestLowerBound) >= k) {
            G.packingViolated = false;
            return k;
        }
        // we can return since the packing constraints are violated
        if (G.packingViolated){
            G.packingViolated = false;
            return k;
        }
        // we can return the vertex cover if all edges are covered
        if (G.totalEdges <= 0 || G.activeNodes <= 0){
            if (G.partialSolution.size() + reduction.VCNodes.size() + solution.size() < upperBound.size()){
                upperBound = new LinkedList<>(solution);
                upperBound.addAll(G.partialSolution);
                upperBound.addAll(reduction.VCNodes);
                bestMergedNodes = (Stack<int[]>) reduction.mergedNodes.clone();
            }
            return c;
        }
        Node v;
        while (true) { // find max-degree node that is active
            if (G.permutation[G.firstActiveNode].activeNeighbours == 0 || !G.permutation[G.firstActiveNode].active){
                G.firstActiveNode++;
                continue;
            }
            v = G.permutation[G.firstActiveNode];
            break;
        }
        //branch for deleting the node
        G.removeNode(v);
        reduction.updatePackingOfMergedNodes(v, 0);
        solution.push(v);
        v.inVC = true;
        LinkedList<Node> ll = new LinkedList<>();
        ll.add(v);
        // update packing constraints
        Stack<Packing> packingList = new Stack<>();
        for (Packing q : v.affectedConstraints) q.updateVC();
        packingList.push(new Packing(v, G, reduction));
        hk.updateDeleteNodes(ll);
        //recursive call over deleting the node
        k = branch(c + 1, k, depth + 1, lastPerm);
        //if the call was not successfull, revert the changes in the graph
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
        // recursive call over deleting the neighbours
        k = branch(c + neighbours.size(), k, depth + 1, lastPerm);
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

    LinkedList<Node> returnModified(LinkedList<Node> vc, Graph oldG, Graph newG, Reduction oldRed){
        //substitutes the fake nodes of newG obtained by reducing for the real nodes of oldG
        LinkedList<Node> r = new LinkedList<>();
        for (Node n : vc){
            r.add(oldG.nodeArray[newG.translationNewToOld[n.id]]);
        }
        r.addAll(oldRed.VCNodes);
        //de-merge merged nodes
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
