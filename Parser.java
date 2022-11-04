import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Parser {

    public static Graph parseGraph(String pathname){
        Graph g = new Graph(6200);
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
        LinkedList<Node> ll = new LinkedList<>(g.nodeHashMap.values());
        Collections.sort(ll);
        g.setNodeList(ll);
        return g;
    }
    public static Graph parseGraph(){
        Graph g = new Graph(6200);
        Scanner scanner = new Scanner(System.in);
        String line;
        while (scanner.hasNext()){
            line = scanner.nextLine();
            if (line.contains("#")) line = line.substring(0,line.indexOf('#'));
            if (line.trim().length() == 0) continue;
            String[] nodes = line.split("\\s+");

            g.addEdge(nodes[0], nodes[1]);
        }
        LinkedList<Node> ll = new LinkedList<>(g.nodeHashMap.values());
        Collections.sort(ll);
        g.setNodeList(ll);
        return g;

    }

}
