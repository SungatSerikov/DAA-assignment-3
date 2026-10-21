package tests;

import connectivity.Kosaraju;
import connectivity.TopologicalSort;
import connectivity.TopologicalSortResult;
import dsu.DSU;
import graph.Edge;
import graph.Graph;
import metrics.OperationCounts;
import mst.Kruskal;
import mst.MSTResult;
import mst.Prim;
import shortestpath.Dijkstra;
import shortestpath.DijkstraResult;

import java.util.Arrays;
import java.util.List;

public class CorrectnessTests {
    private static final long INF = Long.MAX_VALUE;
    private static int passed;

    public static void main(String[] args) {
        passed = 0;
        testDSU();
        testPathCompression();
        testMST();
        testTopologicalSort();
        testSCC();
        testDijkstra();
        testNegativeWeightRejection();
        testOperationCounts();
        System.out.println("Correctness tests: " + passed + " cases passed.");
    }

    private static void testDSU() {
        DSU dsu = new DSU(5);
        check(dsu.getComponentCount() == 5, "DSU: initial count");
        check(dsu.find(0) != dsu.find(1), "DSU: initially separate elements");
        check(dsu.union(0, 1), "DSU: first union");
        check(dsu.union(2, 3), "DSU: second union");
        check(dsu.union(1, 2), "DSU: merge two groups");
        check(dsu.find(0) == dsu.find(3), "DSU: transitive connection");
        check(dsu.find(0) != dsu.find(4), "DSU: isolated element");
        check(!dsu.union(0, 3), "DSU: repeated union");
        check(!dsu.union(4, 4), "DSU: union with itself");
        check(dsu.getComponentCount() == 2, "DSU: final count");
        passed++;
    }

    private static void testMST() {
        checkMST(graph(4, false, new int[][]{{0, 1, 3}, {0, 2, 1},
                {1, 2, 2}, {1, 3, 4}, {2, 3, 5}}), 7, true, 3);
        checkMST(graph(6, false, new int[][]{{1, 2, 2}, {2, 3, 1},
                {1, 3, 5}, {4, 5, 4}}), 7, false, 3);
        checkMST(graph(4, false, new int[][]{{0, 1, 1}, {1, 2, 1},
                {2, 3, 1}, {3, 0, 1}, {0, 2, 2}}), 3, true, 3);
        checkMST(graph(1, false, new int[][]{}), 0, true, 0);
        checkMST(graph(3, false, new int[][]{{0, 1, -2}, {1, 2, 1},
                {0, 2, 4}}), -1, true, 2);
        checkMST(graph(3, false, new int[][]{{0, 1, Integer.MAX_VALUE},
                {1, 2, Integer.MAX_VALUE}}), 4294967294L, true, 2);
        checkMST(graph(3, false, new int[][]{{0, 0, -100}, {0, 1, 8},
                {0, 1, 2}, {1, 2, 1}, {0, 2, 9}}), 3, true, 2);
    }

    private static void testPathCompression() {
        OperationCounts counts = new OperationCounts();
        DSU dsu = new DSU(4, counts);
        dsu.union(0, 1);
        dsu.union(2, 3);
        dsu.union(0, 2);
        long before = counts.dsuParentTraversals;
        check(dsu.find(3) == 0, "DSU: root before compression");
        check(counts.dsuParentTraversals - before == 2, "DSU: first find follows two parents");
        before = counts.dsuParentTraversals;
        check(dsu.find(3) == 0, "DSU: root after compression");
        check(counts.dsuParentTraversals - before == 1, "DSU: second find follows one parent");
        passed++;
    }

    private static void checkMST(Graph graph, long weight, boolean connected, int edges) {
        MSTResult[] results = {Kruskal.findMST(graph), Prim.findMST(graph)};
        String[] names = {"Kruskal", "Prim"};
        for (int i = 0; i < results.length; i++) {
            MSTResult result = results[i];
            check(result.getTotalWeight() == weight, names[i] + ": total weight");
            check(result.isConnected() == connected, names[i] + ": connectivity");
            check(result.getEdges().size() == edges, names[i] + ": edge count");
            DSU forest = new DSU(graph.getVertexCount());
            long sum = 0;
            for (Edge edge : result.getEdges()) {
                boolean exists = false;
                for (Edge original : graph.getNeighbors(edge.u)) {
                    if (original.v == edge.v && original.weight == edge.weight) {
                        exists = true;
                    }
                }
                check(exists, names[i] + ": selected edge exists in input");
                check(forest.union(edge.u, edge.v), names[i] + ": no cycle in result");
                sum += edge.weight;
            }
            check(sum == weight, names[i] + ": sum of selected weights");
            for (Edge edge : graph.getEdges()) {
                check(forest.find(edge.u) == forest.find(edge.v),
                        names[i] + ": every original component stays connected");
            }
        }
        passed++;
    }

