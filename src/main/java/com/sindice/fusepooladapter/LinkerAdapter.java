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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

import no.priv.garshol.duke.Configuration;
import no.priv.garshol.duke.DataSource;
import no.priv.garshol.duke.datasources.CSVDataSource;
import no.priv.garshol.duke.datasources.SparqlDataSource;

import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sindice.fusepool.DukeRunner;
import com.sindice.fusepool.StopWatch;
import com.sindice.fusepool.stores.JenaTripleWriter;
import com.sindice.fusepooladapter.configuration.LinkerConfiguration;
import com.sindice.fusepooladapter.storage.ConfigurableSesameToCsvInputStore;
import com.sindice.fusepooladapter.storage.CsvConfig;
import com.sindice.fusepooladapter.storage.DukeConfigToCsvHeader;
import com.sindice.fusepooladapter.storage.JenaStoreTripleCollection;

import eu.fusepool.datalifecycle.Interlinker;

/**
 * Implementation of linking and deduplication using Duke.
 *
 * Please note that intermediate files are stored in the OS's temporary directory in the directory {@value #TEMP_DIR_NAME}.
 * The directory is not automatically cleaned and may contain large amounts of data after running the linker.
 * 
 */
public abstract class LinkerAdapter implements Interlinker, Deduplicator {
    private static final Logger logger = LoggerFactory.getLogger(LinkerAdapter.class);
    public static final String CSV_FILENAME = "intermediate.csv";

    /**
     * Pseudo-random name so that the temp folder is easy to find and conflict is reasonably impossible.
     */
    private static final String TEMP_DIR_NAME = "fusepool-linker-1F4DAB";

    protected String tmpDir;
    protected String outDir;
    protected int dukeThreadNo = 2;

	public LinkerAdapter() {
        // make sure our TEMP_DIR_NAME exists in the system temporary directory
        File fusepoolLinkerTmpDir = new File(System.getProperty("java.io.tmpdir") + File.separator + TEMP_DIR_NAME);
        if (!fusepoolLinkerTmpDir.exists()) {
            if (!fusepoolLinkerTmpDir.mkdir()) {
                throw new RuntimeException("Couldn't create temporary directory " + fusepoolLinkerTmpDir.getAbsolutePath().toString());
            }
        }
        if (!fusepoolLinkerTmpDir.isDirectory()) {
            throw new RuntimeException("Couldn't create temporary directory " + fusepoolLinkerTmpDir.getAbsolutePath().toString() + ". There is a file with the same name.");
        }
        try {
            this.tmpDir = java.nio.file.Files.createTempDirectory(fusepoolLinkerTmpDir.toPath(), "adapter-").toAbsolutePath().toString();
        } catch (IOException e) {
            throw new RuntimeException("Could not create temporary directory in " + fusepoolLinkerTmpDir, e);
        }
        logger.info("Created temporary directory {} ", tmpDir);

        this.outDir = createTempDirectory("output-").toAbsolutePath().toString();
        logger.info("Created output directory {} ", outDir);
	}

    protected Path createTempDirectory(String prefix) {
        try {
            return java.nio.file.Files.createTempDirectory(Paths.get(tmpDir), prefix);
        } catch (IOException e) {
            throw new RuntimeException("Could not create temporary directory in " + tmpDir, e);
        }
    }

	/**
	 * Returns a default temporary directory where intermediate data will be stored
	 * 
	 * @return Directory created in a temp dir of the system.
	 */
	protected String defaultTmpDir() {
		return tmpDir;
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


    private long convertToCsv(TripleCollection dataset, String query, CsvConfig config, String outputFile) {
        logger.info("Converting input data to CSV {}", outputFile);
        StopWatch.start();

        try (FileWriter writer = new FileWriter(outputFile)) {
            long size = new ConfigurableSesameToCsvInputStore(writer, config, query, defaultTmpDir()).populate(dataset);

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
        String storeFile = defaultTmpDir() + File.separator + CSV_FILENAME;

        Configuration dukeConfiguration = configuration.getDukeConfiguration();
        
        
        DataSource dataSource = dukeConfiguration.getDataSources().iterator().next();
        if (dataSource instanceof CSVDataSource) {
        	CSVDataSource csvDataSource = (CSVDataSource) dataSource; 
        	csvDataSource.setInputFile( storeFile );
        	convertToCsv(dataToInterlink, configuration.getSparqlQuery1(), DukeConfigToCsvHeader.transform(csvDataSource), storeFile);
        }else if(dataSource instanceof SparqlDataSource) {
        	// do nothing as csv file is not needed
        }else {
                throw new IllegalArgumentException("Only CSVDataSource and SparqlDataSource are supported");
        }
        
        DukeRunner runner = new DukeRunner(dukeConfiguration, new JenaTripleWriter(defaultOutputDir()), defaultNumberOfThreads());
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
	public abstract TripleCollection interlink(TripleCollection dataset1, TripleCollection dataset2);

	
    public TripleCollection interlink(TripleCollection source, TripleCollection target, LinkerConfiguration configuration) {

        Iterator<DataSource> iterator = configuration.getDukeConfiguration().getDataSources(1).iterator();
        if (!iterator.hasNext()) {
            throw new RuntimeException(String.format("Duke configuration must have two datasources configured, contains none."));
        }
        
        DataSource dataSource1 = iterator.next();
        if(dataSource1 instanceof CSVDataSource) {
        	CSVDataSource csvDataSource1 = (CSVDataSource) dataSource1;
            String storeFileSource = defaultTmpDir() + File.separator + "source_" + CSV_FILENAME;
            csvDataSource1.setInputFile(storeFileSource);
            convertToCsv(source, configuration.getSparqlQuery1(), DukeConfigToCsvHeader.transform(csvDataSource1), storeFileSource);
        }
        iterator = configuration.getDukeConfiguration().getDataSources(2).iterator();
        if (!iterator.hasNext()) {
            throw new RuntimeException(String.format("Duke configuration must have two datasources configured, contains only one."));
        }
        DataSource dataSource2 = iterator.next();
        if(dataSource2 instanceof CSVDataSource) {
            CSVDataSource csvDataSource2 = (CSVDataSource) dataSource2;
            String storeFileTarget = defaultTmpDir() + File.separator + "target_" + CSV_FILENAME;
            csvDataSource2.setInputFile(storeFileTarget);
            convertToCsv(target, configuration.getSparqlQuery2(), DukeConfigToCsvHeader.transform(csvDataSource2), storeFileTarget);
        }
        
        DukeRunner runner = new DukeRunner(configuration.getDukeConfiguration(), new JenaTripleWriter(defaultOutputDir()), defaultNumberOfThreads());

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
