package experiments;

import metrics.OperationCounts;

class BenchmarkResult {
    final String algorithm;
    final String graphType;
    final int vertices;
    final int edges;
    final long seed;
    final long[] samples;
    final double averageNs;
    final long minNs;
    final long maxNs;
    final OperationCounts counts;

    BenchmarkResult(String algorithm, String graphType, int vertices, int edges, long seed,
                    long[] samples, double averageNs, long minNs, long maxNs, OperationCounts counts) {
        this.algorithm = algorithm;
        this.graphType = graphType;
        this.vertices = vertices;
        this.edges = edges;
        this.seed = seed;
        this.samples = samples;
        this.averageNs = averageNs;
        this.minNs = minNs;
        this.maxNs = maxNs;
        this.counts = counts;
    }
}
