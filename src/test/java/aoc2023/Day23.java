package aoc2023;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Day23 {

    enum Direction {
        UP,
        RIGHT,
        DOWN,
        LEFT
    }

    sealed interface Tile {
    }

    record Path() implements Tile {
    }

    record Slope(Direction direction) implements Tile {
    }

    record State(Pos pos, int distance, Set<Pos> visited) {
    }

    record Edge(Pos pos, int distance) {
    }

    record FindEdgeState(Pos start, Pos pos, int distance) {
    }

    static long solve1(String input) {
        var map = parse(input);
        return findLongest1(map);
    }

    static long findLongest1(Map<Pos, Tile> map) {
        var bounds = PosBounds.calculate(map.keySet());
        var start = map.keySet().stream().filter(p -> p.y() == bounds.minY()).findFirst().get();
        var end = map.keySet().stream().filter(p -> p.y() == bounds.maxY()).findFirst().get();

        var queue = new LinkedList<State>();
        queue.add(new State(start, 0, Set.of()));

        var longest = 0;

        while (!queue.isEmpty()) {
            var state = queue.removeFirst();
            for (Pos neighbor : state.pos().neighbors()) {
                if (state.visited().contains(neighbor)) {
                    continue;
                }

                if (neighbor.equals(end)) {
                    var distance = state.distance() + 1;
                    if (distance > longest) {
                        longest = distance;
                    }
                    break;
                }

                var tile = map.get(neighbor);
                if (tile == null) {
                    continue;
                }

                switch (tile) {
                    case Path path -> {
                        var visited = new HashSet<>(state.visited());
                        visited.add(state.pos());
                        queue.addFirst(new State(neighbor, state.distance() + 1, visited));
                    }
                    case Slope(var direction) -> {
                        var afterSlope = switch (direction) {
                            case UP -> neighbor.plus(new Pos(0, -1));
                            case RIGHT -> neighbor.plus(new Pos(1, 0));
                            case DOWN -> neighbor.plus(new Pos(0, 1));
                            case LEFT -> neighbor.plus(new Pos(-1, 0));
                        };
                        // Can't go against the direction
                        if (!afterSlope.equals(state.pos())) {
                            var visited = new HashSet<>(state.visited());
                            visited.add(state.pos());
                            visited.add(neighbor);
                            queue.addFirst(new State(afterSlope, state.distance() + 2, visited));
                        }
                    }
                }

            }
        }

        return longest;
    }

    static long solve2(String input) {
        var map = parse(input);
        return findLongest2(map);
    }

    static long findLongest2(Map<Pos, Tile> map) {
        var graph = new LinkedHashMap<Pos, List<Edge>>();

        var bounds = PosBounds.calculate(map.keySet());
        var start = map.keySet().stream().filter(p -> p.y() == bounds.minY()).findFirst().get();
        var end = map.keySet().stream().filter(p -> p.y() == bounds.maxY()).findFirst().get();

        var visited = new HashSet<Pos>();

        var queue = new LinkedList<FindEdgeState>();
        queue.add(new FindEdgeState(start, start, 0));
        while (!queue.isEmpty()) {
            var state = queue.removeLast();
            var isJunction = state.pos().neighbors().stream().filter(map::containsKey).count() > 2;
            if ((isJunction || state.pos().equals(end)) && !state.pos().equals(state.start())) {
                graph.computeIfAbsent(state.start(), k -> new ArrayList<>()).add(new Edge(state.pos(), state.distance()));
                graph.computeIfAbsent(state.pos(), k -> new ArrayList<>()).add(new Edge(state.start(), state.distance()));
            }

            if (!visited.add(state.pos())) {
                continue;
            }

            for (Pos neighbor : state.pos().neighbors()) {
                if (!map.containsKey(neighbor)) {
                    continue;
                }

                queue.add(new FindEdgeState(isJunction ? state.pos() : state.start(), neighbor, isJunction ? 1 : state.distance() + 1));
            }
        }

        System.out.println("Graph: " + graph);

        return findLongestInGraph(start, 0, graph, Set.of(start), end);
    }

    static long findLongestInGraph(Pos pos, long distance, Map<Pos, List<Edge>> graph, Set<Pos> used, Pos end) {
        var max = 0L;
        for (Edge edge : graph.get(pos)) {
            // Could use a BitSet of node indices instead. Currently takes about 13 seconds to run with this.
            if (used.contains(edge.pos())) {
                continue;
            }

            var newDistance = distance + edge.distance();
            if (!edge.pos().equals(end)) {
                var newUsed = new HashSet<>(used);
                newUsed.add(edge.pos());
                newDistance = findLongestInGraph(edge.pos(), newDistance, graph, newUsed, end);
            }
            max = Math.max(max, newDistance);
        }

        return max;
    }

    static Map<Pos, Tile> parse(String input) {
        return Grids.parse(input, s -> switch (s) {
            case "." -> new Path();
            case ">" -> new Slope(Direction.RIGHT);
            case "v" -> new Slope(Direction.DOWN);
            case "<" -> new Slope(Direction.LEFT);
            case "^" -> new Slope(Direction.UP);
            case "#" -> null;
            default -> throw new IllegalStateException("Unexpected value: " + s);
        });
    }

    @Test
    void example() {
        var s = """
                #.#####################
                #.......#########...###
                #######.#########.#.###
                ###.....#.>.>.###.#.###
                ###v#####.#v#.###.#.###
                ###.>...#.#.#.....#...#
                ###v###.#.#.#########.#
                ###...#.#.#.......#...#
                #####.#.#.#######.#.###
                #.....#.#.#.......#...#
                #.#####.#.#.#########v#
                #.#...#...#...###...>.#
                #.#.#v#######v###.###v#
                #...#.>.#...>.>.#.###.#
                #####v#.#.###v#.#.###.#
                #.....#...#...#.#.#...#
                #.#########.###.#.#.###
                #...###...#...#...#.###
                ###.###.#.###v#####v###
                #...#...#.#.>.>.#.>.###
                #.###.###.#.###.#.#v###
                #.....###...###...#...#
                #####################.#
                """;
        assertEquals(94, solve1(s));
        assertEquals(154, solve2(s));
    }

    @Test
    void input() {
        var input = Resources.readString(Resources.class.getResource("/day23.txt"));
        assertEquals(2034, solve1(input));
        assertEquals(6302, solve2(input));
    }
}
