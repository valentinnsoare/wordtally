package io.valentinsoare.wordtally.auxiliary;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.valentinsoare.wordtally.exception.ErrorMessage;
import io.valentinsoare.wordtally.exception.Severity;
import io.valentinsoare.wordtally.outputformat.OutputFormat;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;

/**
 * Utility class for common operations.
 */
public class Utils {
    private static OutputFormat outputFormat;

    private Utils() {}

    static {
        outputFormat = new OutputFormat();
    }

    /**
     * Determines whether a file is binary by reading the first 3KB of the file and checking for non-printable characters.
     *
     * @param path The path to the file to check.
     * @return True if the file is binary, false otherwise.
     */
    public static boolean isBinaryFile(Path path) {
        try (FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.READ)) {
            ByteBuffer buffer = ByteBuffer.allocate(5120);

            if (fileChannel.read(buffer) > 0) {
                buffer.flip();

                for (int i = 0; i < buffer.limit(); i++) {
                    byte b = buffer.get(i);

                    if (b < 0x09 || (b > 0x0D && b < 0x20) || b == 0x7F) {
                        return true;
                    }
                }

                buffer.clear();
                return false;
            }

            return false;
        } catch (IOException e) {
            ErrorMessage msg = ErrorMessage.builder()
                    .severity(Severity.ERROR)
                    .methodName("isBinaryFile")
                    .message(e.getMessage())
                    .threadName(Thread.currentThread().getName())
                    .dateTime(Instant.now().toString())
                    .clazzName(Utils.class.getName())
                    .build();

            try {
                System.err.printf("%s %n", outputFormat.withJSONStyle().writeValueAsString(msg));
            } catch (JsonProcessingException ex) {
                System.err.printf("%n\033[1;31m%s - class: %s, method: %s, %s\0330m%n",
                        Severity.FATAL, Utils.class.getName(), "isBinaryFile", ex.getMessage());
            }

            System.exit(0);
        }

        return false;
    }
}
