package interview.forwardNetworks;

/**
 * Problem Statement - Network Router Connectivity Check
 * You are given a list of unidirectional network cable connections. Each connection is represented as an array of two strings
 * [source_router, target_router], indicating that data can flow from source_router to target_router.Write a function
 * canReach(connections, source, target) that returns true if a packet can travel from the source router to the target router,
 *  and false otherwise.

 * Example 1:
 * Input:
 * connections = [["A", "B"], ["B", "C"], ["C", "D"]]
 * source = "A"
 * target = "D"

 * Output: true
 * Explanation: A -> B -> C -> D exists.

 * Example 2:
 * Input:
 * connections = [["A", "B"], ["C", "D"]]
 * source = "A"
 * target = "D"

 * Output: false
 * Explanation: "A" can only reach "B". There is no path to "D".
 */
import java.util.*;

public class NetworkRouterConnectivity {

    public static boolean checkPath(int[][] grid, int source, int target) {
        // Step 1: Build the graph (Adjacency List) for O(1) lookups
        Map<Integer, List<Integer>> graph = new HashMap<>();
        for (int[] connection : grid) {
            graph.putIfAbsent(connection[0], new ArrayList<>());
            graph.get(connection[0]).add(connection[1]);
        }

        // Step 2: Use a visited set to handle network cycles safely
        Set<Integer> visited = new HashSet<>();

        return dfs(graph, source, target, visited);
    }

    private static boolean dfs(
        Map<Integer, List<Integer>> graph,
        int current,
        int target,
        Set<Integer> visited
    ) {
        if (current == target) return true;
        if (visited.contains(current)) return false; // Cycle detected, turn back

        visited.add(current); // Mark current router as visited

        // Check all neighbors
        if (graph.containsKey(current)) {
            for (int neighbor : graph.get(current)) {
                if (dfs(graph, neighbor, target, visited)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static void main(String[] args) {
        int[][] grid = new int[][] { { 0, 1 }, { 1, 2 }, { 2, 3 } };
        System.out.println(checkPath(grid, 0, 3)); // Output: true
    }
}
