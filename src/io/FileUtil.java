package io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class FileUtil {

    private static final Path OUTPUT_FILE = Path.of("output.txt");

    private FileUtil() {
    }

    public static String read(String filePath) throws IOException {
        return Files.readString(Path.of(filePath));
    }

    public static void write(String content) throws IOException {
        Files.writeString(OUTPUT_FILE, content);
    }

    public static List<String> extractWords(String filePath) throws IOException {
        String text = read(filePath);

        if (text.isBlank()) {
            return Collections.emptyList();
        }

        return Arrays.asList(text.trim().split("\\s+"));
    }
}
