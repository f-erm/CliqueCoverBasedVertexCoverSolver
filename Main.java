import java.util.LinkedList;

public class Main {
    public static void main(String[] args) {
        long startTime = System.nanoTime();
        Graph G = Parser.parseGraph("testGraphs/1-random/000020_000000000120.dimacs");
        System.out.println("#parsen works, it took "+ ((System.nanoTime()-startTime)/1000000)+ " ms");
        LinkedList<Node> vc = Algorithms.vc(G);
        System.out.println("#the vertex cover is: ");
        for (Node node: vc) {
            System.out.println(node.name);
        }
        System.out.println("#it took "+ ((System.nanoTime()-startTime)/1000000) + " ms");
    }
}