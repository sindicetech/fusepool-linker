/* 
 * Copyright 2014 Sindice LTD http://sindicetech.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sindice.fusepooladapter;

import com.sindice.fusepooladapter.configuration.LinkerConfiguration;
import no.priv.garshol.duke.*;
import no.priv.garshol.duke.databases.LuceneDatabase;
import no.priv.garshol.duke.utils.Utils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.NIOFSDirectory;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * DebuggingLinkerAdapter is a drop-in replacement for the {@link GenericLinkerAdapter} for the case when its constructed
 * with a {@link LinkerConfiguration}.
 *
 * It does two things:<br>
 *     <ul>
 *     <li>makes the Lucene index persistent (so that it is available for later analysis)</li>
 *     <li>can analyze matches based on the persistent Lucene index</li>
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
    private static final org.slf4j.Logger logger = (org.slf4j.Logger) LoggerFactory.getLogger(LinkerAdapter.class);
    private Path luceneDatabasePath;
    private final Configuration dukeConfig;
    private final LuceneDatabase database;

    public DebuggingLinkerAdapter(LinkerConfiguration linkerConfiguration) {
        super(linkerConfiguration);

        this.dukeConfig = linkerConfiguration.getDukeConfiguration();
        this.database = (LuceneDatabase) dukeConfig.getDatabase(false);

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

    public void analyze(int recordCount) {
        analyze(luceneDatabasePath.toString(), dukeConfig, recordCount);
    }

    public static void analyze(String luceneDbPath, Configuration config, int recordCount) {
        LuceneDatabase db = (LuceneDatabase) config.getDatabase(false);
        db.setPath(luceneDbPath);

      Collection<String> recordIds = findIds(((LuceneDatabase) config.getDatabase(false)).getPath(), config.getIdentityProperties().iterator().next().getName(), recordCount);

      logger.info(String.format("Found %d record ids: %s", recordIds.size(), recordIds));

        for (String id : recordIds) {
            analyzeRecord(db, config, id);
        }
    }

  private static Directory openDirectory(String path) {
    Directory directory;
    try {
        //directory = new MMapDirectory(new File(config.getPath()));
        // as per http://wiki.apache.org/lucene-java/ImproveSearchingSpeed
        // we use NIOFSDirectory, provided we're not on Windows
        if (Utils.isWindowsOS())
          directory = FSDirectory.open(new File(path));
        else
          directory = NIOFSDirectory.open(new File(path));
    } catch (Exception e) {
      throw new RuntimeException("Could not open Lucene database: " + e.getMessage(), e);
    }

    return directory;
  }

  private static Collection<String> findIds(String path, String idFieldName, int recordCount) {
    List<String> ids = new ArrayList<>();
    try {
      IndexReader reader = DirectoryReader.open(openDirectory(path));

      for (int i = 0 ; i < Math.min(recordCount, reader.numDocs()); i++) {
        Document document = reader.document(i);
        ids.add(document.get(idFieldName));
      }
      return ids;
    } catch (IOException ex) {
      throw new RuntimeException("Problem while trying to search Lucene index in " + path + ": " + ex.getMessage(), ex);
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
