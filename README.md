Java in The Browser
===================

Java programming language implementation that runs in a browser. It includes:
* a port of the javac compiler
* a simple JVM
* a selection of the classes in JRE

A live demo can be found at http://javainthebrowser.appspot.com.

Build with Maven
----------------

This project now includes a Maven build that runs the JVM tests and produces
static GWT output for the `javac` and `jib` modules.

```sh
mvn test
mvn -DskipTests package
```

The compiled output is placed in `target/war/`. You can serve it from any
static HTTP server, for example:

```sh
python -m http.server 8080 --directory target/war
```

Then open `http://localhost:8080/Jib.html` in a browser.
