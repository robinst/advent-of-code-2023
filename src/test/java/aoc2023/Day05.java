package aoc2023;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Day05 {

    record Almanac(Map<String, Mappings> typeToMappings, Map<String, String> sourceToDestinations) {
    }

    record Mappings(List<Mapping> mappings) {
        long calculate(long number) {
            int low = 0;
            int high = mappings.size() - 1;

            while (low <= high) {
                int mid = (low + high) / 2;
                var mapping = mappings.get(mid);
                if (mapping.contains(number)) {
                    return mapping.map(number);
                }

                if (number < mapping.sourceStart()) {
                    high = mid - 1;
                } else {
                    low = mid + 1;
                }
            }

            return number;

//            Collections.binarySearch(mappings, )
//            var found = mappings.stream().filter(m -> m.contains(number)).findFirst();
//            return found.map(m -> m.map(number)).orElse(number);
        }
    }

    record Mapping(long sourceStart, long destinationStart, long rangeLength) implements Comparable<Mapping> {
        boolean contains(long number) {
            return number >= sourceStart && number < sourceStart + rangeLength;
        }

        long map(long number) {
            return number - sourceStart + destinationStart;
        }

        @Override
        public int compareTo(Mapping o) {
            return Long.compare(sourceStart, o.sourceStart);
        }
    }

    static long solve1(String input) {
        var almanac = parse(input);
        var seeds = Parsing.numbersLong(input.split("\n")[0]);

        var results = new ArrayList<Long>();

        for (Long seed : seeds) {
            var type = "seed";
            var id = seed;
            while (!type.equals("location")) {
                id = almanac.typeToMappings.get(type).calculate(id);
                type = almanac.sourceToDestinations.get(type);
            }
            results.add(id);
        }

        return results.stream().mapToLong(Long::valueOf).min().getAsLong();
    }

    static long solve2(String input) {
        var almanac = parse(input);
        var typeToMappings = almanac.typeToMappings;
        var sourceToDestinations = almanac.sourceToDestinations;
        var firstLine = Parsing.numbersLong(input.split("\n")[0]);

        var lowest = Long.MAX_VALUE;
//        var results = new HashMap<Long, Long>();
        for (int i = 0; i < firstLine.size(); i += 2) {
            var start = firstLine.get(i);
            var count = firstLine.get(i + 1);

            for (long seed = start; seed < start + count; seed++) {
//                if (results.containsKey(seed)) {
//                    continue;
//                }
                var type = "seed";
                var id = seed;
                while (!type.equals("location")) {
                    id = typeToMappings.get(type).calculate(id);
                    type = sourceToDestinations.get(type);
                }
                if (id < lowest) {
                    lowest = id;
                }
//                results.put(seed, id);
            }
        }
        return lowest;

//        return results.values().stream().mapToLong(Long::valueOf).min().getAsLong();
    }

    static Almanac parse(String input) {
        var maps = new HashMap<String, Mappings>();
        var sourceDestinationMap = new HashMap<String, String>();
        var blocks = List.of(input.split("\n\n"));
        for (var block : blocks.subList(1, blocks.size())) {
            var lines = List.of(block.split("\n"));
            var types = lines.get(0).split(" ")[0].split("-to-");
            var sourceType = types[0];
            var destinationType = types[1];
            sourceDestinationMap.put(sourceType, destinationType);

            var mappings = new ArrayList<Mapping>();

            for (var line : lines.subList(1, lines.size())) {
                // the destination range start, the source range start, and the range length.
                var numbers = Parsing.numbersLong(line);
                var destinationStart = numbers.get(0);
                var sourceStart = numbers.get(1);
                var rangeLength = numbers.get(2);
                mappings.add(new Mapping(sourceStart, destinationStart, rangeLength));
            }

            Collections.sort(mappings);
            maps.put(sourceType, new Mappings(mappings));
        }
        return new Almanac(maps, sourceDestinationMap);
    }

    @Test
    void examples() {
        var s = """
                seeds: 79 14 55 13
                                
                seed-to-soil map:
                50 98 2
                52 50 48
                                
                soil-to-fertilizer map:
                0 15 37
                37 52 2
                39 0 15
                                
                fertilizer-to-water map:
                49 53 8
                0 11 42
                42 0 7
                57 7 4
                                
                water-to-light map:
                88 18 7
                18 25 70
                                
                light-to-temperature map:
                45 77 23
                81 45 19
                68 64 13
                                
                temperature-to-humidity map:
                0 69 1
                1 0 69
                                
                humidity-to-location map:
                60 56 37
                56 93 4
                """;
        assertEquals(35, solve1(s));
        assertEquals(46, solve2(s));
    }

    @Test
    void input() {
        var input = Resources.readString(Resources.class.getResource("/day05.txt"));
        assertEquals(510109797, solve1(input));
        assertEquals(9622622, solve2(input));
    }
}
