import java.util.HashSet;
import java.util.LinkedList;
import java.util.Stack;

public class StrongComponentsFinder {
    static int index = 0;

    public static LinkedList<LinkedList<Node>> findStrongComponents(Graph B, LinkedList<Integer>[] residualGraph){
        Stack<Node> stack = new Stack<>();
        LinkedList<LinkedList<Node>> allComponents= new LinkedList<>();
        for (int i = 0; i < B.nodeArray.length; i++) {
            Node node = B.nodeArray[i];
            if (node.active && node.ccindex == -1 ){
                strongConnect(node, stack,allComponents,B,residualGraph);
            }
        }
        return checkZulaessig(allComponents, B);
    }

    private static void strongConnect(Node node, Stack<Node> stack,LinkedList<LinkedList<Node>> allComponents, Graph B, LinkedList<Integer>[] residualGraph){
        node.ccindex = index;
        node.lowLink = index;
        index ++;
        stack.push(node);
        node.onStack = true;

        for (int neighbourID : residualGraph[node.id]) {
            Node neighbour = B.nodeArray[neighbourID];
            if (!neighbour.active) continue;
            if (neighbour.ccindex == -1){
                strongConnect(neighbour, stack, allComponents, B, residualGraph);
                node.lowLink = min(node.lowLink, neighbour.lowLink);
            }
            else if (neighbour.onStack) {
                node.lowLink = min(node.lowLink, neighbour.ccindex);
            }
        }

        if (node.lowLink == node.ccindex){
            LinkedList<Node> strongComponent = new LinkedList<>();
            Node w = stack.pop();
            w.onStack = false;
            strongComponent.add(w);
            while (w != node){
                w = stack.pop();
                w.onStack = false;
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

    private static LinkedList<LinkedList<Node>> checkZulaessig(LinkedList<LinkedList<Node>> allcomp, Graph B) {
        int size = B.nodeArray.length / 2;
        LinkedList<LinkedList<Node>> goodScc = new LinkedList<>();
        HashSet<Node> evilNodes = new HashSet<>();
        for (LinkedList<Node> scc : allcomp) {
            HashSet<Integer> hs = new HashSet<>();
            for (Node node : scc) {
                int id = node.id;
                if (id > size) id -= size;
                if (!hs.add(id)){
                    evilNodes.addAll(scc);
                    break;
                }
            }
            for (Node node : scc){
                for (int neighbour : node.neighbours){
                    if (evilNodes.contains(neighbour)){
                        evilNodes.addAll(scc);
                        break;
                    }
                }
            }
            goodScc.add(scc);
        }
        return goodScc;
    }

}
