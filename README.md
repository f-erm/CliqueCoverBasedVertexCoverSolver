# DemAlgoIngenieurIstNichtsZuSchwieur


Repository for Algorithm Engineering 2022/2023 at TU Berlin.

This is an exact solver for the NP-complete Vertex Cover problem. It achieves results close to competition-winning solvers as "WeGotYouCovered" from the PACE challenge, solving three additional graphs from their set of instances.
**How to use this solver:**
The program reads a graph in the following format: Each vertex has a name. Each line corresponds to an edge and consists of the name of the first vertex, an empty space and the name of the second vertex. The next edge must be defined in a new line. Comments can be made by the # symbol. An example for the K_{3,3}:
\# 6 9 -- this is a comment. By convention, the first line is a comment with the size of the graph (\# n m).
1 4
1 5
1 6
2 4
2 5
2 6
3 4
3 5
3 6


