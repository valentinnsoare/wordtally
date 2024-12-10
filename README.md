## :abc: WordTally - Build Your Own wc Tool

[![wordtally - Unix wc tool replica](https://img.shields.io/static/v1?label=wordtally&message=Unix+wc+tool+replica&color=2ea44f)](https://github.com/valentinsoare)

A replica of the wc tool, implemented in Java 17, designed to output the number of lines, words, characters, and bytes for each input file. If multiple files are specified, it also provides a total count of lines, words, characters, and bytes. A word is defined as a non-zero-length sequence of printable characters separated by whitespace.

### Concepts/technologies used:
1. [X] Object-Oriented Programming Principles;
2. [X] Collections Framework — Array, ArraysList and HashMap;
3. [X] Lambda functions, Streams and Method references along with serialization into a JSON;
4. [X] Design patterns from the GoF: Singleton and Builder along with Dependency Injection and Inversion of Control;
5. [X] Spring Boot with CommandLineRunner interface and Lombok, Jackson and Apache Commons CLI dependencies;
6. [X] GraalVM for conversion from .jar into a binary file;
7. [X] Logging and banner were disabled (spring.main.banner-mode=OFF, logging.level.root=WARN) on Spring Boot along with Tomcat Server (spring.main.web-application-type=NONE);
8. [X] Asynchronous with Concurrency processing implemented with CompletableStage/CompletableFuture API to handle multiple files as input much easier and faster;  
9. [X] IO and NIO libraries for input/output processing;

<br>

> [!NOTE]
> Challenge is :100: completed!

<br>

## :man_technologist: How To:

:white_check_mark: In the target directory in this repo you can find everything you need to run the app, the .jar and binary file made with GraalVM.

[![Screenshot-from-2024-04-21-07-46-08.png](https://i.postimg.cc/PqRf6LtY/Screenshot-from-2024-04-21-07-46-08.png)](https://moviesondemand.io)

<br>

:white_check_mark: Use it with short options and files as input.

![](contentforreadmepage/WordTallyClassicOptions.gif)

<br>

:white_check_mark: Use it with short/long options with files or standard input.

![](contentforreadmepage/WordTallyWithLongOptions.gif)

<br>

:white_check_mark: What happens when proper input is not provided (wrong options, files, etc).

![](contentforreadmepage/WordTallyWithErrorsHelpAndRedirectToAnotherCommand.gif)

:point_right: If you want to see the code and make additions, you can clone this repo and try it. In case you want a binary file
as a final product, then you need to install SDKMAN from sdkman.io and then with sdk GraalVM (22.3.r17-nik). 
Please know that for this project I'm using Java 17 Corretto from Amazon.
Before you start to build the binary file with Maven, make sure that Runtime Environment is set to GraalVM as you can see bellow:
```
[root@republic]# java --version
openjdk 17.0.5 2022-10-18 LTS
OpenJDK Runtime Environment GraalVM 22.3.0 (build 17.0.5+8-LTS)
OpenJDK 64-Bit Server VM GraalVM 22.3.0 (build 17.0.5+8-LTS, mixed mode, sharing)
[root@republic]#
```

<br>

:arrow_forward: Current UML diagram:

[![WordTallyUMLDiagram.png](https://i.postimg.cc/ydT5MsVH/Word-Tally-UMLDiagram.png)](https://moviesondemand.io)

<br>

[![valentinsoare - wordtally](https://img.shields.io/static/v1?label=valentinnsoare&message=wordtally&color=blue&logo=github)](https://github.com/valentinnsoare/wordtally "Go to Github project")
[![stars - wordtally](https://img.shields.io/github/stars/valentinnsoare/wordtally?style=social)](https://github.com/valentinnsoare/wordtally/stargazers)
[![forks - wordtally](https://img.shields.io/github/forks/valentinnsoare/wordtally?style=social)](https://github.com/valentinnsoare/wordtally/forks)
[![License](https://img.shields.io/badge/License-MIT-blue)](#license)

### Contact

For any inquiries, please contact me www.linkedin.com/in/valentin-soare
