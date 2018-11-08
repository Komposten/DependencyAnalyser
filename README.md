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
The current version is not yet set up to be easy to build and run. Future versions will be built using Maven.
Running this version would require:
1) Cloning the repository.
2) Downloading the JGraphT and JGraphX libraries.
3) Downloading and building my own [utility library](https://github.com/komposten/utilities).
4) Adding the above mentioned libraries to the project build path/classpath.
5) Building the project. 

### Dependencies
DependencyAnalyser relies on the [JGraphX](https://github.com/jgraph/jgraphx) and [JGraphT](https://github.com/jgrapht/jgrapht) libraries for creating and rendering interactive graphs.

### License
This project is currently under exclusive copyright (owned by me, Komposten).
An open source license will be added in the future.
