package io.valentinsoare.wordtally.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.valentinsoare.wordtally.exception.ErrorMessage;
import io.valentinsoare.wordtally.exception.Severity;
import io.valentinsoare.wordtally.outputformat.OutputFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

/***
 *  Here, as you can see by the name, we have the design on how to process the input coming from inputStream (System.in)
 * Here we tackle the input data in a simple way.
 * This is the first version, but in the next one I will use async and parallel processing here and define more methods, one per type of task. In this case, each time we call a method
 * we need to mark the BufferReader object, and then if another method is called, we do a reader. Reset to reset the inputStream to
 * beginning and then mark it again and start the app logic.
 * Also, we are going to need more BufferReader objects, one BufferReader per type of task,
 * and when we create these bufferReaders, we need to use reset in other to go to the beginning of the initial inputStream
 * */

@Service
public class ProcessingTheInputFromPipeline implements ProcessingAsAService {

    private final OutputFormat outputFormat;

    /**
     * Constructor that initializes the service with a dependency for formatting output messages.
     *
     * @param outputFormat The service for formatting output.
     */
    @Autowired
    public ProcessingTheInputFromPipeline(OutputFormat outputFormat) {
        this.outputFormat = outputFormat;
    }

    /**
     * Counts and prints the number of lines, words, characters, and bytes from an input stream based on specified flags.
     * This method demonstrates a basic approach to processing input, with future enhancements planned for parallel execution.
     *
     * @param inputStream The input stream to be processed.
     * @param toCountLines Flag indicating whether to count lines.
     * @param toCountWords Flag indicating whether to count words.
     * @param toCountChars Flag indicating whether to count characters.
     * @param toCountBytes Flag indicating whether to count bytes.
     * @return A list of long values representing the counts of the specified elements.
     * @throws IOException If an I/O error occurs during processing.
     */
    @Override
    public List<Long> countingAndPrinting(InputStream inputStream, boolean toCountLines, boolean toCountWords,
                                          boolean toCountChars, boolean toCountBytes) throws IOException {
        long numberOfChars = 0, numberOfLines = 0, numberOfWords = 0, numberOfBytes = 0;
        inputStream.mark(Integer.MAX_VALUE);

        if (toCountBytes) {
            int readBytes;
            byte[] buffer = new byte[2048];

            while ((readBytes = inputStream.read(buffer)) != -1) {
                numberOfBytes += readBytes;
            }
        }

        int byteRead;
        boolean isWord = false;

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        inputStream.reset();

        while ((byteRead = reader.read()) != -1) {
            if ((toCountLines) && (byteRead == '\n')) {
                numberOfLines += 1;
            }

            if (toCountWords) {
                if (Character.isWhitespace(byteRead)) {
                    if (isWord) {
                        isWord = false;
                    }
                } else {
                    if (!isWord) {
                        isWord = true;
                        numberOfWords += 1;
                    }
                }
            }

            if (toCountChars) {
                numberOfChars += 1;
            }
        }

        return Stream.of(numberOfLines, numberOfWords, numberOfChars, numberOfBytes)
                .filter(e -> e != 0)
                .toList();
    }

    /**
     * Executes counting tasks in parallel using parallel streams, based on the options provided.
     * This method is a placeholder for future implementation that will leverage parallel processing to enhance performance.
     *
     * @param options A list of options specifying which counts to calculate (e.g., lines, words, characters, bytes).
     * @param inputStream The input stream to be processed.
     * @return A list of long values representing the counts for each requested metric, or an empty list in case of an error.
     */
    @Override
    public List<Long> execTheTasksWithCountingInParallelWithParallelStreams(List<String> options, InputStream inputStream) {
        Map<String, Boolean> taskOptions = initializeTaskOptions(options);

        try {
            return performCountingTasks(inputStream, taskOptions);
        } catch (IOException e) {
            handleIOException(e, "executeTasksWithParallelStreams");
            return Collections.emptyList();
        }
    }

    /**
     * Initializes task options based on the provided list of options.
     * This method maps each option (for example, "lines", "words", "chars", "bytes") to a boolean value indicating whether the task should be executed.
     *
     * @param options A list of options specifying which counts to calculate.
     * @return A map of task options to boolean values.
     */
    private Map<String, Boolean> initializeTaskOptions(List<String> options) {
        Map<String, Boolean> taskOptions = new HashMap<>();
        List<String> availableOptions = new ArrayList<>(Arrays.asList("lines", "words", "bytes", "chars"));

        for (String option : availableOptions) {
            taskOptions.put(option, options.contains(option));
        }

        if (Collections.frequency(taskOptions.values(), false) == 4) {
            taskOptions = new HashMap<>(Map.ofEntries(
                    Map.entry("lines", true),
                    Map.entry("words", true),
                    Map.entry("bytes", true),
                    Map.entry("chars", false)
            ));
        }

        return taskOptions;
    }

    /**
     * Performs the counting tasks based on the specified options and input stream.
     * This method delegates to {@link #countingAndPrinting(InputStream, boolean, boolean, boolean, boolean)} to perform the actual counting.
     *
     * @param inputStream The input stream to be processed.
     * @param taskOptions A map of task options to boolean values.
     * @return A list of long values representing the counts for each requested metric.
     * @throws IOException If an I/O error occurs during processing.
     */
    private List<Long> performCountingTasks(InputStream inputStream, Map<String, Boolean> taskOptions) throws IOException {
        return countingAndPrinting(inputStream, taskOptions.get("lines"), taskOptions.get("words"),
                taskOptions.get("chars"), taskOptions.get("bytes")
        );
    }

    /**
     * Handles IOExceptions by logging the error message and throwing a runtime exception.
     * This method formats the error message using the {@link OutputFormat} service and prints it to the console.
     *
     * @param e The IOException encountered during processing.
     * @param methodName The name of the method where the exception occurred.
     */
    private void handleIOException(IOException e, String methodName) {
        ErrorMessage msg = ErrorMessage.builder()
                .threadName(Thread.currentThread().getName())
                .methodName(methodName)
                .clazzName(this.getClass().getName())
                .message(e.getMessage())
                .dateTime(Instant.now().toString())
                .severity(Severity.ERROR)
                .build();

        try {
            System.err.printf("%s %n", outputFormat.withJSONStyle().writeValueAsString(msg));
        } catch (JsonProcessingException ex) {
            System.err.printf("%n\033[1;31m%s - class: %s, method: %s, %s\0330m%n",
                    Severity.FATAL, this.getClass().getName(), "handleIOException", ex.getMessage());
            System.exit(0);
        }
    }
}