    private static void testTopologicalSort() {
        Graph dag = graph(6, true, new int[][]{{0, 2, 1}, {1, 2, 1},
                {1, 3, 1}, {2, 4, 1}, {3, 4, 1}});
        checkTopological(dag, false);
        dag.addEdge(4, 1, 1);
        checkTopological(dag, true);
        checkTopological(graph(1, true, new int[][]{}), false);
        checkTopological(graph(1, true, new int[][]{{0, 0, 1}}), true);
    }

    private static void checkTopological(Graph graph, boolean hasCycle) {
        TopologicalSortResult result = TopologicalSort.sort(graph);
        check(result.hasCycle() == hasCycle, "Topological: cycle detection");
        if (hasCycle) {
            check(result.getOrder().isEmpty(), "Topological: cycle has no order");
        } else {
            List<Integer> order = result.getOrder();
            check(order.size() == graph.getVertexCount(), "Topological: all vertices");
            int[] position = new int[graph.getVertexCount()];
            Arrays.fill(position, -1);
            for (int i = 0; i < order.size(); i++) {
                int vertex = order.get(i);
                check(vertex >= 0 && vertex < position.length, "Topological: valid vertex");
                check(position[vertex] == -1, "Topological: no repeated vertex");
                position[vertex] = i;
            }
            for (Edge edge : graph.getEdges()) {
                check(position[edge.u] < position[edge.v], "Topological: every edge points forward");
            }
        }
        passed++;
    }

    private static void testSCC() {
        Graph graph = graph(7, true, new int[][]{{0, 1, 1}, {1, 2, 1},
                {2, 0, 1}, {2, 3, 1}, {3, 4, 1}, {4, 3, 1}, {4, 5, 1}});
        checkSCC(graph, new int[][]{{0, 1, 2}, {3, 4}, {5}, {6}});
        graph.addEdge(5, 0, 1);
        checkSCC(graph, new int[][]{{0, 1, 2, 3, 4, 5}, {6}});
        checkSCC(graph(4, true, new int[][]{{0, 1, 1}, {1, 2, 1}}),
                new int[][]{{0}, {1}, {2}, {3}});
        checkSCC(graph(1, true, new int[][]{{0, 0, 1}}), new int[][]{{0}});
    }

    private static void checkSCC(Graph graph, int[][] expected) {
        List<List<Integer>> components = Kosaraju.findComponents(graph);
        check(components.size() == expected.length, "SCC: component count");
        int[] componentOf = new int[graph.getVertexCount()];
        Arrays.fill(componentOf, -1);
        for (int i = 0; i < components.size(); i++) {
            for (int vertex : components.get(i)) {
                check(vertex >= 0 && vertex < componentOf.length, "SCC: valid vertex");
                check(componentOf[vertex] == -1, "SCC: no repeated vertex");
                componentOf[vertex] = i;
            }
        }
        for (int vertex = 0; vertex < componentOf.length; vertex++) {
            check(componentOf[vertex] != -1, "SCC: every vertex belongs to a component");
        }
        for (int[] group : expected) {
            int actual = componentOf[group[0]];
            check(components.get(actual).size() == group.length, "SCC: exact group size");
            for (int vertex : group) {
                check(componentOf[vertex] == actual, "SCC: exact group members");
            }
        }
        passed++;
    }

    private static void testDijkstra() {
        Graph routes = graph(5, true, new int[][]{{0, 1, 4}, {0, 2, 1},
                {2, 1, 2}, {1, 3, 1}, {2, 3, 5}});
        checkDijkstra(routes, 0, new long[]{0, 3, 1, 4, INF});
        checkDijkstra(routes, 3, new long[]{INF, INF, INF, 0, INF});
        checkDijkstra(graph(4, false, new int[][]{{0, 1, 1}, {0, 2, 1},
                {1, 3, 1}, {2, 3, 1}}), 0, new long[]{0, 1, 1, 2});
        checkDijkstra(graph(3, true, new int[][]{{0, 1, 0}, {1, 2, 0},
                {0, 2, 5}}), 0, new long[]{0, 0, 0});
        checkDijkstra(graph(1, true, new int[][]{}), 0, new long[]{0});
        checkDijkstra(graph(3, true, new int[][]{{0, 1, Integer.MAX_VALUE},
                {1, 2, Integer.MAX_VALUE}}), 0, new long[]{0, 2147483647L, 4294967294L});
        checkDijkstra(graph(3, true, new int[][]{{0, 0, 0}, {0, 1, 8},
                {0, 1, 2}, {1, 2, 1}}), 0, new long[]{0, 2, 3});
    }

