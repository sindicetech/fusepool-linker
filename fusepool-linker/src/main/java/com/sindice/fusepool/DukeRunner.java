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
package com.sindice.fusepool;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;

import no.priv.garshol.duke.ConfigLoader;
import no.priv.garshol.duke.Configuration;
import no.priv.garshol.duke.Processor;
import no.priv.garshol.duke.datasources.JDBCDataSource;
import no.priv.garshol.duke.matchers.MatchListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.sindice.fusepool.matchers.CollectingMatchListener;
import com.sindice.fusepool.stores.JenaAdapter;
import com.sindice.fusepool.stores.TripleWriter;

/**
 * Convenience class to run Duke in embedded mode.
 * 
 * Always creates a new Lucene index.
 * 
 */
public class DukeRunner {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private Configuration configuration;
	private Processor processor;
	
	public DukeRunner(Configuration configuration) {
		this.configuration = configuration;
		initialize();
	}
	
	/**
	 * Creates a Duke instance with the given config and creates a match listener that 
	 * stores results in the given folder.
	 * 
	 * Always creates a new Lucene index.
	 * 
	 * @param pathToConfig Can be file path, it is looked for in classpath too.
	 * @param dataFolder data folder where matches should be stored
	 */
	public DukeRunner(Configuration configuration, String dataFolder) {
		this.configuration = configuration;
		
		MatchListener[] listeners = initStorage(dataFolder);

		initialize(listeners);
}
	
	/**
	 * Creates a Duke instance with the given config and creates a match listener that 
	 * stores results in the given folder.
	 * 
	 * Always creates a new Lucene index.
	 * 
	 * IMPORTANT: assumes deduplication mode with a single datasource that is a JDBC datasource already configured from 
	 * the config pathToConfig. It loads the config and replaces the connect string with the given jdbcInputConnectionString.
	 * 
	 * @param pathToConfig Can be file path, it is looked for in classpath too.
	 * @param jdbcInputConnectionString JDBC connection string for connecting to the triplestore from where data should be loaded
	 * @param dataFolder data folder where matches should be stored
	 */
	public DukeRunner(Configuration configuration, String jdbcInputConnectionString, String dataFolder) {
		this.configuration = configuration;
		
		MatchListener[] listeners = initStorage(dataFolder);
		
		((JDBCDataSource)configuration.getDataSources().iterator().next()).setConnectionString(jdbcInputConnectionString);

		initialize(listeners);
	}
	
	private MatchListener[] initStorage(String dataFolder) {
		TripleWriter adapter = new JenaAdapter(dataFolder);
		adapter.init();
		return new MatchListener[] { new CollectingMatchListener(adapter) };
	}
	
	/**
	 * Creates a Duke instance with the given config and creates a match listener that 
	 * stores results in the given folder.
	 * 
	 * Always creates a new Lucene index.
	 * 
	 * @param pathToConfig Can be file path, it is looked for in classpath too.
	 * @param dataFolder data folder where matches should be stored
	 */
	public DukeRunner(String pathToConfig, String dataFolder) throws SQLException {
		loadConfig(pathToConfig);
		
		MatchListener[] listeners = initStorage(dataFolder);		
		initialize(listeners);
	}
	
	/**
	 * Creates a Duke instance with the given config and creates a match listener that 
	 * stores results in the given folder.
	 * 
	 * Always creates a new Lucene index.
	 * 
	 * IMPORTANT: assumes deduplication mode with a single datasource that is a JDBC datasource already configured from 
	 * the config pathToConfig. It loads the config and replaces the connect string with the given jdbcOutputConnectionString.
	 * 
	 * @param pathToConfig Can be file path, it is looked for in classpath too.
	 * @param jdbcInputConnectionString JDBC connection string for connecting to the triplestore where matches should be stored
	 * @param dataFolder data folder where matches should be stored
	 */
	public DukeRunner(String pathToConfig, String jdbcInputConnectionString, String dataFolder, int dukeThreads) throws SQLException {
		loadConfig(pathToConfig);

		((JDBCDataSource)configuration.getDataSources().iterator().next()).setConnectionString(jdbcInputConnectionString);
		
		MatchListener[] listeners = initStorage(dataFolder);		
		initialize(listeners);
		
		processor.setThreads(dukeThreads);
	}
	
	/**
	 * Creates a Duke instance with the given config and adds all the match listeners.
	 * 
	 * Always creates a new Lucene index.
	 * 
	 * @param pathToConfig Can be file path, it is looked for in classpath too.
	 * @param listeners {@link MatchListener}s to add.
	 */
	public DukeRunner(String pathToConfig, MatchListener... listeners) {
		loadConfig(pathToConfig);
		initialize(listeners);
	}
	
	private void initialize(MatchListener... listeners) {
		this.processor = new Processor(configuration, true);
		for (MatchListener listener : listeners) {
			addMatchListener(listener);
		}
		this.processor.setLogger(new DukeSlf4jLogger(logger));
	}
	
	private void loadConfig(String pathToConfig) {
		Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
		try {
			try {
				this.configuration = ConfigLoader.load(pathToConfig);
			} catch (IOException e) {
				if (!pathToConfig.startsWith("classpath:")) {
					try {
						this.configuration = ConfigLoader.load("classpath:" + pathToConfig);
					} catch (IOException e1) {
						throw new RuntimeException("Problem accessing Duke configuration: " + pathToConfig);
					}				
				}
			} 
		} catch (SAXException e) {
			throw new RuntimeException("Problem while reading Duke configuration " + pathToConfig, e);
		}		
	}
		
	public void addMatchListener(MatchListener listener) {
		processor.addMatchListener(listener);
	}
	
	public Collection<MatchListener> getMatchListeners() {
		return processor.getListeners();
	}
	
	public void run() {
		processor.deduplicate();
	}
}


