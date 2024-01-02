package aoc2023;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Graphviz {


    record Edge(String from, String to, String label) {
    }

    record Node(String name, String label) {
    }

    private final boolean directed;
    private final List<Node> nodes = new ArrayList<>();
    private final List<Edge> edges = new ArrayList<>();
    private boolean strict;
    private String rankdir;

    public static Graphviz directed() {
        return new Graphviz(true);
    }

    public static Graphviz undirected() {
        return new Graphviz(false);
    }

    public Graphviz strict() {
        this.strict = true;
        return this;
    }

    /**
     * @param rankdir e.g. "LR" for left to right rank direction
     */
    public Graphviz rankdir(String rankdir) {
        this.rankdir = rankdir;
        return this;
    }

    private Graphviz(boolean directed) {
        this.directed = directed;
    }

    public void node(String name, String label) {
        nodes.add(new Node(name, label));
    }

    public void edge(String from, String to) {
        edges.add(new Edge(from, to, null));
    }

    public String build() {
        var sb = new StringBuilder();
        if (strict) {
            sb.append("strict ");
        }
        sb.append(directed ? "digraph " : "graph ");
        sb.append("{\n");

        if (rankdir != null) {
            sb.append("  ");
            sb.append("rankdir=\"");
            sb.append(rankdir);
            sb.append("\"");
            sb.append("\n");
        }

        for (Node node : nodes) {
            sb.append("  ");
            sb.append(node.name());
            attributes(sb, node.label());
            sb.append("\n");
        }

        for (Edge edge : edges) {
            sb.append("  ");
            sb.append(edge.from());
            sb.append(directed ? " -> " : " -- ");
            sb.append(edge.to());
            attributes(sb, edge.label());
            sb.append("\n");
        }
        sb.append("}\n");
        return sb.toString();
    }

    public void generate(String filename) {
        // dot -Tsvg -oday25.svg day25.dot
        var source = build();
        var dotFile = Paths.get(filename + ".dot");
        var svgFile = Paths.get(filename + ".svg");
        try {
            Files.writeString(dotFile, source);
            var process = Runtime.getRuntime().exec(new String[]{"dot", "-Tsvg", "-o", svgFile.toString(), dotFile.toString()});
            var exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Process didn't finish successfully, exit code: " + exitCode);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void attributes(StringBuilder sb, String label) {
        if (label != null) {
            sb.append(" [label=\"");
            sb.append(label);
            sb.append("\"]");
        }
    }
}
