package io.valentinsoare.wordtally.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.valentinsoare.wordtally.exception.ErrorMessage;
import io.valentinsoare.wordtally.exception.Severity;
import io.valentinsoare.wordtally.outputformat.OutputFormat;
import io.valentinsoare.wordtally.setupasync.DynamicThreadPoolManager;
import org.apache.commons.cli.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/***
 * This class is tagged as a service and injected into the runner and
 * then called runTasksFromInput method from the runner to start the options and arguments processing.
 * In other words, this method runTasksFromInput is the entry point into the app design system.
 */

@Service
public class ActOnInputOptionsProcessingAsAService implements InputOptionsAsArguments {
    private final OutputFormat outputFormat;
    private final ParsingAsAService parsingAsAService;
    private final ProcessingAsAService processingAsAService;
    private Options requiredOptions;

    /**
     * Constructor for ActOnInputOptionsProcessingAsAService.
     * It initializes the service with dependencies for output formatting, parsing, and processing.
     * Also, it prepares the command line options that will   be available for the user.
     *
     * @param outputFormat The service for formatting output.
     * @param parsingAsAService The service for parsing input.
     * @param processingAsAService The service for processing the input.
     */
    @Autowired
    private ActOnInputOptionsProcessingAsAService(OutputFormat outputFormat,
                                                 ParsingAsAService parsingAsAService,
                                                 ProcessingAsAService processingAsAService) {
        this.outputFormat = outputFormat;
        this.parsingAsAService = parsingAsAService;
        this.processingAsAService = processingAsAService;

        prepareOptionsAvailable();
    }

    /**
     * Prepares the command line options that the application will accept.
     * This includes options for counting lines, words, characters, bytes, and displaying help.
     */
    private void prepareOptionsAvailable() {
        this.requiredOptions = new Options();

        Option lines = createOption("l", "lines", "print the newline counts");
        Option words = createOption("w", "words", "print the word counts");
        Option chars = createOption("m", "chars", "print the character counts");
        Option bytes = createOption("c", "bytes", "print the byte counts");
        Option help = createOption("h", "help", "print the help page");

        requiredOptions.addOption(lines)
                .addOption(words)
                .addOption(chars)
                .addOption(bytes)
                .addOption(help);
    }

    /**
     * Creates a command line option.
     *
     * @param shortName The short name of the option (e.g., "l" for lines).
     * @param longName The long name of the option (e.g., "lines").
     * @param description A description of what the option does.
     * @return The created Option object.
     */
    private Option createOption(String shortName, String longName,
                                String description) {
        return Option.builder(shortName)
                .longOpt(longName)
                .desc(description)
                .required(false)
                .build();
    }

    /**
     * Prints the help message to the console.
     * This includes a brief description of the application, usage instructions, and a list of all available options.
     *
     * @param options The command line options to include in the help message.
     */
    @Override
    public void printHelp(Options options) {
        HelpFormatter helpFormatter = new HelpFormatter();

        try (PrintWriter printWriter = new PrintWriter(System.out)) {
            printWriter.printf("%nWordTally v0.0.3%n");

            printWriter.printf("%n%s%n%s%n%s%n%n",
                    "Application counts the number of lines, words, characters, and bytes\nfrom one or more text files or from keyboard and prints the results to standard output.",
                    "It's not possible to count the number of lines or words in binary files or another type of special files that are not text.",
                    "For these you can count only bytes or chars.");
            helpFormatter.printUsage(printWriter, 100, "java -jar wordtally.jar [OPTION]...  [FILE]...");
            helpFormatter.printOptions(printWriter, 100, options, 2, 5);
            printWriter.printf("%n%s%n%n", "WordTally was written by Valentin Soare.\nPlease report any bugs to soarevalentinn@gmail.com.");
        }

        System.exit(0);
    }

