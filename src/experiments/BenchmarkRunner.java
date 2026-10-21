package experiments;

import connectivity.Kosaraju;
import connectivity.TopologicalSort;
import graph.Graph;
import metrics.OperationCounts;
import mst.Kruskal;
import mst.Prim;
import shortestpath.Dijkstra;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class BenchmarkRunner {
    private static final long SEED = 42;
    private static final int WARMUPS = 10;
    private static final int REPETITIONS = 5;
    private static final int SOURCE = 0;
    private static volatile Object sink;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            throw new IllegalArgumentException("Usage: BenchmarkRunner [results-directory]");
        }
        Path resultsRoot = Path.of("results");
        if (args.length == 1) {
            resultsRoot = Path.of(args[0]);
        }
        run(resultsRoot);
    }

    public static void run(Path resultsRoot) throws IOException {
        int[][] sizes = {{10, 30}, {100, 500}, {1000, 5000}};
        Graph[] undirectedGraphs = new Graph[sizes.length];
        Graph[] acyclicGraphs = new Graph[sizes.length];
        Graph[] cyclicGraphs = new Graph[sizes.length];

        for (int i = 0; i < sizes.length; i++) {
            int vertices = sizes[i][0];
            int edges = sizes[i][1];
            undirectedGraphs[i] = GraphGenerator.connectedUndirected(vertices, edges, SEED);
            acyclicGraphs[i] = GraphGenerator.directedAcyclic(vertices, edges, SEED);
            cyclicGraphs[i] = GraphGenerator.directedWithCycle(vertices, edges, SEED);
        }

        List<BenchmarkResult> results = new ArrayList<>();
        for (int i = 0; i < sizes.length; i++) {
            results.add(measure("Kruskal", "connected_undirected", undirectedGraphs[i]));
            results.add(measure("Prim", "connected_undirected", undirectedGraphs[i]));
            results.add(measure("TopologicalSort", "dag", acyclicGraphs[i]));
            results.add(measure("TopologicalSort", "directed_cyclic", cyclicGraphs[i]));
            results.add(measure("Kosaraju", "directed_cyclic", cyclicGraphs[i]));
            results.add(measure("Dijkstra", "directed_cyclic", cyclicGraphs[i]));
        }

        Path tables = resultsRoot.resolve("tables");
        Path plots = resultsRoot.resolve("plots");
        Files.createDirectories(tables);
        Files.createDirectories(plots);
        writeTables(tables, results);
        BenchmarkPlots.write(plots.resolve("time-vs-vertices.svg"), results, true);
        BenchmarkPlots.write(plots.resolve("time-vs-edges.svg"), results, false);
        System.out.println("Results saved to: " + resultsRoot.toAbsolutePath());
    }

    private static BenchmarkResult measure(String algorithm, String graphType, Graph graph) {
        for (int i = 0; i < WARMUPS; i++) {
            sink = runAlgorithm(algorithm, graph, null);
        }

        long[] samples = new long[REPETITIONS];
        for (int i = 0; i < REPETITIONS; i++) {
            long start = System.nanoTime();
            Object result = runAlgorithm(algorithm, graph, null);
            long stop = System.nanoTime();
            sink = result;
            samples[i] = stop - start;
            if (samples[i] < 0) {
                throw new IllegalStateException("Timer returned a negative duration");
            }
        }

        OperationCounts counts = new OperationCounts();
        sink = runAlgorithm(algorithm, graph, counts);

        long total = 0;
        long minimum = samples[0];
        long maximum = samples[0];
        for (long sample : samples) {
            total += sample;
            if (sample < minimum) {
                minimum = sample;
            }
            if (sample > maximum) {
                maximum = sample;
            }
        }
        double average = (double) total / REPETITIONS;
        return new BenchmarkResult(algorithm, graphType, graph.getVertexCount(), graph.getEdges().size(),
                SEED, samples, average, minimum, maximum, counts);
    }

    private static Object runAlgorithm(String algorithm, Graph graph, OperationCounts counts) {
        switch (algorithm) {
            case "Kruskal":
                return Kruskal.findMST(graph, counts);
            case "Prim":
                return Prim.findMST(graph, counts);
            case "TopologicalSort":
                return TopologicalSort.sort(graph, counts);
            case "Kosaraju":
                return Kosaraju.findComponents(graph, counts);
            case "Dijkstra":
                return Dijkstra.shortestPaths(graph, SOURCE, counts);
            default:
                throw new IllegalArgumentException("Unknown algorithm: " + algorithm);
        }
    }

    private static void writeTables(Path tables, List<BenchmarkResult> results) throws IOException {
        StringBuilder summary = new StringBuilder("algorithm,graph_type,vertices,edges,seed,repetitions,average_ns,min_ns,max_ns,edge_examinations,weight_comparisons,dsu_find_calls,dsu_union_calls,dsu_parent_traversals,queue_pushes,queue_pops,relaxation_attempts,successful_relaxations,vertex_visits\n");
        StringBuilder runs = new StringBuilder("algorithm,graph_type,vertices,edges,seed,run,elapsed_ns\n");

        for (BenchmarkResult result : results) {
            String identity = result.algorithm + "," + result.graphType + "," + result.vertices
                    + "," + result.edges + "," + result.seed;
            for (int i = 0; i < result.samples.length; i++) {
                runs.append(identity).append(',').append(i + 1).append(',')
                        .append(result.samples[i]).append('\n');
            }

            OperationCounts counts = result.counts;
            summary.append(identity).append(',').append(REPETITIONS).append(',')
                    .append(result.averageNs).append(',').append(result.minNs).append(',').append(result.maxNs).append(',')
                    .append(counts.edgeExaminations).append(',').append(counts.weightComparisons).append(',')
                    .append(counts.dsuFindCalls).append(',').append(counts.dsuUnionCalls).append(',')
                    .append(counts.dsuParentTraversals).append(',').append(counts.queuePushes).append(',')
                    .append(counts.queuePops).append(',').append(counts.relaxationAttempts).append(',')
                    .append(counts.successfulRelaxations).append(',').append(counts.vertexVisits).append('\n');
        }

        Files.writeString(tables.resolve("summary.csv"), summary, StandardCharsets.UTF_8);
        Files.writeString(tables.resolve("runs.csv"), runs, StandardCharsets.UTF_8);
    }
}
