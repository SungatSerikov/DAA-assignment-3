package tests;

import connectivity.Kosaraju;
import connectivity.TopologicalSort;
import connectivity.TopologicalSortResult;
import experiments.GraphGenerator;
import graph.Edge;
import graph.Graph;
import mst.Kruskal;
import mst.MSTResult;
import mst.Prim;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class GeneratorTests {
    private static int passed;

    public static void main(String[] args) {
        passed = 0;
        testUndirectedGraphs();
        testDirectedGraphs();
        testAssignmentSizes();
        testFixedSeeds();
        testInvalidParameters();
        System.out.println("Generator tests: " + passed + " cases passed.");
    }

    private static void testUndirectedGraphs() {
        checkUndirected(GraphGenerator.connectedUndirected(1, 0, 42L), 1, 0);
        checkUndirected(GraphGenerator.connectedUndirected(2, 1, 42L), 2, 1);
        checkUndirected(GraphGenerator.connectedUndirected(30, 29, 42L), 30, 29);
        checkUndirected(GraphGenerator.connectedUndirected(8, 28, 42L), 8, 28);
    }

    private static void testDirectedGraphs() {
        checkDag(GraphGenerator.directedAcyclic(1, 0, 42L), 1, 0);
        checkDag(GraphGenerator.directedAcyclic(8, 0, 42L), 8, 0);
        checkDag(GraphGenerator.directedAcyclic(2, 1, 42L), 2, 1);
        checkDag(GraphGenerator.directedAcyclic(8, 28, 42L), 8, 28);
        checkCyclic(GraphGenerator.directedWithCycle(3, 3, 42L), 3, 3);
        checkCyclic(GraphGenerator.directedWithCycle(8, 3, 42L), 8, 3);
        checkCyclic(GraphGenerator.directedWithCycle(8, 56, 42L), 8, 56);
        checkGraph(GraphGenerator.directedAcyclic(50000, 0, 42L), 50000, 0, true);
        checkGraph(GraphGenerator.directedWithCycle(50000, 3, 42L), 50000, 3, true);
    }

    private static void testAssignmentSizes() {
        int[][] sizes = {{10, 30}, {100, 500}, {1000, 5000}};
        for (int[] size : sizes) {
            int vertices = size[0];
            int edges = size[1];
            checkUndirected(GraphGenerator.connectedUndirected(vertices, edges, 42L), vertices, edges);
            checkDag(GraphGenerator.directedAcyclic(vertices, edges, 42L), vertices, edges);
            checkCyclic(GraphGenerator.directedWithCycle(vertices, edges, 42L), vertices, edges);
        }
    }

    private static void checkGraph(Graph graph, int vertices, int edges, boolean directed) {
        check(graph.getVertexCount() == vertices, "Generator: vertex count");
        check(graph.getEdges().size() == edges, "Generator: edge count");
        check(graph.isDirected() == directed, "Generator: graph direction");
        Set<String> seen = new HashSet<>();
        for (Edge edge : graph.getEdges()) {
            check(edge.u >= 0 && edge.u < vertices && edge.v >= 0 && edge.v < vertices,
                    "Generator: valid endpoints");
            check(edge.u != edge.v, "Generator: no self-loop");
            check(edge.weight >= 1 && edge.weight <= 100, "Generator: weight in 1..100");
            int first = edge.u;
            int second = edge.v;
            if (!directed) {
                first = Math.min(edge.u, edge.v);
                second = Math.max(edge.u, edge.v);
            }
            check(seen.add(first + ":" + second), "Generator: no duplicate edge");
        }
        passed++;
    }

    private static void checkUndirected(Graph graph, int vertices, int edges) {
        checkGraph(graph, vertices, edges, false);
        boolean[] visited = new boolean[vertices];
        Queue<Integer> queue = new ArrayDeque<>();
        queue.add(0);
        visited[0] = true;
        int reached = 0;
        while (!queue.isEmpty()) {
            int vertex = queue.remove();
            reached++;
            for (Edge edge : graph.getNeighbors(vertex)) {
                if (!visited[edge.v]) {
                    visited[edge.v] = true;
                    queue.add(edge.v);
                }
            }
        }
        check(reached == vertices, "Generator: undirected graph is connected");
        MSTResult kruskal = Kruskal.findMST(graph);
        MSTResult prim = Prim.findMST(graph);
        check(kruskal.isConnected(), "Generator: Kruskal connects all vertices");
        check(prim.isConnected(), "Generator: Prim connects all vertices");
        check(kruskal.getTotalWeight() == prim.getTotalWeight(),
                "Generator: Kruskal and Prim agree on the MST weight");
    }

    private static void checkDag(Graph graph, int vertices, int edges) {
        checkGraph(graph, vertices, edges, true);
        for (Edge edge : graph.getEdges()) {
            check(edge.u < edge.v, "Generator: DAG edges point to larger vertices");
        }
        TopologicalSortResult result = TopologicalSort.sort(graph);
        check(!result.hasCycle(), "Generator: DAG has no cycle");
        check(result.getOrder().size() == vertices, "Generator: complete topological order");
        int[] position = new int[vertices];
        boolean[] seen = new boolean[vertices];
        for (int i = 0; i < result.getOrder().size(); i++) {
            int vertex = result.getOrder().get(i);
            check(vertex >= 0 && vertex < vertices, "Generator: valid order vertex");
            check(!seen[vertex], "Generator: no repeated order vertex");
            seen[vertex] = true;
            position[vertex] = i;
        }
        for (Edge edge : graph.getEdges()) {
            check(position[edge.u] < position[edge.v], "Generator: order respects every edge");
        }
        check(Kosaraju.findComponents(graph).size() == vertices, "Generator: DAG has singleton SCCs");
    }

    private static void checkCyclic(Graph graph, int vertices, int edges) {
        checkGraph(graph, vertices, edges, true);
        Set<String> arcs = new HashSet<>();
        for (Edge edge : graph.getEdges()) {
            arcs.add(edge.u + ":" + edge.v);
        }
        check(arcs.contains("0:1") && arcs.contains("1:2") && arcs.contains("2:0"),
                "Generator: required three-edge cycle exists");
        TopologicalSortResult result = TopologicalSort.sort(graph);
        check(result.hasCycle(), "Generator: cycle is detected");
        check(result.getOrder().isEmpty(), "Generator: cycle has no topological order");
        boolean sameComponent = false;
        for (List<Integer> component : Kosaraju.findComponents(graph)) {
            if (component.contains(0) && component.contains(1) && component.contains(2)) {
                sameComponent = true;
            }
        }
        check(sameComponent, "Generator: cycle vertices are in one SCC");
    }

    private static void testFixedSeeds() {
        checkSeeds(GraphGenerator.connectedUndirected(10, 30, 42L),
                GraphGenerator.connectedUndirected(10, 30, 42L),
                GraphGenerator.connectedUndirected(10, 30, 123L));
        checkSeeds(GraphGenerator.directedAcyclic(10, 30, 42L),
                GraphGenerator.directedAcyclic(10, 30, 42L),
                GraphGenerator.directedAcyclic(10, 30, 123L));
        checkSeeds(GraphGenerator.directedWithCycle(10, 30, 42L),
                GraphGenerator.directedWithCycle(10, 30, 42L),
                GraphGenerator.directedWithCycle(10, 30, 123L));
    }

    private static void checkSeeds(Graph first, Graph repeated, Graph different) {
        check(sameEdges(first, repeated), "Generator: same seed reproduces edges and weights");
        check(!sameEdges(first, different), "Generator: different seeds change the graph");
        passed++;
    }

    private static boolean sameEdges(Graph first, Graph second) {
        if (first.getEdges().size() != second.getEdges().size()) {
            return false;
        }
        for (int i = 0; i < first.getEdges().size(); i++) {
            Edge a = first.getEdges().get(i);
            Edge b = second.getEdges().get(i);
            if (a.u != b.u || a.v != b.v || a.weight != b.weight) {
                return false;
            }
        }
        return true;
    }

    private static void testInvalidParameters() {
        int[][] undirected = {{0, 0}, {-1, 0}, {1, -1}, {1, 1}, {2, 0}, {4, 2}, {4, 7}};
        for (int[] input : undirected) {
            boolean rejected = false;
            try {
                GraphGenerator.connectedUndirected(input[0], input[1], 42L);
            } catch (IllegalArgumentException error) {
                rejected = true;
            }
            check(rejected, "Generator: reject invalid undirected parameters");
            passed++;
        }
        int[][] dag = {{0, 0}, {-1, 0}, {1, -1}, {1, 1}, {4, -1}, {4, 7}};
        for (int[] input : dag) {
            boolean rejected = false;
            try {
                GraphGenerator.directedAcyclic(input[0], input[1], 42L);
            } catch (IllegalArgumentException error) {
                rejected = true;
            }
            check(rejected, "Generator: reject invalid DAG parameters");
            passed++;
        }
        int[][] cyclic = {{-1, 3}, {0, 3}, {1, 3}, {2, 3}, {3, -1}, {3, 0}, {3, 2}, {3, 7}};
        for (int[] input : cyclic) {
            boolean rejected = false;
            try {
                GraphGenerator.directedWithCycle(input[0], input[1], 42L);
            } catch (IllegalArgumentException error) {
                rejected = true;
            }
            check(rejected, "Generator: reject invalid cyclic parameters");
            passed++;
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
