import java.util.LinkedList;

//Main halt. duh...
public class Main {
    public static void main(String[] args) {
        long startTime = System.nanoTime();
        Graph G;
        if (args.length > 0 && ! args[0].equals("SCAN")) G = Parser.parseGraph(args[0]);
        else G = Parser.parseGraph();
        long inputTime = (System.nanoTime() - startTime) / 1000000;
        Branching b = new Branching(G, startTime);
        if (args.length > 1 && ! args[1].equals("MAX")) {
            b.totalTime = Long.parseLong(args[1]) * 1000; // parsing in ms, saving in μs
            b.timeLimit = true;
        }
        if (args.length > 2){
            b.upperBoundTime = Long.parseLong(args[2]) * 1000; // for setting user-defined bound times
        }
        LinkedList<Node> vc = b.solve();
        System.out.println("# Recursive steps: " + b.recursiveSteps);
        System.out.println("# The vertex cover is: ");
        long time = System.nanoTime();
        for (Node node : vc) {
                System.out.println(node.name);
        }
        System.out.println("# VC size : " + vc.size());
        System.out.println("# System input time: " + inputTime + " ms");
        System.out.println("# System output time: " + (System.nanoTime() - time) / 1000000 + " ms");
        System.out.println("# Total time: " + ((System.nanoTime() - startTime) / 1000000) + " ms");
    }
}