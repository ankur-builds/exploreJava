/*
 * Copyright (c) 2020.
 * File : Graph.java
 * Author : Ankur
 * Last modified : 17/11/2020
 * Problem Statement at the end of the code
 *
 * All code is for practice purpose only and strictly non-commercial.
 * All rights reserved.
 * Please refer to apache license terms in the project.
 */

package dsa;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;

public class Graph {

    public int[][] adjacencyMatrix;
    public List<Integer>[] adjacencyList;

    public Graph(int v) {
        adjacencyList = new List[v];
        adjacencyMatrix = new int[v][v];
    }

    public Graph(int v, int[][] matrix) {
        adjacencyList = new List[v];
        adjacencyMatrix = matrix;
    }

    public void addEdge(int u, int v) {
        if (adjacencyList[u] == null) adjacencyList[u] = new ArrayList<>();

        if (adjacencyList[v] == null) adjacencyList[v] = new ArrayList<>();

        adjacencyList[u].add(v);
        adjacencyList[v].add(u);
    }

    public void removeEdge(Integer u, Integer v) {
        if (adjacencyList[u] != null) adjacencyList[u].remove(v);
        if (adjacencyList[v] != null) adjacencyList[v].remove(u);
    }

    // Perform Breadth First Search. Time complexity : O(V+E)
    public void doBFS(int n) {
        boolean[] visited = new boolean[size()];
        Queue<Integer> next = new LinkedList<>();

        next.add(n);
        visited[n] = true;

        while (!next.isEmpty()) {
            n = next.poll();
            System.out.print(n + " ");
            for (int i : adjacencyList[n]) {
                if (!visited[i]) {
                    next.add(i);
                    visited[i] = true;
                }
            }
        }
    }

    // Perform Depth First Search. Time complexity : O(V+E)
    public void doDFS(int n) {
        boolean[] visited = new boolean[size()];
        Stack<Integer> stk = new Stack<>();
        stk.push(n);
        while (!stk.isEmpty()) {
            n = stk.pop();
            if (!visited[n]) {
                visited[n] = true;
                System.out.print(n + " ");
            }
            ReverseIterator<Integer> it = new ReverseIterator<>(
                adjacencyList[n]
            );
            while (it.hasNext()) {
                int i = it.next();
                if (!visited[i]) {
                    stk.push(i);
                }
            }

            /*     Alternate Iterative Solution ...
            If you don't reverse adjacency list of corresponding node.
            Then also it will do DFS but from last node. Output will be different
            from what we get from recursive solution.

            Collections.reverse(adjacencyList.get(n));
            for (int i : adjacencyList.get(n)) {
                if (!visited[i]) {
                    stk.push(i);
                }
            }
            */
        }
    }

    public void DFS(int v, boolean[] visited) {
        visited[v] = true;
        System.out.print(v + " ");
        for (int n : adjacencyList[v]) {
            if (!visited[n]) DFS(n, visited);
        }
    }

    public void recursiveDFS(int v) {
        boolean[] visited = new boolean[size()];
        DFS(v, visited);
    }

    public int size() {
        return adjacencyList.length;
    }

    int motherVertex(int n) {
        Stack<Integer> stk = new Stack<>();
        boolean[] visited = new boolean[size()];

        stk.push(n);
        int count = 0;
        while (!stk.isEmpty()) {
            int k = stk.pop();
            if (!visited[k]) {
                visited[k] = true;
                ++count;
            }

            ReverseIterator<Integer> it = new ReverseIterator<>(
                adjacencyList[k]
            );
            while (it.hasNext()) {
                int i = it.next();
                if (!visited[i]) {
                    stk.push(i);
                }
            }
        }

        return count == size() ? n : -1;
    }

    public int kosaraju() {
        boolean[] visited = new boolean[size()];
        int lastFinished = -1;
        for (int i = 0; i < size(); ++i) {
            if (!visited[i]) {
                DFS(i, visited);
                lastFinished = i;
            }
        }

        Arrays.fill(visited, false);
        DFS(lastFinished, visited);
        for (boolean check : visited) {
            if (!check) return -1;
        }

        return lastFinished;
    }

    /*
    The outer loop is executed O(|V|) regardless of the graph structure.
        - Even if we had no edges at all, for every iteration of the outer loop, we would have
        to do a constant number of operations (O(1))
        - The inner loop is executed once for every edge, thus O(deg(v)) times, where deg(v)
        is the degree of the current node.
        - Thus the runtime of a single iteration of the outer loop is O(1 + deg(v)). Note that
        we cannot leave out the 1, because deg(v) might be 0 but we still need to do some work
        in that iteration

    Summing it all up, we get a runtime of O(|V| * 1 + deg(v1) + deg(v2) + ...) = O(|V| + |E|).
    Time complexity : O(V+E)
    */
    public void printGraph() {
        Set<String> edgeList = new HashSet<>();
        for (int u = 0; u < size(); ++u) {
            for (int v : adjacencyList[u]) {
                if (
                    !(edgeList.contains(u + "->" + v) ||
                        edgeList.contains(v + "->" + u))
                ) edgeList.add(u + "->" + v);
            }
        }

        System.out.println(edgeList);
    }

    public static void main(String[] args) {
        Graph input = new Graph(8);

        input.addEdge(0, 1);
        input.addEdge(0, 2);
        input.addEdge(0, 3);
        input.addEdge(1, 2);
        input.addEdge(1, 3);
        input.addEdge(1, 4);
        input.addEdge(2, 5);
        input.addEdge(0, 5);
        input.addEdge(6, 7);

        input.printGraph();

        System.out.print("\n BFS : ");
        input.doBFS(0);

        System.out.print("\n DFS : ");
        input.doDFS(0);

        System.out.print("\n DFS Recursive : ");
        input.recursiveDFS(0);

        // Find Mother Vertex. Time complexity : O(V(V+E))
        for (int i = 0; i < input.size(); ++i) {
            if (input.motherVertex(i) == i) {
                System.out.println("\n\n Mother vertex : " + i);
                break;
            }
        }

        // Suppress System.out.print in next function. Very interesting to learn
        PrintStream originalStream = System.out;
        PrintStream dummyStream = new PrintStream(
            new OutputStream() {
                public void write(int b) {
                    // NO-OP
                }
            }
        );
        System.setOut(dummyStream);

        // Kosaraju's Strongly Connected Component Algorithm. Time complexity : O(V+E)
        int output = input.kosaraju();

        // Enable System.out.print in next function.
        System.setOut(originalStream);
        System.out.println(" Mother Vertex via Kosaraju : " + output);
    }
}
