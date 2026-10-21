package graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Graph {
    private final int vertexCount;
    private final boolean directed;

    private final List<Edge> edges;
    private final List<List<Edge>> adjacency;

    public Graph(int vertexCount, boolean directed) {
        if (vertexCount < 0) {
            throw new IllegalArgumentException("Vertex count cannot be negative");
        }

        this.vertexCount = vertexCount;
        this.directed = directed;

        edges = new ArrayList<>();
        adjacency = new ArrayList<>();

        for (int v = 0; v < vertexCount; v++) {
            adjacency.add(new ArrayList<>());
        }
    }

    public void addEdge(int u, int v, int weight) {
        checkVertex(u);
        checkVertex(v);

        Edge edge = new Edge(u, v, weight);

        edges.add(edge);
        adjacency.get(u).add(edge);

        if (!directed) {
            adjacency.get(v).add(new Edge(v, u, weight));
        }
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public boolean isDirected() {
        return directed;
    }

    public List<Edge> getEdges() {
        return Collections.unmodifiableList(edges);
    }

    public List<Edge> getNeighbors(int vertex) {
        checkVertex(vertex);
        return Collections.unmodifiableList(adjacency.get(vertex));
    }

    private void checkVertex(int vertex) {
        if (vertex < 0 || vertex >= vertexCount) {
            throw new IllegalArgumentException("Invalid vertex: " + vertex);
        }
    }
}