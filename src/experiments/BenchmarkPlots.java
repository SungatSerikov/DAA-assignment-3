package experiments;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

final class BenchmarkPlots {
    private static final double LEFT = 105;
    private static final double TOP = 95;
    private static final double WIDTH = 880;
    private static final double HEIGHT = 420;
    private static final String[] COLORS = {"#2459a6", "#d46918", "#18816a", "#9346a2", "#c73c4a", "#63702a"};
    private static final String[] DASHES = {"", "9 4", "3 4", "11 3 2 3", "7 3 2 3", "2 3"};

    private BenchmarkPlots() {
    }

    static void write(Path file, List<BenchmarkResult> results, boolean byVertices) throws IOException {
        String variable = byVertices ? "vertices" : "edges";
        double xMin = xValue(results.get(0), byVertices);
        double xMax = xMin;
        double measuredMaxMs = 0;
        List<Integer> xTicks = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        for (BenchmarkResult result : results) {
            int x = xValue(result, byVertices);
            xMin = Math.min(xMin, x);
            xMax = Math.max(xMax, x);
            measuredMaxMs = Math.max(measuredMaxMs, result.averageNs / 1_000_000.0);
            if (!xTicks.contains(x)) {
                xTicks.add(x);
            }
            String seriesLabel = label(result);
            if (!labels.contains(seriesLabel)) {
                labels.add(seriesLabel);
            }
        }
        Collections.sort(xTicks);
        double yMaxMs = axisMaximum(measuredMaxMs);
        StringBuilder svg = new StringBuilder();
        svg.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"1100\" height=\"760\" viewBox=\"0 0 1100 760\" role=\"img\" aria-labelledby=\"title description\"");
        svg.append(" data-x-variable=\"").append(variable).append("\" data-x-scale=\"log10\" data-y-scale=\"linear\"")
                .append(" data-plot-left=\"").append(LEFT).append("\" data-plot-top=\"").append(TOP)
                .append("\" data-plot-width=\"").append(WIDTH).append("\" data-plot-height=\"").append(HEIGHT)
                .append("\" data-y-max-ms=\"").append(yMaxMs).append("\">\n");
        svg.append("<title id=\"title\">Measured graph algorithm runtime versus ").append(variable).append("</title>\n")
                .append("<desc id=\"description\">Six algorithm and graph scenarios. Points show arithmetic means of five timed calls after ten warmups. The x axis uses a logarithmic scale; the y axis is linear in milliseconds, starting at zero. Straight lines connect measured points. Both graph vertices and edges vary between sizes.</desc>\n")
                .append("<rect width=\"1100\" height=\"760\" fill=\"#ffffff\"/>\n")
                .append("<g font-family=\"Arial, Helvetica, sans-serif\" fill=\"#202c3a\">\n");
        text(svg, 55, 38, 26, "start", "Graph algorithm runtime vs " + variable);
        text(svg, 55, 65, 14, "start", "Seed 42 | 10 warmups | mean of 5 complete calls | operation counting excluded");
        for (int i = 0; i <= 5; i++) {
            double value = yMaxMs * i / 5;
            double y = TOP + HEIGHT * (1.0 - i / 5.0);
            line(svg, LEFT, y, LEFT + WIDTH, y, "#e0e6ed", 1, "");
            text(svg, LEFT - 12, y + 5, 13, "end", tick(value));
        }
        for (double value : xTicks) {
            double x = xPosition(value, xMin, xMax);
            line(svg, x, TOP, x, TOP + HEIGHT, "#e0e6ed", 1, "");
            text(svg, x, TOP + HEIGHT + 27, 14, "middle", String.format(Locale.ROOT, "%.0f", value));
        }
        line(svg, LEFT, TOP, LEFT, TOP + HEIGHT, "#718095", 1.5, "");
        line(svg, LEFT, TOP + HEIGHT, LEFT + WIDTH, TOP + HEIGHT, "#718095", 1.5, "");
        text(svg, LEFT + WIDTH / 2, 570, 16, "middle", (byVertices ? "Vertices" : "Edges") + " (log scale)");
        svg.append("<text x=\"28\" y=\"305\" font-size=\"16\" text-anchor=\"middle\" transform=\"rotate(-90 28 305)\">Mean runtime (ms)</text>\n");
        for (int seriesIndex = 0; seriesIndex < labels.size(); seriesIndex++) {
            String seriesLabel = labels.get(seriesIndex);
            List<BenchmarkResult> rows = new ArrayList<>();
            for (BenchmarkResult result : results) {
                if (label(result).equals(seriesLabel)) {
                    rows.add(result);
                }
            }
            String color = COLORS[seriesIndex];
            String dash = DASHES[seriesIndex];
            svg.append("<polyline fill=\"none\" stroke=\"").append(color).append("\" stroke-width=\"2.5\" stroke-dasharray=\"").append(dash).append("\" points=\"");
            for (BenchmarkResult row : rows) {
                svg.append(number(xPosition(xValue(row, byVertices), xMin, xMax))).append(',')
                        .append(number(yPosition(row.averageNs, yMaxMs))).append(' ');
            }
            svg.append("\"/>\n");
            for (BenchmarkResult row : rows) {
                svg.append("<circle class=\"data-point\" cx=\"").append(number(xPosition(xValue(row, byVertices), xMin, xMax)))
                        .append("\" cy=\"").append(number(yPosition(row.averageNs, yMaxMs)))
                        .append("\" r=\"4.5\" fill=\"").append(color).append("\" stroke=\"#ffffff\" stroke-width=\"1.3\"")
                        .append(" data-algorithm=\"").append(row.algorithm).append("\" data-graph-type=\"").append(row.graphType)
                        .append("\" data-vertices=\"").append(row.vertices).append("\" data-edges=\"").append(row.edges)
                        .append("\" data-average-ns=\"").append(row.averageNs).append("\"><title>")
                        .append(label(row)).append(": V=").append(row.vertices).append(", E=").append(row.edges)
                        .append(", mean=").append(String.format(Locale.ROOT, "%.6f", row.averageNs / 1_000_000.0))
                        .append(" ms (min=").append(String.format(Locale.ROOT, "%.6f", row.minNs / 1_000_000.0))
                        .append(", max=").append(String.format(Locale.ROOT, "%.6f", row.maxNs / 1_000_000.0)).append(")</title></circle>\n");
            }
            double legendX = 85 + (seriesIndex % 2) * 480;
            double legendY = 610 + (seriesIndex / 2) * 29;
            line(svg, legendX, legendY - 5, legendX + 32, legendY - 5, color, 2.5, dash);
            text(svg, legendX + 43, legendY, 14, "start", seriesLabel);
        }
        text(svg, 55, 712, 13, "start", "Measured sizes: (V, E) = (10, 30), (100, 500), (1000, 5000). Both dimensions change together.");
        text(svg, 55, 734, 13, "start", "Points are measured means; straight lines guide the eye. Raw samples and ranges are in tables/. No samples removed.");
        svg.append("</g>\n</svg>\n");
        Files.writeString(file, svg, StandardCharsets.UTF_8);
    }

    private static int xValue(BenchmarkResult row, boolean byVertices) {
        return byVertices ? row.vertices : row.edges;
    }

    private static double xPosition(double value, double minimum, double maximum) {
        return LEFT + WIDTH * Math.log10(value / minimum) / Math.log10(maximum / minimum);
    }

    private static double yPosition(double nanoseconds, double maxMs) {
        return TOP + HEIGHT * (1.0 - nanoseconds / 1_000_000.0 / maxMs);
    }

    private static double axisMaximum(double measuredMaximum) {
        if (measuredMaximum == 0) {
            return 1;
        }
        double unit = Math.pow(10, Math.floor(Math.log10(measuredMaximum)));
        return Math.ceil(measuredMaximum / unit * 1.1) * unit;
    }

    private static String label(BenchmarkResult row) {
        String graphName = "directed cyclic";
        if (row.graphType.equals("connected_undirected")) {
            graphName = "connected undirected";
        } else if (row.graphType.equals("dag")) {
            graphName = "DAG";
        }
        return row.algorithm + " / " + graphName;
    }

    private static String tick(double value) {
        if (value == 0) {
            return "0";
        }
        return String.format(Locale.ROOT, "%.3g", value);
    }

    private static String number(double value) {
        return String.format(Locale.ROOT, "%.3f", value);
    }

    private static void line(StringBuilder svg, double x1, double y1, double x2, double y2, String color, double width, String dash) {
        svg.append("<line x1=\"").append(number(x1)).append("\" y1=\"").append(number(y1))
                .append("\" x2=\"").append(number(x2)).append("\" y2=\"").append(number(y2))
                .append("\" stroke=\"").append(color).append("\" stroke-width=\"").append(width)
                .append("\" stroke-dasharray=\"").append(dash).append("\"/>\n");
    }

    private static void text(StringBuilder svg, double x, double y, int size, String anchor, String value) {
        svg.append("<text x=\"").append(number(x)).append("\" y=\"").append(number(y))
                .append("\" font-size=\"").append(size).append("\" text-anchor=\"").append(anchor)
                .append("\">").append(value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")).append("</text>\n");
    }
}
