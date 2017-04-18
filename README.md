
# Hippo JCR Runner

The JCR Runner makes it easy to do bulk operations on a running JCR repository. For example you can change values of certain properties of all nodes of a certain type or move nodes from one type to another. The JCR Runner connects over rmi to a running repository.

Writing your own plugin is simple. The only thing you have to do is to extend the AbstractRunnerPlugin and implement the visit(Node) method

# Querying?

See [runner.properties](runner.properties) and [pom.xml](pom.xml).

# Build & run

```bash
$ mvn clean compile exec:java
```

# Rerun (faster)

```bash
$ mvn -o -q compile exec:java
```

# Create app

```bash
$ mvn clean package appassembler:assemble
$ sh target/jcr-runner/bin/jcr-runner
```

# Documentation 

Documentation is available at [onehippo-forge.github.io/jcr-runner/](https://onehippo-forge.github.io/jcr-runner/)

The documentation is generated by this command:

```bash
$ mvn clean site:site
```

The output is in the docs directory; push it and GitHub Pages will serve the site automatically. 

