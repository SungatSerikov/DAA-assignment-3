package graph;

public class Edge {
    public final int u;
    public final int v;
    public final int weight;

    public Edge(int u, int v, int weight) {
        this.u = u;
        this.v = v;
        this.weight = weight;
    }

    @Override
    public String toString() {
        return u + " -- " + v + " (weight=" + weight + ")";
    }
}