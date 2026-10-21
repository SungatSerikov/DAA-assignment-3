package shortestpath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DijkstraResult {
    public static final long INF = Long.MAX_VALUE;

    private final long[] distances;
    private final int[] previous;

    public DijkstraResult(long[] distances, int[] previous) {
        this.distances = distances.clone();
        this.previous = previous.clone();
    }

    public long getDistance(int vertex) {
        checkVertex(vertex);
        return distances[vertex];
    }

    public boolean isReachable(int vertex) {
        return getDistance(vertex) != INF;
    }

    public List<Integer> getPathTo(int vertex) {
        if (!isReachable(vertex)) {
            return List.of();
        }

        List<Integer> path = new ArrayList<>();

        int current = vertex;
        while (current != -1) {
            path.add(current);
            current = previous[current];
        }

        Collections.reverse(path);

        return List.copyOf(path);
    }

    private void checkVertex(int vertex) {
        if (vertex < 0 || vertex >= distances.length) {
            throw new IllegalArgumentException("Invalid vertex: " + vertex);
        }
    }
}