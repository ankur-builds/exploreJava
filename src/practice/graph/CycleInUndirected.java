/*
 * Copyright (c) 2020.
 * File : DetectCycle.java
 * Author : Ankur
 * Last modified : 26/11/2020
 * Problem Statement at the end of the code
 *
 * All code is for practice purpose only and strictly non-commercial.
 * All rights reserved.
 * Please refer to apache license terms in the project.
 */

package practice.graph;

import dsa.Graph;

// Detect Graph in an undirected Graph
public class CycleInUndirected extends Graph {

    CycleInUndirected(int v) {
        super(v);
    }

    /*
    There is a cycle in a graph only if there is a back edge present in the graph.
    A back edge is an edge that is joining a node to itself (self-loop) or one of
    its ancestor in the tree produced by DFS.
    To find the back edge to any of its ancestor keep a visited array and if there
    is a back edge to any visited node then there is a loop and return true.

    *** Approach is simple : Just do a DFS and pass parent as one of parameter. Compare parent with
    next visited element, if they are not same then there is a back edge ***
     */
    boolean isGraphCyclic() {
        boolean[] visited = new boolean[size()];
        for (int i = 0; i < size(); ++i) {
            if (!visited[i]) if (checkCycle(i, -1, visited)) return true;
        }

        return false;
    }

    boolean checkCycle(int i, int parent, boolean[] visited) {
        if (visited[i]) return true;

        visited[i] = true;
        for (int nxt : adjacencyList[i]) {
            if (nxt != parent) if (checkCycle(nxt, i, visited)) return true;
        }

        return false;
    }

    public static void main(String[] args) {
        CycleInUndirected input = new CycleInUndirected(6);
        input.addEdge(0, 1);
        input.addEdge(0, 2);
        input.addEdge(0, 3);
        input.addEdge(1, 4);
        input.addEdge(2, 5);

        System.out.println(input.isGraphCyclic() ? "Cyclic" : "Not cyclic");
        input.addEdge(0, 5);
        System.out.println(input.isGraphCyclic() ? "Cyclic" : "Not cyclic");
    }
}
