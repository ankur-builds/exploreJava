/*
 * Copyright (c) 2021.
 * File : DirectedGraph.java
 * Author : Ankur
 * Last modified : 30/11/2020
 * Problem Statement at the end of the code
 *
 * All code is for practice purpose only and strictly non-commercial.
 * All rights reserved.
 * Please refer to apache license terms in the project.
 */

package practice.graph;

import dsa.Graph;
import java.util.HashSet;
import java.util.Set;

public class CycleInDirected extends Graph {

    public CycleInDirected(int v) {
        super(v);
    }

    @Override
    public void addEdge(int u, int v) {
        adjacencyList[u].add(v);
        adjacencyMatrix[u][v] = 1;
    }

    public boolean isCyclic() {
        boolean[] visited = new boolean[size()];

        // Iterate over each connected components.
        for (int i = 0; i < size(); ++i) {
            if (!visited[i]) {
                Set<Integer> pathNodes = new HashSet<>();
                if (cyclicUtil(i, visited, pathNodes)) return true;
            }
        }
        return false;
    }

    /*
    Whether its a directed graph or undirected graph, to check for cycles we need to check if there
    is a back edge. In undirected graph, we confirm by checking if next node to visit is not parent node.
    In directed graph we confirm by adding nodes that we have visited in a Set or List and check if next
    node to visit is an element of Set or List. If it is present then we have reached starting point of a loop.
    Hence cycle detected.
     */
    boolean cyclicUtil(int v, boolean[] visited, Set<Integer> pathNodes) {
        if (pathNodes.contains(v)) return true;

        if (visited[v]) return false;

        pathNodes.add(v);
        visited[v] = true;
        for (int n : adjacencyList[v]) {
            if (cyclicUtil(n, visited, pathNodes)) return true;
        }

        pathNodes.remove(v);
        return false;
    }

    public static void main(String[] args) {
        CycleInDirected input = new CycleInDirected(5);
        // 0 ---> 3 ---> 4 ----> 2
        //   ---> 1 -----------> 2
        input.addEdge(0, 1);
        input.addEdge(0, 3);
        input.addEdge(1, 2);
        input.addEdge(3, 4);
        input.addEdge(4, 2);

        System.out.println(input.isCyclic() ? "Cyclic" : "ACyclic");
        input.addEdge(2, 0); // Lets turn acyclic to cyclic
        System.out.println(input.isCyclic() ? "Cyclic" : "ACyclic");

        CycleInDirected input2 = new CycleInDirected(6);
        // 0 ----> 1----->2----->3----->5
        //          ----->4------|

        input2.addEdge(0, 1);
        input2.addEdge(1, 2);
        input2.addEdge(2, 3);
        input2.addEdge(1, 4);
        input2.addEdge(4, 3);
        input2.addEdge(3, 5);

        System.out.println(input2.isCyclic() ? "Cyclic" : "ACyclic");
    }
}
