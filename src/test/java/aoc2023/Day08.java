package aoc2023;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Day08 {

    record Puzzle(List<String> steps, Map<String, Node> nodes) {
    }

    record Node(String left, String right) {
    }

    static long solve1(String input) {
        var puzzle = parse(input);
        var steps = puzzle.steps();
        var map = puzzle.nodes();

        var current = "AAA";
        var result = 0;
        var stepIndex = 0;

        while (true) {
            var step = steps.get(stepIndex);
            var node = map.get(current);
            switch (step) {
                case "L" -> current = node.left;
                case "R" -> current = node.right;
                default -> throw new IllegalStateException("Unknown step " + step);
            }
            result++;

            if (current.equals("ZZZ")) {
                break;
            }

            stepIndex = (stepIndex + 1) % steps.size();
        }

        return result;
    }

    static class State {
        String current;
        final Map<String, Long> seenAt = new HashMap<>();
        long loopLength = 0;
        long loopStart = 0;

        public State(String current) {
            this.current = current;
        }
    }

    static long solve2(String input) {
        var puzzle = parse(input);
        var steps = puzzle.steps();
        var map = puzzle.nodes();

        var states = new ArrayList<State>();
        map.keySet().stream().filter(s -> s.endsWith("A")).forEach(s -> {
            states.add(new State(s));
        });

        var result = 0L;
        var stepIndex = 0;

        while (true) {
            var step = steps.get(stepIndex);
            for (var state : states) {
                var node = map.get(state.current);
                switch (step) {
                    case "L" -> state.current = node.left;
                    case "R" -> state.current = node.right;
                    default -> throw new IllegalStateException("Unknown step " + step);
                }
            }

            result++;

            if (stepIndex == steps.size() - 1) {
                for (State state : states) {
                    if (state.loopLength > 0 || !state.current.endsWith("Z")) {
                        continue;
                    }
                    var seenAt = state.seenAt.get(state.current);
                    if (seenAt != null) {
                        state.loopLength = result - seenAt;
                        state.loopStart = seenAt;
                    } else {
                        state.seenAt.put(state.current, result);
                    }
                }

                if (states.stream().allMatch(s -> s.loopLength > 0)) {
                    System.out.println("Found all periods");

                    var period = states.getFirst().loopLength;
                    long i = states.getFirst().loopStart + period;
                    while (true) {
                        long offset = i;
                        if (states.stream().allMatch(s -> (offset - s.loopStart) % s.loopLength == 0)) {
                            return i;
                        }
                        i += period;
                    }
                }
            }

            if (states.stream().allMatch(s -> s.current.endsWith("Z"))) {
                break;
            }

            stepIndex = (stepIndex + 1) % steps.size();
        }

        return result;
    }

    private static Puzzle parse(String input) {
        var blocks = input.split("\n\n");
        var steps = List.of(blocks[0].split(""));
        var nodes = blocks[1].split("\n");
        var map = new HashMap<String, Node>();
        for (String node : nodes) {
            var parts = node.split(" = ");
            var key = parts[0];
            var leftRight = parts[1].replaceAll("[()]", "").split(", ");
            map.put(key, new Node(leftRight[0], leftRight[1]));
        }
        return new Puzzle(steps, map);
    }

    @Test
    void example1() {
        var s = """
                RL
                                
                AAA = (BBB, CCC)
                BBB = (DDD, EEE)
                CCC = (ZZZ, GGG)
                DDD = (DDD, DDD)
                EEE = (EEE, EEE)
                GGG = (GGG, GGG)
                ZZZ = (ZZZ, ZZZ)
                """;
        assertEquals(2, solve1(s));
    }

    @Test
    void example2() {
        var s = """
                LR
                                
                11A = (11B, XXX)
                11B = (XXX, 11Z)
                11Z = (11B, XXX)
                22A = (22B, XXX)
                22B = (22C, 22C)
                22C = (22Z, 22Z)
                22Z = (22B, 22B)
                XXX = (XXX, XXX)
                """;
        assertEquals(6, solve2(s));
    }

    @Test
    void input() {
        var input = Resources.readString(Resources.class.getResource("/day08.txt"));
        assertEquals(13019, solve1(input));
        assertEquals(13524038372771L, solve2(input));
    }
}
