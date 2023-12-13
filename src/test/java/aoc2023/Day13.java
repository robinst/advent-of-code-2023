package aoc2023;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Day13 {

    record Pattern(List<BitSet> rows, List<BitSet> columns) {
    }

    static List<Integer> solve1(String input) {
        var blocks = input.split("\n\n");

        var result = new ArrayList<Integer>();
        for (String block : blocks) {
            var pattern = parse(block);
            var reflections = findReflections(pattern.columns, pattern.rows);
            if (reflections.isEmpty()) {
                throw new IllegalStateException("Should always find either a row or a column");
            }
            result.add(reflections.getFirst());
        }
        return result;
    }

    static long solve2(String input, List<Integer> previousReflections) {
        var blocks = input.split("\n\n");

        var result = 0L;
        for (int i = 0; i < blocks.length; i++) {
            var block = blocks[i];
            var pattern = parse(block);
            var previousReflection = previousReflections.get(i);
            result += findNewReflection(pattern, previousReflection, i);
        }
        return result;
    }

    static int findNewReflection(Pattern pattern, int previousReflection, int patternIndex) {
        var columns = pattern.columns;
        var rows = pattern.rows;

        for (int y = 0; y < rows.size(); y++) {
            for (int x = 0; x < columns.size(); x++) {
                columns.get(x).flip(y);
                rows.get(y).flip(x);
                var reflections = findReflections(columns, rows);
                if (!reflections.isEmpty()) {
                    for (Integer reflection : reflections) {
                        if (!Objects.equals(reflection, previousReflection)) {
                            return reflection;
                        }
                    }
                }
                columns.get(x).flip(y);
                rows.get(y).flip(x);
            }
        }
        throw new IllegalStateException("Didn't find a new reflection for " + patternIndex);
    }

    static List<Integer> findReflections(List<BitSet> columns, List<BitSet> rows) {
        var reflections = new ArrayList<Integer>();
        var columnReflections = findReflections(columns);
        if (!columnReflections.isEmpty()) {
            reflections.addAll(columnReflections);
        }
        var rowReflections = findReflections(rows);
        if (!rowReflections.isEmpty()) {
            for (Integer rowReflection : rowReflections) {
                reflections.add(rowReflection * 100);
            }
        }
        return reflections;
    }

    static List<Integer> findReflections(List<BitSet> list) {
        var reflections = new ArrayList<Integer>();
        for (int i = 1; i < list.size(); i++) {
            if (list.get(i).equals(list.get(i - 1))) {
                if (checkReflection(list, i)) {
                    reflections.add(i);
                }
            }
        }
        return reflections;
    }

    static boolean checkReflection(List<BitSet> list, int col) {
        var prev = col - 2;
        var next = col + 1;
        while (prev >= 0 && next < list.size()) {
            if (!list.get(prev).equals(list.get(next))) {
                return false;
            }
            prev--;
            next++;
        }
        return true;
    }

    static Pattern parse(String input) {
        var rows = new ArrayList<BitSet>();
        var columns = new ArrayList<BitSet>();
        var lines = input.split("\n");
        for (int y = 0; y < lines.length; y++) {
            var line = lines[y];
            var cells = line.split("");
            var row = new BitSet();
            for (int x = 0; x < cells.length; x++) {
                if (y == 0) {
                    columns.add(new BitSet());
                }
                var cell = cells[x];
                if (cell.equals("#")) {
                    row.set(x);
                    columns.get(x).set(y);
                }
            }
            rows.add(row);
        }

        return new Pattern(rows, columns);
    }

    @Test
    void example() {
        var s = """
                #.##..##.
                ..#.##.#.
                ##......#
                ##......#
                ..#.##.#.
                ..##..##.
                #.#.##.#.
                                
                #...##..#
                #....#..#
                ..##..###
                #####.##.
                #####.##.
                ..##..###
                #....#..#
                """;
        var solution1 = solve1(s);
        assertEquals(405, solution1.stream().mapToInt(Integer::valueOf).sum());
        assertEquals(400, solve2(s, solution1));
    }

    @Test
    void input() {
        var input = Resources.readString(Resources.class.getResource("/day13.txt"));
        var solution1 = solve1(input);
        assertEquals(30518, solution1.stream().mapToInt(Integer::valueOf).sum());
        assertEquals(36735, solve2(input, solution1));
    }
}
