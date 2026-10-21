import connectivity.Kosaraju;
import connectivity.TopologicalSort;
import experiments.BenchmarkRunner;
import graph.Graph;
import mst.Kruskal;
import mst.MSTResult;
import mst.Prim;
import shortestpath.Dijkstra;
import shortestpath.DijkstraResult;
import tests.CorrectnessTests;
import tests.GeneratorTests;

import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length > 2) {
            throw new IllegalArgumentException("Usage: Main [demo|test|benchmark|all] [results-directory]");
        }
        String command = "demo";
        if (args.length > 0) {
            command = args[0];
        }
        Path results = Path.of("results");
        if (args.length == 2) {
            if (!command.equals("benchmark") && !command.equals("all")) {
                throw new IllegalArgumentException("Only benchmark and all accept a results directory");
            }
            results = Path.of(args[1]);
        }

        if (command.equals("demo")) {
            demo();
        } else if (command.equals("test")) {
            test();
        } else if (command.equals("benchmark")) {
            BenchmarkRunner.run(results);
        } else if (command.equals("all")) {
            demo();
            test();
            BenchmarkRunner.run(results);
        } else {
            throw new IllegalArgumentException("Choose demo, test, benchmark or all");
        }
    }

    private static void demo() {
        showMST();
        showDirectedGraphs();
        showShortestPaths();
    }

    private static void showMST() {
        Graph roads = new Graph(4, false);
        roads.addEdge(0, 1, 3);
        roads.addEdge(0, 2, 1);
        roads.addEdge(1, 2, 2);
        roads.addEdge(1, 3, 4);
        roads.addEdge(2, 3, 5);

        MSTResult kruskal = Kruskal.findMST(roads);
        MSTResult prim = Prim.findMST(roads);
        System.out.println("MST example: 4 districts, 5 roads");
        System.out.println("Kruskal weight: " + kruskal.getTotalWeight());
        System.out.println("Prim weight: " + prim.getTotalWeight());
        System.out.println("Connected: " + kruskal.isConnected());
        System.out.println("Selected roads: " + kruskal.getEdges());
    }

    private static void showDirectedGraphs() {
        Graph dependencies = new Graph(4, true);
        dependencies.addEdge(0, 1, 1);
        dependencies.addEdge(0, 2, 1);
        dependencies.addEdge(1, 3, 1);
        dependencies.addEdge(2, 3, 1);
        System.out.println("Topological order: " + TopologicalSort.sort(dependencies).getOrder());

        Graph directed = new Graph(5, true);
        directed.addEdge(0, 1, 1);
        directed.addEdge(1, 2, 1);
        directed.addEdge(2, 0, 1);
        directed.addEdge(2, 3, 1);
        directed.addEdge(3, 4, 1);
        directed.addEdge(4, 3, 1);
        System.out.println("Directed graph has cycle: " + TopologicalSort.sort(directed).hasCycle());
        System.out.println("SCCs: " + Kosaraju.findComponents(directed));
    }

    private static void showShortestPaths() {
        Graph routes = new Graph(5, true);
        routes.addEdge(0, 1, 4);
        routes.addEdge(0, 2, 1);
        routes.addEdge(2, 1, 2);
        routes.addEdge(1, 3, 1);
        routes.addEdge(2, 3, 5);

        DijkstraResult result = Dijkstra.shortestPaths(routes, 0);
        for (int vertex = 0; vertex < routes.getVertexCount(); vertex++) {
            if (result.isReachable(vertex)) {
                System.out.println("0 -> " + vertex + ": distance=" + result.getDistance(vertex)
                        + ", path=" + result.getPathTo(vertex));
            } else {
                System.out.println("0 -> " + vertex + ": unreachable");
            }
        }
    }

    private static void test() {
        CorrectnessTests.main(new String[0]);
        GeneratorTests.main(new String[0]);
    }
}
