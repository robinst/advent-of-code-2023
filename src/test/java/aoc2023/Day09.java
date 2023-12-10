package aoc2023;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Day09 {

    enum Edge {
        FIRST,
        LAST
    }

    static long solve1(String input) {
        return solve(input, Edge.LAST);
    }

    static long solve2(String input) {
        return solve(input, Edge.FIRST);
    }

    static long solve(String input, Edge last) {
        var lines = input.split("\n");
        return Arrays.stream(lines).mapToLong(line -> extrapolate(Parsing.numbersLong(line), List.of(), last)).sum();
    }

    static long extrapolate(List<Long> sequence, List<Long> edgeNumbers, Edge type) {
        if (sequence.stream().allMatch(n -> n == 0)) {
            // Calculate the end number
            var sum = 0L;
            for (Long number : edgeNumbers.reversed()) {
                switch (type) {
                    case FIRST -> sum = number - sum;
                    case LAST -> sum += number;
                }
            }
            return sum;
        } else {
            var differences = new ArrayList<Long>();
            for (int i = 0; i < sequence.size() - 1; i++) {
                var a = sequence.get(i);
                var b = sequence.get(i + 1);
                var difference = b - a;
                differences.add(difference);
            }
            var newEdgeNumbers = new ArrayList<>(edgeNumbers);
            switch (type) {
                case FIRST -> newEdgeNumbers.add(sequence.getFirst());
                case LAST -> newEdgeNumbers.add(sequence.getLast());
            }
            return extrapolate(differences, newEdgeNumbers, type);
        }
    }

    @Test
    void example() {
        var s = """
                0 3 6 9 12 15
                1 3 6 10 15 21
                10 13 16 21 30 45
                """;
        assertEquals(114, solve1(s));
        assertEquals(2, solve2(s));
    }

    @Test
    void input() {
        var input = Resources.readString(Resources.class.getResource("/day09.txt"));
        assertEquals(1974913025L, solve1(input));
        assertEquals(884L, solve2(input));
    }
}
