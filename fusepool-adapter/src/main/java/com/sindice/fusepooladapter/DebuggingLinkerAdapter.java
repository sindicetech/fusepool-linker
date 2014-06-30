package com.sindice.fusepooladapter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;
import com.sindice.fusepooladapter.configuration.LinkerConfiguration;
import no.priv.garshol.duke.*;
import no.priv.garshol.duke.databases.LuceneDatabase;
import no.priv.garshol.duke.utils.Utils;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * DebuggingLinkerAdapter is a drop-in replacement for the {@link GenericLinkerAdapter} for the case when its constructed
 * with a {@link LinkerConfiguration}.
 *
 * <p><b>IMPORTANT</b>:<pre>
 *            this class assumes that a FileAppender (or a subclass such as RollingFileAppender) with name FILE is configured in logback configuration,
 *            creates an additional FileAppender for a file saved in the same directory with the same file name + "_debug.log" suffix.
 *            AND it analyzes the first lines of the log file with the "_debug.log" suffix that match the {@link #RECORD_ID_PATTERN}.
 *            (this is of course fragile as it depends on Logback keeping the same log format. It can be later changed to read record
 *            ids directly from the data sources or by custom queries of the Lucene index)
 *</pre>
 *
 * It does three things:<br>
 *     <ul>
 *     <li>sets logging level for Duke's Processor class to DEBUG (so information about matches is logged)</li>
 *     <li>makes the Lucene index persistent (so that it is available for later analysis)</li>
 *     <li>can analyze matches based on the persistent Lucene index and an additional log file</li>
 *     </ul>
 *
 * <p>After calling one of the interlink() methods, analysis can be invoked by the {@link #analyze(int)} method directly
 * on the instance, or one of the static analyze or compare methods of DebuggingLinkerAdapter can be called. <br/>
 *
 * <p>Note the hint how to run the analyze method at the beginning of the log output:
 <pre>
 LinkerAdapter - ***** RUN com.sindice.fusepooladapter.DebuggingLinkerAdapter.analyze("/tmp/fusepool-linker-1F4DAB/adapter-380522670133963710/lucene-interlink-9115585418444752257", config, recordCount) to analyze this run later. *****
 </pre>
 *
 * <h2>Explanation of the output</h2>
 * The analyze() method produces output similar to the following:<br/>
 * (note the explanatory comments after #...)<br/>
 <pre>
 LinkerAdapter - Record urn:x-temp:/id/0421605d-c151-47e1-834d-432beb1732c2 has 27 candidate matches  #... speed up matching by limiting the number of candidates by setting the max-search-hits LuceneDatabase parameter (see https://code.google.com/p/duke/wiki/DatabaseConfig)
 LinkerAdapter - 	Candidate record id urn:x-temp:/id/0421605d-c151-47e1-834d-432beb1732c2
 LinkerAdapter - 		---NAME
 LinkerAdapter - 		'hoechst aktiengesellschaft' ~ 'hoechst aktiengesellschaft': 1.0 (prob 0.9) #... 1.0 means 100% match of property values of the records. Translates to 0.9 probability match of the whole record, as set in the high threshold in the configuration
 LinkerAdapter - 		Result: 0.5 -> 0.9   #... initial probability of a match was 50%, after inspecting the NAME property, it increased to 90%

 LinkerAdapter - 		---COUNTRY
 LinkerAdapter - 		'urn:x-temp:/code/country/DE' ~ 'urn:x-temp:/code/country/DE': 1.0 (prob 0.8)
 LinkerAdapter - 		Result: 0.9 -> 0.9729729729729729  #... the probability is adjusted after inspecting each property

 LinkerAdapter - 		---LOCALITY
 LinkerAdapter - 		'65926 frankfurt am main' ~ '65926 frankfurt am main': 1.0 (prob 0.88)
 LinkerAdapter - 		Result: 0.9729729729729729 -> 0.9962264150943396

 LinkerAdapter - 		---STREET
 LinkerAdapter - 		Overall: 0.9962264150943396  #... probability that the records match
 </pre>
 *
 * DebuggingLinkerAdapter
 */
public class DebuggingLinkerAdapter extends GenericLinkerAdapter {
    private static final String FILE_APPENDER_NAME = "FILE";
    private static final String DEBUG_FILE_APPENDER_NAME = "FILE_DEBUG";
    private static final Logger logger = (Logger) LoggerFactory.getLogger(LinkerAdapter.class);

    //example log line:
    //16:50:00.136 [MatchThread 0] DEBUG no.priv.garshol.duke.Processor - Matching record ID: 'urn:x-temp:/id/35d34246-9f50-4f02-80f2-5289cb380ebc', NAME: 'europaische atomgemeinschaft (euratom)', COUNTRY: 'urn:x-temp:/code/country/LU', LOCALITY: 'l-1019 luxembourg', STREET: 'batiment jean monnet plateau du kirchberg boite postale 1907',  found 3 candidates
    private static final Pattern RECORD_ID_PATTERN = Pattern.compile("^.*Matching record ID: '([^']*)'.*");

    private Path luceneDatabasePath;
    private final Configuration dukeConfig;
    private final LuceneDatabase database;
    private String debugFileName;

    public DebuggingLinkerAdapter(LinkerConfiguration linkerConfiguration) {
        super(linkerConfiguration);

        this.dukeConfig = linkerConfiguration.getDukeConfiguration();
        this.database = (LuceneDatabase) dukeConfig.getDatabase(false);

        setDukeLogLevelToDebug();
        makeLucenePersistent();

        printHelpToLog();
    }

    private void printHelpToLog() {
        logger.info("***** RUN "+this.getClass().getName()+".analyze(\"{}\", config, recordCount) to analyze this run later. *****", luceneDatabasePath);
    }

    private void makeLucenePersistent() {
        this.luceneDatabasePath = createTempDirectory("lucene-interlink-");
        logger.info("Created Lucene database directory {}", luceneDatabasePath);
        database.setPath(luceneDatabasePath.toAbsolutePath().toString());
    }

    private void setDukeLogLevelToDebug() {
        Logger processor = (Logger) LoggerFactory.getLogger(Processor.class);
        processor.setLevel(Level.DEBUG);
        Logger root = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

        Appender appender = root.getAppender(FILE_APPENDER_NAME);
        if (appender == null || !(appender instanceof FileAppender)) {
            throw new RuntimeException("No file appender with name " + FILE_APPENDER_NAME + " found in Logback configuration. It needs to be there so that the file can be analyzed.");
        }
        FileAppender mainFileAppender = (FileAppender) root.getAppender(FILE_APPENDER_NAME);


        FileAppender fileAppender = new FileAppender();
        fileAppender.setName(DEBUG_FILE_APPENDER_NAME);
        this.debugFileName = mainFileAppender.getFile() + "_debug.log";
        fileAppender.setFile(debugFileName);
        fileAppender.setAppend(false);

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        fileAppender.setContext(loggerContext);
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        encoder.setPattern("%msg%n");
        encoder.start();
        fileAppender.setEncoder(encoder);

        root.addAppender(fileAppender);

        fileAppender.start();

    }

    public void analyze(int recordCount) {
        analyze(luceneDatabasePath.toString(), dukeConfig, recordCount);
    }

    public static void analyze(String luceneDbPath, Configuration config, int recordCount) {
        LuceneDatabase db = (LuceneDatabase) config.getDatabase(false);
        db.setPath(luceneDbPath);

        Logger root = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        FileAppender appender = (FileAppender) root.getAppender(DEBUG_FILE_APPENDER_NAME);
        logger.info("Looking for record ids in log file " + appender.getFile() + ", pattern " + RECORD_ID_PATTERN);

        List<String> recordIds = new ArrayList<>();
        try (
                BufferedReader reader = Files.newBufferedReader(Paths.get(appender.getFile()), Charset.forName("UTF-8"))
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = RECORD_ID_PATTERN.matcher(line);
                if (!matcher.find()) {
                    continue;
                }
                String recordId = matcher.group(1);
                recordIds.add(recordId);
                if (recordIds.size() == recordCount) {
                    break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Some problem occurred", e);
        }

        logger.info(String.format("Found %d record ids: %s", recordIds.size(), recordIds));

        for (String id : recordIds) {
            analyzeRecord(db, config, id);
        }
    }

    private static void analyzeRecord(LuceneDatabase database, Configuration config, String recordId) {
        Record record = database.findRecordById(recordId);
        Collection<Record> candidateMatches = database.findCandidateMatches(record);
        logger.info(String.format("Record %s has %d candidate matches", recordId, candidateMatches.size()));
        for (Record candidate : candidateMatches) {
            logger.info("\tCandidate record id {}", candidate.getValue("ID"));
            compare(config, recordId, candidate.getValue("ID"));
        }
    }

    public static void compare(String luceneDbPath, Configuration config, String recId1, String recId2) {
        LuceneDatabase db = (LuceneDatabase) config.getDatabase(false);
        db.setPath(luceneDbPath);
        compare(config, recId1, recId2);
    }

    public static void compare(Configuration config, String recId1, String recId2) {
        /*

        This piece of code is copied almost literally from no.priv.garshol.duke.DebugCompare

         */
        Database database = config.getDatabase(false);

        // load records
        Record r1 = database.findRecordById(recId1);
        if (r1 == null) {
            logger.warn("\t\tCouldn't find record for '" + recId1 + "'");
            logger.warn("\t\tConsider using --reindex");
            return;
        }
        Record r2 = database.findRecordById(recId2);
        if (r2 == null) {
            logger.warn("\t\tCouldn't find record for '" + recId2 + "'");
            logger.warn("\t\tConsider using --reindex");
            return;
        }

        // do comparison
        double prob = 0.5;
        for (Property prop : config.getProperties()) {
            if (prop.isIdProperty())
                continue;

            String propname = prop.getName();
            logger.info("\t\t---" + propname);

            Collection<String> vs1 = r1.getValues(propname);
            Collection<String> vs2 = r2.getValues(propname);
            if (vs1.isEmpty() || vs2.isEmpty() || prop.isIgnoreProperty())
                continue; // no values to compare, so skip (or property is type=ignore)

            double high = 0.0;
            for (String v1 : vs1) {
                if (v1.equals(""))
                    continue;

                for (String v2 : vs2) {
                    if (v2.equals(""))
                        continue;

                    try {
                        Comparator comp = prop.getComparator();
                        if (comp == null) {
                            high = 0.5; // no comparator, so we learn nothing
                            break;
                        }

                        double d = comp.compare(v1, v2);
                        double p = prop.compare(v1, v2);
                        logger.info("\t\t'" + v1 + "' ~ '" + v2 + "': " + d +
                                " (prob " + p + ")");
                        high = Math.max(high, p);
                    } catch (Exception e) {
                        throw new DukeException("Comparison of values '" + v1 + "' and "+
                                "'" + v2 + "' failed", e);
                    }
                }
            }

            double newprob = Utils.computeBayes(prob, high);
            logger.info("\t\tResult: " + prob + " -> " + newprob + "\n");
            prob = newprob;
        }

        logger.info("\t\tOverall: " + prob);
    }
}
