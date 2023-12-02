package aoc2023;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Day01 {

    static int solve1(String input) {
        var sum = 0;
        for (var line : input.split("\n")) {
            var digits = Arrays.stream(line.split(""))
                    .filter(s -> Character.isDigit(s.charAt(0)))
                    .map(Integer::parseInt)
                    .toList();
            sum += digits.get(0) * 10 + digits.get(digits.size() - 1);
        }
        return sum;
    }

    static int solve2(String input) {
        // There are overlapping digits, like "twone", so we need to use a lookahead instead of a simple group.
        var pattern = Pattern.compile("(?=(\\d|one|two|three|four|five|six|seven|eight|nine))");
        var sum = 0;
        for (var line : input.split("\n")) {
            var digits = pattern.matcher(line).results().map(r -> switch (r.group(1)) {
                case "1", "one" -> 1;
                case "2", "two" -> 2;
                case "3", "three" -> 3;
                case "4", "four" -> 4;
                case "5", "five" -> 5;
                case "6", "six" -> 6;
                case "7", "seven" -> 7;
                case "8", "eight" -> 8;
                case "9", "nine" -> 9;
                default -> throw new IllegalStateException("Unexpected value: " + r.group());
            }).toList();
            sum += digits.get(0) * 10 + digits.get(digits.size() - 1);
        }
        return sum;
    }

    @Test
    void example1() {
        var s = """
                1abc2
                pqr3stu8vwx
                a1b2c3d4e5f
                treb7uchet
                """;
        assertEquals(142, solve1(s));
    }

    @Test
    void example2() {
        var s = """
                two1nine
                eightwothree
                abcone2threexyz
                xtwone3four
                4nineeightseven2
                zoneight234
                7pqrstsixteen
                """;
        assertEquals(281, solve2(s));
    }

    @Test
    void input() {
        var input = Resources.readString(Resources.class.getResource("/day01.txt"));
        assertEquals(54667, solve1(input));
        assertEquals(54203, solve2(input));
    }
}
