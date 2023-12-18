package aoc2023;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Day18 {

    record Instruction(Direction direction, int steps) {
    }

    enum Direction {
        U,
        R,
        D,
        L;
    }

    record Row(int x, int width, boolean swap) implements Comparable<Row> {
        @Override
        public int compareTo(Row that) {
            return Integer.compare(this.x, that.x);
        }
    }

    static long solve1(String input) {
        var instructions = parse1(input);
        return calculate1(instructions);
    }

    static List<Instruction> parse1(String input) {
        var instructions = new ArrayList<Instruction>();

        var lines = input.split("\n");
        for (var line : lines) {
            var parts = line.split(" ");
            var dir = Direction.valueOf(parts[0]);
            var steps = Parsing.numbers(parts[1]).getFirst();
            instructions.add(new Instruction(dir, steps));
        }
        return instructions;
    }

    static long calculate1(List<Instruction> instructions) {
        var map = new HashSet<Pos>();
        var current = new Pos(0, 0);

        for (Instruction instruction : instructions) {
            var steps = instruction.steps();
            var destination = switch (instruction.direction()) {
                case U -> current.plus(new Pos(0, -steps));
                case R -> current.plus(new Pos(steps, 0));
                case D -> current.plus(new Pos(0, steps));
                case L -> current.plus(new Pos(-steps, 0));
            };
            map.addAll(current.straightLineToIncluding(destination));
            current = destination;
        }

        var bounds = PosBounds.calculate(map);
        var emptyOutside = fillEmptyFromOutside(map, bounds);
        return (long) bounds.width() * bounds.height() - emptyOutside;
    }

    static int fillEmptyFromOutside(Set<Pos> map, PosBounds bounds) {
        var empty = new HashSet<Pos>();
        var visited = new HashSet<Pos>();
        var check = new LinkedList<>(bounds.borderInside());

        while (!check.isEmpty()) {
            var pos = check.removeFirst();
            if (!visited.add(pos)) {
                continue;
            }
            if (map.contains(pos)) {
                // Not empty, stop here
                continue;
            }

            empty.add(pos);
            for (Pos neighbor : pos.neighbors()) {
                if (bounds.contains(neighbor)) {
                    check.add(neighbor);
                }
            }
        }
        return empty.size();
    }

    static long solve2(String input) {
        var instructions = parse2(input);
        return calculate2(instructions);
    }

    static List<Instruction> parse2(String input) {
        var instructions = new ArrayList<Instruction>();

        var lines = input.split("\n");
        for (var line : lines) {
            var parts = line.split(" ");
            var color = parts[2].substring(2, parts[2].length() - 1);
            var steps = HexFormat.fromHexDigits(color.substring(0, 5));
            var s = color.substring(5);
            // direction to dig: 0 means R, 1 means D, 2 means L, and 3 means U.
            var dir = switch (s) {
                case "0" -> Direction.R;
                case "1" -> Direction.D;
                case "2" -> Direction.L;
                case "3" -> Direction.U;
                default -> throw new IllegalStateException("Unexpected value: " + s);
            };
            instructions.add(new Instruction(dir, steps));
        }
        return instructions;
    }

    static long calculate2(List<Instruction> instructions) {
        var current = new Pos(0, 0);
        var rows = new TreeMap<Integer, TreeSet<Row>>();
        for (int i = 0; i < instructions.size(); i++) {
            var instruction = instructions.get(i);
            var steps = instruction.steps();
            var destination = switch (instruction.direction()) {
                case U -> current.plus(new Pos(0, -steps));
                case R -> current.plus(new Pos(steps, 0));
                case D -> current.plus(new Pos(0, steps));
                case L -> current.plus(new Pos(-steps, 0));
            };

            if (current.y() == destination.y()) {
                // Horizontal, insert single wide row
                int x = Math.min(current.x(), destination.x());
                int width = Math.abs(current.x() - destination.x()) + 1;
                var prev = instructions.get(Math.floorMod(i - 1, instructions.size()));
                var next = instructions.get(Math.floorMod(i + 1, instructions.size()));
                rows.computeIfAbsent(current.y(), k -> new TreeSet<>()).add(new Row(x, width, prev.direction() == next.direction()));
            } else {
                // Vertical, insert N rows each 1 wide
                int x = current.x();
                int fromY = Math.min(current.y(), destination.y()) + 1;
                int toY = Math.max(current.y(), destination.y()) - 1;
                for (int y = fromY; y <= toY; y++) {
                    rows.computeIfAbsent(y, k -> new TreeSet<>()).add(new Row(x, 1, true));
                }
            }

            current = destination;
        }

        var result = 0L;

        for (Map.Entry<Integer, TreeSet<Row>> entry : rows.entrySet()) {
            var inside = false;
            Integer previous = null;
            for (Row row : entry.getValue()) {
                if (previous != null && inside) {
                    result += row.x() - previous;
                }
                previous = row.x() + row.width();
                result += row.width();
                if (row.swap()) {
                    inside = !inside;
                }
            }
        }

        return result;
    }

    @Test
    void example() {
        var s = """
                R 6 (#70c710)
                D 5 (#0dc571)
                L 2 (#5713f0)
                D 2 (#d2c081)
                R 2 (#59c680)
                D 2 (#411b91)
                L 5 (#8ceee2)
                U 2 (#caa173)
                L 1 (#1b58a2)
                U 2 (#caa171)
                R 2 (#7807d2)
                U 3 (#a77fa3)
                L 2 (#015232)
                U 2 (#7a21e3)
                """;
        var instructions = parse1(s);
        assertEquals(62, calculate1(instructions));
        assertEquals(62, calculate2(instructions));
        assertEquals(952408144115L, solve2(s));
    }

    @Test
    void input() {
        var input = Resources.readString(Resources.class.getResource("/day18.txt"));
        assertEquals(95356, solve1(input));
        assertEquals(92291468914147L, solve2(input));
    }
}
