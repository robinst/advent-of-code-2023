package aoc2023;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Day11 {

    static long solve1(String input) {
        return solve(input, 2);
    }

    static long solve2(String input) {
        return solve(input, 1000000);
    }

    private static long solve(String input, int factor) {
        var lines = input.split("\n");
        var width = 0;
        var map = new HashSet<Pos>();
        var galaxyRows = new TreeSet<Integer>();
        var galaxyColumns = new TreeSet<Integer>();
        for (int y = 0; y < lines.length; y++) {
            var line = lines[y];
            var cells = line.split("");
            if (width == 0) {
                width = cells.length;
            }
            for (int x = 0; x < cells.length; x++) {
                var cell = cells[x];
                if (cell.equals("#")) {
                    galaxyRows.add(y);
                    galaxyColumns.add(x);
                    map.add(new Pos(x, y));
                }
            }
        }

        // Expand
        var expandedMap = new ArrayList<Pos>();
        for (Pos pos : map) {
            var shiftX = (pos.x() - galaxyColumns.headSet(pos.x()).size()) * (factor - 1);
            var shiftY = (pos.y() - galaxyRows.headSet(pos.y()).size()) * (factor - 1);
            expandedMap.add(new Pos(pos.x() + shiftX, pos.y() + shiftY));
        }

        var result = 0L;
        for (int i = 0; i < expandedMap.size(); i++) {
            var pos1 = expandedMap.get(i);
            for (int j = i + 1; j < expandedMap.size(); j++) {
                var pos2 = expandedMap.get(j);

                result += pos1.distance(pos2);
            }
        }
        return result;
    }

    @Test
    void example() {
        var s = """
                ...#......
                .......#..
                #.........
                ..........
                ......#...
                .#........
                .........#
                ..........
                .......#..
                #...#.....
                """;
        assertEquals(374, solve1(s));
        assertEquals(8410, solve(s, 100));
    }

    @Test
    void input() {
        var input = Resources.readString(Resources.class.getResource("/day11.txt"));
        assertEquals(9627977L, solve1(input));
        assertEquals(644248339497L, solve2(input));
    }
}
