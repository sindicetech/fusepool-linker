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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import no.priv.garshol.duke.Configuration;
import no.priv.garshol.duke.DataSource;
import no.priv.garshol.duke.datasources.CSVDataSource;

import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;
import com.sindice.fusepool.DukeDeduplicatorRunner;
import com.sindice.fusepool.StopWatch;
import com.sindice.fusepool.stores.JenaTripleWriter;
import com.sindice.fusepooladapter.configuration.LinkerConfiguration;
import com.sindice.fusepooladapter.storage.ConfigurableSesameToCsvInputStore;
import com.sindice.fusepooladapter.storage.CsvConfig;
import com.sindice.fusepooladapter.storage.DukeConfigToCsvHeader;
import com.sindice.fusepooladapter.storage.JenaStoreTripleCollection;
import com.sindice.fusepooladapter.storage.SesameToCsvInputStore;

import eu.fusepool.datalifecycle.Interlinker;

/**
 * Implementation of linking and deduplication using Duke.
 * 
 */
public abstract class LinkerAdapter implements Interlinker, Deduplicator {
    public static final String AGENTS_CSV_FILENAME = "agents.csv";

    private static final Logger logger = LoggerFactory.getLogger(LinkerAdapter.class);
    private String tmpDir;
	private String outDir;
	private int dukeThreadNo = 2;

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
	
	public void setTmpDir(String pathToTempDir) {
		this.tmpDir = pathToTempDir;
	}

	public void setOutDir(String pathToOutDir) {
		this.outDir = pathToOutDir;
	}

	public void setDukeThreadNo(int dukeThreadNo) {
		this.dukeThreadNo = dukeThreadNo;
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
		return dukeThreadNo;
	}


    private long convertToCsv(TripleCollection data, String query, CsvConfig config, String outputFile) {
        logger.info("Converting input data to CSV {}", outputFile);
        StopWatch.start();

        try (FileWriter writer = new FileWriter(outputFile)) {

            long size = new ConfigurableSesameToCsvInputStore(writer, config, query).populate(data);

            StopWatch.end();
            logger.info("{} triples converted to {} in " + StopWatch.popTimeString("%s ms"), size, outputFile);

            return size;
        } catch (IOException e) {
            throw new RuntimeException("Problem converting input to CSV: " + e.getMessage(), e);
        }

    }

	public abstract TripleCollection interlink(TripleCollection dataToInterlink);
			
		
	/*
     * dataToInterlink -> Sesame -> CSV file -> Duke -> Jena-based output store
     */
	public TripleCollection interlink(TripleCollection dataToInterlink, LinkerConfiguration configuration) {
		tmpDir = Files.createTempDir().getAbsolutePath();
		// populates input store

        String storeFile = defaultTmpDir() + File.separator + AGENTS_CSV_FILENAME;

        
        Configuration dukeConfiguration = configuration.getDukeConfiguration();
        
        CSVDataSource dataSource = (CSVDataSource) dukeConfiguration.getDataSources().iterator().next();
        dataSource.setInputFile( storeFile );
        convertToCsv(dataToInterlink, configuration.getSparqlQuery1(), DukeConfigToCsvHeader.transform(dataSource), storeFile);
        DukeDeduplicatorRunner runner = new DukeDeduplicatorRunner(dukeConfiguration, new JenaTripleWriter(defaultOutputDir()), defaultNumberOfThreads());

		logger.debug("Starting Duke");
		StopWatch.start();
		runner.run();
		StopWatch.end();
		logger.info(StopWatch.popTimeString("Duke finished in %s ms."));

		// wrap results as a TripleCollection and provide it back
        JenaStoreTripleCollection outStore = new JenaStoreTripleCollection(defaultOutputDir());
        //outStore.clean();
		outStore.init();
		logger.info("Output store, {}, contains {} triples", defaultOutputDir(), outStore.size());
		return outStore;
	}

	@Override
	public TripleCollection interlink(TripleCollection dataToInterlink,
			UriRef interlinkAgainst) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public abstract TripleCollection interlink(TripleCollection source, TripleCollection target);

	
    public TripleCollection interlink(TripleCollection source, TripleCollection target, LinkerConfiguration configuration) {
        // using equals takes potentially too much time
        if (source == target) {
            return interlink(target, configuration);
        }

        tmpDir = Files.createTempDir().getAbsolutePath();

        Iterator<DataSource> iterator = configuration.getDukeConfiguration().getDataSources(1).iterator();
        if (!iterator.hasNext()) {
            throw new RuntimeException(String.format("Duke configuration must have two datasources configured, contains none."));
        }
        CSVDataSource dataSource1 = (CSVDataSource) iterator.next();
        String storeFileSource = defaultTmpDir() + File.separator + "source_" + AGENTS_CSV_FILENAME;
        dataSource1.setInputFile(storeFileSource);

        iterator = configuration.getDukeConfiguration().getDataSources(2).iterator();
        if (!iterator.hasNext()) {
            throw new RuntimeException(String.format("Duke configuration must have two datasources configured, contains only one."));
        }
        CSVDataSource dataSource2 = (CSVDataSource) iterator.next();
        String storeFileTarget = defaultTmpDir() + File.separator + "target_" + AGENTS_CSV_FILENAME;
        dataSource2.setInputFile(storeFileTarget);

        convertToCsv(source, configuration.getSparqlQuery1(), DukeConfigToCsvHeader.transform(dataSource1), storeFileSource);

        convertToCsv(target, configuration.getSparqlQuery2(), DukeConfigToCsvHeader.transform(dataSource2), storeFileTarget);


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
        logger.info("Output store, {}, contains {} triples", defaultOutputDir(), outStore.size());
        return outStore;
    }

}
