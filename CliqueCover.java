import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class CliqueCover {

    LinkedList<Integer>[] colorclasses;
    int[] colorcounts;
    int FirstFreeColor = 0;//keep track of first available spot in colorclasses
    int lowerBound;
    Graph G;
    LinkedList<Integer> permutation;
    int stopRecomputing;
    int sizeOfCliquesToResolve;
    int[] cliqueCut = new int[]{0,0,0,0,0,0,0};

    int[] color;

    public CliqueCover(Graph G){
        this.G = G;
        lowerBound = 0;
        color = new int[G.nodeArray.length];
    }

    /**
     * This is a heuristic for computing a clique cover.
     * The size of the clique cover serves as a lower bound in the main branching algorithm
     * The algorithm for the heuristic is inspired by "On the Efficiency of an Order-based Representation in the Clique Covering Problem" by David Chalupa
     * The algorithm quality of the clique cover depends on the permutation of the nodes which is used
     * @param k number of iterations that the clique cover is computed
     * @param stopRecomputing computing clique cover is stopped if the clique cover did not improve stopRecomputing-many times
     * @param oldPermutation permutation of nodes which was previously computed
     * @param sizeOfCliquesToResolve we try to resolve all cliques of at most this size (effective but costly)
     */
    public void iterativeCliqueCover(int k, int stopRecomputing, LinkedList<Integer> oldPermutation, int sizeOfCliquesToResolve) {
        lowerBound = 0;
        this.sizeOfCliquesToResolve = sizeOfCliquesToResolve;

        //if no permutation exists, compute random permutation
        if (oldPermutation == null){
            permutation = new LinkedList<>();
            for (int i = 0; i < G.nodeArray.length; i++) {
                if (G.nodeArray[i].active) permutation.add(i);
            }
            Collections.shuffle(permutation);
        }
        //else use existing permutation
        else permutation = oldPermutation;
        this.stopRecomputing = stopRecomputing;
        while (k > 0 && stopRecomputing > 0){
            colorclasses = new LinkedList[G.activeNodes];
            colorcounts = new int[G.activeNodes];
            FirstFreeColor = 0;
            //set all nodes to color unknown
            for (int j = 0; j < G.nodeArray.length; j++) {
                color[j] = -1;
            }
            permutation = cliqueCover();
            k--;
        }
    }


    private LinkedList<Integer> cliqueCover(){
        //color nodes based on permutation
        for (int i: permutation) {
            Node myNode = G.nodeArray[i];
            if (!myNode.active) continue;
            //for all neighbors that are active and colored decrement that color
            for (int neighbourInfo: myNode.neighbours) {
                Node neighbour = G.nodeArray[neighbourInfo];
                if (!neighbour.active || color[neighbourInfo] == -1) continue;
                colorcounts[color[neighbourInfo]]--;
            }
            int bestColorSize = 0;
            int bestColor = -1;
            for (int j = 0; j < FirstFreeColor; j++) {
                if (colorcounts[j] == 0) {
                    if (bestColorSize < colorclasses[j].size()) {
                        bestColor = j;
                        bestColorSize = colorclasses[bestColor].size();
                    }
                }
            }
            if (bestColor > -1){
                colorclasses[bestColor].add(myNode.id);
                color[i] = bestColor;
                colorcounts[bestColor]++;
            }

            for (int neighbourInfo : myNode.neighbours){
                Node neighbour = G.nodeArray[neighbourInfo];
                if (!neighbour.active || color[neighbourInfo]==-1) continue;
                colorcounts[color[neighbourInfo]] ++;
            }
            if (bestColor == -1) {
                LinkedList<Integer> ll = new LinkedList<>();
                ll.add(myNode.id);
                color[i] = FirstFreeColor;
                colorclasses[FirstFreeColor] = ll;
                colorcounts[FirstFreeColor] = 1;
                FirstFreeColor++;
            }
        }
        improveSolution();
        //build new permutation from color classes
        LinkedList<Integer> perm = new LinkedList<>();
        shuffleArray(colorclasses); //shuffling sometimes improves the color classes in the next iteration
        for (LinkedList<Integer> color: colorclasses) {
            if (color == null) break;
            perm.addAll(color);
        }
        int newLowerBound = G.activeNodes - FirstFreeColor;
        if (newLowerBound <= lowerBound){
            stopRecomputing--;
        }
        else {
            lowerBound = newLowerBound;
        }
        Collections.reverse(perm);
        return perm;
    }

    /**
     * This function tries to improve the clique cover by redistributing nodes from small cliques to different cliques
     */
    public void improveSolution() {
        for (int i = 0; i < sizeOfCliquesToResolve; i++) {
            for (int l = 0; l < FirstFreeColor; l++) {
                LinkedList<Integer> colorlist = colorclasses[l];
                    if (colorlist.size() < i) {
                        int oldSize = colorlist.size();
                        LinkedList<Integer> colorCopy = (LinkedList<Integer>) colorlist.clone();
                        for (int nodeID : colorCopy) {
                            Node myNode = G.nodeArray[nodeID];
                            int originalColor = color[nodeID];
                            for (int neighbourInfo : myNode.neighbours) {
                                Node neighbour = G.nodeArray[neighbourInfo];
                                if (!neighbour.active || color[neighbourInfo] == -1) continue;
                                colorcounts[color[neighbourInfo]]--;
                            }
                            int bestColorSize = 0;
                            int bestColor = -1;
                            for (int j = 0; j < FirstFreeColor; j++) {
                                if (colorcounts[j] == 0 && j != originalColor) {
                                    if (bestColorSize < colorclasses[j].size()) {
                                        bestColor = j;
                                        bestColorSize = colorclasses[bestColor].size();
                                    }
                                }
                            }
                            for (int neighbourInfo : myNode.neighbours) {
                                Node neighbour = G.nodeArray[neighbourInfo];
                                if (!neighbour.active || color[neighbourInfo] == -1) continue;
                                colorcounts[color[neighbourInfo]]++;
                            }
                            if (bestColor != -1) {
                                colorcounts[originalColor]--;
                                colorlist.remove((Integer) myNode.id);
                                colorclasses[bestColor].add(myNode.id);
                                color[nodeID] = bestColor;
                                colorcounts[bestColor]++;
                            }
                        }
                        if (colorcounts[l] == 0) {
                            cliqueCut[oldSize]++;
                            colorclasses[l] = colorclasses[--FirstFreeColor];
                            colorcounts[l] = colorcounts[FirstFreeColor];
                            colorcounts[FirstFreeColor] = 0;
                            colorclasses[FirstFreeColor] = new LinkedList<>();
                            for (int n : colorclasses[l]) color[n] = l;
                            l--;
                        }
                    }
                }
            }
    }

    private void shuffleArray(LinkedList<Integer>[] ar)
    {
        Random rnd = ThreadLocalRandom.current();
        for (int i = FirstFreeColor - 1; i > 0; i--)
        {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            LinkedList<Integer> a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }
    
}
