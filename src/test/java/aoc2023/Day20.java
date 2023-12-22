package aoc2023;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Day20 {

    enum Pulse {
        HIGH,
        LOW,
    }

    enum ModuleType {
        BROADCASTER,
        FLIP_FLOP,
        CONJUNCTION,
    }

    record Module(ModuleType type, List<String> destinations) {
    }

    static class Conjunction {
        Map<String, Pulse> inputs = new HashMap<String, Pulse>();

        boolean allHigh() {
            return inputs.values().stream().allMatch(v -> v == Pulse.HIGH);
        }
    }

    static class FlipFlop {
        boolean state = false;
    }

    record PulseSend(String source, String destination, Pulse pulse) {
    }

    static class Machines {
        // Schema
        private final Map<String, Module> modules;
        // State
        private final Map<String, Conjunction> conjunctions;
        private final Map<String, FlipFlop> flipFlops;

        static Machines parse(String input) {
            var modules = new HashMap<String, Module>();
            var conjunctions = new HashMap<String, Conjunction>();
            var flipFlops = new HashMap<String, FlipFlop>();

            var lines = input.split("\n");
            for (var line : lines) {
                var parts = line.split(" -> ");
                var module = parts[0];
                var destinations = List.of(parts[1].split(", "));

                ModuleType moduleType;
                String name;
                if (module.equals("broadcaster")) {
                    moduleType = ModuleType.BROADCASTER;
                    name = module;
                } else if (module.startsWith("%")) {
                    moduleType = ModuleType.FLIP_FLOP;
                    name = module.substring(1);
                    flipFlops.put(name, new FlipFlop());
                } else if (module.startsWith("&")) {
                    moduleType = ModuleType.CONJUNCTION;
                    name = module.substring(1);
                    conjunctions.put(name, new Conjunction());
                } else {
                    throw new IllegalArgumentException("Unknown module type: " + module);
                }
                modules.put(name, new Module(moduleType, destinations));
            }

            for (Map.Entry<String, Module> entry : modules.entrySet()) {
                var key = entry.getKey();
                for (String destination : entry.getValue().destinations()) {
                    var conjunction = conjunctions.get(destination);
                    if (conjunction != null) {
                        conjunction.inputs.put(key, Pulse.LOW);
                    }
                }
            }
            return new Machines(modules, conjunctions, flipFlops);
        }

        Machines(Map<String, Module> modules, Map<String, Conjunction> conjunctions, Map<String, FlipFlop> flipFlops) {
            this.modules = modules;
            this.conjunctions = conjunctions;
            this.flipFlops = flipFlops;
        }

        List<PulseSend> process(List<PulseSend> pulses) {
            var newPulses = new ArrayList<PulseSend>();
            for (var pulseSend : pulses) {
                String key = pulseSend.destination();
                Pulse pulse = pulseSend.pulse();

                var module = modules.get(key);
                if (module == null) {
                    continue;
                }

                switch (module.type()) {
                    case BROADCASTER -> module.destinations().forEach(d -> newPulses.add(new PulseSend(key, d, pulse)));
                    case FLIP_FLOP -> {
                        if (pulse == Pulse.LOW) {
                            var flipFlop = flipFlops.get(key);
                            if (flipFlop.state) {
                                module.destinations().forEach(d -> newPulses.add(new PulseSend(key, d, Pulse.LOW)));
                            } else {
                                module.destinations().forEach(d -> newPulses.add(new PulseSend(key, d, Pulse.HIGH)));
                            }
                            flipFlop.state = !flipFlop.state;
                        }
                    }
                    case CONJUNCTION -> {
                        var conjunction = conjunctions.get(key);
                        conjunction.inputs.put(pulseSend.source(), pulse);
                        var send = conjunction.allHigh() ? Pulse.LOW : Pulse.HIGH;
                        module.destinations().forEach(d -> newPulses.add(new PulseSend(key, d, send)));
                    }
                }
            }
            return newPulses;
        }
    }

    static long solve1(String input) {
        var machines = Machines.parse(input);

        var low = 0;
        var high = 0;
        for (int i = 0; i < 1000; i++) {
            var pulses = List.of(new PulseSend("button", "broadcaster", Pulse.LOW));
            low++;

            while (!pulses.isEmpty()) {
                pulses = machines.process(pulses);
                for (PulseSend pulse : pulses) {
                    switch (pulse.pulse()) {
                        case HIGH -> high++;
                        case LOW -> low++;
                    }
                }
            }
        }

        return (long) low * high;
    }

    static long solve2(String input) {
        var machines = Machines.parse(input);

        // Looked at the input: Only dh leads to rx, and dh requires low pulses on 4 input modules.
        // So to trigger a low pulse to rx, we need only high pulses to dh, which we get by having
        // all inputs send low pulses.
        var requiredModules = machines.conjunctions.get("dh").inputs.keySet();
        var requiredModulePresses = new HashMap<String, Long>();

        var presses = 0L;
        while (true) {
            presses++;
            var pulses = List.of(new PulseSend("button", "broadcaster", Pulse.LOW));
            while (!pulses.isEmpty()) {
                pulses = machines.process(pulses);
                for (PulseSend pulse : pulses) {
                    if (pulse.pulse() == Pulse.LOW && requiredModules.contains(pulse.destination())) {
                        requiredModulePresses.putIfAbsent(pulse.destination(), presses);
                        if (requiredModulePresses.size() == requiredModules.size()) {
                            return Maths.lcm(requiredModulePresses.values());
                        }
                    }
                }
            }
        }
    }

    @Test
    void example() {
        var s1 = """
                broadcaster -> a, b, c
                %a -> b
                %b -> c
                %c -> inv
                &inv -> a
                """;
        assertEquals(32000000, solve1(s1));

        var s2 = """
                broadcaster -> a
                %a -> inv, con
                &inv -> b
                %b -> con
                &con -> output
                """;
        assertEquals(11687500, solve1(s2));
    }

    @Test
    void input() {
        var input = Resources.readString(Resources.class.getResource("/day20.txt"));
        assertEquals(763500168, solve1(input));
        assertEquals(207652583562007L, solve2(input));
    }
}