    /**
     * Extracts and categorizes command line arguments into options and file locations.
     * It parses the command line arguments to distinguish between options (e.g., "-l", "--words") and file paths.
     *
     * @param arguments The command line arguments passed to the application.
     * @return A map with two keys: "options" for command line options and "locations" for file paths.
     */
    @Override
    public Map<String, List<String>> extractTypeOfTasksAndLocationsFromInput(String[] arguments) {
        List<String> optionsFromUser = new ArrayList<>();
        Map<String, List<String>> optionsAndLocationsFromUser = new HashMap<>();

        CommandLineParser commandLineParser = new DefaultParser();

        try {
            CommandLine commandLine = commandLineParser.parse(requiredOptions, arguments);

            for (Option o : requiredOptions.getOptions()) {
                if (commandLine.hasOption(o)) {
                    optionsFromUser.add(o.getLongOpt());
                }
            }

            List<String> locations = new ArrayList<>(Arrays.asList(commandLine.getArgs()));

            optionsAndLocationsFromUser.putAll(Map.ofEntries(
                    Map.entry("options", optionsFromUser),
                    Map.entry("locations", locations)
            ));
        } catch (ParseException e) {
            String v = e.getMessage()
                    .substring(e.getMessage().indexOf(":") + 1)
                    .trim()
                    .replace("-", "");

            for (String s : v.split("")) {
                if (!requiredOptions.hasOption(s)) {
                    System.err.printf("wordtally: invalid option -- '%s'%nTry 'wordtally -h|--help' for more information.%n", s);
                }
            }

            System.exit(0);
        }

        return optionsAndLocationsFromUser;
    }

    /**
     * Checks the availability and read permissions of the files specified in the command line arguments.
     * It filters out directories, non-existent files, and files without read permissions, printing appropriate messages for each.
     *
     * @param locations A list of file paths to check.
     * @return A list of file paths that are available and readable.
     */
    @Override
    public List<String> checkFilesAvailabilityAndPermissions(List<String> locations) {
        /*
         * Here we need a sync/thread safe list
         * in order to have available all the files processed in parallel on multiple threads.
         */
        List<String> availableFiles = Collections.synchronizedList(new ArrayList<>());

        /*
         * Here we process the destinations/locations in parallel on multiple streams with multithreading.
         */
        locations.parallelStream().forEach(f -> {
            File file = new File(f);

            if (file.isDirectory()) {
                System.err.printf("wordtally: %s: Is a directory.%n", f);
            } else if (Files.notExists(Path.of(f))) {
                System.err.printf("wordtally: %s: No such file or directory.%n", f);
            } else if (!file.canRead()) {
                System.err.printf("wordtally: %s: Permission denied.%n", f);
            } else {
                availableFiles.add(f);
            }
        });

        return availableFiles;
    }

    /**
     * Constructs the output to be printed to the console for each file processed.
     * It formats the results (for example, counts of lines, words, characters, bytes) and prints them alongside the file name.
     *
     * @param results The list of result counts to print.
     * @param fileToPrint The name of the file these results correspond to.
     */
    private void constructOutputToPrint(List<Long> results, String fileToPrint) {
        for (long value : results) {
            if (value >= 0) {
                System.out.printf("%13s", value);
            }
        }

        if (fileToPrint != null) {
            System.out.printf("%s", " ".repeat(3));
            System.out.printf("%s%n", fileToPrint);
        } else {
            System.out.println();
        }
    }

    /**
     * Calculates and prints the total counts for all files if multiple files were processed.
     * It sums up the counts for each metric (lines, words, characters, bytes) across all files and prints the totals.
     *
     * @param givenValuesFromCounter A list of lists, where each inner list contains the counts for a single file.
     * @param nCol The number of columns to print, determined by the number of options specified by the user.
     */
    private void calculateTotalIfMultipleFilesAndPrint(List<List<Long>> givenValuesFromCounter, int nCol) {
        List<Long> v = Collections.emptyList();

        try {
            v = givenValuesFromCounter.get(0);
        } catch (IndexOutOfBoundsException e) {
            System.exit(1);
        }

        List<Long> calcTotal = new ArrayList<>(v);

        for (int j = 1; j < givenValuesFromCounter.size(); j++) {
            for (int i = 0; i < nCol; i++)
                calcTotal.set(i, givenValuesFromCounter.get(j).get(i) + calcTotal.get(i));
        }

        boolean toPrintTotalTag = false;

        for (long value : calcTotal) {
            if (value >= 0) {
                toPrintTotalTag = true;
                System.out.printf("%13s", value);
            }
        }

        if (toPrintTotalTag) {
            System.out.printf("%s", " ".repeat(3));
            System.out.printf("%s%n", "total");
        }
    }

    /**
     * The main method that orchestrates the processing of input tasks from command line arguments or standard input.
     * It extracts tasks and file locations from the input, checks file permissions, and processes each file or standard input accordingly.
     *
     * @param arguments The command line arguments.
     * @param inputStream The standard input stream, used if no files are specified.
     */

