package io.valentinsoare.wordtally.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.valentinsoare.wordtally.auxiliary.Utils;
import io.valentinsoare.wordtally.exception.ErrorMessage;
import io.valentinsoare.wordtally.exception.Severity;
import io.valentinsoare.wordtally.outputformat.OutputFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Stream;

/***
 * Here we defined several methods that are in fact used when files are given as arguments.
 * With these methods, we count the lines, words, chars and bytes in these files.
 * Almost all of them are used in async way and processing the files is done in parallel.
 * Since it is async, we can process more files at a time in case it is needed and increase the throughput.
 */
@Service
public class ParseTheInput implements ParsingAsAService {
    private final OutputFormat outputFormat;

    /**
     * Constructs a new ParseTheInput instance with a dependency on OutputFormat for formatting output messages.
     *
     * @param outputFormat The service for formatting output.
     */
    @Autowired
    private ParseTheInput(OutputFormat outputFormat) {
        this.outputFormat = outputFormat;
    }

    /**
     * Asynchronously counts the number of lines in a given file.
     * Utilizes parallel streams to enhance performance.
     *
     * @param inputFile The path to the file whose lines are to be counted.
     * @return A CompletableFuture that, upon completion, returns the count of lines in the file.
     */
    @Async
    @Override
    public CompletableFuture<Long> countTheNumberOfLines(Path inputFile) {
        try (Stream<String> execCounting = Files.lines(inputFile)) {
           return CompletableFuture.completedFuture(execCounting.count());
        } catch (IOException e) {
            ErrorMessage msg = ErrorMessage.builder()
                    .severity(Severity.ERROR)
                    .threadName(Thread.currentThread().getName())
                    .methodName("countTheNumberOfLines")
                    .clazzName(this.getClass().getName())
                    .dateTime(Instant.now().toString())
                    .message(e.getMessage())
                    .build();

            try {
                System.err.printf("%s %n", outputFormat.withJSONStyle().writeValueAsString(msg));
            } catch (JsonProcessingException ex) {
                System.err.printf("%n\033[1;31m%s - class: %s, method: %s, %s\0330m%n",
                        Severity.FATAL, this.getClass().getName(), "countTheNumberOfLines", ex.getMessage());
            }

            System.exit(0);
        } catch (CompletionException | UncheckedIOException f) {
            /*
             *   This buffer occupies space on the actual storage device, and it's set to 1048576 bytes
             *   in order to help us to read input from pipeline, like from another command.
             */
            ByteBuffer buffer = ByteBuffer.allocateDirect(1048576);

            int count = 0;
            final int newLine = 0xA;

            try (FileChannel channel = FileChannel.open(inputFile, StandardOpenOption.READ)) {
                while (channel.read(buffer) != -1) {
                    buffer.flip();

                    for (int i = 0; i < buffer.limit(); i++) {
                        if (buffer.get(i) == newLine) {
                            count++;
                        }
                    }

                    buffer.clear();
                }
            } catch (IOException e) {
                System.err.printf("%n\033[1;31m%s - class: %s, method: %s, %s\0330m%n",
                        Severity.FATAL, this.getClass().getName(), "countTheNumberOfLines", e.getMessage());
                System.exit(0);
            }

            return CompletableFuture.completedFuture((long) count);
        }

        return CompletableFuture.completedFuture(-1L);
    }

    /**
     * Logs an error message when an exception occurs during the counting of characters.
     *
     * @param message The error message to be logged.
     */
    public void printExceptionForCountingChars(String message) {
        ErrorMessage msg = ErrorMessage.builder()
                .severity(Severity.ERROR)
                .methodName("countTheNumberOfChars")
                .message(message)
                .threadName(Thread.currentThread().getName())
                .dateTime(Instant.now().toString())
                .clazzName(this.getClass().getName())
                .build();

        try {
            System.err.printf("%s %n", outputFormat.withJSONStyle().writeValueAsString(msg));
        } catch (JsonProcessingException ex) {
            System.err.printf("%n\033[1;31m%s - class: %s, method: %s, %s\0330m%n",
                    Severity.FATAL, this.getClass().getName(), "countTheNumberOfChars", ex.getMessage());
        }
    }

    /**
     * Asynchronously counts the number of characters in a given file.
     * Reads the file character by character to ensure accurate counting.
     *
     * @param inputFile The path to the file whose characters are to be counted.
     * @return A CompletableFuture that, upon completion, returns the count of characters in the file.
     */
    @Async
    @Override
    public CompletableFuture<Long> countTheNumberOfChars(Path inputFile) {
        long numberOfChars = 0;

        if (!Utils.isBinaryFile(inputFile)) {
            try (BufferedReader reader =
                         new BufferedReader(new InputStreamReader(Files.newInputStream(inputFile), StandardCharsets.UTF_8))) {
                while (reader.read() != -1) {
                    numberOfChars += 1;
                }

                return CompletableFuture.completedFuture(numberOfChars);
            } catch (IOException e) {
                printExceptionForCountingChars(e.getMessage());
            }

            return CompletableFuture.completedFuture(-1L);
        }

        ByteBuffer buffer = ByteBuffer.allocateDirect(1048576);

        try (FileChannel channel = FileChannel.open(inputFile, StandardOpenOption.READ)) {
            while (channel.read(buffer) != -1) {
                buffer.flip();

                for (int i = 0; i < buffer.limit(); i++) {
                    byte b = buffer.get(i);

                    if ((b >= 0x9 && b <= 0x7E) || ((b & 0xFF) >= 0x80)) {
                        numberOfChars++;
                    }
                }

                buffer.clear();
            }

            return CompletableFuture.completedFuture(numberOfChars);
        } catch (IOException e) {
            printExceptionForCountingChars(e.getMessage());
        }

        return CompletableFuture.completedFuture(-1L);
    }

