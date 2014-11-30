# toylisp

Very simple and basic Lisp interpreter written in Java

## Build

You need JDK 1.7+ to build toylisp.

Go to the project directory from a terminal, and type the following command
to build:

```
./gradlew build
```

If you are running Windows, use the following command instead:

```
gradlew.bat build
```

If everything goes well, you we find a jar under `build/libs` named `toylist-VERSION.jar`.

## Usage

Run the main class to get a REPL and you are free to play with it:

```
java -cp build/libs/toylisp-0.1.0-SNAPSHOT.jar org.toylisp.Main
```

## Data Types

Currently only three:

- Symbol
- String
- Number (implemented with Java `BigDecimal`)

## Operations Supported
Currently only the following operators are supported:

- Special forms:
    - `def` Define a global variable
    - `lambda` Define a function
    - `do` Execute forms in sequence and return the value of the last expression
    - `cond` Conditional expression. Note that only one level of parentheses is needed, like in Clojure.
    - `quote`
- Functions: `cons`, `car`, `cdr`, `+`, `-`, `\*`, `/`, `eq?`


## TODO

Here is a list of features I'm planning to implement:

- macros
- core library written in toylisp itself, including but not limited to the following operations:
    -`let`, `if`, `when`, `when-let`, `if-let`, `->`, `->>` (implemented with macros)
    - `map`, `mapc`, `mapcat`, `cat` and other list manipulation functions
- a metacircular interpreter implemented with toylisp itself

## License

Copyright © 2014 Jerry Peng

Distributed under the Eclipse Public License, the same as Clojure.

