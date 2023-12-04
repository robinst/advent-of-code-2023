package aoc2023;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Day04 {

    static int solve1(String input) {
        var sum = 0;
        var lines = input.split("\n");
        for (var line : lines) {
            var parts = line.split(": ");
            var sides = parts[1].split(" \\| ");
            var winners = Parsing.numbers(sides[0]);
            var scratched = Parsing.numbers(sides[1]);
            var matching = new HashSet<>(winners);
            matching.retainAll(scratched);
            if (!matching.isEmpty()) {
                sum += (int) Math.pow(2, matching.size() - 1);
            }
        }
        return sum;
    }

    static int solve2(String input) {
        var cardWins = new LinkedHashMap<Integer, Integer>();
        var cardCounts = new HashMap<Integer, Integer>();

        var lines = input.split("\n");
        for (int i = 0; i < lines.length; i++) {
            var line = lines[i];
            var parts = line.split(": ");
            var sides = parts[1].split(" \\| ");
            var winners = Parsing.numbers(sides[0]);
            var scratched = Parsing.numbers(sides[1]);
            var matching = new HashSet<>(winners);
            matching.retainAll(scratched);

            cardWins.put(i, matching.size());
            cardCounts.put(i, 1);
        }

        for (var entry : cardWins.entrySet()) {
            var cardIndex = entry.getKey();
            var count = cardCounts.get(cardIndex);
            var winningNumbers = entry.getValue();

            if (winningNumbers > 0) {
                for (int i = cardIndex + 1; i <= cardIndex + winningNumbers; i++) {
                    cardCounts.put(i, cardCounts.get(i) + count);
                }
            }
        }

        return cardCounts.values().stream().mapToInt(Integer::intValue).sum();
    }

    @Test
    void example() {
        var s = """
                Card 1: 41 48 83 86 17 | 83 86  6 31 17  9 48 53
                Card 2: 13 32 20 16 61 | 61 30 68 82 17 32 24 19
                Card 3:  1 21 53 59 44 | 69 82 63 72 16 21 14  1
                Card 4: 41 92 73 84 69 | 59 84 76 51 58  5 54 83
                Card 5: 87 83 26 28 32 | 88 30 70 12 93 22 82 36
                Card 6: 31 18 13 56 72 | 74 77 10 23 35 67 36 11
                """;
        assertEquals(13, solve1(s));
        assertEquals(30, solve2(s));
    }

    @Test
    void input() {
        var input = Resources.readString(Resources.class.getResource("/day04.txt"));
        assertEquals(18619, solve1(input));
        assertEquals(8063216, solve2(input));
    }
}
