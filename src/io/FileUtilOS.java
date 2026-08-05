package io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class FileUtilOS {

    private static final Path OUTPUT_FILE = Path.of("output.txt");

    private FileUtilOS() {
    }

    public static String read(String filePath) throws IOException {
        try (FileChannel channel = FileChannel.open(
                Path.of(filePath),
                StandardOpenOption.READ);
             FileLock lock = channel.lock(0L, Long.MAX_VALUE, true)) {

            ByteBuffer buffer = ByteBuffer.allocate((int) channel.size());
            channel.read(buffer);
            buffer.flip();
            return StandardCharsets.UTF_8.decode(buffer).toString();
        }
    }

    public static void write(String content) throws IOException {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);

        try (FileChannel channel = FileChannel.open(
                OUTPUT_FILE,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING);
             FileLock lock = channel.lock()) {

            channel.write(ByteBuffer.wrap(bytes));
            channel.force(true);
        }
    }

    public static List<String> extractWords(String filePath) throws IOException {
        String text = read(filePath);

        if (text.isBlank()) {
            return Collections.emptyList();
        }

        return Arrays.asList(text.trim().split("\\s+"));
    }
}
