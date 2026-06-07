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
        if (parent[x] != x) {
            parent[x] = find(parent[x]);
        }
        return parent[x];
    }

    boolean union(int a, int b) {
        int pa = find(a);
        int pb = find(b);

        if (pa == pb) return false;

        if (rank[pa] > rank[pb]) {
            parent[pb] = pa;
        } else if (rank[pb] > rank[pa]) {
            parent[pa] = pb;
        } else {
            parent[pb] = pa;
            rank[pa]++;
        }

        return true;
    }
}
