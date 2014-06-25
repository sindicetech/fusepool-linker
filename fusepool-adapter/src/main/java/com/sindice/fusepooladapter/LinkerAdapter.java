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

import com.google.common.io.Files;
import com.sindice.fusepool.DukeDeduplicatorRunner;
import com.sindice.fusepool.StopWatch;
import com.sindice.fusepool.stores.JenaTripleWriter;
import com.sindice.fusepooladapter.storage.ConfigurableSesameToCsvInputStore;
import com.sindice.fusepooladapter.storage.JenaStoreTripleCollection;
import com.sindice.fusepooladapter.storage.SesameToCsvInputStore;
import eu.fusepool.datalifecycle.Interlinker;
import no.priv.garshol.duke.Configuration;
import no.priv.garshol.duke.DataSource;
import no.priv.garshol.duke.datasources.CSVDataSource;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

/**
 * Implementation of linking and deduplication using Duke.
 * 
 */
@Component(service = Interlinker.class)
public class LinkerAdapter implements Interlinker {
    public static final String AGENTS_CSV_FILENAME = "agents.csv";

    private static final Logger logger = LoggerFactory.getLogger(LinkerAdapter.class);
    public static final String DEFAULT_DEDUP_CONFIG_FILE_LOCATION = "classpath:patents-csv.xml";
    public static final String DEFAULT_INTERLINK_CONFIG_FILE_LOCATION = "classpath:dbpedia-csv.xml";
    private String tmpDir;
	private String outDir;

	public LinkerAdapter() {
		this.tmpDir = Files.createTempDir().getAbsolutePath();
		logger.info("Created inputDir {} ", tmpDir);
		this.outDir = Files.createTempDir().getAbsolutePath();
		logger.info("Created outputDir {} ", outDir);
	}

	/**
	 * Returns a default temporary directory where intermediate data will be stored
	 * 
	 * @return Directory created in a temp dir of the system.
	 */
	protected String defaultTmpDir() {
		return tmpDir;
	}

	/**
	 * Creates a default temporary directory to be used as the output dir.
	 * 
	 * @return Directory created in a temp dir of the system.
	 */
	protected String defaultOutputDir() {
		return outDir;
	}

	/**
	 * Returns the default number of threads to run Duke with.
	 * 
	 * @return 2
	 */
	protected int defaultNumberOfThreads() {
		return 2;
	}

	/**
	 * Returns the default deduplication config file location.
	 * 
	 * @return DEFAULT_DEDUP_CONFIG_FILE_LOCATION = {@value #DEFAULT_DEDUP_CONFIG_FILE_LOCATION}
	 */
	protected String defaultDedupConfigFileLocation() {
		return DEFAULT_DEDUP_CONFIG_FILE_LOCATION;
	}

    /**
     * Returns the default interlinking config file location.
     *
     * @return DEFAULT_INTERLINK_CONFIG_FILE_LOCATION = {@value #DEFAULT_INTERLINK_CONFIG_FILE_LOCATION}
     */
    protected String defaultInterlinkConfigFileLocation() {
        return DEFAULT_INTERLINK_CONFIG_FILE_LOCATION;
    }

    private long populateInStore(TripleCollection data, String query, String outputFile) {
        logger.info("Populating input store {}", outputFile);
        StopWatch.start();

        try (FileWriter writer = new FileWriter(outputFile)) {

            long size = new ConfigurableSesameToCsvInputStore(writer, query).populate(data);

            StopWatch.end();
            logger.info("Input store in {} populated with {} triples in " + StopWatch.popTimeString("%s ms"), outputFile, size);

            return size;
        } catch (IOException e) {
            throw new RuntimeException("Problem populating input store: " + e.getMessage(), e);
        }

    }

    /*
     * dataToInterlink -> Sesame -> CSV file -> Duke -> Jena-based output store
     */
	public TripleCollection interlink(TripleCollection dataToInterlink) {
		tmpDir = Files.createTempDir().getAbsolutePath();
		// populates input store

        String storeFile = defaultTmpDir() + File.separator + AGENTS_CSV_FILENAME;

        //TODO instead of passing a hardcoded query like this, find a way to read the query from duke's configuration
        //TODO ... when that's done then we can perhaps also read the columns from Duke's configuration instead of parsing the query, see {@link SparqlToCsvHeader}
        populateInStore(dataToInterlink, SesameToCsvInputStore.query, storeFile);

        Configuration configuration = LinkerConfiguration.loadConfig(defaultDedupConfigFileLocation());
        ((CSVDataSource)configuration.getDataSources().iterator().next()).setInputFile( storeFile );

        DukeDeduplicatorRunner runner = new DukeDeduplicatorRunner(configuration, new JenaTripleWriter(defaultOutputDir()), defaultNumberOfThreads());

		logger.debug("Starting Duke");
		StopWatch.start();
		runner.run();
		StopWatch.end();
		logger.info(StopWatch.popTimeString("Duke finished in %s ms."));

		// wrap results as a TripleCollection and provide it back
        JenaStoreTripleCollection outStore = new JenaStoreTripleCollection(defaultOutputDir());
        outStore.clean();
		outStore.init();
		logger.info("Output store contains {} triples", outStore.size());
		return outStore;
	}

	@Override
	public TripleCollection interlink(TripleCollection dataToInterlink,
			UriRef interlinkAgainst) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

    public TripleCollection interlink(TripleCollection source, TripleCollection target, LinkerConfiguration configuration) {
        // using equals takes potentially too much time
        if (source == target) {
            return interlink(target);
        }

        tmpDir = Files.createTempDir().getAbsolutePath();

        String storeFileSource = defaultTmpDir() + File.separator + "source_" + AGENTS_CSV_FILENAME;
        populateInStore(source, configuration.getSparqlQuery1(), storeFileSource);

        String storeFileTarget = defaultTmpDir() + File.separator + "target_" + AGENTS_CSV_FILENAME;
        populateInStore(target, configuration.getSparqlQuery2(), storeFileTarget);

        Iterator<DataSource> iterator = configuration.getDukeConfiguration().getDataSources(1).iterator();

        if (!iterator.hasNext()) {
            throw new RuntimeException(String.format("Duke configuration must have two datasources configured, contains none."));
        }
        ((CSVDataSource)iterator.next()).setInputFile(storeFileSource);

        iterator = configuration.getDukeConfiguration().getDataSources(2).iterator();
        if (!iterator.hasNext()) {
            throw new RuntimeException(String.format("Duke configuration must have two datasources configured, contains only one."));
        }
        ((CSVDataSource)iterator.next()).setInputFile(storeFileTarget);

        DukeDeduplicatorRunner runner = new DukeDeduplicatorRunner(configuration.getDukeConfiguration(), new JenaTripleWriter(defaultOutputDir()), defaultNumberOfThreads());

        logger.debug("Starting Duke");
        StopWatch.start();
        runner.run();
        StopWatch.end();
        logger.info(StopWatch.popTimeString("Duke finished in %s ms."));

        // wrap results as a TripleCollection and provide it back
        JenaStoreTripleCollection outStore = new JenaStoreTripleCollection(defaultOutputDir());
        //outStore.clean();
        outStore.init();
        logger.info("Output store contains {} triples", outStore.size());
        return outStore;
    }

	@Override
	public TripleCollection interlink(TripleCollection source, TripleCollection target) {
        return interlink(source, target, PatentsDbpediaLinkerConfiguration.getInstance());
	}

	@Override
	public String getName() {
		return "duke-interlinker";
	}

}
