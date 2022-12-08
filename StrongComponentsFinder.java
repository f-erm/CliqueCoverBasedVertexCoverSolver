import java.util.HashSet;
import java.util.LinkedList;
import java.util.Stack;

public class StrongComponentsFinder {
    static int index;
    static Stack<Integer> stack;
    static LinkedList<LinkedList<Node>> allComponents;
    static Graph B;
    static LinkedList<Integer>[] residualGraph;
    static int[] ccindex;
    static int[] lowLink;
    static boolean[] onStack;

    public static LinkedList<LinkedList<Node>> findStrongComponents(Graph BIn, LinkedList<Integer>[] residualGraphIn){
        stack = new Stack<>();
        allComponents= new LinkedList<>();
        B = BIn;
        residualGraph = residualGraphIn;
        index = 0;
        ccindex = new int[B.nodeArray.length - 1];
        lowLink = new int[B.nodeArray.length - 1];
        onStack = new boolean[B.nodeArray.length - 1];
        for (int i = 0; i < ccindex.length; i++){
            ccindex[i] = -1;
            lowLink[i] = -1;
        }
        for (int i = 0; i < B.nodeArray.length - 1; i++) {
            Node node = B.nodeArray[i];
            if (node.active && ccindex[i] == -1){
                strongConnect(node);
            }
        }
        return checkZulaessig();
    }

    private static void strongConnect(Node node){
        if (node.id > B.nodeArray.length - 2) return;
        ccindex[node.id] = index;
        lowLink[node.id] = index;
        index ++;
        stack.push(node.id);
        onStack[node.id] = true;

        for (int neighbourID : residualGraph[node.id]) {
            if (neighbourID > B.nodeArray.length - 2 ||!B.nodeArray[neighbourID].active) continue;
            Node neighbour = B.nodeArray[neighbourID];
            if (ccindex[neighbourID] == -1){
                strongConnect(neighbour);
                lowLink[node.id] = min(lowLink[node.id], lowLink[neighbourID]);
            }
            else if (onStack[neighbourID]) {
                lowLink[node.id] = min(lowLink[node.id], ccindex[neighbourID]);
            }
        }

        if (lowLink[node.id] == ccindex[node.id]){
            LinkedList<Node> strongComponent = new LinkedList<>();
            Integer integer = stack.pop();
            Node w = B.nodeArray[integer];
            onStack[integer] = false;
            strongComponent.add(w);
            while (w != node){
                integer = stack.pop();
                w = B.nodeArray[integer];
                onStack[integer] = false;
                strongComponent.add(w);
            }
            allComponents.add(strongComponent);
        }
    }


    //this method is not redundent because -1 is the value for uninitialized and I dont want this to be the minimum
    private static int min(int a, int b){
        if (a == -1 ) return b;
        else if (b == -1) return a;
        else if (a <= b) return a;
        else return b;
    }

    private static LinkedList<LinkedList<Node>> checkZulaessig() {
        int size = B.nodeArray.length / 2;
        LinkedList<LinkedList<Node>> goodScc = new LinkedList<>();
        HashSet<Node> evilNodes = new HashSet<>();
        for (LinkedList<Node> scc : allComponents) {
            boolean add = true;
            HashSet<Integer> hs = new HashSet<>();
            for (Node node : scc) {
                int id = node.id;
                if (id > size) id -= size;
                if (!hs.add(id)){
                    evilNodes.addAll(scc);
                    add = false;
                    break;
                }
            }
            for (Node node : scc){
                for (int neighbour : residualGraph[node.id]){
                    /*if (evilNodes.contains(neighbour)){
                        evilNodes.addAll(scc);
                        break;*/
                    if (neighbour > B.nodeArray.length - 2 || !B.nodeArray[neighbour].active) continue;
                    Node neighbourNode = B.nodeArray[neighbour];
                    if (!scc.contains(neighbourNode)){
                        evilNodes.addAll(scc);
                        add = false;
                        break;
                    }
                }
            }
            if (add) goodScc.add(scc);
        }
        return goodScc;
    }

}
