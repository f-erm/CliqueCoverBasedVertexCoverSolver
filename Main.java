import java.util.LinkedList;

//Main halt. duh...
public class Main {
    public static void main(String[] args) {
        long startTime = System.nanoTime();
        boolean inputFromFile = false; //set to true for file input, false for autograder
        Graph G;
        if (inputFromFile) G = Parser.parseGraph("../Algorithm Engineering/2-social-networks/16-3elt.graph.dimacs");
        //if (inputFromFile) G = Parser.parseGraph("../Algorithm Engineering/3-medium-sized/vc005.dimacs");
        else G = Parser.parseGraph();
        Algorithms a = new Algorithms();
        LinkedList<Node> vc = a.vc(G);
        System.out.println("#the vertex cover is: ");
        System.out.println("#recursive steps: " + a.recursiveSteps);
        for (Node node: vc) {
            System.out.println(node.name);
        }
        System.out.println("#it took "+ ((System.nanoTime()-startTime)/1000000) + " ms");
    }

}