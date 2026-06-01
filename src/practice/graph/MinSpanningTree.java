/*
 * Copyright (c) 2021.
 * File : MinSpanningTree.java
 * Author : Ankur
 * Last modified : 10/4/2021
 * Problem Statement at the end of the code
 *
 * All code is for practice purpose only and strictly non-commercial.
 * All rights reserved.
 * Please refer to apache license terms in the project.
 */

package practice.graph;

import dsa.Graph;
import java.util.*;

public class MinSpanningTree extends Graph {

    public MinSpanningTree(int v) {
        super(v);
    }

    public void addWeight(int i, int j, int wt) {
        addEdge(i, j);
        adjacencyMatrix[i][j] = wt;
        adjacencyMatrix[j][i] = wt;
    }

    public int getWeight(int i, int j) {
        return adjacencyMatrix[i][j];
    }

    public void thePrimAlgorithm() {
        int[] lastVertex = new int[size()];
        Map<Integer, Integer> distanceInfo = new HashMap<>();

        // Setup build Table
        for (int i = 0; i < size() - 1; ++i) {
            lastVertex[i] = -1;
            distanceInfo.put(i, Integer.MAX_VALUE);
        }

        // We will be adding all the edges adjacent to vertices of MST in this queue & choose min edge
        Queue<Integer> q = new PriorityQueue<>(
            new Comparator<Integer>() {
                @Override
                public int compare(Integer i, Integer j) {
                    return distanceInfo.get(i).compareTo(distanceInfo.get(j));
                }
            }
        );

        // Step 1 : Choose a random source vertex
        lastVertex[size() - 1] = size() - 1;
        distanceInfo.put(size() - 1, 0);
        q.add(size() - 1);

        Set<String> edgeMap = new HashSet<>();
        boolean[] visited = new boolean[size()];

        // Step 2 : Populate the edge map of MST by populating distance table and choosing vertex with min distance
        while (!q.isEmpty()) {
            int current = q.poll();

            if (!visited[current]) {
                // Check if edge results in a cycle
                if (
                    current !=
                    size() - 1 // If current vertex is a source then we do not have edge yet
                ) edgeMap.add(lastVertex[current] + "->" + current); // + " : " + distanceInfo.get(current)

                visited[current] = true;
                for (int neighbour : adjacencyList[current]) {
                    int distance = getWeight(current, neighbour);
                    if (distance < distanceInfo.get(neighbour)) {
                        distanceInfo.put(neighbour, distance);
                        lastVertex[neighbour] = current;

                        // In a way we are just adding edge here in q.add(neighbour).
                        // If vertex is already visited, it will be discarded
                        q.add(neighbour);
                    }
                }
            }
        }

        // Step : Print MST
        System.out.println(edgeMap);
    }

    public void theKruskalAlgorithm() {
        // Step 1 : Setup a priority queue which returns edge with smallest weight
        Queue<EdgeInfo> q = new PriorityQueue<>(
            new Comparator<EdgeInfo>() {
                @Override
                public int compare(EdgeInfo e1, EdgeInfo e2) {
                    return e1.getWeight().compareTo(e2.getWeight());
                }
            }
        );

        // Add all edges to Priority Queue
        Set<String> edge = new HashSet<>();
        for (int i = 0; i < size(); ++i) {
            for (int neighbour : adjacencyList[i]) {
                if (
                    !(edge.contains(i + "," + neighbour) ||
                        edge.contains(neighbour + "," + i))
                ) {
                    q.add(new EdgeInfo(i, neighbour, getWeight(i, neighbour)));
                    edge.add(i + "," + neighbour);
                }
            }
        }

        CycleInUndirected G = new CycleInUndirected(size());
        int count = 0;

        // Step 2 : Populate edgeMap
        while (!q.isEmpty()) {
            EdgeInfo current = q.poll();
            Integer source = current.getSource();
            Integer destination = current.getDestination();

            if (count < size() - 1) {
                G.addEdge(source, destination);

                if (G.isGraphCyclic()) {
                    G.removeEdge(source, destination);
                } else {
                    count++;
                }
            } else break;
        }

        G.printGraph();
    }

    private class EdgeInfo {

        private Integer vertex1;
        private Integer vertex2;
        private Integer weight;

        public EdgeInfo(Integer vertex1, Integer vertex2, Integer weight) {
            this.vertex1 = vertex1;
            this.vertex2 = vertex2;
            this.weight = weight;
        }

        public Integer getSource() {
            return vertex1;
        }

        public Integer getDestination() {
            return vertex2;
        }

        public Integer getWeight() {
            return weight;
        }

        @Override
        public String toString() {
            return vertex1 + " -> " + vertex2;
        }
    }

    public static void main(String[] args) {
        MinSpanningTree input = new MinSpanningTree(6);
        input.addWeight(0, 4, 5);
        input.addWeight(0, 2, 15);
        input.addWeight(0, 1, 3);
        input.addWeight(2, 1, 2);
        input.addWeight(2, 5, 9);
        input.addWeight(1, 4, 5);
        input.addWeight(1, 5, 8);
        input.addWeight(4, 5, 4);
        input.addWeight(4, 3, 11);
        input.addWeight(5, 3, 4);

        input.thePrimAlgorithm();

        input.theKruskalAlgorithm();
    }
}
