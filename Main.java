import java.util.LinkedList;

//Main halt. duh...
public class Main {
    public static void main(String[] args) {
        long startTime = System.nanoTime();
        boolean inputFromFile = true; //set to true for file input, false for autograder
        Graph G;
        if (inputFromFile)
            G = Parser.parseGraph("../Algorithm Engineering/2-social-networks/83-roadNet-PA.txt.dimacs");
        //if (inputFromFile) G = Parser.parseGraph("../Algorithm Engineering/3-medium-sized/vc089.dimacs");
        else G = Parser.parseGraph();
        InitialSolution insol = new InitialSolution(G, startTime, Parser.doDominating);
        LinkedList<Node> vc = insol.vc(true);
        /*Algorithms a = new Algorithms();
        LinkedList<Node> vc = a.vc(G);*/
        System.out.println("#recursive steps: " + ((System.nanoTime() - startTime) / 1000000));
        System.out.println("# the vertex cover is: ");
        long time = System.nanoTime();
        //System.out.println("#recursive steps: " + a.recursiveSteps);
        for (Node node : vc) {
                System.out.println(node.name);
        }

        System.out.println("# VC size : " + vc.size());
        System.out.println("# Output: " + (System.nanoTime() - time));
        System.out.println("# it took " + ((System.nanoTime() - startTime) / 1000000) + " ms");
        /*System.out.println("# degrules - time: " + a.reduction.deg2Time);
        System.out.println("# degrules - cuts: " + a.reduction.deg2cuts);
        System.out.println("# unconfined - time: " + a.reduction.unconfTime);
        System.out.println("# unconfined - cuts: " + a.reduction.unconfcuts);
        System.out.println("# twin - time: " + a.reduction.twintime);
        System.out.println("# twin - cuts: " + a.reduction.twincuts);
        System.out.println("# lp - time: " + a.reduction.lpTime);
        System.out.println("# lp - cuts: " + a.reduction.lpcuts);
        System.out.println("# cc - time: " + a.reduction.cctime);
        System.out.println("# cc - cuts: " + a.reduction.cccuts);
        System.out.println("# domination - time: " + a.reduction.domtime);
        System.out.println("# domination - cuts: " + a.reduction.domcuts);*/
    }
}