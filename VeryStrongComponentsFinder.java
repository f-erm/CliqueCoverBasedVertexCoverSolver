import java.util.HashSet;
import java.util.LinkedList;
import java.util.Stack;

public class VeryStrongComponentsFinder {
    int index;
    Stack<Integer> stack;
    LinkedList<LinkedList<Integer>> allComponents;
    Graph B;
    LinkedList<Integer>[] residualGraph;
    int[] ccindex;
    int[] lowLink;
    final int size;
    boolean[] onStack;

    public VeryStrongComponentsFinder(Graph B, LinkedList<Integer>[] residualGraph){
        this.B = B;
        this.residualGraph = residualGraph;
        size = residualGraph.length - 2;
        stack = new Stack<>();
        allComponents = new LinkedList<>();
        index = 0;
        ccindex = new int[size];
        lowLink = new int[size];
        onStack = new boolean[size];
    }
    public  LinkedList<LinkedList<Integer>> findStrongComponents(){
        stack = new Stack<>();
        allComponents= new LinkedList<>();
        index = 0;
        for (int i = 0; i < size; i++){
            ccindex[i] = -1;
            lowLink[i] = -1;
        }
        for (int i = 0; i < size; i++) {
            Node node = B.nodeArray[i];
            if (node.active && ccindex[i] == -1){
                strongConnect(node.id);
            }
        }
        return checkZulaessig();
    }

    private void strongConnect(Integer node) {
        Stack<Integer> work = new Stack<>();
        Stack<Integer> work2 = new Stack<>();
        work.push(node);
        work2.push(0);
        while (!work.isEmpty()){
            int u = work.pop();
            int j = work2.pop();
            if (j == 0){
                ccindex[u] = index;
                lowLink[u] = index++;
                stack.push(u);
                onStack[u] = true;
            }
            boolean recurse = false;
            for (int i = j; i < residualGraph[u].size(); i++){
                int v = residualGraph[u].get(i);
                if (v >= size || !B.nodeArray[v].active) continue;
                if (lowLink[v] == -1){
                    work.push(u);
                    work2.push(i + 1);
                    work.push(v);
                    work2.push(0);
                    recurse = true;
                    break;
                }
                else if (onStack[v]) lowLink[u] = min(lowLink[u], ccindex[v]);
            }
            if (!recurse){
                if (lowLink[u] == ccindex[u]){
                    LinkedList<Integer> scc = new LinkedList<>();
                    while (true){
                        int v = stack.pop();
                        onStack[v] = false;
                        scc.add(v);
                        if (v == u) break;
                    }
                    allComponents.add(scc);
                }
                if (!work.isEmpty()){
                    int v = u;
                    u = work.peek();
                    lowLink[u] = min(lowLink[u], lowLink[v]);
                }
            }
        }
    }

    private  LinkedList<LinkedList<Integer>> checkZulaessig() {
        int hkSize = B.nodeArray.length / 2;
        LinkedList<LinkedList<Integer>> goodScc = new LinkedList<>();
        HashSet<Integer> evilNodes = new HashSet<>();
        for (LinkedList<Integer> scc : allComponents) {
            boolean add = true;
            HashSet<Integer> hs = new HashSet<>();
            for (int id : scc) {
                if (id > hkSize) id -= hkSize;
                if (!hs.add(id)){
                    evilNodes.addAll(scc);
                    add = false;
                    break;
                }
            }
            if (!add) continue;
            for (int node : scc){
                if (!add) break;
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
    private  int min(int a, int b){
        if (a == -1 ) return b;
        else if (b == -1) return a;
        else if (a <= b) return a;
        else return b;
    }


}
