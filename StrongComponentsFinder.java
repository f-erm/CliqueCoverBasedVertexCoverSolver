import java.util.LinkedList;
import java.util.Stack;

public class StrongComponentsFinder {
    static int index = 0;


    public static LinkedList<LinkedList<Node>> findStrongComponents(Graph G){
        Stack<Node> stack = new Stack<>();
        LinkedList<LinkedList<Node>> allComponents= new LinkedList<>();
        for (Node node: G.nodeArray) {
            if (node.active && node.ccindex == -1 ){
                strongConnect(node, stack,allComponents,G);
            }
        }
        return allComponents;
    }

    private static void strongConnect(Node node, Stack<Node> stack,LinkedList<LinkedList<Node>> allComponents, Graph G){
        node.ccindex = index;
        node.lowLink = index;
        index ++;
        stack.push(node);
        node.onStack = true;

        for (int neighbourID: node.neighbours) {
            Node neighbour = G.nodeArray[neighbourID];
            if (!neighbour.active) continue;
            if (neighbour.ccindex == -1){
                strongConnect(neighbour, stack, allComponents, G);
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


}
