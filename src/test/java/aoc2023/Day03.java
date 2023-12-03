package aoc2023;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Day03 {

    static int solve1(String input) {
        var result = 0;
        var lines = input.split("\n");
        var map = new LinkedHashMap<Pos, String>();
        for (int row = 0; row < lines.length; row++) {
            var line = lines[row];
            var cells = line.split("");
            for (int col = 0; col < cells.length; col++) {
                var cell = cells[col];
                if (Character.isDigit(cell.charAt(0))) {
                    var start = col;
                    while (col < line.length() && Character.isDigit(line.charAt(col))) {
                        col++;
                    }
                    var number = line.substring(start, col);
                    // TODO: Don't use col, use another variable instead
                    col--;
                    map.put(new Pos(start, row), number);
                } else if (!cell.equals(".")) {
                    map.put(new Pos(col, row), cell);
                }
            }
        }

        for (Map.Entry<Pos, String> entry : map.entrySet()) {
            var value = entry.getValue();
            if (Character.isDigit(value.charAt(0))) {
                var pos = entry.getKey();
                for (int y = pos.y() - 1; y <= pos.y() + 1; y++) {
                    for (int x = pos.x() - 1; x <= pos.x() + value.length(); x++) {
                        if (!Character.isDigit(map.getOrDefault(new Pos(x, y), "0").charAt(0))) {
                            result += Integer.valueOf(value);
                        }
                    }
                }
            }
        }

        return result;
    }

    static int solve2(String input) {
        var result = 0;
        var lines = input.split("\n");
        var map = new LinkedHashMap<Pos, String>();
        for (int row = 0; row < lines.length; row++) {
            var line = lines[row];
            var cells = line.split("");
            for (int col = 0; col < cells.length; col++) {
                var cell = cells[col];
                if (!cell.equals(".")) {
                    map.put(new Pos(col, row), cell);
                }
            }
        }

        for (Map.Entry<Pos, String> entry : map.entrySet()) {
            var value = entry.getValue();
            if (value.equals("*")) {
                var entryPos = entry.getKey();

                var numbers = getNumbers(entryPos, value, map);
                if (numbers.size() == 2) {
                    result += numbers.get(0) * numbers.get(1);
                }
            }
        }

        return result;
    }

    private static List<Integer> getNumbers(Pos entryPos, String value, LinkedHashMap<Pos, String> map) {
        var numbers = new ArrayList<Integer>();

        var checked = new HashSet<Pos>();
        for (int y = entryPos.y() - 1; y <= entryPos.y() + 1; y++) {
            for (int x = entryPos.x() - 1; x <= entryPos.x() + value.length(); x++) {
                var pos = new Pos(x, y);
                if (!checked.contains(pos) && Character.isDigit(map.getOrDefault(pos, " ").charAt(0))) {
                    var start = pos;
                    while (Character.isDigit(map.getOrDefault(start.plus(new Pos(-1, 0)), " ").charAt(0))) {
                        start = start.plus(new Pos(-1, 0));
                    }
                    var end = start;
                    checked.add(start);
                    var number = new StringBuilder(map.get(start));
                    while (Character.isDigit(map.getOrDefault(end.plus(new Pos(1, 0)), " ").charAt(0))) {
                        var next = end.plus(new Pos(1, 0));
                        checked.add(next);
                        number.append(map.get(next));
                        end = next;
                    }

                    numbers.add(Integer.valueOf(number.toString()));
                }
            }
        }
        return numbers;
    }

    @Test
    void example() {
        var s = """
                467..114..
                ...*......
                ..35..633.
                ......#...
                617*......
                .....+.58.
                ..592.....
                ......755.
                ...$.*....
                .664.598..
                """;
        assertEquals(4361, solve1(s));
        assertEquals(467835, solve2(s));
    }

    @Test
    void input() {
        var input = Resources.readString(Resources.class.getResource("/day03.txt"));
        assertEquals(533784, solve1(input));
        assertEquals(78826761, solve2(input));
    }
}
