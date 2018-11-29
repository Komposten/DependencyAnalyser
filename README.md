# DependencyAnalyser
### What is DependencyAnalyser?
DependencyAnalyser is a simple program that analyses Java source code to give information about the code quality.
Currently it only analyses the dependency hierarchy between packages and finds cyclic dependencies, but
future versions will include other statistics as well.

### Features
**Current features**
- View the dependencies of individual packages.
- See which packages are part of cyclic dependencies.
- View graphs of all cyclic dependencies a package is part of.
- View which classes in one package are dependent on what classes in another package.

 Dependency view |Cycle view |Class view 
--- | --- | ---
![Dependency view](../assets/screenshots/dependency_view.png?raw=true)|![Dependency view](../assets/screenshots/cycle_view.png?raw=true)|![Dependency view](../assets/screenshots/class_view.png?raw=true)

**Planned features**
- More statistics (like length of code elements, and method-level cyclomatic complexity).
- Multi-threading of the analysis.
- Export the analysis result as HTML.
- Save individual graphs as images.
- Ability to run the software as a command-line tool.

### Running DependencyAnalyser
Using Maven:
1) Clone the repository.
2) Run `mvn package`.
3) Find the runnable jar-file and required libraries in `target/packaged`.
4) Run the jar file using `javaw -jar dependency-analyser-[VERSION].jar` or by double-clicking it.

### Dependencies
Interactive graphs:
* [JGraphX](https://github.com/jgraph/jgraphx)
* [JGraphT](https://github.com/jgrapht/jgrapht)

Other
* [Komposten's Utilities](https://github.com/Komposten/Utilities)

### License
This project is currently under exclusive copyright (owned by me, Komposten).
An open source license will be added in the future.
