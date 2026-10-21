package experiments;

import graph.Graph;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class GraphGenerator {

    public static Graph connectedUndirected(int vertexCount, int edgeCount, long seed) {
        if (vertexCount < 1) {
            throw new IllegalArgumentException("At least one vertex is required");
        }

        long maxEdges = (long) vertexCount * (vertexCount - 1) / 2;

        if (edgeCount < vertexCount - 1 || edgeCount > maxEdges) {
            throw new IllegalArgumentException("Invalid edge count");
        }

        Graph graph = new Graph(vertexCount, false);
        Random random = new Random(seed);
        Set<String> usedEdges = new HashSet<>();

        for (int v = 1; v < vertexCount; v++) {
            int u = random.nextInt(v);
            int weight = random.nextInt(100) + 1;

            graph.addEdge(u, v, weight);
            usedEdges.add(edgeKey(u, v));
        }

        while (graph.getEdges().size() < edgeCount) {
            int u = random.nextInt(vertexCount);
            int v = random.nextInt(vertexCount);

            if (u == v) {
                continue;
            }

            if (usedEdges.add(edgeKey(u, v))) {
                int weight = random.nextInt(100) + 1;
                graph.addEdge(u, v, weight);
            }
        }

        return graph;
    }

    public static Graph directedAcyclic(int vertexCount, int edgeCount, long seed) {
        if (vertexCount < 1) {
            throw new IllegalArgumentException("At least one vertex is required");
        }

        long maxEdges = (long) vertexCount * (vertexCount - 1) / 2;
        if (edgeCount < 0 || edgeCount > maxEdges) {
            throw new IllegalArgumentException("Invalid edge count");
        }

        Graph graph = new Graph(vertexCount, true);
        Random random = new Random(seed);
        Set<String> usedEdges = new HashSet<>();

        while (usedEdges.size() < edgeCount) {
            int u = random.nextInt(vertexCount);
            int v = random.nextInt(vertexCount);
            if (u >= v) {
                continue;
            }
            if (usedEdges.add(u + ":" + v)) {
                graph.addEdge(u, v, random.nextInt(100) + 1);
            }
        }

        return graph;
    }

    public static Graph directedWithCycle(int vertexCount, int edgeCount, long seed) {
        if (vertexCount < 3) {
            throw new IllegalArgumentException("At least three vertices are required");
        }

        long maxEdges = (long) vertexCount * (vertexCount - 1);
        if (edgeCount < 3 || edgeCount > maxEdges) {
            throw new IllegalArgumentException("Invalid edge count");
        }

        Graph graph = new Graph(vertexCount, true);
        Random random = new Random(seed);
        Set<String> usedEdges = new HashSet<>();

        for (int u = 0; u < 3; u++) {
            int v = (u + 1) % 3;
            graph.addEdge(u, v, random.nextInt(100) + 1);
            usedEdges.add(u + ":" + v);
        }

        while (usedEdges.size() < edgeCount) {
            int u = random.nextInt(vertexCount);
            int v = random.nextInt(vertexCount);
            if (u == v) {
                continue;
            }
            if (usedEdges.add(u + ":" + v)) {
                graph.addEdge(u, v, random.nextInt(100) + 1);
            }
        }

        return graph;
    }

    private static String edgeKey(int u, int v) {
        return Math.min(u, v) + ":" + Math.max(u, v);
    }
}
