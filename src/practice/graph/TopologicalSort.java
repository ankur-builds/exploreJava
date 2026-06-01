/*
 * Copyright (c) 2021.
 * File : TopologicalSort.java
 * Author : Ankur
 * Last modified : 7/4/2021
 * Problem Statement at the end of the code
 *
 * All code is for practice purpose only and strictly non-commercial.
 * All rights reserved.
 * Please refer to apache license terms in the project.
 */

package practice.graph;

import dsa.Graph;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

/*
    It is an ordering of vertices in Directed Acyclic Graph(DAG) in which each node comes before all
    the nodes to which it has outgoing edges.
    There can be more than one topological sorting for a graph.
*/
public class TopologicalSort extends Graph {

    TopologicalSort(int v) {
        super(v);
    }

    @Override
    public void addEdge(int u, int v) {
        adjacencyList[u].add(v);
    }

    public int getInDegree(int vertex) {
        int inDegree = 0;
        for (int i = 0; i < size(); ++i) {
            if (adjacencyList[i].contains(vertex)) inDegree++;
        }
        return inDegree;
    }

    /*
        Kahn's algorithm of Topological Sorting
            1. Compute in-degree for each of the vertex present in DAG and initialize the count of visited
            nodes as 0.
            2. Pick all the vertices with in-degree as 0 and add them into queue.
            3. Remove vertex from queue and add it in sorted list. Increment count of visited nodes by 1
            4. Decrease in-degree for all its neighbouring nodes by 1. If in-degree of neighbouring nodes
            is reduced to 1 then add it to queue.
            5. Repeat step 3 - 4 until queue is empty.
            6. If count of visited nodes is not equal to nodes in graph then topological sort is not
            possible as graph has cycles else return list of visited nodes.
     */
    public void topologicalSort() {
        Queue<Integer> q = new LinkedList<>();
        int count = 0;
        int[] indegree = new int[size()];
        int[] sorted = new int[size()];

        for (int i = 0; i < size(); ++i) {
            indegree[i] = getInDegree(i);
            if (indegree[i] == 0) q.add(i);
        }

        while (!q.isEmpty()) {
            int v = q.poll();
            sorted[count++] = v;
            for (int i : adjacencyList[v]) {
                indegree[i]--;
                if (indegree[i] == 0) q.add(i);
            }
        }

        if (count == size()) System.out.println(
            "Topologically sorted list : " + Arrays.toString(sorted)
        );
        else System.out.println(
            "Topological Sorting not possible as Graph as cycles."
        );
    }

    public static void main(String[] args) {
        TopologicalSort input = new TopologicalSort(8);
        input.addEdge(2, 0);
        input.addEdge(0, 3);
        input.addEdge(1, 2);
        input.addEdge(1, 3);
        input.addEdge(1, 4);
        input.addEdge(3, 5);
        input.addEdge(4, 6);
        input.addEdge(2, 5);
        input.addEdge(0, 5);
        input.addEdge(6, 7);

        input.printGraph();
        System.out.println();

        for (int i = 0; i < input.size(); ++i) {
            System.out.println(
                "Vertex : " +
                    i +
                    ", indegree -> " +
                    input.getInDegree(i) +
                    " :: outdegree -> " +
                    input.adjacencyList[i].size()
            );
        }
        input.topologicalSort();

        // Lets check if graph has cycles
        System.out.println();
        input.addEdge(7, 5);
        input.addEdge(5, 1);
        for (int i = 0; i < input.size(); ++i) {
            System.out.println(
                "Vertex : " +
                    i +
                    ", indegree -> " +
                    input.getInDegree(i) +
                    " :: outdegree -> " +
                    input.adjacencyList[i].size()
            );
        }
        input.topologicalSort();
    }
}
