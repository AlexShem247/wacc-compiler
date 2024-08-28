## WACC Compiler and IDE

This repository contains a group project developed for Imperial College London's Year 2 Laboratory module. The project is a compiler for the WACC language, converting WACC code into x86-64 assembly code using Scala and the Parsley library for parsing.

### Table of Contents
- [About WACC](#about-wacc)
- [Features](#features)
- [Extensions](#extensions)
- [Dependencies](#dependencies)
- [Getting Started](#getting-started)
- [Usage](#usage)
- [Contributors](#contributors)
- [License](#license)

### About WACC

**WACC** (pronounced "whack") is a simplified language that shares characteristics with the While family of languages, which are commonly studied in program reasoning and verification courses. WACC was designed to be simple yet expressive enough to provide meaningful learning experiences in both theoretical and practical aspects of computing.

Key features of WACC include:
- **Basic data types**: integers, booleans, characters, and strings.
- **Structured types**: arrays and tuples.
- **Control flow**: conditionals, loops, and function definitions.
- **Memory management**: heap allocation and garbage collection.

WACC serves as a bridge between the theoretical concepts taught in courses like **Models of Computation** and practical skills taught in courses like **Compilers**.

### Features

- **Lexical Analysis and Parsing**: Implemented using the Parsley library for efficient and flexible parsing.
- **Semantic Analysis**: Type checking, scope management, and semantic error detection.
- **Code Generation**: Compiles WACC code to x86-64 assembly code, supporting basic optimisations.
- **Error Reporting**: Provides detailed error messages for both syntactic and semantic errors.
- **Modular Design**: Clean separation between different stages of compilation for maintainability and extensibility.

### Extensions

The compiler project went beyond the base specification to implement several notable extensions:

#### 1. **Standard Library with Import Functionality**

The compiler was extended to support the import of other WACC files, along with a custom standard library written in WACC and IR. This library includes functionalities like random integer generation, getting the current date and time, and sleeping for a specified duration. These were implemented using C library calls.

#### 2. **WACC Integrated Development Environment (IDE)**

We developed an IDE for the WACC language using Java Swing in Scala. The IDE supports syntax highlighting, real-time syntax and semantic error checking, run functionality, and integrated unit testing through an "Assertion" library. This IDE provides a more streamlined development experience for WACC programmers.

#### 3. **Parallel Compilation Optimisation**

The compiler incorporates a parallel compilation optimisation that leverages concurrent programming to speed up the compilation process. Tasks such as translating functions and the main program body are executed concurrently using Scala's Future API. This optimisation reduces overall compilation time by utilising multiple CPU cores efficiently.

#### 4. **Global Monomorphic Type Inference**

We implemented a type inference system for locally inferred types, including return types of functions and local variables. This feature simplifies the code, reduces the need for explicit type annotations, and improves developer productivity by catching potential type-related errors early in the compilation process.

#### 5. **Full Pair Types Support**

Traditional WACC semantics allowed pairs but required nested pairs to be erased to a base `pair` type. We extended the language to support fully nested pairs, preserving type information and preventing information loss during type checks and code generation.

#### 6. **Control-Flow Analysis for Optimisation**

We introduced control-flow analysis to optimise program structures like functions, loops, and branching. This included optimisations like loop unrolling, dead code elimination, and branch merging to produce more efficient assembly code.

### Dependencies

- **Scala**: The project is developed using Scala 2.13.1.
- **Parsley**: A library for parser combinators in Scala, used for parsing WACC code.
- **JUnit**: For testing various components of the compiler.

### Getting Started

To get started with the WACC Compiler, follow these steps:

1. **Download the JAR file**: Visit the [Releases](https://github.com/AlexShem247/wacc-compiler/releases) page on this repository and download the latest version of the WACC Compiler JAR file.

2. **Prepare your environment**: Make sure you have **Java** installed on your system (Java 8 or higher is recommended).

3. **Write WACC code**: You can write WACC programs in your favorite text editor, or you can download and use the WACC IDE from the [Releases](https://github.com/AlexShem247/wacc-compiler/releases) page, which offers additional features like syntax highlighting, code snippets, and integrated compiler support.

### Usage

To compile a WACC program to x86-64 assembly, use the following command:

```bash
java -jar WACC.jar path/to/your/program.wacc
```

This will generate an assembly file (`.s`) in the same directory as the source file. The assembly code can then be compiled and linked using an assembler like `gcc` or `nasm`.

For example, to compile and run the generated assembly code using `gcc`, you can use:

```bash
gcc -o output_file path/to/your/program.s
./output_file
```

By using the WACC IDE, you can streamline the development process and leverage additional tools to enhance your workflow.

### Contributors

This project was a collaborative effort developed by a dedicated team of students from Imperial College London. Each member brought their unique skills and expertise to various aspects of the WACC Compiler's development.

- [Alexander Shemaly](https://github.com/AlexShem247)
- [Aniket Gupta](https://github.com/aniket1101)
- [Alejandro Perez Fadon](https://github.com/Aito0)
- Alexander Balinsky


### License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for more details.