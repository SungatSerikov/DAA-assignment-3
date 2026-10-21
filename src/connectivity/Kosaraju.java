package connectivity;

import graph.Edge;
import graph.Graph;
import metrics.OperationCounts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Kosaraju {

    public static List<List<Integer>> findComponents(Graph graph) {
        return findComponents(graph, null);
    }

    public static List<List<Integer>> findComponents(Graph graph, OperationCounts counts) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException("Kosaraju requires a directed graph");
        }

        int vertexCount = graph.getVertexCount();

        boolean[] visited = new boolean[vertexCount];
        List<Integer> finishOrder = new ArrayList<>();

        for (int v = 0; v < vertexCount; v++) {
            if (!visited[v]) {
                recordFinishOrder(graph, v, visited, finishOrder, counts);
            }
        }

        Graph reversed = reverseGraph(graph, counts);

        Arrays.fill(visited, false);

        List<List<Integer>> components = new ArrayList<>();

        for (int i = finishOrder.size() - 1; i >= 0; i--) {
            int vertex = finishOrder.get(i);

            if (!visited[vertex]) {
                List<Integer> component = new ArrayList<>();

                collectComponent(reversed, vertex, visited, component, counts);

                components.add(component);
            }
        }

        return components;
    }

    private static void recordFinishOrder(
            Graph graph,
            int vertex,
            boolean[] visited,
            List<Integer> finishOrder,
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
                recordFinishOrder(graph, edge.v, visited, finishOrder, counts);
            }
        }

        finishOrder.add(vertex);
    }

    private static Graph reverseGraph(Graph graph, OperationCounts counts) {
        Graph reversed = new Graph(graph.getVertexCount(), true);

        for (Edge edge : graph.getEdges()) {
            if (counts != null) {
                counts.edgeExaminations++;
            }
            reversed.addEdge(edge.v, edge.u, edge.weight);
        }

        return reversed;
    }

    private static void collectComponent(
            Graph graph,
            int vertex,
            boolean[] visited,
            List<Integer> component,
            OperationCounts counts
    ) {
        visited[vertex] = true;
        if (counts != null) {
            counts.vertexVisits++;
        }
        component.add(vertex);

        for (Edge edge : graph.getNeighbors(vertex)) {
            if (counts != null) {
                counts.edgeExaminations++;
            }
            if (!visited[edge.v]) {
                collectComponent(graph, edge.v, visited, component, counts);
            }
        }
    }
}
