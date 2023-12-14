package aoc2023;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class Grids {
    
    public static <T> Map<Pos, T> parse(String input, Function<String, T> parseCell) {
        var map = new LinkedHashMap<Pos, T>();
        var lines = input.split("\n");
        for (int y = 0; y < lines.length; y++) {
            var line = lines[y];
            var cells = line.split("");
            for (int x = 0; x < cells.length; x++) {
                var cell = parseCell.apply(cells[x]);
                if (cell != null) {
                    map.put(new Pos(x, y), cell);
                }
            }
        }
        return map;
    }
}
