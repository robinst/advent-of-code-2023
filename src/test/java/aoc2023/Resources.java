package aoc2023;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class Resources {

    public static List<String> readAllLines(URL resource) throws IOException, URISyntaxException {
        return Files.readAllLines(Path.of(Objects.requireNonNull(resource).toURI()));
    }

    public static String readString(URL resource) {
        try {
            return Files.readString(Path.of(Objects.requireNonNull(resource).toURI()));
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
