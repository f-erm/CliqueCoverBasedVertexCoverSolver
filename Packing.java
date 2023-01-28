import java.util.HashSet;

public class Packing {

    int count;
    int type;
    int neighbourhoodPlusSize;


    public Packing(Node v, Graph G){
        type = 1;
        for (int neighbour : v.neighbours)
            if (G.nodeArray[neighbour].inVC){
            count++;
        }
    }

    public Packing(Node v, Node x, Graph G){
        type = 2;
        for (int neighbour : v.neighbours)
            if (G.nodeArray[neighbour].inVC){
                count++;
            }
        HashSet<Integer> hs = new HashSet<>();
        for (int n : x.neighbours) hs.add(n);
        hs.add(x.id);
        for (int n : v.neighbours) if (!hs.contains(n)) neighbourhoodPlusSize++;
    }

}
