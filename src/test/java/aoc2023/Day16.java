package aoc2023;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Day16 {

    enum Tile {
        SLASH, // /
        BACKSLASH, // \
        SPLIT_VERTICAL, // |
        SPLIT_HORIZONTAL; // -

        List<Direction> apply(Direction dir) {
            return switch (this) {
                case SLASH -> switch (dir) {
                    case UP -> List.of(Direction.RIGHT);
                    case RIGHT -> List.of(Direction.UP);
                    case DOWN -> List.of(Direction.LEFT);
                    case LEFT -> List.of(Direction.DOWN);
                };
                case BACKSLASH -> switch (dir) {
                    case UP -> List.of(Direction.LEFT);
                    case RIGHT -> List.of(Direction.DOWN);
                    case DOWN -> List.of(Direction.RIGHT);
                    case LEFT -> List.of(Direction.UP);
                };
                case SPLIT_VERTICAL -> switch (dir) {
                    case UP -> List.of(Direction.UP);
                    case DOWN -> List.of(Direction.DOWN);
                    case LEFT, RIGHT -> List.of(Direction.UP, Direction.DOWN);
                };
                case SPLIT_HORIZONTAL -> switch (dir) {
                    case LEFT -> List.of(Direction.LEFT);
                    case RIGHT -> List.of(Direction.RIGHT);
                    case UP, DOWN -> List.of(Direction.LEFT, Direction.RIGHT);
                };
            };
        }
    }

    enum Direction {
        UP,
        RIGHT,
        DOWN,
        LEFT;

        Pos pos() {
            return switch (this) {
                case UP -> new Pos(0, -1);
                case RIGHT -> new Pos(1, 0);
                case DOWN -> new Pos(0, 1);
                case LEFT -> new Pos(-1, 0);
            };
        }
    }

    record Beam(Pos pos, Direction direction) {
    }

    static long solve1(String input) {
        var map = parse(input);
        var bounds = PosBounds.calculate(map.keySet());

        var start = new Beam(new Pos(-1, 0), Direction.RIGHT);
        return solve(start, map, bounds);
    }

    static long solve2(String input) {
        var map = parse(input);
        var bounds = PosBounds.calculate(map.keySet());

        var vertical = IntStream.range(0, bounds.width()).boxed().flatMap(x -> Stream.of(new Beam(new Pos(x, -1), Direction.DOWN), new Beam(new Pos(x, bounds.maxY() + 1), Direction.UP)));
        var horizontal = IntStream.range(0, bounds.height()).boxed().flatMap(y -> Stream.of(new Beam(new Pos(-1, y), Direction.RIGHT), new Beam(new Pos(bounds.maxX() + 1, y), Direction.LEFT)));
        return Stream.concat(vertical, horizontal).mapToLong(b -> solve(b, map, bounds)).max().getAsLong();
    }

    static int solve(Beam start, Map<Pos, Tile> map, PosBounds bounds) {
        var beams = new ArrayList<Beam>();
        beams.add(start);
        var energized = new HashSet<Pos>();
        // Keep track of all bean states that we've tried. Without this, we could be looping indefinitely.
        var allBeams = new HashSet<Beam>();
        while (!beams.isEmpty()) {
            var newBeams = new ArrayList<Beam>();
            for (Beam beam : beams) {
                var newPos = beam.pos().plus(beam.direction().pos());
                if (!bounds.contains(newPos)) {
                    continue;
                }
                energized.add(newPos);
                var newTile = map.get(newPos);
                if (newTile == null) {
                    var newBeam = new Beam(newPos, beam.direction());
                    if (allBeams.add(newBeam)) {
                        newBeams.add(newBeam);
                    }
                } else {
                    var newDirections = newTile.apply(beam.direction());
                    for (Direction newDirection : newDirections) {
                        var newBeam = new Beam(newPos, newDirection);
                        if (allBeams.add(newBeam)) {
                            newBeams.add(newBeam);
                        }
                    }
                }
            }
            beams = newBeams;
        }

        return energized.size();
    }

    private static Map<Pos, Tile> parse(String input) {
        return Grids.parse(input, s -> switch (s) {
            case "/" -> Tile.SLASH;
            case "\\" -> Tile.BACKSLASH;
            case "|" -> Tile.SPLIT_VERTICAL;
            case "-" -> Tile.SPLIT_HORIZONTAL;
            case "." -> null;
            default -> throw new IllegalStateException("Unexpected value: " + s);
        });
    }

    @Test
    void example() {
        var s = """
                .|...\\....
                |.-.\\.....
                .....|-...
                ........|.
                ..........
                .........\\
                ..../.\\\\..
                .-.-/..|..
                .|....-|.\\
                ..//.|....
                """;
        assertEquals(46, solve1(s));
        assertEquals(51, solve2(s));
    }

    @Test
    void input() {
        var input = Resources.readString(Resources.class.getResource("/day16.txt"));
        assertEquals(7979, solve1(input));
        assertEquals(8437, solve2(input));
    }
}
