# Clique Cover based Vertex Cover Solver


This is an exact solver for the NP-complete Vertex Cover problem. It achieves results close to competition-winning solvers as "WeGotYouCovered" from the PACE challenge, solving three additional graphs from their set of instances.</br></br>

#### Input:</br>

The program reads a graph in the following format: Each vertex has a name. Each line corresponds to an edge and consists of the name of the first vertex, an empty space and the name of the second vertex. The next edge must be defined in a new line. Comments can be made by the # symbol. An example for the $K_{3,3}$: </br></br>
`# 6 9 this is a comment. By convention, the first line is a comment with the size of the graph (# n m).`
</br>`1 4`
</br>`1 5`
</br>`1 6`
</br>`2 4`
</br>`2 5`
</br>`2 6`
</br>`3 4`
</br>`3 5`
</br>`3 6`</br></br>

#### Output:
Lines starting with the # symbol are considered a comment and give useful information, as the upper bound and lower bound size, the number of recursive branching steps, the time required to read the input and the total running time.</br>
The minimum vertex cover is given by the name of one vertex which is part of the solution in each line. The output for the above graph could be:</br>
</br>`1`</br>`2`</br>`3`

#### Program arguments:

* no arguments: input by scanner, no time limit. When all edges have been parsed, type `END_OF_INPUT` to start the computation.
* 1 argument: input by filename, no time limit. The program will apply reduction rules, then search for an upper bound for 15 seconds, then start the branching.
* 2 arguments `args[1]`,`args[2]`: input by filename `args[1]`, trying to exactly solve for `args[2]` seconds, then the upper bound is output.
</br>

###### Repository for Algorithm Engineering 2022/2023 at TU Berlin. Fanny Hauser, Ferdinand Ermel, Kimon Böhmer.

