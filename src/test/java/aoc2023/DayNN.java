package aoc2023;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DayNN {

    static int solve1(String input) {
        var result = 0;
        return result;
    }

    static int solve2(String input) {
        var result = 0;
        return result;
    }

    @Test
    void example1() {
        var s = """
                
                """;
        assertEquals(0, solve1(s));
    }

    @Test
    void example2() {
        var s = """
                
                """;
        assertEquals(0, solve2(s));
    }

    @Test
    void input() {
        var input = Resources.readString(Resources.class.getResource("/dayNN.txt"));
        assertEquals(0, solve1(input));
        assertEquals(0, solve2(input));
    }
}