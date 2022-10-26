public class Main {
    public static void main(String[] args) {
        System.out.println("Hello, World!");
        Graph g = Parser.parseGraph("testGraphs/3-medium-sized/vc001.dimacs");
        System.out.println("ok.");
    }
}