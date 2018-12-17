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

Note: Currently the dependency analysis requires that package and class names follow conventions (lowercase package names and camelcase class names)! This will be fixed in a future release (but is easier said than done).

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
**Download a pre-built version**
1) Download the build from my dropbox: [DependencyAnalyser 0.1-20181201](https://www.dropbox.com/s/a0xaqhmlah7ikil/dependency-analyser-0.1-20181201.zip?dl=0)
2) Extract the .zip archive. Make sure both `dependency-analyser-[VERSION].jar` and `libs` end up next to each other.
4) Run the jar file using `javaw -jar dependency-analyser-[VERSION].jar` or by double-clicking it.

**Build the latest version using Maven**
1) Clone the repository.
2) Run `mvn package`.
3) Find the runnable jar-file and required libraries in `target/packaged`.
4) Run the jar file using `javaw -jar dependency-analyser-[VERSION].jar` or by double-clicking it.

### Using DependencyAnalyser
**Basics**
* Use `File -> Open` and choose a folder containing Java *source* files to analyse a project.
* Select a package in the list on the left to view the dependencies and/or cycles for that package.
* Double-clicking a package in a graph will switch to that package.
* Double-clicking an arrow in a graph will show the classes that create that arrow.

**Colour legend**
* Blue = packages in the analysed project.
* Red = packages in the analysed project that are part of at least one cycle.
* Green = external packages (i.e. packages outside the analysed project).


* **Note**: Two red packages can have a blue arrow between them. This means that both packages are part of at least one cycle each, but share no common cycles.

### Dependencies
Interactive graphs:
* [JGraphX](https://github.com/jgraph/jgraphx)
* [JGraphT](https://github.com/jgrapht/jgrapht)

Other
* [Komposten's Utilities](https://github.com/Komposten/Utilities)

### License
This project is currently under exclusive copyright (owned by me, Komposten).
An open source license will be added in the future.
