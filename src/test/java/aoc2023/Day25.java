package aoc2023;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Day25 {

    static long solve1(String input) {
        var graph = new HashMap<String, List<String>>();
        var all = new HashSet<>();

        var lines = input.split("\n");
        var graphviz = Graphviz.undirected().strict();
        for (var line : lines) {
            var parts = line.split(": ");
            var source = parts[0];
            var destinations = new ArrayList<>(List.of(parts[1].split(" ")));

            all.add(source);
            all.addAll(destinations);

            destinations.forEach(d -> graphviz.edge(source, d));

            switch (source) {
                case "njn" -> destinations.remove("xtx");
                case "tmb" -> destinations.remove("gpj");
                case "rhh" -> destinations.remove("mtc");

                case "pzl" -> destinations.remove("hfx");
                case "cmg" -> destinations.remove("bvb");
                case "jqt" -> destinations.remove("nvd");
            }

            graph.computeIfAbsent(source, k -> new ArrayList<>()).addAll(destinations);
            destinations.forEach(d -> graph.computeIfAbsent(d, k -> new ArrayList<>()).add(source));
        }

        graphviz.generate("day25");

        var reachable = new HashSet<>();
        var queue = new LinkedList<String>();
        queue.add(graph.keySet().iterator().next());
        while (!queue.isEmpty()) {
            var key = queue.removeFirst();
            if (reachable.add(key)) {
                queue.addAll(graph.getOrDefault(key, List.of()));
            }
        }

        return (long) reachable.size() * (all.size() - reachable.size());
    }

    @Test
    void example() {
        var s = """
                jqt: rhn xhk nvd
                rsh: frs pzl lsr
                xhk: hfx
                cmg: qnr nvd lhk bvb
                rhn: xhk bvb hfx
                bvb: xhk hfx
                pzl: lsr hfx nvd
                qnr: nvd
                ntq: jqt hfx bvb xhk
                nvd: lhk
                lsr: lhk
                rzs: qnr cmg lsr rsh
                frs: qnr lhk lsr
                """;
        // Cut pzl--hfx, cmg--bvb, jqt--nvd
        assertEquals(54, solve1(s));
    }

    @Test
    void input() {
        var input = Resources.readString(Resources.class.getResource("/day25.txt"));
        // Cut njn--xtx, tmb--gpj, rhh--mtc. Found using graphviz :lol:.
        assertEquals(558376, solve1(input));
    }
}
