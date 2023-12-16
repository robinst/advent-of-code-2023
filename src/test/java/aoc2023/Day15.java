package aoc2023;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Day15 {

    static class Entry {
        private String key;
        private Integer value;

        public Entry(String key, Integer value) {
            this.key = key;
            this.value = value;
        }
    }

    static class Box {
        LinkedList<Entry> entries = new LinkedList<>();

        void put(String key, Integer value) {
            for (Entry entry : entries) {
                if (entry.key.equals(key)) {
                    entry.value = value;
                    return;
                }
            }
            entries.add(new Entry(key, value));
        }

        void remove(String key) {
            var it = entries.iterator();
            while (it.hasNext()) {
                var entry = it.next();
                if (entry.key.equals(key)) {
                    it.remove();
                    break;
                }
            }
        }
    }

    sealed interface Step {
        String key();
    }

    record Remove(String key) implements Step {
    }

    record Put(String key, Integer value) implements Step {
    }

    static long solve1(String input) {
        var steps = input.trim().split(",");

        var result = 0L;
        for (var step : steps) {
            result += hash(step);
        }
        return result;
    }

    static long solve2(String input) {
        var steps = input.trim().split(",");

        var boxes = new Box[256];
        Arrays.setAll(boxes, i -> new Box());
        for (var stepString : steps) {
            var step = parse(stepString);
            var hash = hash(step.key());
            switch (step) {
                case Put(var key, var value) -> boxes[hash].put(key, value);
                case Remove(var key) -> boxes[hash].remove(key);
            }
        }

        var result = 0L;
        for (int boxIndex = 0; boxIndex < boxes.length; boxIndex++) {
            var box = boxes[boxIndex];
            var slotIndex = 0;
            for (Entry entry : box.entries) {
                result += (long) (boxIndex + 1) * (slotIndex + 1) * entry.value;
                slotIndex++;
            }
        }
        return result;
    }

    static Step parse(String step) {
        if (step.endsWith("-")) {
            return new Remove(step.substring(0, step.length() - 1));
        } else {
            var parts = step.split("=");
            var key = parts[0];
            var value = Integer.valueOf(parts[1]);
            return new Put(key, value);
        }
    }

    static int hash(String word) {
        int hash = 0;
        for (var ch : word.toCharArray()) {
            hash += ch;
            hash *= 17;
            hash %= 256;
        }
        return hash;
    }

    @Test
    void example() {
        assertEquals(52, solve1("HASH"));
        var s = """
                rn=1,cm-,qp=3,cm=2,qp-,pc=4,ot=9,ab=5,pc-,pc=6,ot=7
                """;
        assertEquals(1320, solve1(s));
        assertEquals(145, solve2(s));
    }

    @Test
    void input() {
        var input = Resources.readString(Resources.class.getResource("/day15.txt"));
        assertEquals(498538, solve1(input));
        assertEquals(286278, solve2(input));
    }
}