    @Override
    public void runTasksFromInput(String[] arguments, InputStream inputStream) {
        Map<String, List<String>> tasksAndFiles = extractTypeOfTasksAndLocationsFromInput(arguments);
        List<String> options = tasksAndFiles.get("options"), locations = tasksAndFiles.get("locations");
        Semaphore semaphore = DynamicThreadPoolManager.getSemaphore();
        Map<String, CompletableFuture<List<Long>>> results = new ConcurrentHashMap<>();

        if (!locations.isEmpty()) {
            List<String> filesToBeProcess = checkFilesAvailabilityAndPermissions(locations);

            for (String f : filesToBeProcess) {
                Path file = Path.of(f);

                CompletableFuture<List<Long>> cfT = CompletableFuture.supplyAsync(() -> {
                    try {
                        semaphore.acquire();
                        return executeTasks(options, file);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        semaphore.release();
                    }

                    return Collections.emptyList();
                });

                results.put(file.toString(), cfT);
            }

            Map<String, List<Long>> rs = CompletableFuture.allOf(results.values().toArray(e -> new CompletableFuture[]{}))
                    .thenApply(w -> results.entrySet().stream()
                            .collect(Collectors.toMap(Map.Entry::getKey, v -> v.getValue().join()))).join();

            for (Map.Entry<String, List<Long>> e : rs.entrySet()) {
                constructOutputToPrint(e.getValue(), e.getKey());
            }

            if (locations.size() > 1) {
                calculateTotalIfMultipleFilesAndPrint(new ArrayList<>(rs.values()),options.isEmpty() ? 3 : options.size());
            }
        } else {
            if (options.contains("help")) {
                printHelp(requiredOptions);
            }

            catchCheckTheReaderException(inputStream);

            List<Long> r = processingAsAService.execTheTasksWithCountingInParallelWithParallelStreams(options, inputStream);
            constructOutputToPrint(r, null);
        }
    }

    /**
     * Executes the specified tasks (e.g., counting lines, words, characters, bytes) on a given file.
     * It processes the file according to the options provided and returns the counts as a list of long values.
     *
     * @param options   A list of options specifying which counts to calculate.
     * @param inputFile The path to the file to be processed.
     * @return A list of long values representing the counts for each requested metric.
     */
    @Override
    public List<Long> executeTasks(List<String> options, Path inputFile) {
        List<CompletableFuture<Long>> allCFs = Collections.synchronizedList(new ArrayList<>());

        if (options.isEmpty()) {
            allCFs.addAll(Arrays.asList(
                        parsingAsAService.countTheNumberOfLines(inputFile),
                        parsingAsAService.countTheNumberOfWords(inputFile),
                        parsingAsAService.countTheNumberOfBytes(inputFile)
                    )
            );
        } else {
            for (String s : options) {
                switch (s) {
                    case "lines" -> allCFs.add(parsingAsAService.countTheNumberOfLines(inputFile));
                    case "words" -> allCFs.add(parsingAsAService.countTheNumberOfWords(inputFile));
                    case "chars" -> allCFs.add(parsingAsAService.countTheNumberOfChars(inputFile));
                    case "bytes" -> allCFs.add(parsingAsAService.countTheNumberOfBytes(inputFile));
                    default -> printHelp(requiredOptions);
                }
            }
        }

        return CompletableFuture.allOf(allCFs.toArray(e -> new CompletableFuture[]{}))
                .thenApply(v -> allCFs.stream()
                        .map(CompletableFuture::join)
                        .toList()).join();
    }

    /**
     * Checks if the input stream is ready and prints an error message if no input is provided.
     * This method is used to ensure that there is input available when the application expects to read from standard input.
     *
     * @param inputStream The input stream to check.
     */
    private void catchCheckTheReaderException(InputStream inputStream) {
        try {
            if (!parsingAsAService.checkTheReaderIsReady(inputStream)) {
                System.err.printf("wordtally: no input provided%nTry 'wordtally -h|--help' for more information.%n");
                System.exit(0);
            }
        } catch (IOException e) {
            ErrorMessage msg = ErrorMessage.builder()
                    .threadName(Thread.currentThread().getName())
                    .clazzName(this.getClass().getName())
                    .message(e.getMessage())
                    .severity(Severity.ERROR)
                    .methodName("runTasksFromInput")
                    .dateTime(Instant.now().toString())
                    .build();

            try {
                System.err.printf("%s %n", outputFormat.withJSONStyle().writeValueAsString(msg));
            } catch (JsonProcessingException ex) {
                System.err.printf("%n\033[1;31m%s - class: %s, method: %s, %s\0330m%n",
                        Severity.FATAL, this.getClass().getName(), "catchCheckTheReaderException", ex.getMessage());
                System.exit(0);
            }
        }
    }
}
