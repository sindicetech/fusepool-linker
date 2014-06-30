# Fusepool Linker

# Build
Currently, you have to clone and build https://github.com/fusepool/fusepool-platform and modify the main `pom.xml`
to point to the platform: `<relativePath>../../fusepool-platform</relativePath>`.

Then build with Maven as usual: `mvn clean install -DskipTests`

# Fusepool Adapter

Fusepool adapter is an adapter between the Fusepool Clerezza-based interface and the Fusepool Linker.

The adapter implements the required interface and takes care of the workflow and input and output conversions.

The main class is the LinkerAdapter which is abstract and has concrete implementation for specific datasets and dataset
combinations.

## Configuration and JVM settings
3GB heap size and use of huge pages are recommended.

The recommended number of threads to run LinkerAdapter, is (#cores-2) on a dedicated machine. 

To test the linking quickly, you can use the LinkerAdapterHelperTest class. It contains methods (all @Ignore-d by default)
to help load the initial store with the input dataset and to run the full deduplication test.

The GenericLinkerAdapter class can be used for deduplication and interlinking of any datasets. It has to be provided
with the respective configuration.

# Fusepool Linker

The Fusepool Linker deduplicates and interlinks triple datasets using [Duke][1].

The main entry point is the DukeRunner class that executes Duke and takes care of 
starting Duke and storing output.

Currently, DukeRunner is executed from the LinkerAdapter class in the 
fusepool-adapter project.

# Duke
Example Duke \*.xml configurations are provided in the src/main/resources/. Note that
deduplication is very sensitive to configuration parameters and it has to be 
adjusted for a given dataset. For deduplication of agents in the MAREC patent
dataset example we implemented for instance special address and name cleaners.
Proper comparators and thresholds have to be chosen for each record field.

[Duke provides documentation][2] of all the parameters.

## Output triples

If agents A and B match then both triples A owl:sameAs B and B owl:sameAs A are
generated and stored. 

## Debugging and tuning
Debugging and tuning the interlinking process can be a little tricky with Duke.
That's why we provide the DebuggingLinkerAdapter which serves as a nearly drop-in
replacement of the GenericLinkerAdapter and provides additional information useful
for debugging in the logs.

See LinkerAdapterTest.testSmallDataFile() and LinkerAdapterTest.testSmallDataFileWithDebugging()
for an example how to use the DebuggingLinkerAdapter.
LinkerAdapterHelperTest.compareRecords() shows how to compare specific records given a path
to a LuceneDatabase stored by previously running Duke (for example with the DebuggingLinkerAdapter).
DebuggingLinkerAdapter's JavaDoc explains log output in detail.

## Improving performance
Duke uses Lucene fuzzy matching by default. This is very slow and disabled by 
default in the provided configurations.

Duke's performance can be improved also by increasing heap and Duke's deduplication
batch size, reducing lucene query result set by setting the `max-search-hits`
LuceneDatabase parameter, etc.

# Running inside the Fusepool Platform
Build Fusepool platform and run it:

             cd launcher/target
             java -Xmx1024M -XX:MaxPermSize=400M -Xss512k -jar launcher-0.1-SNAPSHOT.jar

Then access http://localhost:8080/system/console/bundles to install the linker and adapter bundles.

To examine Fusepool logs, first locate them at http://localhost:8080/system/console/slinglog



[1]: https://github.com/larsga/Duke
[2]: https://code.google.com/p/duke/
