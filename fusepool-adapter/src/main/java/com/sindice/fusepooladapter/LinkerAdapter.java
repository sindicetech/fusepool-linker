/*
 * Created by Sindice LTD http://sindicetech.com
 * Sindice LTD licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
import no.priv.garshol.duke.ConfigLoader;
import no.priv.garshol.duke.Configuration;
import no.priv.garshol.duke.DataSource;
import no.priv.garshol.duke.datasources.CSVDataSource;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

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
    private String inDir;
	private String outDir;

	public LinkerAdapter() {
		this.inDir = Files.createTempDir().getAbsolutePath();
		logger.info("Created inputDir {} ", inDir);
		this.outDir = Files.createTempDir().getAbsolutePath();
		logger.info("Created outputDir {} ", outDir);
	}

	/**
	 * Creates a default temporary directory to be used as the input dir.
	 * 
	 * @return Directory created in a temp dir of the system.
	 */
	protected String defaultInputDir() {
		return inDir;
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

    private int populateInStore(TripleCollection data, String query, String outputFile) {
        logger.info("Populating input store {}", outputFile);
        StopWatch.start();

        try (FileWriter writer = new FileWriter(outputFile)) {

            int size = new ConfigurableSesameToCsvInputStore(writer, query).populate(data);

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
		inDir = Files.createTempDir().getAbsolutePath();
		// populates input store

        String storeFile = defaultInputDir() + File.separator + AGENTS_CSV_FILENAME;

        //TODO instead of passing a hardcoded query like this, find a way to read the query from duke's configuration
        //TODO ... when that's done then we can perhaps also read the columns from Duke's configuration instead of parsing the query, see {@link SparqlToCsvHeader}
        populateInStore(dataToInterlink, SesameToCsvInputStore.query, storeFile);

        Configuration configuration = loadConfig(defaultDedupConfigFileLocation());
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

    private Configuration loadConfig(String pathToConfig) {
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        try {
            try {
                return ConfigLoader.load(pathToConfig);
            } catch (IOException e) {
                if (!pathToConfig.startsWith("classpath:")) {
                    try {
                        return ConfigLoader.load("classpath:" + pathToConfig);
                    } catch (IOException e1) {
                        throw new RuntimeException("Problem accessing Duke configuration: " + pathToConfig, e1);
                    }
                }
                throw new RuntimeException("Problem accessing Duke configuration: " + pathToConfig);
            }
        } catch (SAXException e) {
            throw new RuntimeException("Problem while reading Duke configuration " + pathToConfig, e);
        }
    }

	@Override
	public TripleCollection interlink(TripleCollection dataToInterlink,
			UriRef interlinkAgainst) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public TripleCollection interlink(TripleCollection source, TripleCollection target) {
		// using equals takes potentially too much time
		if (source == target) {
			return interlink(target);
		}

        inDir = Files.createTempDir().getAbsolutePath();
        // populates input store

        String storeFileSource = defaultInputDir() + File.separator + "source_" + AGENTS_CSV_FILENAME;
        populateInStore(source, SesameToCsvInputStore.query, storeFileSource);

        String storeFileTarget = defaultInputDir() + File.separator + "target_" + AGENTS_CSV_FILENAME;
        String query = "PREFIX foaf:        <http://xmlns.com/foaf/0.1/>\n" +
                "PREFIX rdfs:        <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX owl:         <http://www.w3.org/2002/07/owl#>\n" +
                "PREFIX dbpedia-owl: <http://dbpedia.org/ontology/> \n" +
                "PREFIX dbpedia:     <http://dbpedia.org/property/>\n" +
                "PREFIX sindicetech: <http://sindicetech.com/ontology/>\n" +
                "SELECT ?iri ?companyName ?countryCode ?locationCityName WHERE {" +
                "    ?iri a dbpedia-owl:Company;\n" +
                "         foaf:name ?companyName;\n" +
                "         dbpedia:countryCode ?countryCode;\n" +
                "         sindicetech:locationCityName ?locationCityName" +
                "}";
        populateInStore(target, query, storeFileTarget);

        Configuration configuration = loadConfig(defaultInterlinkConfigFileLocation());
        Iterator<DataSource> iterator = configuration.getDataSources(1).iterator();

        if (!iterator.hasNext()) {
            throw new RuntimeException(String.format("Configuration %s must have two datasources configured, contains none.", defaultInterlinkConfigFileLocation()));
        }
        ((CSVDataSource)iterator.next()).setInputFile(storeFileSource);

        iterator = configuration.getDataSources(2).iterator();
        if (!iterator.hasNext()) {
            throw new RuntimeException(String.format("Configuration %s must have two datasources configured, contains only one.", defaultInterlinkConfigFileLocation()));
        }
        ((CSVDataSource)iterator.next()).setInputFile(storeFileTarget);
        DukeDeduplicatorRunner runner = new DukeDeduplicatorRunner(configuration, new JenaTripleWriter(defaultOutputDir()), defaultNumberOfThreads());

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
	public String getName() {
		return "duke-interlinker";
	}

}
