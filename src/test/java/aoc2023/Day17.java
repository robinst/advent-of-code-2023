package aoc2023;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Day17 {

    // TODO: Extract this to library
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

        /**
         * @return possible new directions with a 90-degree turn
         */
        List<Direction> turns() {
            return switch (this) {
                case UP -> List.of(RIGHT, LEFT);
                case RIGHT -> List.of(DOWN, UP);
                case DOWN -> List.of(LEFT, RIGHT);
                case LEFT -> List.of(UP, DOWN);
            };
        }
    }

    record State(Pos pos, Direction direction, int directionSteps) {
    }

    static int solve1(String input) {
        return solve(input, 1, 3);
    }

    static int solve2(String input) {
        return solve(input, 4, 10);
    }

    // A* algorithm
    static int solve(String input, int minStraight, int maxStraight) {
        var map = Grids.parse(input, Integer::valueOf);
        var bounds = PosBounds.calculate(map.keySet());
        var endPos = new Pos(bounds.maxX(), bounds.maxY());

        var startPos = new Pos(0, 0);
        var startState = new State(startPos, Direction.RIGHT, 0);
        var startState2 = new State(startPos, Direction.DOWN, 0);

        var gScore = new HashMap<State, Integer>();
        gScore.put(startState, 0);
        gScore.put(startState2, 0);

        var fScore = new HashMap<State, Integer>();
        fScore.put(startState, startPos.distance(endPos));
        fScore.put(startState2, startPos.distance(endPos));

        var queue = new PriorityQueue<State>(Comparator.comparing(fScore::get));
        queue.add(startState);
        queue.add(startState2);

        while (!queue.isEmpty()) {
            var state = queue.remove();

            if (state.pos().equals(endPos)) {
                return gScore.get(state);
            }

            var directions = new ArrayList<Direction>();
            if (state.directionSteps() < maxStraight) {
                directions.add(state.direction());
            }
            if (state.directionSteps() >= minStraight) {
                directions.addAll(state.direction().turns());
            }

            for (Direction direction : directions) {
                var newPos = state.pos().plus(direction.pos());
                var heat = map.get(newPos);
                if (heat == null) {
                    continue;
                }

                var directionSteps = direction == state.direction() ? state.directionSteps() + 1 : 1;
                var newState = new State(newPos, direction, directionSteps);
                var score = gScore.get(state) + heat;
                if (!gScore.containsKey(newState) || score < gScore.get(newState)) {
                    gScore.put(newState, score);
                    fScore.put(newState, score + newPos.distance(endPos));
                    if (!queue.contains(newState)) {
                        queue.add(newState);
                    }
                }
            }
        }
        return Integer.MAX_VALUE;
    }

    @Test
    void example() {
        var s = """
                2413432311323
                3215453535623
                3255245654254
                3446585845452
                4546657867536
                1438598798454
                4457876987766
                3637877979653
                4654967986887
                4564679986453
                1224686865563
                2546548887735
                4322674655533
                """;
        assertEquals(102, solve1(s));
        assertEquals(94, solve2(s));
    }

    @Test
    void input() {
        var input = Resources.readString(Resources.class.getResource("/day17.txt"));
        assertEquals(843, solve1(input));
        assertEquals(1017, solve2(input));
    }
}
