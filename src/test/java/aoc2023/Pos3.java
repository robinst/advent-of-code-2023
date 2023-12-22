package aoc2023;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toCollection;

record Pos3(int x, int y, int z) {

    public List<Pos3> neighbors() {
        return List.of(new Pos3(x() + 1, y(), z()), new Pos3(x(), y() + 1, z()), new Pos3(x(), y(), z() + 1),
                new Pos3(x() - 1, y(), z()), new Pos3(x(), y() - 1, z()), new Pos3(x(), y(), z() - 1));
    }

    public List<Pos3> straightLineToIncluding(Pos3 to) {
        if (y() == to.y() && z() == to.z()) {
            // X is changing
            var fromX = Math.min(x(), to.x());
            var toX = Math.max(x(), to.x());
            return IntStream.rangeClosed(fromX, toX)
                    .mapToObj(x -> new Pos3(x, y(), z()))
                    .collect(toCollection(ArrayList::new));
        } else if (x() == to.x() && z() == to.z()) {
            // Y is changing
            var fromY = Math.min(y(), to.y());
            var toY = Math.max(y(), to.y());
            return IntStream.rangeClosed(fromY, toY)
                    .mapToObj(y -> new Pos3(x(), y, z()))
                    .collect(toCollection(ArrayList::new));
        } else if (x() == to.x() && y() == to.y()) {
            // Z is changing
            var fromZ = Math.min(z(), to.z());
            var toZ = Math.max(z(), to.z());
            return IntStream.rangeClosed(fromZ, toZ)
                    .mapToObj(z -> new Pos3(x(), y(), z))
                    .collect(toCollection(ArrayList::new));
        } else {
            throw new IllegalStateException("Not a straight line from " + this + " to " + to);
        }
    }
}
