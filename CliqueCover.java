import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class CliqueCover {

    LinkedList<Integer>[] colorclasses;
    int[] colorcounts;
    int FirstFreeColor = 0;//keep track of first availible spot in colorclasses
    int lowerBound;
    Graph G;
    LinkedList<Integer> permutation;
    int reerun;
    int tryKillCliqus;
    int[] cliqueCut = new int[]{0,0,0,0,0,0,0};

    int[] color;

    public CliqueCover(Graph G){
        this.G = G;
        lowerBound = 0;
        color = new int[G.nodeArray.length];
    }

    public int cliqueCoverIterations(int k, int reerun, LinkedList<Integer> oldPermutation, int notResolveCliques) {
        lowerBound = 0;
        tryKillCliqus = notResolveCliques;
        if (oldPermutation == null){
            permutation = new LinkedList<>();
            for (int i = 0; i < G.permutation.length; i++) {
                if (G.permutation[i].active) permutation.add(i);
            }
            Collections.shuffle(permutation);
        }
        else permutation = oldPermutation;
        this.reerun = reerun;
        while (k > 0 && reerun > 0){
            colorclasses = new LinkedList[G.activeNodes];
            colorcounts = new int[G.activeNodes];
            FirstFreeColor = 0;
            //set all nodes to color unknown
            for (int j = 0; j < G.nodeArray.length; j++) {
                color[j] = -1;
            }
            permutation = cliqueCoverNew();
            k--;
        }
        //improveSolution();
        return  G.activeNodes - FirstFreeColor;
    }


    private LinkedList<Integer> cliqueCover(){ 
        for (int i: permutation) {//forall active nodes in permutation
            Node myNode = G.nodeArray[i];
            if (!myNode.active) continue;
            //for all neighbors that are active and colored decrement that color
            int remember = -1;
            boolean createNewClass = true;
            for (int neighbourInfo: myNode.neighbours) {
                Node neighbour = G.nodeArray[neighbourInfo];
                if (!neighbour.active || color[neighbourInfo]==-1) continue;
                colorcounts[color[neighbourInfo]] --;
                if (colorcounts[color[neighbourInfo]] == 0){
                    colorclasses[color[neighbourInfo]].add(myNode.id);
                    color[i] = color[neighbourInfo];
                    colorcounts[color[neighbourInfo]]++;
                    remember = neighbourInfo;
                    createNewClass = false;
                    break;
                }

            }
            for (int neighbourInfo : myNode.neighbours){
                Node neighbour = G.nodeArray[neighbourInfo];
                if (!neighbour.active || color[neighbourInfo]==-1) continue;
                colorcounts[color[neighbourInfo]] ++;
                if (remember == neighbourInfo) break;
            }
            if (createNewClass) {
                LinkedList<Integer> ll = new LinkedList<>();
                ll.add(myNode.id);
                color[i] = FirstFreeColor;
                colorclasses[FirstFreeColor] = ll;
                colorcounts[FirstFreeColor] = 1;
                FirstFreeColor++;
            }

        }
        LinkedList<Integer> perm = new LinkedList<>();
        shuffleArray(colorclasses);
        for (LinkedList<Integer> color: colorclasses) {
            if (color == null) break;
            perm.addAll(color);
        }
        int newlowerBound = G.activeNodes - FirstFreeColor;
        if (newlowerBound <= lowerBound){
            reerun --;
        }
        else {
            lowerBound = newlowerBound;
        }
        Collections.reverse(perm);
        //System.out.println("the lower bound is " + lowerBound);
        //System.out.println("the first free color is " + FirstFreeColor);
        return perm;
    }

    private LinkedList<Integer> cliqueCoverNew(){
        for (int i: permutation) {//forall active nodes in permutation
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
        LinkedList<Integer> perm = new LinkedList<>();
        shuffleArray(colorclasses);
        for (LinkedList<Integer> color: colorclasses) {
            if (color == null) break;
            perm.addAll(color);
        }
        int newlowerBound = G.activeNodes - FirstFreeColor;
        if (newlowerBound <= lowerBound){
            reerun --;
        }
        else {
            lowerBound = newlowerBound;
        }
        Collections.reverse(perm);
        //System.out.println("the lower bound is " + lowerBound);
        //System.out.println("the first free color is " + FirstFreeColor);
        return perm;
    }

    public void improveSolution() {
        for (int i = 0; i < tryKillCliqus; i++) {
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
