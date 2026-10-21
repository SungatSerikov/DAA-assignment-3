package mst;

import graph.Edge;
import graph.Graph;
import metrics.OperationCounts;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class Prim {

    public static MSTResult findMST(Graph graph) {
        return findMST(graph, null);
    }

    public static MSTResult findMST(Graph graph, OperationCounts counts) {
        if (graph.isDirected()) {
            throw new IllegalArgumentException("Prim requires an undirected graph");
        }

        int vertexCount = graph.getVertexCount();

        if (vertexCount == 0) {
            throw new IllegalArgumentException("The graph must contain at least one vertex");
        }

        boolean[] visited = new boolean[vertexCount];
        List<Edge> selectedEdges = new ArrayList<>();
        long totalWeight = 0;
        int componentCount = 0;

        PriorityQueue<Edge> candidates = new PriorityQueue<>((left, right) -> {
            if (counts != null) {
                counts.weightComparisons++;
            }
            return Integer.compare(left.weight, right.weight);
        });

        for (int start = 0; start < vertexCount; start++) {
            if (visited[start]) {
                continue;
            }

            componentCount++;
            addVertex(graph, start, visited, candidates, counts);

            while (!candidates.isEmpty()) {
                Edge edge = candidates.poll();
                if (counts != null) {
                    counts.queuePops++;
                }

                if (visited[edge.v]) {
                    continue;
                }

                selectedEdges.add(edge);
                totalWeight += edge.weight;

                addVertex(graph, edge.v, visited, candidates, counts);
            }
        }

        boolean connected = componentCount == 1;

        return new MSTResult(selectedEdges, totalWeight, connected);
    }

    private static void addVertex(
            Graph graph,
            int vertex,
            boolean[] visited,
            PriorityQueue<Edge> candidates,
            OperationCounts counts
    ) {
        visited[vertex] = true;
        if (counts != null) {
            counts.vertexVisits++;
        }

        for (Edge edge : graph.getNeighbors(vertex)) {
            if (counts != null) {
                counts.edgeExaminations++;
            }
            if (!visited[edge.v]) {
                candidates.add(edge);
                if (counts != null) {
                    counts.queuePushes++;
                }
            }
        }
    }
}
