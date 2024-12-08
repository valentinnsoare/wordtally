package io.valentinsoare.wordtally.service;

import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/***
 * Defines the contract for processing input options and arguments.
 * This interface outlines methods for extracting tasks and file locations from command line arguments,
 * running tasks based on the input, printing help information, checking file availability and permissions,
 * and executing specified tasks on given files or input streams.
 */
public interface InputOptionsAsArguments {
    Map<String, List<String>> extractTypeOfTasksAndLocationsFromInput(String[] arguments) throws ParseException;
    void runTasksFromInput(String[] arguments, InputStream inputStream);
    void printHelp(Options options);
    List<String> checkFilesAvailabilityAndPermissions(List<String> locations);
    List<Long> executeTasks(List<String> options, Path inputFile) throws InterruptedException;
}
