package connectivity;

import graph.Edge;
import graph.Graph;
import metrics.OperationCounts;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class TopologicalSort {

    public static TopologicalSortResult sort(Graph graph) {
        return sort(graph, null);
    }

    public static TopologicalSortResult sort(Graph graph, OperationCounts counts) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException("Topological ordering requires a directed graph");
        }

        int vertexCount = graph.getVertexCount();
        int[] indegree = new int[vertexCount];

        for (Edge edge : graph.getEdges()) {
            if (counts != null) {
                counts.edgeExaminations++;
            }
            indegree[edge.v]++;
        }

        Queue<Integer> ready = new ArrayDeque<>();

        for (int v = 0; v < vertexCount; v++) {
            if (indegree[v] == 0) {
                ready.add(v);
                if (counts != null) {
                    counts.queuePushes++;
                }
            }
        }

        List<Integer> order = new ArrayList<>();

        while (!ready.isEmpty()) {
            int current = ready.remove();
            if (counts != null) {
                counts.queuePops++;
                counts.vertexVisits++;
            }
            order.add(current);

            for (Edge edge : graph.getNeighbors(current)) {
                if (counts != null) {
                    counts.edgeExaminations++;
                }
                int neighbor = edge.v;
                indegree[neighbor]--;

                if (indegree[neighbor] == 0) {
                    ready.add(neighbor);
                    if (counts != null) {
                        counts.queuePushes++;
                    }
                }
            }
        }

        if (order.size() != vertexCount) {
            return new TopologicalSortResult(List.of(), true);
        }

        return new TopologicalSortResult(order, false);
    }
}
