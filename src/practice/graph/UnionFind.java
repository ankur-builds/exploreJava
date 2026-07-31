package practice.graph;

import java.util.*;

public class UnionFind {

    int[] parent;
    int[] rank;

    UnionFind(int n) {
        parent = new int[n];
        rank = new int[n];

        for (int i = 0; i < n; i++) {
            parent[i] = i;
        }
    }

    int find(int x) {
        while (parent[x] != x) {
            parent[x] = parent[parent[x]];
            x = parent[x];
        }
        return x; // At this point, x == parent[x]
    }

    boolean union(int a, int b) {
        int pa = find(a);
        int pb = find(b);

        if (pa == pb) return false;

        if (rank[pa] > rank[pb]) {
            parent[pb] = pa;
            rank[pa] += rank[pb];
        } else {
            parent[pb] = pa;
            rank[pb] += rank[pa];
        }

        return true;
    }
}
