package aoc2023;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Day06 {

    static int solve1(String input) {
        var lines = input.split("\n");
        var times = Parsing.numbers(lines[0]);
        var distances = Parsing.numbers(lines[1]);
        var result = 1;

        for (int raceIndex = 0; raceIndex < times.size(); raceIndex++) {
            var time = times.get(raceIndex);
            var record = distances.get(raceIndex);

            result *= beatRecord(time, record);
        }

        return result;
    }


    static int solve2(String input) {
        var lines = input.split("\n");
        var times = Parsing.numbersLong(lines[0].replaceAll("\\D", ""));
        var distances = Parsing.numbersLong(lines[1].replaceAll("\\D", ""));

        var time = times.get(0);
        var record = distances.get(0);

        return beatRecord(time, record);
    }

    private static int beatRecord(long time, long record) {
        var beat = 0;

        for (int chargeTime = 1; chargeTime < time - 1; chargeTime++) {
            var distance = (time - chargeTime) * chargeTime;
            if (distance > record) {
                beat++;
            }
        }
        return beat;
    }


    @Test
    void example() {
        var s = """
                Time:      7  15   30
                Distance:  9  40  200
                """;
        assertEquals(288, solve1(s));
        assertEquals(71503, solve2(s));
    }

    @Test
    void input() {
        var input = Resources.readString(Resources.class.getResource("/day06.txt"));
        assertEquals(2374848, solve1(input));
        assertEquals(39132886, solve2(input));
    }
}