    /**
     * Asynchronously counts the number of words in a given file, text/binary file.
     * Splits the file content by whitespace to identify words, using parallel streams for efficiency.
     *
     * @param inputFile The path to the file whose words are to be counted.
     * @return A CompletableFuture that, upon completion, returns the count of words in the file.
     */
    @Async
    @Override
    public CompletableFuture<Long> countTheNumberOfWords(Path inputFile) {
        try (Stream<String> s = Files.lines(inputFile)) {
            long wordCount = s.flatMap(line -> Stream.of(line.split("\\s")))
                    .filter(word -> !"".equals(word))
                    .count();

            return CompletableFuture.completedFuture(wordCount);
        } catch (IOException e) {
            ErrorMessage msg = ErrorMessage.builder()
                    .clazzName(this.getClass().getName())
                    .methodName("countTheNumberOfWords")
                    .severity(Severity.ERROR)
                    .message(e.getMessage())
                    .threadName(Thread.currentThread().getName())
                    .dateTime(Instant.now().toString())
                    .build();
            try {
                System.err.printf("%s %n", outputFormat.withJSONStyle().writeValueAsString(msg));
            } catch (JsonProcessingException ex) {
                System.err.printf("%n\033[1;31m%s - class: %s, method: %s, %s\0330m%n",
                        Severity.FATAL, this.getClass().getName(), "countTheNumberOfWords", ex.getMessage());
                System.exit(0);
            }
        } catch (CompletionException | UncheckedIOException | UnsupportedOperationException f) {

            /*
             * This approach bellow is for counting number of words in a binary file. But please take notice that it's possible
             * counting not to be exact. We don't know the encoding for this, we just analyze the raw bytes and see if we have:
             * white spaces and printable characters.
             */
            long wordCount = 0L;
            ByteBuffer buffer = ByteBuffer.allocateDirect(1048576);
            boolean inWord = false;

            try (FileChannel channel = FileChannel.open(inputFile, StandardOpenOption.READ)) {
                while (channel.read(buffer) != -1) {
                    buffer.flip();

                    for (int i = 0; i < buffer.limit(); i++) {
                        byte b = buffer.get(i);

                        if ((b >= 0x21 && b <= 0x7E)) {
                            if (!inWord) {
                                wordCount++;
                                inWord = true;
                            }
                        } else if (b == 0x9 || b == 0xA || b == 0xB || b == 0xC || b == 0xD || b == 0x20) {
                            inWord = false;
                        }
                    }

                    buffer.clear();
                }
            } catch (IOException | CompletionException e) {
                System.err.printf("%n\033[1;31m%s - class: %s, method: %s, %s\0330m%n",
                        Severity.FATAL, this.getClass().getName(), "countTheNumberOfWords", e.getMessage());
                System.exit(0);
            }

            return CompletableFuture.completedFuture(wordCount);
        }

        return CompletableFuture.completedFuture(-1L);
    }

    /**
     * Asynchronously counts the number of bytes in a given file.
     * Reads the file in chunks to efficiently count the bytes.
     *
     * @param inputFile The path to the file whose bytes are to be counted.
     * @return A CompletableFuture that, upon completion, returns the count of bytes in the file.
     */
    @Async
    @Override
    public CompletableFuture<Long> countTheNumberOfBytes(Path inputFile) {
        try (InputStream inputStream = Files.newInputStream(inputFile)) {
           long bytesCount = 0L;

            int byteRead;
            byte[] bytes = new byte[1048576];

            while ((byteRead = inputStream.read(bytes)) != -1) {
                bytesCount += byteRead;
            }

            return CompletableFuture.completedFuture(bytesCount);
        } catch (IOException e) {
            ErrorMessage msg = ErrorMessage.builder()
                    .threadName(Thread.currentThread().getName())
                    .clazzName(this.getClass().getName())
                    .methodName("countTheNumberOfBytes")
                    .dateTime(Instant.now().toString())
                    .severity(Severity.ERROR)
                    .message(e.getMessage())
                    .build();

            try {
                System.err.printf("%s %n", outputFormat.withJSONStyle().writeValueAsString(msg));
            } catch (JsonProcessingException ex) {
                System.err.printf("%n\033[1;31m%s - class: %s, method: %s, %s\0330m%n",
                        Severity.FATAL, this.getClass().getName(), "countTheNumberOfBytes", ex.getMessage());
                System.exit(0);
            }
        }

        return CompletableFuture.completedFuture(-1L);
    }

    /**
     * Checks if the input stream is ready to be read.
     * This method is used to ensure that there is input available from the stream before attempting to read.
     *
     * @param inputStream The input stream to check.
     * @return true if the stream is ready to be read, false otherwise.
     * @throws JsonProcessingException If there is an error in processing the JSON output for error messages.
     */
    @Override
    public boolean checkTheReaderIsReady(InputStream inputStream) throws JsonProcessingException {
        try {
            return inputStream.available() > 0;
        } catch (IOException e) {
            ErrorMessage msg = ErrorMessage.builder()
                    .methodName("parseTheFileDescriptor")
                    .dateTime(Instant.now().toString())
                    .severity(Severity.ERROR)
                    .clazzName(this.getClass().getName())
                    .threadName(Thread.currentThread().getName())
                    .message(e.getMessage())
                    .build();

            System.err.printf("%s%n", outputFormat.withJSONStyle().writeValueAsString(msg));
            return false;
        }
    }
}
