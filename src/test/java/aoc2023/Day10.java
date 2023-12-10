package aoc2023;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Day10 {

    record Puzzle(Pos start, Map<Pos, Pipe> map, int width, int height) {
    }

    enum Direction {
        UP,
        RIGHT,
        DOWN,
        LEFT;

        public Direction opposite() {
            return switch (this) {
                case UP -> Direction.DOWN;
                case RIGHT -> Direction.LEFT;
                case LEFT -> Direction.RIGHT;
                case DOWN -> Direction.UP;
            };
        }

        public Direction next(int clockwiseSteps) {
            return Direction.values()[Math.floorMod(this.ordinal() + clockwiseSteps, Direction.values().length)];
        }
    }

    enum Pipe {
        VERTICAL(EnumSet.of(Direction.UP, Direction.DOWN)),
        HORIZONTAL(EnumSet.of(Direction.LEFT, Direction.RIGHT)),
        UP_RIGHT(EnumSet.of(Direction.UP, Direction.RIGHT)),
        UP_LEFT(EnumSet.of(Direction.UP, Direction.LEFT)),
        DOWN_RIGHT(EnumSet.of(Direction.DOWN, Direction.RIGHT)),
        DOWN_LEFT(EnumSet.of(Direction.DOWN, Direction.LEFT)),
        // Not sure if we need this
        START(EnumSet.allOf(Direction.class));

        private final EnumSet<Direction> directions;

        Pipe(EnumSet<Direction> directions) {
            this.directions = directions;
        }

        boolean canGo(Direction dir, Pipe neighbor) {
            return directions.contains(dir) && neighbor.directions.contains(dir.opposite());
        }
    }

    static Pos getNeighbor(Pos pos, Direction dir) {
        return switch (dir) {
            case UP -> new Pos(pos.x(), pos.y() - 1);
            case RIGHT -> new Pos(pos.x() + 1, pos.y());
            case LEFT -> new Pos(pos.x() - 1, pos.y());
            case DOWN -> new Pos(pos.x(), pos.y() + 1);
        };
    }

    static long solve1(String input) {
        var puzzle = parse(input);
        var start = puzzle.start();
        var map = puzzle.map();

        var visited = new HashSet<Pos>();
        var current = List.of(start);

        var furthest = 0;

        while (!current.isEmpty()) {
            var next = new ArrayList<Pos>();

            for (Pos pos : current) {
                visited.add(pos);
                for (var dir : Direction.values()) {
                    var neighborPos = getNeighbor(pos, dir);
                    if (visited.contains(neighborPos)) {
                        continue;
                    }

                    var pipe = map.get(pos);
                    var neighborPipe = map.get(neighborPos);
                    if (neighborPipe == null) {
                        continue;
                    }
                    if (pipe.canGo(dir, neighborPipe)) {
                        next.add(neighborPos);
                    }
                }
            }

            furthest++;

            current = next;
        }

        return furthest - 1;
    }

    // How this works:
    // 1. Keep two sets of positions (one is going to be outside, the other inside, don't know yet)
    // 2. Go through the main loop in one direction (similar to part 1, but only go one way)
    // 3. When going from one pipe to the next, look at the neighbours of that pipe starting from the direction
    //    we came in; stop at the direction the pipe goes out.
    //    - Do that clockwise and put the resulting positions into the first set
    //    - Do the same anti-clockwise and put results into second set
    // 4. After we've gone through the main loop, do bucket fill for both sets,
    //    return the count of the set that doesn't leak to outside the map
    static long solve2(String input) {
        var puzzle = parse(input);
        var start = puzzle.start();
        var map = puzzle.map();

        var mainLoop = new HashSet<Pos>();
        var leftSet = new HashSet<Pos>();
        var rightSet = new HashSet<Pos>();
        var pos = start;

        while (pos != null) {
            Pos nextPos = null;

            mainLoop.add(pos);
            for (var dir : Direction.values()) {
                var neighborPos = getNeighbor(pos, dir);
                if (mainLoop.contains(neighborPos)) {
                    continue;
                }

                var pipe = map.get(pos);
                var neighborPipe = map.get(neighborPos);
                if (neighborPipe == null) {
                    continue;
                }
                if (pipe.canGo(dir, neighborPipe)) {
                    var leftOutsides = getOutsides(neighborPos, neighborPipe, dir.opposite(), 1);
                    var rightOutsides = getOutsides(neighborPos, neighborPipe, dir.opposite(), -1);
                    leftSet.addAll(leftOutsides);
                    rightSet.addAll(rightOutsides);

                    nextPos = neighborPos;
                    // Just go around one direction, not both. This makes it easier to keep track of left/right.
                    break;
                }
            }

            pos = nextPos;
        }

        leftSet.removeAll(mainLoop);
        rightSet.removeAll(mainLoop);
        var leftSpaces = fillSpace(leftSet, mainLoop, puzzle.width(), puzzle.height());
        var rightSpaces = fillSpace(rightSet, mainLoop, puzzle.width(), puzzle.height());
        return Math.max(leftSpaces, rightSpaces);
    }

    static List<Pos> getOutsides(Pos pos, Pipe pipe, Direction start, int clockwiseSteps) {
        var outsides = new ArrayList<Pos>();
        var dir = start.next(clockwiseSteps);
        while (!pipe.directions.contains(dir)) {
            var outside = getNeighbor(pos, dir);
            outsides.add(outside);
            dir = dir.next(clockwiseSteps);
        }
        return outsides;
    }

    static int fillSpace(Set<Pos> start, Set<Pos> mainLoop, int width, int height) {
        var current = new HashSet<>(start);
        var all = new HashSet<>(start);

        while (!current.isEmpty()) {
            var next = new HashSet<Pos>();

            for (Pos pos : current) {
                for (Pos neighborPos : pos.neighbors()) {
                    if (neighborPos.x() == 0 || neighborPos.y() == 0 || neighborPos.x() == width - 1 || neighborPos.y() == height - 1) {
                        // Fell off the map, this set can not be within the main loop.
                        return 0;
                    }

                    if (mainLoop.contains(neighborPos) || all.contains(neighborPos)) {
                        continue;
                    }

                    all.add(neighborPos);
                    next.add(neighborPos);
                }
            }

            current = next;
        }
        return all.size();
    }

    static Puzzle parse(String input) {
        var map = new HashMap<Pos, Pipe>();
        Pos start = null;
        int width = 0;
        var lines = input.split("\n");
        for (int y = 0; y < lines.length; y++) {
            var line = lines[y];
            if (width == 0) {
                width = line.length();
            }
            var cells = line.split("");
            for (int x = 0; x < cells.length; x++) {
                var cell = cells[x];
                if (cell.equals("S")) {
                    start = new Pos(x, y);
                }
                var pipe = parsePipe(cell);
                if (pipe != null) {
                    map.put(new Pos(x, y), pipe);
                }
            }
        }
        int height = lines.length;
        Objects.requireNonNull(start);
        return new Puzzle(start, map, width, height);
    }

    static Pipe parsePipe(String s) {
        return switch (s) {
            case "|" -> Pipe.VERTICAL; // is a vertical pipe connecting north and south.
            case "-" -> Pipe.HORIZONTAL; // is a horizontal pipe connecting east and west.
            case "L" -> Pipe.UP_RIGHT; // is a 90-degree bend connecting north and east.
            case "J" -> Pipe.UP_LEFT; // is a 90-degree bend connecting north and west.
            case "7" -> Pipe.DOWN_LEFT; // is a 90-degree bend connecting south and west.
            case "F" -> Pipe.DOWN_RIGHT; // is a 90-degree bend connecting south and east.
            case "." -> null; // is ground; there is no pipe in this tile.
            case "S" ->
                    Pipe.START; // is the starting position of the animal; there is a pipe on this tile, but your sketch doesn't show what shape the pipe has.
            default -> throw new IllegalArgumentException("Unknown pipe symbol " + s);
        };
    }


    @Test
    void examplesPart1() {
        var s1 = """
                .....
                .S-7.
                .|.|.
                .L-J.
                .....
                """;
        assertEquals(4, solve1(s1));

        var s2 = """
                ..F7.
                .FJ|.
                SJ.L7
                |F--J
                LJ...
                """;
        assertEquals(8, solve1(s2));
    }

    @Test
    void examplesPart2() {
        var s = """
                ...........
                .S-------7.
                .|F-----7|.
                .||.....||.
                .||.....||.
                .|L-7.F-J|.
                .|..|.|..|.
                .L--J.L--J.
                ...........
                """;
        assertEquals(4, solve2(s));

        var s2 = """
                .F----7F7F7F7F-7....
                .|F--7||||||||FJ....
                .||.FJ||||||||L7....
                FJL7L7LJLJ||LJ.L-7..
                L--J.L7...LJS7F-7L7.
                ....F-J..F7FJ|L7L7L7
                ....L7.F7||L7|.L7L7|
                .....|FJLJ|FJ|F7|.LJ
                ....FJL-7.||.||||...
                ....L---J.LJ.LJLJ...
                """;
        assertEquals(8, solve2(s2));

        var s3 = """
                FF7FSF7F7F7F7F7F---7
                L|LJ||||||||||||F--J
                FL-7LJLJ||||||LJL-77
                F--JF--7||LJLJ7F7FJ-
                L---JF-JLJ.||-FJLJJ7
                |F|F-JF---7F7-L7L|7|
                |FFJF7L7F-JF7|JL---7
                7-L-JL7||F7|L7F-7F7|
                L.L7LFJ|||||FJL7||LJ
                L7JLJL-JLJLJL--JLJ.L
                """;
        assertEquals(10, solve2(s3));
    }

    @Test
    void input() {
        var input = Resources.readString(Resources.class.getResource("/day10.txt"));
        assertEquals(6733, solve1(input));
        assertEquals(435, solve2(input));
    }
}
