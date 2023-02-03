import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class CliqueCover {

    LinkedList<Integer>[] colorclasses;
    int[] colorcounts;
    int FirstFreeColor = 0;//keep track of first availible spot in colorclasses
    int lowerBound = 0;
    int oldLowerBound = 0;
    Graph G;
    LinkedList<Integer> permutation;
    int reerun;

    public CliqueCover(Graph G){
        this.G = G;
        lowerBound = 0;
    }

    public int cliqueCoverIterations(int k, int reerun, LinkedList<Integer> oldPermutation, int bestLowerBound) {
        oldLowerBound = bestLowerBound;
        lowerBound = 0;
        if (oldPermutation == null){
            permutation = new LinkedList<>();
            for (int i = 0; i < G.permutation.length; i++) {
                permutation.add(i);
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
                G.nodeArray[j].color = -1;
            }
            permutation = cliqueCoverNew();
            k--;
        }
        return lowerBound;
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
                if (!neighbour.active || neighbour.color==-1) continue;
                colorcounts[neighbour.color] --;
                if (colorcounts[neighbour.color] == 0){
                    colorclasses[neighbour.color].add(myNode.id);
                    myNode.color = neighbour.color;
                    colorcounts[neighbour.color]++;
                    remember = neighbourInfo;
                    createNewClass = false;
                    break;
                }

            }
            for (int neighbourInfo : myNode.neighbours){
                Node neighbour = G.nodeArray[neighbourInfo];
                if (!neighbour.active || neighbour.color==-1) continue;
                colorcounts[neighbour.color] ++;
                if (remember == neighbourInfo) break;
            }
            if (createNewClass) {
                LinkedList<Integer> ll = new LinkedList<>();
                ll.add(myNode.id);
                myNode.color = FirstFreeColor;
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
                if (!neighbour.active || neighbour.color == -1) continue;
                colorcounts[neighbour.color]--;
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
                myNode.color = bestColor;
                colorcounts[bestColor]++;
            }

            for (int neighbourInfo : myNode.neighbours){
                Node neighbour = G.nodeArray[neighbourInfo];
                if (!neighbour.active || neighbour.color==-1) continue;
                colorcounts[neighbour.color] ++;
            }
            if (bestColor == -1) {
                LinkedList<Integer> ll = new LinkedList<>();
                ll.add(myNode.id);
                myNode.color = FirstFreeColor;
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
        for (int l = 0; l < FirstFreeColor; l++) {
            LinkedList<Integer> color = colorclasses[l];
            if (color.size() < 5) {
                LinkedList<Integer> colorCopy = (LinkedList<Integer>) color.clone();
                for (int nodeID : colorCopy) {
                    Node myNode = G.nodeArray[nodeID];
                    int originalColor = myNode.color;
                    for (int neighbourInfo : myNode.neighbours) {
                        Node neighbour = G.nodeArray[neighbourInfo];
                        if (!neighbour.active || neighbour.color == -1) continue;
                        colorcounts[neighbour.color]--;
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
                        if (!neighbour.active || neighbour.color == -1) continue;
                        colorcounts[neighbour.color]++;
                    }
                    if (bestColor != -1) {
                        colorcounts[originalColor]--;
                        color.remove((Integer) myNode.id);
                        colorclasses[bestColor].add(myNode.id);
                        myNode.color = bestColor;
                        colorcounts[bestColor]++;
                    }
                }
                if (colorcounts[l] == 0) {
                    colorclasses[l] = colorclasses[--FirstFreeColor];
                    colorcounts[l] = colorcounts[FirstFreeColor];
                    colorcounts[FirstFreeColor] = 0;
                    colorclasses[FirstFreeColor] = new LinkedList<>();
                    for (int n : colorclasses[l]) G.nodeArray[n].color = l;
                    l--;
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
