import java.util.LinkedList;
import java.util.Stack;




public class Reduction {

    LinkedList<Node> uselessNeighbours;
    LinkedList<Node> VCNodes;

    Stack<int[]> removedNodes;

    Graph G;

    public void reduction(Graph G){
        this.G = G;
        removedNodes.push(new int[]{0});
    }
    public void removeDegreeLargerKAndZero(Graph G, int k){
        LinkedList<Node> partialSolution = new LinkedList<>();
        for (Node node : G.nodeArray){
            if (node.activeNeighbours > k){
                partialSolution.add(node);
                G.removeNode(node);
                removedNodes.push(new int[]{2,node.id});
            }
            else if (node.activeNeighbours == 0){
                G.removeNode(node);
                removedNodes.push(new int[]{1, node.id});
            }
        }
        G.partialSolution.addAll(partialSolution);
    }

    public void revertReduction() {
        int[] action = removedNodes.pop();
        while (action[0] != 0) {
            Node node = G.nodeArray[action[1]];
            switch (action[0]) {
                case 1: // useless Nodes
                    G.reeaddNode(node);
                    break;
                case 2: //usefull Nodes
                    G.reeaddNode(node);
                    VCNodes.remove(node);
                    break;
            }
            action = removedNodes.pop();
        }
    }
        

    public LinkedList<Node> reduceThroughCC(CliqueCover cc, int k, Graph G){
        LinkedList<Node> cliqueNeighbours = new LinkedList<>();
        LinkedList<Node> uselessNeighbours = new LinkedList<>();
        for (int i = 0; i < cc.FirstFreeColor; i++) {
            for (Integer nodeId: cc.colorclasses[i]) {
                Node v = G.nodeArray[nodeId];
                if(v.active && v.activeNeighbours > 0 && v.activeNeighbours == (cc.colorclasses[i].size()-1)){
                    if (k >= v.activeNeighbours) {
                        for (int u : v.neighbours) {
                            Node toDelete = G.nodeArray[u];
                            if (toDelete.active) {
                                cliqueNeighbours.add(toDelete);
                                k--;
                                G.removeNode(toDelete);
                            }
                        }
                    }
                    else {
                        return null;
                    }
                    uselessNeighbours.add(v);
                    G.removeNode(v);
                    break;
                }
            }
        }
        this.uselessNeighbours = uselessNeighbours;
        this.VCNodes = cliqueNeighbours;
        return null;
    }

    public void revertReduceCC(Graph G){
        for (Node node: uselessNeighbours) {
            G.reeaddNode(node);
        }
        for (Node node: VCNodes) {
            G.reeaddNode(node);
        }
    }


    private void removeUselessNodes(Node node){
        removedNodes.push(new int[]{1, node.id});
        G.removeNode(node);
    }

    private void removeVCNodes(Node node){
        removedNodes.push(new int[]{2, node.id});
        VCNodes.add(node);
        G.removeNode(node);
    }

    private boolean unconfined(Graph G, int v){
        //input: G und index von v, output: v unconfined?
        LinkedList<Integer> S = new LinkedList<Integer>();
        S.add(v);
        LinkedList<Integer> N_S = new LinkedList<Integer>();
        for(Integer i : G.nodeArray[v].neighbours) N_S.add(v);//N_S = N(v)
        Integer final_u = null; //the current best u
        LinkedList<Integer> final_u_Cut = new LinkedList<Integer>(); //N(u) \ N(S) for current u and S
        while(true){
            for(Integer u : N_S){
                LinkedList<Integer> N_u = new LinkedList<>();
                for (Integer i : G.nodeArray[u].neighbours) N_u.add(i);
                if (CheckSizeOfCut(G.nodeArray[u].neighbours, S)){
                    if (final_u == null){//the first node that satisfied |N(u) \cap S| = 1
                        final_u = u;
                        final_u_Cut = GetSizeSetminus(N_u, N_S, Integer.MAX_VALUE);
                    }
                    else{//the other nodes
                        LinkedList<Integer> res = GetSizeSetminus(N_u, N_S, final_u_Cut.size());
                        if (res.size() < final_u_Cut.size()){
                            final_u = u;
                            final_u_Cut = res;
                        }
                    }
                }
            }
            //evaluate N(u) \cap S
            if (final_u==null) return false;
            if (final_u_Cut.size()==0) return true;
            if (final_u_Cut.size()==1) {
                Integer w = final_u_Cut.get(0);
                S.add(w);
                for (Integer i : G.nodeArray[w].neighbours) {
                    if(!N_S.contains(i)){//wir wollen keine Doppelungen
                        N_S.add(i);
                    }
                }
                continue;
            }
            return false;
        }
    }

    private boolean CheckSizeOfCut(int[] N_u, LinkedList<Integer> S){
        //Helper function of unconfined. Returns true if N_u \cap S = 1
        boolean found_one = false;
        for (Integer i : N_u ){
            if (S.contains(i)){
                if (found_one){
                    return false;
                }else{
                found_one = true;
                }
            }
        }
        return found_one;
    }

    private LinkedList<Integer> GetSizeSetminus(LinkedList<Integer> N_u, LinkedList<Integer> N_S, Integer tobeat){
        /*resturns Rest = N_u \ N_S . Stops if Rest.size > tobeat. If lookingforW ist set to true, we are calling the 
        function with intention of checking wether R = {w} for some w. Therefore we can stop if |Rest| > 1. */
        LinkedList<Integer> Rest = new LinkedList<Integer>();
        for (Integer i : N_u){
            if (!N_S.contains(i)) {
                Rest.add(i);
                if (Rest.size() > tobeat) return Rest;
            }
        }
        return Rest;
    }
}




