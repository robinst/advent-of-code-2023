package aoc2023;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Day02 {

    static int solve1(String input) {
        // 12 red cubes, 13 green cubes, and 14 blue cubes
        var max = Map.of("red", 12, "green", 13, "blue", 14);
        var result = 0;
        var lines = input.split("\n");
        for (var line : lines) {
            var colonParts = line.split(": ");
            var id = Parsing.numbers(colonParts[0]).get(0);
            var hands = colonParts[1].split("; ");
            var possible = true;
            for (var hand : hands) {
                var commaParts = hand.split(", ");
                for (var commaPart : commaParts) {
                    var numberColor = commaPart.split(" ");
                    var number = Parsing.numbers(numberColor[0]).get(0);
                    var color = numberColor[1];
                    if (number > max.get(color)) {
                        possible = false;
                    }
                }
            }
            if (possible) {
                result += id;
            }
        }
        return result;
    }

    static int solve2(String input) {
        var sum = 0;
        var lines = input.split("\n");
        for (var line : lines) {
            var colonParts = line.split(": ");
            var hands = colonParts[1].split("; ");
            var max = new HashMap<String, Integer>();
            for (var hand : hands) {
                var commaParts = hand.split(", ");
                for (var commaPart : commaParts) {
                    var numberColor = commaPart.split(" ");
                    var number = Parsing.numbers(numberColor[0]).get(0);
                    var color = numberColor[1];
                    max.put(color, Math.max(max.getOrDefault(color, 0), number));
                }
            }
            var power = max.values().stream().reduce(1, (a, b) -> a * b);
            sum += power;
        }
        return sum;
    }

    @Test
    void example1() {
        var s = """
                Game 1: 3 blue, 4 red; 1 red, 2 green, 6 blue; 2 green
                Game 2: 1 blue, 2 green; 3 green, 4 blue, 1 red; 1 green, 1 blue
                Game 3: 8 green, 6 blue, 20 red; 5 blue, 4 red, 13 green; 5 green, 1 red
                Game 4: 1 green, 3 red, 6 blue; 3 green, 6 red; 3 green, 15 blue, 14 red
                Game 5: 6 red, 1 blue, 3 green; 2 blue, 1 red, 2 green
                """;
        assertEquals(8, solve1(s));
    }

    @Test
    void example2() {
        var s = """
                Game 1: 3 blue, 4 red; 1 red, 2 green, 6 blue; 2 green
                Game 2: 1 blue, 2 green; 3 green, 4 blue, 1 red; 1 green, 1 blue
                Game 3: 8 green, 6 blue, 20 red; 5 blue, 4 red, 13 green; 5 green, 1 red
                Game 4: 1 green, 3 red, 6 blue; 3 green, 6 red; 3 green, 15 blue, 14 red
                Game 5: 6 red, 1 blue, 3 green; 2 blue, 1 red, 2 green              
                """;
        assertEquals(2286, solve2(s));
    }

    @Test
    void input() {
        var input = Resources.readString(Resources.class.getResource("/day02.txt"));
        assertEquals(2256, solve1(input));
        assertEquals(0, solve2(input));
    }
}
