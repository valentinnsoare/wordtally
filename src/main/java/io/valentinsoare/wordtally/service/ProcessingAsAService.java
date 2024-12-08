package io.valentinsoare.wordtally.service;

import java.io.*;
import java.util.List;

/***
 * The ProcessingAsAService interface defines the contract for services that process input streams or files based on specified options.
 * It outlines methods for executing tasks in parallel streams and for counting and printing elements (lines, words, characters, bytes) from an input stream.
 * Implementations of this interface are expected to handle the logic for processing input according to the options provided,
 * leveraging parallel processing where applicable to enhance performance.
 */
public interface ProcessingAsAService {
    List<Long> execTheTasksWithCountingInParallelWithParallelStreams(List<String> options, InputStream inputStream);
    List<Long> countingAndPrinting(InputStream inputStream, boolean toCountLines, boolean toCountWords, boolean toCountChars, boolean toCountBytes) throws IOException;
}