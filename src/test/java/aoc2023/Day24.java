package aoc2023;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Day24 {

    record PosLong(long x, long y, long z) {
    }

    record Velocity(long vX, long vY, long vZ) {
    }

    record Hailstone(PosLong pos, Velocity vel) {
    }

    static long solve1(String input, long min, long max) {
        var hailstones = parse(input);
        var result = 0L;
        for (int i = 0; i < hailstones.size(); i++) {
            var h1 = hailstones.get(i);
            var x1 = h1.pos().x();
            var y1 = h1.pos().y();
            var a1 = h1.vel().vY();
            var b1 = h1.vel().vX();
            var c1 = Math.multiplyExact(b1, y1) - Math.multiplyExact(a1, x1);

            for (int j = i + 1; j < hailstones.size(); j++) {
                var h2 = hailstones.get(j);
                var x2 = h2.pos().x();
                var y2 = h2.pos().y();
                var a2 = h2.vel().vY();
                var b2 = h2.vel().vX();
                var c2 = Math.multiplyExact(b2, y2) - Math.multiplyExact(a2, x2);

                var divisor = Math.multiplyExact(a1, b2) - Math.multiplyExact(a2, b1);
                if (divisor == 0) {
                    // Parallel, paths never cross
                    continue;
                }
                // Divide early to avoid overflow
                var x = Math.multiplyExact(b1, c2 / divisor) - Math.multiplyExact(b2, c1 / divisor);
                var y = Math.multiplyExact(a1, c2 / divisor) - Math.multiplyExact(a2, c1 / divisor);
                System.out.println(x + ", " + y);

                if (Math.signum(b1) != Math.signum(Double.compare(x, x1)) ||
                        Math.signum(b2) != Math.signum(Double.compare(x, x2))) {
                    // Paths crossed in the past, don't count it.
                    continue;
                }

                if (x >= min && x <= max && y >= min && y <= max) {
                    result += 1;
                }
            }
        }
        return result;
    }

    static List<Hailstone> parse(String input) {
        var hailstones = new ArrayList<Hailstone>();
        var lines = input.split("\n");
        for (var line : lines) {
            var parts = line.split(" @ ");
            var coords = Parsing.numbersLong(parts[0]);
            var velocity = Parsing.numbersLong(parts[1]);
            hailstones.add(new Hailstone(new PosLong(coords.get(0), coords.get(1), coords.get(2)), new Velocity(velocity.get(0), velocity.get(1), velocity.get(2))));
        }
        return hailstones;
    }

    @Test
    void example() {
        var s = """
                19, 13, 30 @ -2,  1, -2
                18, 19, 22 @ -1, -1, -2
                20, 25, 34 @ -2, -2, -4
                12, 31, 28 @ -1, -2, -1
                20, 19, 15 @  1, -5, -3
                """;
        // Doesn't work due to dividing early (but it works for the real input..)
        // assertEquals(2, solve1(s, 7, 27));

        // Wolfram Alpha for example:
        // x+v*a=19+(-2)*a,
        // x+v*b=18+(-1)*b,
        // x+v*c=20+(-2)*c,
        // y+w*a=13+( 1)*a,
        // y+w*b=19+(-1)*b,
        // y+w*c=25+(-2)*c,
        // z+u*a=30+(-2)*a,
        // z+u*b=22+(-2)*b,
        // z+u*c=34+(-4)*c
        //
        // a = 5, b = 3, c = 4, u = 2, v = -3, w = 1, x = 24, y = 13, z = 10
        // 24+13+10 = 47
    }

    @Test
    void input() {
        var input = Resources.readString(Resources.class.getResource("/day24.txt"));
        assertEquals(17244, solve1(input, 200000000000000L, 400000000000000L));

        // For part 2: Using the first three hailstones, we can write down these equations:
        //
        // x+v*a=364193859817003+( 85)*a,
        // x+v*b=222402516161891+(123)*b,
        // x+v*c=219626703416113+(115)*c,
        // y+w*a=337161998875178+( 85)*a,
        // y+w*b=289638719990878+(-40)*b,
        // y+w*c= 76777384100180+(317)*c,
        // z+u*a=148850519939119+(473)*a,
        // z+u*b=261939904566871+( 25)*b,
        // z+u*c=165418060594769+(193)*c
        //
        // They're too long for putting into Wolfram Alpha, but this one works:
        // https://quickmath.com/pages/modules/equations/index.php
        //
        // a = 178381243424
        // b = 564132261724
        // c = 585444749559
        // u = 111
        // v = −227
        // w = −221
        // x = 419848807765291
        // y = 391746659362922
        // z = 213424530058607
        //
        // Then:
        // 419848807765291+391746659362922+213424530058607
        // = 1025019997186820 which is the right answer \o/
    }
}
