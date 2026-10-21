package dsu;

import metrics.OperationCounts;

public class DSU {
    private final int[] parent;
    private final int[] size;
    private final OperationCounts counts;
    private int componentCount;

    public DSU(int elementCount) {
        this(elementCount, null);
    }

    public DSU(int elementCount, OperationCounts counts) {
        if (elementCount < 0) {
            throw new IllegalArgumentException("Element count cannot be negative");
        }

        parent = new int[elementCount];
        size = new int[elementCount];
        this.counts = counts;
        componentCount = elementCount;

        for (int i = 0; i < elementCount; i++) {
            parent[i] = i;
            size[i] = 1;
        }
    }

    public int find(int x) {
        if (counts != null) {
            counts.dsuFindCalls++;
        }
        if (x < 0 || x >= parent.length) {
            throw new IllegalArgumentException("Invalid element: " + x);
        }

        return findRoot(x);
    }

    private int findRoot(int x) {
        if (parent[x] != x) {
            if (counts != null) {
                counts.dsuParentTraversals++;
            }
            parent[x] = findRoot(parent[x]);
        }

        return parent[x];
    }

    public boolean union(int a, int b) {
        if (counts != null) {
            counts.dsuUnionCalls++;
        }
        int rootA = find(a);
        int rootB = find(b);

        if (rootA == rootB) {
            return false;
        }

        if (size[rootA] < size[rootB]) {
            int temp = rootA;
            rootA = rootB;
            rootB = temp;
        }

        parent[rootB] = rootA;
        size[rootA] += size[rootB];
        componentCount--;

        return true;
    }

    public int getComponentCount() {
        return componentCount;
    }
}
