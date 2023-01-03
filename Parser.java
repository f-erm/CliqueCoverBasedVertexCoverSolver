import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Parser {
    static boolean doDominating = true;
    public static Graph parseGraph(String pathname){
        //Create Graph as Hashmap of OldNodes
        Graph g = new Graph();
        try (BufferedReader br = new BufferedReader(new FileReader(pathname))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("#")) line = line.substring(0,line.indexOf('#'));
                if (line.trim().length() == 0) continue;
                String[] nodes = line.split("\\s+");

                g.addEdge(nodes[0], nodes[1]);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return createGraph(g);
    }


    //Same function as above, but input is not given as string and provided by the scanner instead. TODO: combine both into one function
    public static Graph parseGraph(){
        Graph g = new Graph();
        Scanner scanner = new Scanner(System.in);
        String line;
        while (scanner.hasNext()){
            line = scanner.nextLine();
            if (line.contains("#")) line = line.substring(0,line.indexOf('#'));
            if (line.trim().length() == 0) continue;
            String[] nodes = line.split("\\s+");

            g.addEdge(nodes[0], nodes[1]);
        }
      return createGraph(g);

    }
    private static Graph createGraph(Graph g){
        //Convert Hashmap into sorted list and apply REDUKTIONSREGELN.
        //This detour is mainly to allow sorting of the graph. Sorting after we have converted into an array is impossible, as we reference nodes by their graph array index
        long time = System.nanoTime();
        LinkedList<OldNode> ll = new LinkedList<>(g.nodeHashMap.values());
        Collections.sort(ll);
        g.oldNodeList = ll;
        g.setPartialSolution(Preprossessing.doAllThePrep(g));
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
        for (Node n : g.nodeArray){
            for (i = 0; i < n.neighbours.length; i++){
                Node v = g.nodeArray[n.neighbours[i]];
                if (v.id < n.id) v.neighbourPositions[n.neighbourPositions[i]] = i;
                else v.neighbourPositions[arrayContains(v.neighbours, n.id)] = i;
            }
        }
        try {
            for (Node n : g.nodeArray) { // find initial triangles in the graph
                if ((System.nanoTime() - time) / 1024 > 0) {
                    doDominating = false;
                    for (Node node : g.nodeArray) node.triangles.clear();
                    break;
                }
                int cu = 0;
                for (int u : n.neighbours) {
                    if (n.id > u) {
                        cu++;
                        continue;
                    }
                    int cv = 0;
                    for (int v : g.nodeArray[u].neighbours) {
                        if (u >= v) {
                            cv++;
                            continue;
                        }
                        int cont = arrayContains(n.neighbours, v);
                        if (cont >= 0) {
                            n.triangleCounts[cu]++;
                            n.triangles.add(cu);
                            n.triangles.add(cont);
                            n.triangles.add(cv);
                            g.nodeArray[u].triangles.add(n.neighbourPositions[cu]);
                            g.nodeArray[u].triangles.add(cv);
                            g.nodeArray[u].triangles.add(cont);
                            g.nodeArray[v].triangles.add(n.neighbourPositions[cont]);
                            g.nodeArray[v].triangles.add(g.nodeArray[u].neighbourPositions[cv]);
                            g.nodeArray[v].triangles.add(cu);
                            g.nodeArray[u].triangleCounts[n.neighbourPositions[cu]]++;
                            g.nodeArray[u].triangleCounts[cv]++;
                            g.nodeArray[v].triangleCounts[g.nodeArray[u].neighbourPositions[cv]]++;
                            n.triangleCounts[cont]++;
                            g.nodeArray[v].triangleCounts[n.neighbourPositions[cont]]++;
                        }
                        cv++;
                    }
                    cu++;
                }
            }
        }catch (OutOfMemoryError e){
            doDominating = false;
            for (Node n : g.nodeArray) n.triangles.clear();
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
