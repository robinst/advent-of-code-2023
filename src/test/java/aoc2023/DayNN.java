package aoc2023;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DayNN {

    static long solve1(String input) {
        var result = 0L;
        return result;
    }

    static long solve2(String input) {
        var result = 0L;
        return result;
    }

    @Test
    void example() {
        var s = """
                
                """;
        assertEquals(0, solve1(s));
        assertEquals(0, solve2(s));
    }

    @Test
    void input() {
        var input = Resources.readString(Resources.class.getResource("/dayNN.txt"));
        assertEquals(0, solve1(input));
        assertEquals(0, solve2(input));
    }
}
