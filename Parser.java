import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Parser {
    public static Graph parseGraph(String pathname){
        //Create Graph as Hashmap of OldNodes
        Graph g = new Graph();
        try (BufferedReader br = new BufferedReader(new FileReader(pathname))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("#")) line = line.substring(0,line.indexOf('#'));
                if (line.contains("p")) line = line.substring(0,line.indexOf('p'));
                if (line.trim().length() == 0) continue;
                String[] nodes = line.split("\\s+");

                g.addEdge(nodes[0], nodes[1]);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        g.makeConsistent();
        return createGraph(g);
    }


    //Same function as above, but input is not given as string and provided by the scanner instead.
    public static Graph parseGraph(){
        Graph g = new Graph();
        Scanner scanner = new Scanner(System.in);
        String line;
        while (scanner.hasNext()){
            line = scanner.nextLine();
            if (line.contains("#")) line = line.substring(0,line.indexOf('#'));
            if (line.contains("p")) line = line.substring(0,line.indexOf('p'));
            if (line.trim().length() == 0) continue;
            String[] nodes = line.split("\\s+");

            g.addEdge(nodes[0], nodes[1]);
        }
        g.makeConsistent();
      return createGraph(g);

    }
    private static Graph createGraph(Graph g){
        //Convert Hashmap into sorted list and apply REDUKTIONSREGELN.
        //This detour is mainly to allow sorting of the graph. Sorting after we have converted into an array is impossible, as we reference nodes by their graph array index
        LinkedList<OldNode> ll = new LinkedList<>(g.nodeHashMap.values());
        Collections.sort(ll);
        Collections.reverse(ll);
        g.oldNodeList = ll;
        g.setPartialSolution(Preprossessing.doAllThePrep(g));
        Collections.sort(ll);
        //convert list of OldNodes into array of Nodes
        g.nodeArray = new Node[g.oldNodeList.size()];
        int j = 0;
        for (OldNode oldNode : ll){
            oldNode.id = j++;
        }
        int i = 0;
        for (OldNode oldNode : ll){
            oldNode.id = i;
            Node n = new Node(oldNode);
            g.activeNodes++;
            g.nodeArray[i++] = n;
        }
        g.permutation = new Node[g.nodeArray.length];
        g.posInPermutation = new int[g.nodeArray.length];
        g.borderIndices = new int[g.nodeArray.length];
        // initialize the data structure to keep nodes sorted by degree
        if (g.nodeArray.length > 0) {
            int deg = g.nodeArray[0].activeNeighbours;
            for (int k = deg + 1; k < g.borderIndices.length; k++) g.borderIndices[k] = -1;
            for (Node n : g.nodeArray) {
                if (n.activeNeighbours < deg) {
                    while (deg > n.activeNeighbours) g.borderIndices[deg--] = n.id - 1;
                }
                g.permutation[n.id] = n;
                g.posInPermutation[n.id] = n.id;
                for (i = 0; i < n.neighbours.length; i++) {
                    Node v = g.nodeArray[n.neighbours[i]];
                    if (v.id < n.id) v.neighbourPositions[n.neighbourPositions[i]] = i;
                    else v.neighbourPositions[arrayContains(v.neighbours, n.id)] = i;
                }
            }
            for (; deg >= 0; deg--) g.borderIndices[deg] = g.nodeArray.length - 1;
        }
        return g;
    }

    private static int arrayContains(int[] array, int el){
        int j = 0;
        for (int i : array) {
            if (i == el) return j;
            j++;
        }
        return -1;
    }

}
