package mst;

import dsu.DSU;
import graph.Edge;
import graph.Graph;
import metrics.OperationCounts;

import java.util.ArrayList;
import java.util.List;

public class Kruskal {

    public static MSTResult findMST(Graph graph) {
        return findMST(graph, null);
    }

    public static MSTResult findMST(Graph graph, OperationCounts counts) {
        if (graph.isDirected()) {
            throw new IllegalArgumentException("Kruskal requires an undirected graph");
        }

        int vertexCount = graph.getVertexCount();

        if (vertexCount == 0) {
            throw new IllegalArgumentException("The graph must contain at least one vertex");
        }

        List<Edge> sortedEdges = new ArrayList<>(graph.getEdges());

        sortedEdges.sort((left, right) -> {
            if (counts != null) {
                counts.weightComparisons++;
            }
            return Integer.compare(left.weight, right.weight);
        });

        DSU dsu = new DSU(vertexCount, counts);
        List<Edge> selectedEdges = new ArrayList<>();
        long totalWeight = 0;

        for (Edge edge : sortedEdges) {
            if (counts != null) {
                counts.edgeExaminations++;
            }
            if (dsu.union(edge.u, edge.v)) {
                selectedEdges.add(edge);
                totalWeight += edge.weight;

                if (selectedEdges.size() == vertexCount - 1) {
                    break;
                }
            }
        }

        boolean connected = dsu.getComponentCount() == 1;

        return new MSTResult(selectedEdges, totalWeight, connected);
    }
}
