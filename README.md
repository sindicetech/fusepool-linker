# Fusepool Adapter

Fusepool adapter is an adapter between the Fusepool Clerezza-based interface and the Fusepool Linker.

The adapter implements the required interface and takes care of the workflow and input and output conversions.

The main class is the LinkerAdapter with the method 
**TripleCollection interlink(TripleCollection dataToInterlink)**

The LinkerAdapter class needs the following configuration arguments:

*  Duke configuration file name or path
*  Path to input data store 
*  Path to output data store  
*  Number of threads to run Duke

There are two constructors. The first one uses directly provided arguments and the second one reads arguments 
from a property file the name of which is passed to the constructor.

Fusepool adapter depends on Fusepool linker, therefore they should be built as follows:

```
cd fusepool-linker
mvn install
cd ../fusepool-adapter
mvn install
```

## Configuration and JVM settings
3GB heap size and use of huge pages are recommended.

The recommended number of threads to run LinkerAdapter, is (#cores-2) on a dedicated machine. 

To test the linking quickly, you can use the LinkerAdapterTest class. It contains methods (all @Ignore-d by default)
to help load the initial store with the input dataset and to run the full deduplication test.

The LinkerAdapterTest.testFull() uses the patents-jena-jdbc.xml configuration. The recommended configuration is 
conf-final.xml which also employs cleaners for higher precision.

# Fusepool Linker

The Fusepool Linker deduplicates triples using [Duke][1]. This version 
deduplicates only within a single dataset but can be extended for linking.

The main entry point is the DukeRunner class that executes Duke and takes care of 
loading input and storing output. Loading and storing is done via JDBC. Currently,
the only tested JDBC driver is the Jena JDBC driver. Other drivers, such as the
Virtuoso JDBC driver should be possible to use with small adjustments. 
For example, a SPARQL query has to start with the "sparql" keyword for Virtuoso
but not for Jena. 

DukeRunner is configured from external configuration which is on the classpath by
default. See the src/main/resources/ folder. DukeRunner configures Duke with one
such configuration file and then modifies the configuration to pass on the JDBC
input source and also to set the level of concurrency with which Duke should run.

DukeRunner is intended to be run in the same JVM as the user of the class runs in.
It is however prepared to be run separately too with minor modifications.

Currently, DukeRunner is executed from the LinkerAdapter class in the 
fusepool-adapter project.

## Configuring Duke
Example *.xml configuration is provided in the src/main/resources/. Note that 
deduplication is very sensitive to configuration parameters and it has to be 
adjusted for a given dataset. For deduplication of agents in the patent dataset
example we implemented for instance special address and name cleaners. Proper
comparators and thresholds have to be chosen for each record field.

[Duke provides some documentation][2] of all the parameters but it is necessary to
refer to its JavaDocs and source code too. 

## Performance and results

If agents A and B match then both triples A owl:sameAs B and B owl:sameAs A are
generated and stored. 

### Improving performance
Duke uses Lucene fuzzy matching by default. This is very slow and disabled by 
default in the Linker. 

Duke's performance can be improved also by increasing heap and Duke's deduplication
batch size, etc. 

[1]: https://github.com/larsga/Duke
[2]: https://code.google.com/p/duke/
