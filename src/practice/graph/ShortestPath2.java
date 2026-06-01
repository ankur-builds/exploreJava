/*
 * Copyright (c) 2021.
 * File : ShortestPathII.java
 * Author : Ankur
 * Last modified : 8/4/2021
 * Problem Statement at the end of the code
 *
 * All code is for practice purpose only and strictly non-commercial.
 * All rights reserved.
 * Please refer to apache license terms in the project.
 */

package practice.graph;

import dsa.Graph;
import java.util.*;

public class ShortestPath2 extends Graph {

    ShortestPath2(int v) {
        super(v);
    }

    @Override
    public void addEdge(int u, int v) {
        adjacencyList[u].add(v);
    }

    public void addWeight(int u, int v, int wt) {
        adjacencyMatrix[u][v] = wt;
    }

    public int getWeight(int u, int v) {
        return adjacencyMatrix[u][v];
    }

    public void theBellmanFordAlgorithm(int source, int destination) {
        int[] lastVertex = new int[size()];
        Map<Integer, Integer> distanceInfo = new HashMap<>();
        Queue<Integer> q = new LinkedList<>();

        // Step 1 : Initialize distance table
        distanceInfo.put(source, 0);
        lastVertex[source] = source;

        for (int i = 0; i < size(); ++i) {
            if (i != source) {
                distanceInfo.put(i, 1000000);
                lastVertex[i] = -1;
            }
        }

        // Step 2 : Iterate over all the edges over v-1 iterations to get accurate distance
        for (int i = 0; i < size() - 1; ++i) {
            q.add(source);

            Set<String> edge = new HashSet<>();
            while (!q.isEmpty()) {
                int currentVertex = q.poll();
                for (int neighbour : adjacencyList[currentVertex]) {
                    String str = currentVertex + " " + neighbour;
                    if (edge.contains(str)) continue;
                    edge.add(str);

                    int distance =
                        distanceInfo.get(currentVertex) +
                        getWeight(currentVertex, neighbour);
                    if (distance < distanceInfo.get(neighbour)) {
                        distanceInfo.put(neighbour, distance);
                        lastVertex[neighbour] = currentVertex;
                        q.add(neighbour);
                    }
                }
            }
        }

        // Step 3 : Check for negative cycle
        for (int i = 0; i < size(); ++i) {
            q.add(i);
        }

        while (!q.isEmpty()) {
            int currentVertex = q.poll();
            for (int neighbour : adjacencyList[currentVertex]) {
                int distance =
                    distanceInfo.get(currentVertex) +
                    getWeight(currentVertex, neighbour);
                if (distanceInfo.get(neighbour) > distance) {
                    System.out.println("Graph has -ve cycles");
                    return;
                }
            }
        }

        // Step 4 : Populate shortest path based on lastVertex in a stack
        Stack<Integer> stk = new Stack<>();
        stk.push(destination);
        while (
            lastVertex[destination] != -1 && lastVertex[destination] != source
        ) {
            stk.push(lastVertex[destination]);
            destination = lastVertex[destination];
        }

        if (lastVertex[destination] == -1) System.out.println(
            "No Path found between source & destination"
        );
        else {
            System.out.print(
                "Shortest path using Dijkstra's algorithm :: " + source
            );
            while (!stk.isEmpty()) System.out.print(" -> " + stk.pop());
        }
    }

    public static void main(String[] args) {
        ShortestPath2 input = new ShortestPath2(5);
        input.addEdge(0, 1);
        input.addWeight(0, 1, 2);
        input.addEdge(0, 2);
        input.addWeight(0, 2, 3);
        input.addEdge(2, 4);
        input.addWeight(2, 4, 6);
        input.addEdge(4, 1);
        input.addWeight(4, 1, -5);
        input.addEdge(1, 3);
        input.addWeight(1, 3, 2);
        input.addEdge(4, 3);
        input.addWeight(4, 3, -6);

        input.theBellmanFordAlgorithm(0, 3);

        System.out.println();
        input.theBellmanFordAlgorithm(0, 3);
    }
}
