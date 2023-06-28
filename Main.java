import java.util.LinkedList;

//Main halt. duh...
public class Main {
    public static void main(String[] args) {
        long startTime = System.nanoTime();
        // the variable in the next line can be set manually:
        // setting inputFromFile = true, the program will read the graph from the file specified below.
        // setting inputFromFile = false, the program will read the graph from the standard input.
        boolean inputFromFile = true; //set to true for file input, false for autograder
        Graph G;
        if (inputFromFile) G = Parser.parseGraph("../Algorithm Engineering/3-medium-sized/vc005.dimacs");
        else G = Parser.parseGraph();
        Branching b = new Branching(G);
        LinkedList<Node> vc = b.solve();
        //System.out.println("#recursive steps: " + b.recursiveSteps);
        System.out.println("# the vertex cover is: ");
        long time = System.nanoTime();
        for (Node node : vc) {
                System.out.println(node.name);
        }
        System.out.println("# VC size : " + vc.size());
        System.out.println("# Output: " + (System.nanoTime() - time));
        System.out.println("# it took " + ((System.nanoTime() - startTime) / 1000000) + " ms");
    }
}