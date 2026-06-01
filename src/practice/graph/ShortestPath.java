/*
 * Copyright (c) 2021.
 * File : ShortestPath.java
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
import java.util.*;

public class ShortestPath extends Graph {

    public ShortestPath(int v) {
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

    // Dijkstra's Algorithm for Shortest path in a weighted graph
    public void dijkstraAlgorithm(int source, int destination) {
        int[] lastVertex = new int[size()];
        Map<Integer, Integer> distanceInfo = new HashMap<>();

        // Define a comparator for priority queue which will be used to pick next node
        Queue<Integer> q = new PriorityQueue<>(
            new Comparator<Integer>() {
                @Override
                public int compare(Integer i, Integer j) {
                    return distanceInfo.get(i).compareTo(distanceInfo.get(j));
                }
            }
        );

        // Step 1 : Initialization of Distance Table
        distanceInfo.put(source, 0);
        lastVertex[source] = source;
        for (int v = 0; v < size(); ++v) {
            if (v != source) {
                distanceInfo.put(v, Integer.MAX_VALUE);
                lastVertex[v] = -1;
            }
        }

        // Step 2 : Iterate over all the adjacent nodes to source & update distance table
        q.add(source);
        while (!q.isEmpty()) {
            int current = q.poll();
            for (int neighbour : adjacencyList[current]) {
                int distance =
                    distanceInfo.get(current) + getWeight(current, neighbour);
                if (distance < distanceInfo.get(neighbour)) {
                    distanceInfo.put(neighbour, distance);
                    lastVertex[neighbour] = current;
                    q.add(neighbour);
                }
            }
        }

        // Step 3 : Populate shortest path based on lastVertex in a stack
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
        ShortestPath input = new ShortestPath(5);
        input.addEdge(0, 1);
        input.addWeight(0, 1, 2);
        input.addEdge(0, 2);
        input.addWeight(0, 2, 3);
        input.addEdge(2, 4);
        input.addWeight(2, 4, 6);
        input.addEdge(4, 1);
        input.addWeight(4, 1, 5);
        input.addEdge(1, 3);
        input.addWeight(1, 3, 2);
        input.addEdge(4, 3);
        input.addWeight(4, 3, 4);

        input.dijkstraAlgorithm(0, 3);

        System.out.println();
        input.dijkstraAlgorithm(0, 4);
    }
}
