package connectivity;

import java.util.List;

public class TopologicalSortResult {
    private final List<Integer> order;
    private final boolean hasCycle;

    public TopologicalSortResult(
            List<Integer> order,
            boolean hasCycle
    ) {
        this.order = List.copyOf(order);
        this.hasCycle = hasCycle;
    }

    public List<Integer> getOrder() {
        return order;
    }

    public boolean hasCycle() {
        return hasCycle;
    }
}