package aoc2023;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Day07 {

    enum Card {
        A, K, Q, J, T, N9, N8, N7, N6, N5, N4, N3, N2;

        static Card parse(String s) {
            return switch (s) {
                case "A" -> A;
                case "K" -> K;
                case "Q" -> Q;
                case "J" -> J;
                case "T" -> T;
                case "9" -> N9;
                case "8" -> N8;
                case "7" -> N7;
                case "6" -> N6;
                case "5" -> N5;
                case "4" -> N4;
                case "3" -> N3;
                case "2" -> N2;
                default -> throw new IllegalArgumentException("Unknown card " + s);
            };
        }
    }

    enum Type {
        FIVE,
        FOUR,
        FULL_HOUSE,
        THREE,
        TWO_PAIR,
        ONE_PAIR,
        HIGH_CARD
    }

    record Hand(Type type, List<Card> cards) implements Comparable<Hand> {

        static Hand parse(String s, boolean jokers) {
            var cards = Arrays.stream(s.split("")).map(Card::parse).toList();
            var type = getType(cards, jokers);
            return new Hand(type, cards);
        }

        static Type getType(List<Card> cards, boolean jokersEnabled) {
            var counts = cards.stream().collect(Collectors.groupingBy(c -> c, Collectors.counting()));

            if (jokersEnabled) {
                var jokers = counts.get(Card.J);
                var maxCount = 0L;
                Card maxCard = null;
                for (Map.Entry<Card, Long> entry : counts.entrySet()) {
                    if (entry.getKey() != Card.J) {
                        if (entry.getValue() > maxCount) {
                            maxCount = entry.getValue();
                            maxCard = entry.getKey();
                        }
                    }
                }

                if (maxCard != null && jokers != null) {
                    // Have jokers and other cards -> add jokers to card that we have the most of
                    counts = new HashMap<>(counts);
                    counts.put(maxCard, counts.get(maxCard) + jokers);
                    counts.remove(Card.J);
                }
            }

            if (counts.containsValue(5L)) {
                return Type.FIVE;
            } else if (counts.containsValue(4L)) {
                return Type.FOUR;
            } else if (counts.containsValue(3L) && counts.containsValue(2L)) {
                return Type.FULL_HOUSE;
            } else if (counts.containsValue(3L)) {
                return Type.THREE;
            } else {
                long pairs = counts.values().stream().filter(v -> v == 2L).count();
                if (pairs == 2) {
                    return Type.TWO_PAIR;
                } else if (pairs == 1) {
                    return Type.ONE_PAIR;
                } else {
                    return Type.HIGH_CARD;
                }
            }
        }

        @Override
        public int compareTo(Hand o) {
            if (type == o.type) {
                for (int i = 0; i < cards.size(); i++) {
                    var thisCard = cards.get(i);
                    var otherCard = o.cards.get(i);
                    var cmp = thisCard.compareTo(otherCard);
                    if (cmp != 0) {
                        return cmp;
                    }
                }
                return 0;
            } else {
                return type.compareTo(o.type);
            }
        }
    }

    static class HandComparator implements Comparator<Hand> {

        private final boolean jokers;

        HandComparator(boolean jokers) {
            this.jokers = jokers;
        }

        @Override
        public int compare(Hand a, Hand b) {
            if (a.type == b.type) {
                for (int i = 0; i < a.cards.size(); i++) {
                    var cardA = a.cards.get(i);
                    var cardB = b.cards.get(i);
                    var cmp = cardA.compareTo(cardB);
                    if (cmp != 0) {
                        if (jokers) {
                            if (cardA == Card.J) {
                                return 1;
                            } else if (cardB == Card.J) {
                                return -1;
                            }
                        }
                        return cmp;
                    }
                }
                return 0;
            } else {
                return a.type.compareTo(b.type);
            }

        }
    }

    record HandBid(Hand hand, long bid) {

        static HandBid parse(String s, boolean jokers) {
            var parts = s.split(" ");
            var hand = Hand.parse(parts[0], jokers);
            var bid = Parsing.numbersLong(parts[1]).get(0);
            return new HandBid(hand, bid);
        }
    }

    static long solve1(String input) {
        return solve(input, false);
    }

    static long solve2(String input) {
        return solve(input, true);
    }

    private static long solve(String input, boolean jokers) {
        var lines = input.split("\n");
        var handBids = Arrays.stream(lines).map(line -> HandBid.parse(line, jokers)).collect(Collectors.groupingBy(HandBid::hand));
        var hands = handBids.keySet().stream().sorted(new HandComparator(jokers)).toList();
        var result = 0L;
        for (int i = 0; i < hands.size(); i++) {
            var rank = hands.size() - i;
            result += rank * handBids.get(hands.get(i)).get(0).bid();
        }
        return result;
    }

    @Test
    void example() {
        var s = """
                32T3K 765
                T55J5 684
                KK677 28
                KTJJT 220
                QQQJA 483
                """;
        assertEquals(6440, solve1(s));
        assertEquals(5905, solve2(s));
    }

    @Test
    void input() {
        var input = Resources.readString(Resources.class.getResource("/day07.txt"));
        assertEquals(248105065, solve1(input));
        assertEquals(249515436, solve2(input));
    }
}