    private static void checkDijkstra(Graph graph, int source, long[] expected) {
        DijkstraResult result = Dijkstra.shortestPaths(graph, source);
        for (int vertex = 0; vertex < expected.length; vertex++) {
            check(result.getDistance(vertex) == expected[vertex], "Dijkstra: distance to " + vertex);
            boolean reachable = expected[vertex] != INF;
            check(result.isReachable(vertex) == reachable, "Dijkstra: reachability");
            List<Integer> path = result.getPathTo(vertex);
            if (!reachable) {
                check(path.isEmpty(), "Dijkstra: unreachable vertex has no path");
                continue;
            }
            check(!path.isEmpty(), "Dijkstra: reachable vertex has a path");
            check(path.get(0) == source, "Dijkstra: path starts at source");
            check(path.get(path.size() - 1) == vertex, "Dijkstra: path ends at destination");
            check(path.size() <= graph.getVertexCount(), "Dijkstra: path has no extra vertices");
            if (vertex == source) {
                check(path.size() == 1, "Dijkstra: source path contains only source");
            }
            long sum = 0;
            for (int i = 1; i < path.size(); i++) {
                long weight = INF;
                for (Edge edge : graph.getNeighbors(path.get(i - 1))) {
                    if (edge.v == path.get(i)) {
                        weight = Math.min(weight, edge.weight);
                    }
                }
                check(weight != INF, "Dijkstra: path uses an existing edge");
                sum += weight;
            }
            check(sum == expected[vertex], "Dijkstra: path weight matches distance");
        }
        passed++;
    }

    private static void testNegativeWeightRejection() {
        boolean rejected = false;
        try {
            Dijkstra.shortestPaths(graph(2, true, new int[][]{{0, 1, -1}}), 0);
        } catch (IllegalArgumentException error) {
            rejected = true;
        }
        check(rejected, "Dijkstra: reject negative weights");
        passed++;
    }

    private static Graph graph(int vertices, boolean directed, int[][] edges) {
        Graph graph = new Graph(vertices, directed);
        for (int[] edge : edges) {
            graph.addEdge(edge[0], edge[1], edge[2]);
        }
        return graph;
    }

    private static void testOperationCounts() {
        int[][] edges = {{0, 1, 4}, {0, 2, 1}, {2, 1, 2}};
        Graph directed = graph(3, true, edges);
        OperationCounts topological = new OperationCounts();
        TopologicalSort.sort(directed, topological);
        check(topological.edgeExaminations == 6, "Topological counts: six edge examinations");
        check(topological.vertexVisits == 3, "Topological counts: three vertex visits");
        OperationCounts scc = new OperationCounts();
        Kosaraju.findComponents(directed, scc);
        check(scc.edgeExaminations == 9, "SCC counts: nine edge examinations");
        check(scc.vertexVisits == 6, "SCC counts: six vertex visits");
        OperationCounts dijkstra = new OperationCounts();
        DijkstraResult paths = Dijkstra.shortestPaths(directed, 0, dijkstra);
        check(paths.getDistance(1) == 3, "Dijkstra counts: correct distance");
        check(dijkstra.edgeExaminations == 6, "Dijkstra counts: six edge examinations");
        check(dijkstra.relaxationAttempts == 3, "Dijkstra counts: three relaxation attempts");
        check(dijkstra.successfulRelaxations == 3, "Dijkstra counts: three successful relaxations");
        check(dijkstra.queuePushes == 4, "Dijkstra counts: four queue pushes");
        check(dijkstra.queuePops == 4, "Dijkstra counts: four queue pops");
        check(dijkstra.vertexVisits == 3, "Dijkstra counts: three vertex visits");
        Graph undirected = graph(3, false, edges);
        OperationCounts kruskal = new OperationCounts();
        Kruskal.findMST(undirected, kruskal);
        check(kruskal.edgeExaminations == 2, "Kruskal counts: two edge examinations");
        check(kruskal.dsuFindCalls == 4, "Kruskal counts: four find calls");
        check(kruskal.dsuUnionCalls == 2, "Kruskal counts: two union calls");
        OperationCounts prim = new OperationCounts();
        Prim.findMST(undirected, prim);
        check(prim.edgeExaminations == 6, "Prim counts: six edge examinations");
        check(prim.queuePushes == 3, "Prim counts: three queue pushes");
        check(prim.queuePops == 3, "Prim counts: three queue pops");
        passed++;
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
