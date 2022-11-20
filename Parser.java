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
        LinkedList<OldNode> ll = new LinkedList<>(g.nodeHashMap.values());
        Collections.sort(ll);
        g.oldNodeList = ll;
        Reduction.removeDegreeOne(g);
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
        return g;
    }

}
