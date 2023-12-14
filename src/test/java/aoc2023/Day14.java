package aoc2023;

import org.junit.jupiter.api.Test;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Day14 {

    enum Direction {
        NORTH,
        WEST,
        SOUTH,
        EAST;

        boolean isVertical() {
            return this == NORTH || this == SOUTH;
        }
    }

    enum Rock {
        ROUND,
        CUBE
    }

    static long solve1(String input) {
        var map = parse(input);
        var bounds = PosBounds.calculate(map.keySet());

        tilt(map, bounds, Direction.NORTH);
        return calculateLoad(map, bounds);
    }

    static long solve2(String input) {
        var map = parse(input);
        var bounds = PosBounds.calculate(map.keySet());

        var states = new HashMap<BitSet, Integer>();
        states.put(captureState(map, bounds), 0);
        var maxCycles = 1000000000;
        var jumped = false;
        for (int cycle = 0; cycle < maxCycles; cycle++) {
            System.out.println("Cycle " + cycle);
            runCycle(map, bounds);
            if (!jumped) {
                var state = captureState(map, bounds);
                var previousCycle = states.get(state);
                if (previousCycle != null) {
                    System.out.println("Found repetition at cycle " + cycle + " with previous cycle " + previousCycle + ", fast forwarding");
                    int loop = cycle - previousCycle;
                    int left = (maxCycles - cycle) % loop;
                    cycle = maxCycles - left;
                    jumped = true;
                } else {
                    states.put(state, cycle);
                }
            }
        }

        return calculateLoad(map, bounds);
    }

    static void print(Map<Pos, Rock> map, PosBounds bounds) {
        for (int y = 0; y < bounds.height(); y++) {
            for (int x = 0; x < bounds.width(); x++) {
                var pos = new Pos(x, y);
                var rock = map.get(pos);
                if (rock == Rock.ROUND) {
                    System.out.print("O");
                } else if (rock == Rock.CUBE) {
                    System.out.print("#");
                } else {
                    System.out.print(".");
                }
            }
            System.out.println();
        }
        System.out.println();
    }

    static BitSet captureState(Map<Pos, Rock> map, PosBounds bounds) {
        var state = new BitSet();
        for (Map.Entry<Pos, Rock> entry : map.entrySet()) {
            if (entry.getValue() == Rock.ROUND) {
                var pos = entry.getKey();
                state.set(pos.y() * bounds.width() + pos.x());
            }
        }
        return state;
    }

    static void runCycle(Map<Pos, Rock> map, PosBounds bounds) {
        tilt(map, bounds, Direction.NORTH);
//        print(map, bounds);
        tilt(map, bounds, Direction.WEST);
//        print(map, bounds);
        tilt(map, bounds, Direction.SOUTH);
//        print(map, bounds);
        tilt(map, bounds, Direction.EAST);
//        print(map, bounds);
    }

    static void tilt(Map<Pos, Rock> map, PosBounds bounds, Direction dir) {
        var outerLimit = bounds.height();
        var innerLimit = bounds.width();
        if (!dir.isVertical()) {
            outerLimit = bounds.width();
            innerLimit = bounds.height();
        }
        var dirX = 0;
        var dirY = 0;
        switch (dir) {
            case NORTH -> dirY = -1;
            case WEST -> dirX = -1;
            case SOUTH -> dirY = 1;
            case EAST -> dirX = 1;
        }
        for (int outer = 0; outer < outerLimit; outer++) {
            for (int inner = 0; inner < innerLimit; inner++) {
                var pos = dir.isVertical() ?
                        new Pos(inner, dir == Direction.NORTH ? outer : outerLimit - outer - 1) :
                        new Pos(dir == Direction.WEST ? outer : outerLimit - outer - 1, inner);
                var rock = map.get(pos);
                if (rock == Rock.ROUND) {
                    var newPos = pos;
                    for (int i = 1; i <= outer; i++) {
                        var candidate = new Pos(pos.x() + i * dirX, pos.y() + i * dirY);
                        if (map.containsKey(candidate)) {
                            break;
                        }
                        newPos = candidate;
                    }

                    if (newPos != pos) {
                        // Rock has moved, reposition it
                        map.remove(pos);
                        map.put(newPos, rock);
                    }
                }
            }
        }
    }

    static int calculateLoad(Map<Pos, Rock> map, PosBounds bounds) {
        var load = 0;
        for (Map.Entry<Pos, Rock> entry : map.entrySet()) {
            if (entry.getValue() == Rock.ROUND) {
                var pos = entry.getKey();
                load += bounds.height() - pos.y();
            }
        }
        return load;
    }

    static Map<Pos, Rock> parse(String input) {
        return Grids.parse(input, s -> switch (s) {
            case "O" -> Rock.ROUND;
            case "#" -> Rock.CUBE;
            case "." -> null;
            default -> throw new IllegalStateException("Unexpected value: " + s);
        });
    }

    @Test
    void example() {
        var s = """
                O....#....
                O.OO#....#
                .....##...
                OO.#O....O
                .O.....O#.
                O.#..O.#.#
                ..O..#O..O
                .......O..
                #....###..
                #OO..#....
                """;
        assertEquals(136, solve1(s));
        assertEquals(64, solve2(s));
    }

    @Test
    void input() {
        var input = Resources.readString(Resources.class.getResource("/day14.txt"));
        assertEquals(111339, solve1(input));
        assertEquals(93736, solve2(input));
    }
}
