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

import java.sql.SQLException;

import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;
import com.sindice.fusepool.DukeRunner;
import com.sindice.fusepool.StopWatch;
import com.sindice.fusepooladapter.storage.InputTripleStore;
import com.sindice.fusepooladapter.storage.OutputStore;
import com.sindice.fusepooladapter.storage.SesameInputNativeStoreImpl;

import eu.fusepool.datalifecycle.Interlinker;

/**
 * implementation of {@link com.sindice.fusepooladapter.SingleLinker} using Duke
 * 
 */
@Component(service = Interlinker.class)
public class LinkerAdapter implements Interlinker {

	private static final Logger logger = LoggerFactory
			.getLogger(LinkerAdapter.class);
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
	 * Returns the default config file location.
	 * 
	 * @return "classpath:patents-jena-jdbc.xml"
	 */
	protected String defaultConfigFileLocation() {
		return "classpath:patents-csv.xml";
	}

	public TripleCollection interlink(TripleCollection dataToInterlink) {
		inDir = Files.createTempDir().getAbsolutePath();
		// populates input store

		StopWatch.start();
		logger.info("Populating input store ...");
		InputTripleStore instore = new SesameInputNativeStoreImpl(
				defaultInputDir());
		int inputSize = instore.populate(dataToInterlink);
		logger.info("Input store in {} populated with {} triples",
				defaultInputDir(), inputSize);
		OutputStore outStore = new OutputStore(defaultOutputDir());
		outStore.clean();
		StopWatch.end();
		logger.info(StopWatch.popTimeString("Populating store took %s ms"));

		// starts processing
		DukeRunner runner = null;
		try {
			// runner = new
			// DukeRunner(defaultConfigFileLocation(),"jdbc:jena:tdb:location="
			// + defaultInputDir(), defaultOutputDir(),
			// defaultNumberOfThreads());
			runner = new DukeRunner(defaultConfigFileLocation(),
					defaultInputDir(), defaultOutputDir(),
					defaultNumberOfThreads());
		} catch (SQLException e) {
			logger.error("error during initialization of the Duke");
		}
		logger.debug("going to start the Duke");
		StopWatch.start();
		runner.run();
		StopWatch.end();
		logger.info(StopWatch.popTimeString("Duke finished in %s ms."));

		// after Duke finish, exposes out store
		outStore.init();
		logger.info("Output store contains {} triples", outStore.size());
		return outStore;
	}

	@Override
	public TripleCollection interlink(TripleCollection dataToInterlink,
			UriRef interlinkAgainst) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public TripleCollection interlink(TripleCollection source,
			TripleCollection target) {
		// using equals takes potentialy too much time
		if (source == target) {
			return interlink(target);
		} else {
			throw new UnsupportedOperationException(
					"duke doesn't support different source and target yet");
		}
	}

	@Override
	public String getName() {
		return "duke-interlinker";
	}

}
