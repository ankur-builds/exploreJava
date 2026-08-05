package io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class ThreadSafeFileUtil {

    private static final Object LOCK = new Object();
    private static final Path OUTPUT_FILE = Path.of("output.txt");

    private ThreadSafeFileUtil() {
    }

    public static String read(String filePath) throws IOException {
        synchronized (LOCK) {
            return Files.readString(Path.of(filePath));
        }
    }

    public static void write(String content) throws IOException {
        synchronized (LOCK) {
            Files.writeString(OUTPUT_FILE, content);
        }
    }

    public static List<String> extractWords(String filePath) throws IOException {
        synchronized (LOCK) {
            String text = Files.readString(Path.of(filePath));

            if (text.isBlank()) {
                return Collections.emptyList();
            }

            return Arrays.asList(text.trim().split("\\s+"));
        }
    }
}
