package aoc2023;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Day12 {

    enum Type {
        OPERATIONAL,
        DAMAGED,
        UNKNOWN,
    }

    static long solve1(String input) {
        var result = 0L;

        var lines = input.split("\n");
        for (String line : lines) {
            var parts = line.split(" ");
            var springs = Arrays.stream(parts[0].split("")).map(Day12::parseType).toList();
            var damaged = Parsing.numbers(parts[1]);
            result += combinations(springs, damaged);
        }

        return result;
    }

    static long solve2(String input) {
        var result = 0L;

        var lines = input.split("\n");
        int lineNumber = 1;
        for (String line : lines) {
            var parts = line.split(" ");
            var springs = Arrays.stream(parts[0].split("")).map(Day12::parseType).toList();
            var damaged = Parsing.numbers(parts[1]);

            var unfoldedSprings = new ArrayList<Type>();
            var unfoldedDamaged = new ArrayList<Integer>();

            var repeats = 5;
            for (int i = 0; i < repeats; i++) {
                unfoldedSprings.addAll(springs);
                if (i < repeats - 1) {
                    unfoldedSprings.add(Type.UNKNOWN);
                }
                unfoldedDamaged.addAll(damaged);
            }

            var start = System.currentTimeMillis();
            result += combinations(unfoldedSprings, unfoldedDamaged);
            System.out.println("Finished " + lineNumber + " in " + (System.currentTimeMillis() - start) + " ms");
            lineNumber++;
        }

        return result;
    }

    static Type parseType(String s) {
        return switch (s) {
            case "." -> Type.OPERATIONAL;
            case "#" -> Type.DAMAGED;
            case "?" -> Type.UNKNOWN;
            default -> throw new IllegalStateException("Unexpected value: " + s);
        };
    }

    record State(int index, int currentRun, int runLength) {
    }

    static long combinations(List<Type> pattern, List<Integer> damagedRuns) {
        var mem = new HashMap<State, Long>();
        return combinations(pattern, damagedRuns, 0, 0, 0, mem);
    }

    static long combinations(List<Type> pattern, List<Integer> damagedRuns, int index, int currentRun, int runLength, Map<State, Long> mem) {
        if (index == pattern.size()) {
            // Check result, if possible return 1
            if ((currentRun == damagedRuns.size() && runLength == 0) || (currentRun == damagedRuns.size() - 1 && damagedRuns.getLast() == runLength)) {
                return 1;
            }
            return 0;
        }

        var state = new State(index, currentRun, runLength);
        var previousResult = mem.get(state);
        if (previousResult != null) {
            return previousResult;
        }

        var candidates = switch (pattern.get(index)) {
            case DAMAGED -> List.of(true);
            case OPERATIONAL -> List.of(false);
            case UNKNOWN -> List.of(true, false);
        };

        var result = 0L;
        for (boolean candidate : candidates) {
            if (candidate) {
                if (currentRun >= damagedRuns.size() || runLength + 1 > damagedRuns.get(currentRun)) {
                    continue;
                }
                result += combinations(pattern, damagedRuns, index + 1, currentRun, runLength + 1, mem);
            } else {
                if (runLength != 0) {
                    if (currentRun >= damagedRuns.size() || damagedRuns.get(currentRun) != runLength) {
                        continue;
                    }
                    result += combinations(pattern, damagedRuns, index + 1, currentRun + 1, 0, mem);
                } else {
                    result += combinations(pattern, damagedRuns, index + 1, currentRun, runLength, mem);
                }
            }
        }

        mem.put(state, result);
        return result;
    }

    @Test
    void example() {
        var s = """
                ???.### 1,1,3
                .??..??...?##. 1,1,3
                ?#?#?#?#?#?#?#? 1,3,1,6
                ????.#...#... 4,1,1
                ????.######..#####. 1,6,5
                ?###???????? 3,2,1
                """;
        assertEquals(1, solve1("???.### 1,1,3"));
        assertEquals(4, solve1(".??..??...?##. 1,1,3"));
        assertEquals(21, solve1(s));
        assertEquals(525152, solve2(s));
    }

    @Test
    void inputPart1() {
        var input = Resources.readString(Resources.class.getResource("/day12.txt"));
        assertEquals(7260, solve1(input));
    }

    @Test
    void inputPart2() {
        var input = Resources.readString(Resources.class.getResource("/day12.txt"));
        assertEquals(1909291258644L, solve2(input));
    }
}
