package aoc2023;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Day21 {

    enum Plot {
        START,
        GARDEN,
        ROCK;

        boolean canGo() {
            return switch (this) {
                case START, GARDEN -> true;
                case ROCK -> false;
            };
        }
    }

    record State(Pos pos, int steps) {
    }

    static long solve1(String input) {
        var map = parse(input);
        return calculate1(map, 64);
    }

    static long calculate1(Map<Pos, Plot> map, int steps) {
        var start = map.entrySet().stream().filter(e -> e.getValue() == Plot.START).findFirst().get().getKey();
        return calculate1(map, start, steps);
    }

    static long calculate1(Map<Pos, Plot> map, Pos start, int steps) {
        var positions = new HashSet<Pos>();
        positions.add(start);

        for (int i = 0; i < steps; i++) {
            var newPositions = new HashSet<Pos>();
            for (Pos position : positions) {
                for (Pos neighbor : position.neighbors()) {
                    var plot = map.get(neighbor);
                    if (plot != null && plot.canGo()) {
                        newPositions.add(neighbor);
                    }
                }
            }
            positions = newPositions;
        }

        return positions.size();
    }

    static long solve2(String input) {
        var map = parse(input);
        return calculate2(map, 26501365);
    }

    private static long calculate2(Map<Pos, Plot> map, int steps) {
        var bounds = PosBounds.calculate(map.keySet());
        var origin = map.entrySet().stream().filter(e -> e.getValue() == Plot.START).findFirst().get().getKey();

        var evenOdd = calculateCompletedTileEvenOdd(map, origin);
        System.out.println("Even, odd: " + Arrays.toString(evenOdd));

        var stepsToBorderUp = origin.y();
        var remainingStepsUp = steps - stepsToBorderUp;
        System.out.println(remainingStepsUp % bounds.height());

        var stepsToBorderRight = bounds.width() - 1 - origin.x();
        var remainingStepsRight = steps - stepsToBorderRight;
        System.out.println(remainingStepsRight % bounds.width());

        var stepsToBorderLeft = origin.x();
        var remainingStepsLeft = steps - stepsToBorderLeft;
        System.out.println(remainingStepsLeft % bounds.width());

        var stepsToBorderDown = bounds.height() - 1 - origin.y();
        var remainingStepsDown = steps - stepsToBorderDown;
        System.out.println(remainingStepsDown % bounds.height());

        var tilesUp = remainingStepsUp / bounds.height();
        var tilesDown = remainingStepsDown / bounds.height();
        System.out.println(tilesUp);
        System.out.println(tilesDown);

        var tilesLeft = remainingStepsLeft / bounds.width();
        var tilesRight = remainingStepsRight / bounds.width();
        System.out.println(tilesLeft);
        System.out.println(tilesRight);

        var cache = new HashMap<State, Long>();
        var result = 0L;

        for (int x = -tilesLeft; x <= tilesRight; x++) {
            if (x % 1000 == 0) {
                System.out.println("x: " + x);
            }
            for (int y = -tilesUp; y <= tilesDown; y++) {
                var tileDistance = Math.abs(x) + Math.abs(y);
                if (tileDistance < tilesUp - 2) {
                    if (tileDistance % 2 == 0) {
                        result += evenOdd[1];
                    } else {
                        result += evenOdd[0];
                    }
                    continue;
                }
                if (tileDistance > tilesUp + 2) {
                    continue;
                }
                var tile = new Pos(x, y);
                var state = getReachable(tile, origin, bounds, steps);
                if (state != null) {
                    var cachedResult = cache.get(state);
                    if (cachedResult != null) {
                        result += cachedResult;
                    } else {
                        var r = calculate1(map, state.pos(), state.steps());
                        cache.put(state, r);
                        result += r;
                    }
                }
            }
        }

        return result;
    }

    static State getReachable(Pos tile, Pos origin, PosBounds bounds, int steps) {
        var localPos = calculateStartingPosWithin(tile, origin, bounds);
        var globalPos = new Pos(tile.x() * bounds.width() + localPos.x(), tile.y() * bounds.height() + localPos.y());
        var stepsToStart = origin.distance(globalPos);
        var stepsLeft = steps - stepsToStart;
        if (stepsLeft >= 0) {
            return new State(localPos, stepsLeft);
        } else {
            return null;
        }
    }

    static Pos calculateStartingPosWithin(Pos tile, Pos origin, PosBounds bounds) {
        int localY;
        if (tile.y() == 0) {
            localY = origin.y();
        } else if (tile.y() < 0) {
            localY = bounds.maxY();
        } else {
            localY = 0;
        }
        int localX;
        if (tile.x() == 0) {
            localX = origin.x();
        } else if (tile.x() < 0) {
            localX = bounds.maxX();
        } else {
            localX = 0;
        }

        return new Pos(localX, localY);
    }

    static long[] calculateCompletedTileEvenOdd(Map<Pos, Plot> map, Pos start) {
        var positions = new HashSet<Pos>();
        positions.add(start);

        var steps = 0;
        var even = 0;
        var odd = 0;

        while (true) {
            var newPositions = new HashSet<Pos>();
            for (Pos position : positions) {
                for (Pos neighbor : position.neighbors()) {
                    var plot = map.get(neighbor);
                    if (plot != null && plot.canGo()) {
                        newPositions.add(neighbor);
                    }
                }
            }
            steps += 1;

            var newEven = steps % 2 == 0 ? newPositions.size() : positions.size();
            var newOdd = steps % 2 == 1 ? newPositions.size() : positions.size();
            if (newEven == even && newOdd == odd) {
                return new long[]{even, odd};
            }
            even = newEven;
            odd = newOdd;
            positions = newPositions;
        }
    }

    static long calculate2Polynomial(Map<Pos, Plot> map, int steps) {
        var bounds = PosBounds.calculate(map.keySet());
        var origin = map.entrySet().stream().filter(e -> e.getValue() == Plot.START).findFirst().get().getKey();

        int width = bounds.width();
        System.out.println(origin.x());
        System.out.println(width);

        var sequence = new ArrayList<Long>();
        sequence.add((long) calculateInfinite(map, bounds, origin, origin.x()));
        sequence.add((long) calculateInfinite(map, bounds, origin, origin.x() + width));
        sequence.add((long) calculateInfinite(map, bounds, origin, origin.x() + width * 2));
        sequence.add((long) calculateInfinite(map, bounds, origin, origin.x() + width * 3));

        // Can actually take these numbers and put them into Wolfram Alpha to get the formula,
        // then calculate with N.
        System.out.println(sequence);
        System.out.println(Day09.extrapolate(sequence, List.of(), Day09.Edge.LAST));

        var n = steps / width - sequence.size() + 1;
        System.out.println("Total steps: " + n);
        for (int i = 0; i < n; i++) {
            if (i % 1000 == 0) {
                System.out.println("i: " + i);
            }
            sequence.add(Day09.extrapolate(sequence, List.of(), Day09.Edge.LAST));
        }

        return sequence.getLast();
    }

    static int calculateInfinite(Map<Pos, Plot> map, PosBounds bounds, Pos start, int steps) {
        var positions = new HashSet<Pos>();
        positions.add(start);
        for (int i = 0; i < steps; i++) {
            var newPositions = new HashSet<Pos>();
            for (Pos position : positions) {
                for (Pos neighbor : position.neighbors()) {
                    var x = Math.floorMod(neighbor.x(), bounds.width());
                    var y = Math.floorMod(neighbor.y(), bounds.height());
                    var plot = map.get(new Pos(x, y));

                    if (plot.canGo()) {
                        newPositions.add(neighbor);
                    }
                }
            }
            positions = newPositions;
        }
        return positions.size();
    }

    static Map<Pos, Plot> parse(String input) {
        return Grids.parse(input, s -> switch (s) {
            case "S" -> Plot.START;
            case "." -> Plot.GARDEN;
            case "#" -> Plot.ROCK;
            default -> throw new IllegalStateException("Unexpected value: " + s);
        });
    }

    @Test
    void example() {
        var s = """
                ...........
                .....###.#.
                .###.##..#.
                ..#.#...#..
                ....#.#....
                .##..S####.
                .##..#...#.
                .......##..
                .##.#.####.
                .##..##.##.
                ...........
                """;
        assertEquals(16, calculate1(parse(s), 6));
        // Solution only works on the real input because there's no obstacles going straight up/right/down/left
//        assertEquals(6536, calculate2(parse(s), 100));
//        assertEquals(167004, calculate2(parse(s), 500));
//        assertEquals(668697, calculate2(parse(s), 1000));
//        assertEquals(16733044, calculate2(parse(s), 5000));
    }

    @Test
    void input() {
        var input = Resources.readString(Resources.class.getResource("/day21.txt"));
        assertEquals(3729, solve1(input));
        assertEquals(621289922886149L, solve2(input));
        // Alternative solution using polynomial sequence.
        assertEquals(621289922886149L, calculate2Polynomial(parse(input), 26501365));
    }
}
