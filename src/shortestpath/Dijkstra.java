package shortestpath;

import graph.Edge;
import graph.Graph;
import metrics.OperationCounts;

import java.util.Arrays;
import java.util.PriorityQueue;

public class Dijkstra {

    private static class QueueEntry {
        final int vertex;
        final long distance;

        QueueEntry(int vertex, long distance) {
            this.vertex = vertex;
            this.distance = distance;
        }
    }

    public static DijkstraResult shortestPaths(Graph graph, int source) {
        return shortestPaths(graph, source, null);
    }

    public static DijkstraResult shortestPaths(Graph graph, int source, OperationCounts counts) {
        int vertexCount = graph.getVertexCount();

        if (source < 0 || source >= vertexCount) {
            throw new IllegalArgumentException("Invalid source vertex: " + source);
        }

        for (Edge edge : graph.getEdges()) {
            if (counts != null) {
                counts.edgeExaminations++;
            }
            if (edge.weight < 0) {
                throw new IllegalArgumentException("Dijkstra requires non-negative weights");
            }
        }

        long[] distances = new long[vertexCount];
        int[] previous = new int[vertexCount];

        Arrays.fill(distances, DijkstraResult.INF);
        Arrays.fill(previous, -1);

        PriorityQueue<QueueEntry> queue = new PriorityQueue<>((left, right) -> {
            if (counts != null) {
                counts.weightComparisons++;
            }
            return Long.compare(left.distance, right.distance);
        });

        distances[source] = 0;
        queue.add(new QueueEntry(source, 0));
        if (counts != null) {
            counts.queuePushes++;
        }

        while (!queue.isEmpty()) {
            QueueEntry current = queue.poll();
            if (counts != null) {
                counts.queuePops++;
            }

            if (current.distance != distances[current.vertex]) {
                continue;
            }
            if (counts != null) {
                counts.vertexVisits++;
            }

            for (Edge edge : graph.getNeighbors(current.vertex)) {
                if (counts != null) {
                    counts.edgeExaminations++;
                    counts.relaxationAttempts++;
                }
                long newDistance = current.distance + edge.weight;

                if (newDistance < distances[edge.v]) {
                    distances[edge.v] = newDistance;
                    previous[edge.v] = current.vertex;

                    queue.add(new QueueEntry(edge.v, newDistance));
                    if (counts != null) {
                        counts.queuePushes++;
                        counts.successfulRelaxations++;
                    }
                }
            }
        }

        return new DijkstraResult(distances, previous);
    }
}
