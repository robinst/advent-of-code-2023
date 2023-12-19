package aoc2023;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Day19 {

    sealed interface Condition {
        boolean apply(int value);
    }

    record GreaterThan(int value) implements Condition {

        @Override
        public boolean apply(int value) {
            return value > this.value();
        }
    }

    record LessThan(int value) implements Condition {

        @Override
        public boolean apply(int value) {
            return value < this.value();
        }
    }

    enum Rating {
        X,
        M,
        A,
        S
    }

    record Rule(Rating rating, Condition condition, String destination) {
        boolean apply(Part part) {
            if (condition == null) {
                return true;
            }

            return condition.apply(part.value(rating));
        }
    }

    record Workflow(List<Rule> rules) {
    }

    record Part(int x, int m, int a, int s) {
        int value(Rating rating) {
            return switch (rating) {
                case X -> x;
                case M -> m;
                case A -> a;
                case S -> s;
            };
        }

        int sum() {
            return x + m + a + s;
        }
    }

    record Range(int fromInclusive, int toExclusive) {
        Range[] split(int number) {
            return new Range[]{new Range(fromInclusive, number), new Range(number, toExclusive)};
        }

        boolean isEmpty() {
            return fromInclusive == toExclusive;
        }

        int size() {
            return toExclusive - fromInclusive;
        }
    }

    record PartMatch(Range x, Range m, Range a, Range s) {
        Range range(Rating rating) {
            return switch (rating) {
                case X -> x;
                case M -> m;
                case A -> a;
                case S -> s;
            };
        }

        PartMatch withRange(Rating rating, Range range) {
            return switch (rating) {
                case X -> new PartMatch(range, m, a, s);
                case M -> new PartMatch(x, range, a, s);
                case A -> new PartMatch(x, m, range, s);
                case S -> new PartMatch(x, m, a, range);
            };
        }

        long combinations() {
            return (long) x.size() * m.size() * a.size() * s.size();
        }
    }

    record State(String workflow, int rule, PartMatch partMatch) {
    }

    static long solve1(String input) {
        var blocks = input.split("\n\n");
        var workflows = parseWorkflows(blocks[0]);
        var parts = parseParts(blocks[1]);

        var result = 0L;
        parts:
        for (Part part : parts) {
            var workflow = workflows.get("in");
            while (true) {
                for (Rule rule : workflow.rules()) {
                    if (rule.apply(part)) {
                        var destination = rule.destination();
                        if (destination.equals("A")) {
                            result += part.sum();
                            continue parts;
                        } else if (destination.equals("R")) {
                            continue parts;
                        } else {
                            workflow = workflows.get(destination);
                            break;
                        }
                    }
                }
            }
        }

        return result;
    }

    static long solve2(String input) {
        var blocks = input.split("\n\n");
        var workflows = parseWorkflows(blocks[0]);

        var acceptedRanges = new ArrayList<PartMatch>();
        var queue = new ArrayList<State>();
        // 1 to 4000 for each x, m, a, s
        queue.add(new State("in", 0, new PartMatch(new Range(1, 4001), new Range(1, 4001), new Range(1, 4001), new Range(1, 4001))));
        while (!queue.isEmpty()) {
            var state = queue.removeFirst();
            var workflow = workflows.get(state.workflow());
            var rule = workflow.rules().get(state.rule());
            var partMatch = state.partMatch();
            if (rule.condition() == null) {
                // No split
                if (rule.destination().equals("A")) {
                    acceptedRanges.add(partMatch);
                } else if (!rule.destination().equals("R")) {
                    queue.add(new State(rule.destination(), 0, partMatch));
                }
            } else {
                var rating = rule.rating();
                var range = partMatch.range(rating);

                var splits = switch (rule.condition()) {
                    case GreaterThan(var value) -> {
                        var parts = range.split(value + 1);
                        yield new Range[]{parts[1], parts[0]}; //{
                    }
                    case LessThan(var value) -> range.split(value);
                };
                if (!splits[0].isEmpty()) {
                    // true part
                    if (rule.destination().equals("A")) {
                        acceptedRanges.add(partMatch.withRange(rating, splits[0]));
                    } else if (!rule.destination().equals("R")) {
                        queue.add(new State(rule.destination(), 0, partMatch.withRange(rating, splits[0])));
                    }
                }
                if (!splits[1].isEmpty()) {
                    // false part
                    queue.add(new State(state.workflow(), state.rule() + 1, partMatch.withRange(rating, splits[1])));
                }
            }
        }

        var result = 0L;
        for (PartMatch acceptedRange : acceptedRanges) {
            result += acceptedRange.combinations();
        }
        return result;
    }

    static Map<String, Workflow> parseWorkflows(String input) {
        var map = new HashMap<String, Workflow>();
        for (var line : input.split("\n")) {
            var parts = line.split("\\{");
            var name = parts[0];
            var rulesText = parts[1].split("\\}")[0];
            var rules = new ArrayList<Rule>();
            for (String s : rulesText.split(",")) {
                var colon = s.split(":");
                if (colon.length == 1) {
                    // No condition, just destination
                    rules.add(new Rule(null, null, s));
                    continue;
                }
                var destination = colon[1];
                var cond = colon[0];
                var condParts = cond.split("[<>]");
                var rating = Rating.valueOf(condParts[0].toUpperCase(Locale.ROOT));
                var value = Parsing.numbers(condParts[1]).getFirst();
                Condition condition;
                if (cond.contains("<")) {
                    condition = new LessThan(value);
                } else if (cond.contains(">")) {
                    condition = new GreaterThan(value);
                } else {
                    throw new IllegalArgumentException("Unknown condition " + cond);
                }
                rules.add(new Rule(rating, condition, destination));
            }
            map.put(name, new Workflow(rules));
        }
        return map;
    }

    static List<Part> parseParts(String input) {
        var parts = new ArrayList<Part>();
        for (var line : input.split("\n")) {
            var nums = Parsing.numbers(line);
            if (nums.size() != 4) {
                throw new IllegalArgumentException("Can't parse part " + line);
            }
            parts.add(new Part(nums.get(0), nums.get(1), nums.get(2), nums.get(3)));
        }
        return parts;
    }

    @Test
    void example() {
        var s = """
                px{a<2006:qkq,m>2090:A,rfg}
                pv{a>1716:R,A}
                lnx{m>1548:A,A}
                rfg{s<537:gd,x>2440:R,A}
                qs{s>3448:A,lnx}
                qkq{x<1416:A,crn}
                crn{x>2662:A,R}
                in{s<1351:px,qqz}
                qqz{s>2770:qs,m<1801:hdj,R}
                gd{a>3333:R,R}
                hdj{m>838:A,pv}
                                
                {x=787,m=2655,a=1222,s=2876}
                {x=1679,m=44,a=2067,s=496}
                {x=2036,m=264,a=79,s=2244}
                {x=2461,m=1339,a=466,s=291}
                {x=2127,m=1623,a=2188,s=1013}
                """;
        assertEquals(19114, solve1(s));
        // Max is   4000**4 = 256_000_000_000_000, phew
        assertEquals(167_409_079_868_000L, solve2(s));
    }

    @Test
    void input() {
        var input = Resources.readString(Resources.class.getResource("/day19.txt"));
        assertEquals(397134, solve1(input));
        assertEquals(127517902575337L, solve2(input));
    }
}
