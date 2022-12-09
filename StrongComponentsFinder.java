import java.util.HashSet;
import java.util.LinkedList;
import java.util.Stack;

public class StrongComponentsFinder {
     int index;
     Stack<Integer> stack;
     LinkedList<LinkedList<Integer>> allComponents;
     Graph B;
     LinkedList<Integer>[] residualGraph;
     int[] ccindex;
     int[] lowLink;
     boolean[] onStack;

     public StrongComponentsFinder(Graph B, LinkedList<Integer>[] residualGraph){
         this.B = B;
         this.residualGraph = residualGraph;
         stack = new Stack<>();
         allComponents= new LinkedList<>();
         index = 0;
         ccindex = new int[B.nodeArray.length - 1];
         lowLink = new int[B.nodeArray.length - 1];
         onStack = new boolean[B.nodeArray.length - 1];
         for (int i = 0; i < ccindex.length; i++){
             ccindex[i] = -1;
             lowLink[i] = -1;
         }
     }
    public  LinkedList<LinkedList<Integer>> findStrongComponents(){
        stack = new Stack<>();
        allComponents= new LinkedList<>();
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

    private  void strongConnect(Node node){
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
            LinkedList<Integer> strongComponent = new LinkedList<>();
            Integer integer = stack.pop();
            Node w = B.nodeArray[integer];
            onStack[integer] = false;
            strongComponent.add(integer);
            while (w != node){
                integer = stack.pop();
                w = B.nodeArray[integer];
                onStack[integer] = false;
                strongComponent.add(integer);
            }
            allComponents.add(strongComponent);
        }
    }


    //this method is not redundent because -1 is the value for uninitialized and I dont want this to be the minimum
    private  int min(int a, int b){
        if (a == -1 ) return b;
        else if (b == -1) return a;
        else if (a <= b) return a;
        else return b;
    }

    private  LinkedList<LinkedList<Integer>> checkZulaessig() {
        int size = B.nodeArray.length / 2;
        LinkedList<LinkedList<Integer>> goodScc = new LinkedList<>();
        HashSet<Integer> evilNodes = new HashSet<>();
        for (LinkedList<Integer> scc : allComponents) {
            boolean add = true;
            HashSet<Integer> hs = new HashSet<>();
            for (int id : scc) {
                if (id > size) id -= size;
                if (!hs.add(id)){
                    evilNodes.addAll(scc);
                    add = false;
                    break;
                }
            }
            for (int node : scc){
                for (int neighbour : residualGraph[node]){
                    /*if (evilNodes.contains(neighbour)){
                        evilNodes.addAll(scc);
                        break;*/
                    if (neighbour > B.nodeArray.length - 2 || !B.nodeArray[neighbour].active) continue;
                    if (!scc.contains(neighbour)){
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
