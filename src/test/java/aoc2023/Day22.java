package aoc2023;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Day22 {

    record Brick(Set<Pos3> cubes, int minZ) {

        boolean intersects(Brick other) {
            return cubes.stream().anyMatch(other.cubes::contains);
        }

        Brick down() {
            return new Brick(cubes.stream().map(p -> new Pos3(p.x(), p.y(), p.z() - 1)).collect(Collectors.toSet()), minZ - 1);
        }
    }

    static long solve1(String input) {
        var originalBricks = parse(input);

        var result = 0L;
        var bricks = moveDown(originalBricks);
        for (int i = 0; i < bricks.size(); i++) {
            System.out.println("i: " + i);
            var brick = bricks.get(i);
            var bricksMinusOne = new ArrayList<Brick>();
            bricks.stream().filter(b -> b != brick).forEach(bricksMinusOne::add);

            if (!anyMove(bricksMinusOne)) {
                result++;
            }
        }

        return result;
    }

    static long solve2(String input) {
        var originalBricks = parse(input);

        var result = 0L;
        var bricks = moveDown(originalBricks);
        for (int i = 0; i < bricks.size(); i++) {
            System.out.println("i: " + i);
            var brick = bricks.get(i);
            var bricksMinusOne = new ArrayList<Brick>();
            bricks.stream().filter(b -> b != brick).forEach(bricksMinusOne::add);

            result += countMoves(bricksMinusOne);
        }

        return result;
    }

    static List<Brick> moveDown(List<Brick> bricks) {
        var newBricks = new ArrayList<Brick>();

        for (Brick brick : bricks) {
            var newBrick = brick;
            while (newBrick.minZ() != 1) {
                var candidate = newBrick.down();
                if (newBricks.stream().anyMatch(candidate::intersects)) {
                    break;
                }
                newBrick = candidate;
            }
            newBricks.add(newBrick);
        }

        newBricks.sort(Comparator.comparing(Brick::minZ));
        return newBricks;
    }

    static boolean anyMove(List<Brick> bricks) {
        var newBricks = new LinkedList<Brick>();

        for (Brick brick : bricks) {
            if (brick.minZ() != 1) {
                var candidate = brick.down();
                if (newBricks.stream().noneMatch(candidate::intersects)) {
                    return true;
                }
            }
            newBricks.addFirst(brick);
        }
        return false;
    }

    static int countMoves(List<Brick> bricks) {
        var newBricks = new LinkedList<Brick>();
        var moves = 0;

        for (Brick brick : bricks) {
            var newBrick = brick;
            if (newBrick.minZ() != 1) {
                var candidate = newBrick.down();
                if (newBricks.stream().noneMatch(candidate::intersects)) {
                    // One move is enough to trigger other moves, we don't need to move all the way down.
                    moves++;
                    newBrick = candidate;
                }

            }
            newBricks.addFirst(newBrick);
        }
        return moves;
    }

    static List<Brick> parse(String input) {
        List<Brick> bricks = new ArrayList<>();
        var lines = input.split("\n");
        for (var line : lines) {
            var parts = line.split("~");
            var fromNumbers = Parsing.numbers(parts[0]);
            var toNumbers = Parsing.numbers(parts[1]);
            var from = new Pos3(fromNumbers.get(0), fromNumbers.get(1), fromNumbers.get(2));
            var to = new Pos3(toNumbers.get(0), toNumbers.get(1), toNumbers.get(2));
            var cubes = from.straightLineToIncluding(to);
            var brick = new Brick(new HashSet<>(cubes), cubes.stream().mapToInt(Pos3::z).min().getAsInt());
            bricks.add(brick);
        }
        bricks.sort(Comparator.comparing(Brick::minZ));
        return bricks;
    }

    @Test
    void example() {
        var s = """
                1,0,1~1,2,1
                0,0,2~2,0,2
                0,2,3~2,2,3
                0,0,4~0,2,4
                2,0,5~2,2,5
                0,1,6~2,1,6
                1,1,8~1,1,9
                """;
        assertEquals(5, solve1(s));
        assertEquals(7, solve2(s));
    }

    @Test
    void input() {
        var input = Resources.readString(Resources.class.getResource("/day22.txt"));
        assertEquals(398, solve1(input));
        assertEquals(70727, solve2(input));
    }
}
