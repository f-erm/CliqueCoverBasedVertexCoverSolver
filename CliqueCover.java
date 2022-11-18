import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class CliqueCover {

    LinkedList<Integer>[] colorclasses;
    int[] colorcounts;
    int FirstFreeColor = 0;//keep track of first availible spot in colorclasses
    int lowerBound = 0;
    Graph G;
    LinkedList<Integer> permutation;
    int reerun;

    public CliqueCover(Graph G){
        this.G = G;
    }

    public int cliqueCoverIterations(int k, int reerun) {
        permutation = new LinkedList<Integer>();
        for (int i = 0; i < G.nodeArray.length; i++) {
            permutation.add(i);
        }
        Collections.shuffle(permutation);
        this.reerun = reerun;
        lowerBound = 0;
        while (k > 0 && reerun > 0){
            colorclasses = new LinkedList[G.activeNodes];
            colorcounts = new int[G.activeNodes];
            FirstFreeColor = 0;
            //set all nodes to color unknown
            for (int j = 0; j < G.nodeArray.length; j++) {
                G.nodeArray[j].color = -1;
            }
            permutation = cliqueCover();
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
            for (int[] neighbourInfo: myNode.neighbours) {
                Node neighbour = G.nodeArray[neighbourInfo[0]];
                if (!neighbour.active || neighbour.color==-1) continue;
                colorcounts[neighbour.color] --;
                if (colorcounts[neighbour.color] == 0){
                    colorclasses[neighbour.color].add(myNode.id);
                    myNode.color = neighbour.color;
                    colorcounts[neighbour.color]++;
                    remember = neighbourInfo[0];
                    createNewClass = false;
                    break;
                }

            }
            for (int[] neighbourInfo : myNode.neighbours){
                Node neighbour = G.nodeArray[neighbourInfo[0]];
                if (!neighbour.active || neighbour.color==-1) continue;
                colorcounts[neighbour.color] ++;
                if (remember == neighbourInfo[0]) break;
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
