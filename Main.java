import java.util.LinkedList;

public class Main {
    public static void main(String[] args) {
        long startTime = System.nanoTime();
        Graph G = Parser.parseGraph("../Algorithm Engineering/2-social-networks/04-polbooks.graph.dimacs");
        //Graph G = Parser.parseGraph();
        Algorithms a = new Algorithms();
        System.out.println("#parsen works, it took "+ ((System.nanoTime()-startTime)/1000000)+ " ms");
        LinkedList<Node> vc = a.vc(G);
        System.out.println("#the vertex cover is: ");
        System.out.println("#recursive steps: " + a.recursiveSteps);
        for (Node node: vc) {
            System.out.println(node.name);
        }
        System.out.println("#it took "+ ((System.nanoTime()-startTime)/1000000) + " ms");
    }

}