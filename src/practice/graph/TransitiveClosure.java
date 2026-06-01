/*
 * Copyright (c) 2020.
 * File : TransitiveClosure.java
 * Author : Ankur
 * Last modified : 28/11/2020
 * Problem Statement at the end of the code
 *
 * All code is for practice purpose only and strictly non-commercial.
 * All rights reserved.
 * Please refer to apache license terms in the project.
 */

package practice.graph;

/*
Given a directed graph, find out if a vertex j is reachable from another vertex i
for all vertex pairs (i, j) in the given graph. Here reachable mean that there is
a path from vertex i to j. The reach-ability matrix is called the transitive
closure of a graph.
 */
public class TransitiveClosure {

    CycleInDirected G;
    int[][] transitiveMatrix;

    TransitiveClosure(int v) {
        G = new CycleInDirected(v);
        transitiveMatrix = new int[v][v];
    }

    /*
    Floyd Warshall Algorithm is for solving All Pairs Shortest Path problem
    For every pair (i, j) of the source and destination vertices respectively, there are
    two possible cases.
    1) k is not an intermediate vertex in shortest path from i to j. We keep the value of
       dist[i][j] as it is.
    2) k is an intermediate vertex in shortest path from i to j. We update the value of
    dist[i][j] as dist[i][k] + dist[k][j] if dist[i][j] > dist[i][k] + dist[k][j]
    Time complexity : O(V^3)
     */
    void floydWarshallAlgorithm() {
        int V = G.size();
        int[][] distance = new int[V][V];
        for (int i = 0; i < V; ++i) {
            for (int j = 0; j < V; ++j) {
                if (i == j) distance[i][j] = 0;
                else if (G.adjacencyMatrix[i][j] != 0) distance[i][j] =
                    G.adjacencyMatrix[i][j];
                else distance[i][j] = 999999;
            }
        }

        // Iterate over all the intermediate vertices
        for (int k = 0; k < V; ++k) {
            // Iterate over all the source vertices
            for (int i = 0; i < V; ++i) {
                // Iterate over all the destination vertices
                for (int j = 0; j < V; ++j) {
                    if (distance[i][k] + distance[k][j] < distance[i][j]) {
                        distance[i][j] = distance[i][k] + distance[k][j];
                    }
                }
            }
        }

        printMatrix(distance);
    }

    // Using DFS. Time Complexity : O(V^2)
    void dfs() {
        // Iterate over all the vertices
        for (int i = 0; i < G.size(); ++i) {
            dfsUtil(i, i);
        }

        printMatrix(transitiveMatrix);
    }

    // Using DFS online
    void dfsUtil(int i, int j) {
        // Set all the vertices that you can visit from ith vertex
        transitiveMatrix[i][j] = 1;
        for (int s : G.adjacencyList[j]) {
            if (transitiveMatrix[i][s] == 0) {
                dfsUtil(i, s);
            }
        }
    }

    void printMatrix(int[][] matrix) {
        for (int[] row : matrix) {
            for (int column : row) {
                if (column == 999999) System.out.print("INF ");
                else System.out.print(column + " ");
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        TransitiveClosure input = new TransitiveClosure(5);
        // 0 ---> 3 ---> 4 ----> 2
        //   ---> 1 -----------> 2
        input.G.addEdge(0, 1);
        input.G.addEdge(0, 3);
        input.G.addEdge(1, 2);
        input.G.addEdge(3, 4);
        input.G.addEdge(4, 2);

        input.printMatrix(input.G.adjacencyMatrix);
        System.out.println();
        input.floydWarshallAlgorithm();
        System.out.println();
        input.dfs();
    }
}
