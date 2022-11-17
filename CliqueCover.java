import java.util.Collections;
import java.util.LinkedList;

public class CliqueCover {

    LinkedList<LinkedList<Integer>> colorclasses;
    int lowerBound = 0;
    Graph G;
    LinkedList<Integer> permutation;
    int reerun;

    public CliqueCover(Graph G){
        this.G = G;
    }

    public int cliqueCoverIterations(int k) {
        //long startTime = System.nanoTime();
        permutation = new LinkedList<Integer>();
        //int[] colorclasses = new int[G.nodeArray.length];
        for (int i = 0; i < G.nodeArray.length; i++) {
            permutation.add(i);
        }
        Collections.shuffle(permutation);
        reerun = 5;
        lowerBound = 0;


        while (k > 0 && reerun > 0){
            colorclasses = new LinkedList<LinkedList<Integer>>();
            for (int j = 0; j < G.nodeArray.length; j++) {
                G.nodeArray[j].color = -1;
            }
            LinkedList<Integer> oldperm = (LinkedList<Integer>) permutation.clone();
            permutation = cliqueCover(G, permutation, colorclasses);
            LinkedList<Integer> permcopy = (LinkedList<Integer>) permutation.clone();
            permcopy.removeAll(oldperm);
            for (Integer integer: permcopy) {
                System.out.println(integer);
            }
        k--;
        }
        //System.out.println("#the clique cover took "+ ((System.nanoTime()-startTime)/1000000) + " ms");
        return lowerBound;
    }


    private LinkedList<Integer> cliqueCover(Graph G, LinkedList<Integer> permutation, LinkedList<LinkedList<Integer>> colorclasses){
        for (int i: permutation) {
            Node myNode = G.nodeArray[i];
            if (!myNode.active) continue;
            int[] neighbourcolors = new int[colorclasses.size()];
            for (int[] neighbourInfo: myNode.neighbours) {
                Node neighbour = G.nodeArray[neighbourInfo[0]];
                if (!neighbour.active || neighbour.color==-1) continue;
                neighbourcolors[neighbour.color]++;
            }
            boolean newcolorclassneeded = true;
            for (int j = 0; j < colorclasses.size(); j++) {
                if(colorclasses.get(j).size()==neighbourcolors[j]){
                    myNode.color = j;
                    colorclasses.get(j).add(i);
                    newcolorclassneeded = false;
                    break;
                }
            }
            if(newcolorclassneeded){
                LinkedList<Integer> newColor = new LinkedList<Integer>();
                newColor.add(i);
                G.nodeArray[i].color = colorclasses.size();
                colorclasses.add(newColor);
            }
        }
        LinkedList<Integer> perm = new LinkedList<>();
        Collections.shuffle(colorclasses);
        for (LinkedList<Integer> color: colorclasses) {
            perm.addAll(color);
        }
        int newlowerBound = G.activeNodes - colorclasses.size();
        if (newlowerBound <= lowerBound){
            reerun --;
        }
        else {
            lowerBound = newlowerBound;
        }
        return perm;
    }

}
